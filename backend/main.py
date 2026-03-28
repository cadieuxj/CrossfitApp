"""
ApexAI Athletics — FastAPI Microservice
========================================
Orchestrates the Gemini AI coaching pipeline for the Android app.

Endpoints:
  POST   /v1/analyze-video              — Upload video, trigger Gemini analysis
  GET    /v1/coaching/status/{id}       — Poll analysis status
  GET    /v1/coaching/report/{id}       — Retrieve completed report
  POST   /v1/generate-correction-image  — Generate corrected posture image via Gemini Flash
  POST   /v1/cache/refresh              — Refresh CrossFit knowledge base context cache
  GET    /v1/health                     — Health check

Auth:
  All endpoints (except /health) require a valid Supabase JWT in the
  Authorization: Bearer header. The JWT is validated against the
  Supabase JWT secret (HS256). The user's sub claim becomes the athlete_id
  used throughout the pipeline.

Rate Limiting:
  /analyze-video: 10 requests per hour per user (enforced per spec §9.5)
  All other endpoints: no hard limit (rely on Supabase's default rate limiting)

Background Tasks:
  Video analysis runs as a FastAPI BackgroundTask so the client receives
  a 202 immediately. Status is polled via GET /coaching/status/{analysis_id}.
"""

from __future__ import annotations

import asyncio
import os
import tempfile
import time
import uuid
from contextlib import asynccontextmanager
from datetime import datetime, timezone
from pathlib import Path
from typing import Annotated, Any

import aiofiles
import sentry_sdk
import structlog
from fastapi import (
    BackgroundTasks,
    Depends,
    FastAPI,
    File,
    Form,
    Header,
    HTTPException,
    Request,
    UploadFile,
    status,
)
from fastapi.exception_handlers import http_exception_handler
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from jose import JWTError, jwt
from pydantic_settings import BaseSettings, SettingsConfigDict
from slowapi import Limiter, _rate_limit_exceeded_handler
from slowapi.errors import RateLimitExceeded
from slowapi.util import get_remote_address
from supabase import Client, create_client

from gemini_service import GeminiService
from models import (
    AI_DISCLAIMER,
    AnalysisConfidence,
    AnalysisStatus,
    AnalysisStatusResponse,
    AnalyzeVideoResponse,
    CacheRefreshResponse,
    CoachingReportResponse,
    ErrorDetail,
    ErrorResponse,
    GenerateCorrectionImageRequest,
    GenerateCorrectionImageResponse,
    HealthCheckResponse,
    MovementFaultResponse,
)

logger = structlog.get_logger(__name__)

# ---------------------------------------------------------------------------
# Settings
# ---------------------------------------------------------------------------

class Settings(BaseSettings):
    """
    Application settings loaded from environment variables.
    In development, these can be set in a .env file.
    In production, they must be set as container environment variables.
    """
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")

    # Supabase
    supabase_url: str
    supabase_service_role_key: str  # Used by FastAPI to write reports (bypasses RLS)
    supabase_anon_key: str          # Used for user-scoped reads (RLS enforced)
    supabase_jwt_secret: str        # Used to validate user JWTs

    # Gemini (see gemini_service.py for model name settings)
    gemini_api_key: str

    # Storage
    video_bucket_name: str = "videos"
    corrections_bucket_name: str = "corrections"

    # Sentry (optional — leave blank to disable error reporting)
    sentry_dsn: str = ""

    # App
    app_version: str = "1.0.0"
    debug: bool = False
    cors_origins: list[str] = ["*"]  # Restrict in production


settings = Settings()  # type: ignore[call-arg]

# ---------------------------------------------------------------------------
# Sentry Error Reporting (PII scrubbing — Quebec Law 25 compliance)
# ---------------------------------------------------------------------------

def _sentry_before_send(event: dict, hint: dict) -> dict | None:  # type: ignore[type-arg]
    """
    Strip PII from Sentry events before transmission.
    Removes user email and IP address to comply with Quebec Law 25
    and GDPR data minimisation requirements.
    """
    if "user" in event:
        event["user"].pop("email", None)
        event["user"].pop("ip_address", None)
    if "request" in event and "env" in event["request"]:
        event["request"]["env"].pop("REMOTE_ADDR", None)
    return event


if settings.sentry_dsn:
    sentry_sdk.init(
        dsn=settings.sentry_dsn,
        before_send=_sentry_before_send,
        traces_sample_rate=0.1,
        environment="production" if not settings.debug else "development",
    )
    logger.info("sentry_initialized")

# ---------------------------------------------------------------------------
# Application State
# ---------------------------------------------------------------------------

# In-memory job status store.
# In production with multiple replicas, replace with Redis.
# Keys: analysis_id (str) -> AnalysisStatusResponse
_analysis_jobs: dict[str, dict[str, Any]] = {}

_start_time = time.monotonic()

# ---------------------------------------------------------------------------
# Dependency: Supabase Clients
# ---------------------------------------------------------------------------

def get_supabase_client() -> Client:
    """
    Returns a Supabase client using the service role key.
    This client bypasses RLS and is used ONLY for server-side writes
    (inserting coaching reports, updating video status).
    Never expose this client to user-controlled input without explicit
    row-level filtering.
    """
    return create_client(settings.supabase_url, settings.supabase_service_role_key)


def get_user_supabase_client(user_jwt: str) -> Client:
    """
    Returns a Supabase client authenticated with the user's own JWT.
    RLS policies on the database enforce row-level ownership automatically,
    providing defence-in-depth for all read operations.
    """
    client = create_client(settings.supabase_url, settings.supabase_anon_key)
    client.postgrest.auth(user_jwt)
    return client


# ---------------------------------------------------------------------------
# Dependency: JWT Authentication
# ---------------------------------------------------------------------------

async def get_current_user_id(
    authorization: Annotated[str | None, Header()] = None,
) -> str:
    """
    FastAPI dependency that validates the Supabase JWT and returns the user ID.

    Supabase JWTs are HS256-signed with the project JWT secret.
    The 'sub' claim contains the user's UUID (matches profiles.id).

    Raises HTTP 401 if the token is missing, expired, or invalid.
    """
    if authorization is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Authorization header is required",
            headers={"WWW-Authenticate": "Bearer"},
        )

    parts = authorization.split(" ")
    if len(parts) != 2 or parts[0].lower() != "bearer":
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Authorization header must be 'Bearer <token>'",
            headers={"WWW-Authenticate": "Bearer"},
        )

    token = parts[1]

    try:
        payload = jwt.decode(
            token,
            settings.supabase_jwt_secret,
            algorithms=["HS256"],
            audience="authenticated",
        )
        user_id: str | None = payload.get("sub")
        if user_id is None:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="JWT is missing 'sub' claim",
            )
        return user_id
    except JWTError as exc:
        logger.warning("jwt_validation_failed", error=str(exc))
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid or expired token",
            headers={"WWW-Authenticate": "Bearer"},
        ) from exc


# ---------------------------------------------------------------------------
# Dependency: Gemini Service
# ---------------------------------------------------------------------------

_gemini_service: GeminiService | None = None


async def get_gemini_service() -> GeminiService:
    global _gemini_service  # noqa: PLW0603
    if _gemini_service is None:
        _gemini_service = GeminiService()
    return _gemini_service


# ---------------------------------------------------------------------------
# Admin Dependency
# ---------------------------------------------------------------------------

async def require_admin_user(
    authorization: Annotated[str | None, Header()] = None,
    current_user_id: str = Depends(get_current_user_id),
) -> str:
    """
    Dependency that verifies the caller has role='admin' in their JWT app_metadata.
    Raises 403 if the claim is absent or does not equal 'admin'.
    """
    if authorization is None:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Missing Authorization header")
    token = authorization.split(" ", 1)[1]
    try:
        payload = jwt.decode(token, settings.supabase_jwt_secret, algorithms=["HS256"], audience="authenticated")
    except JWTError as exc:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token") from exc
    app_metadata = payload.get("app_metadata", {})
    if app_metadata.get("role") != "admin":
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Admin access required")
    return current_user_id


# ---------------------------------------------------------------------------
# Rate Limiter
# ---------------------------------------------------------------------------

def _get_rate_limit_key(request: Request) -> str:
    """Key rate limits by authenticated user ID, falling back to IP for unauthenticated requests."""
    auth = request.headers.get("authorization", "")
    if auth.lower().startswith("bearer "):
        token = auth.split(" ", 1)[1]
        try:
            payload = jwt.decode(
                token, settings.supabase_jwt_secret,
                algorithms=["HS256"], audience="authenticated"
            )
            user_id = payload.get("sub")
            if user_id:
                return f"user:{user_id}"
        except JWTError:
            pass
    return f"ip:{get_remote_address(request)}"


limiter = Limiter(key_func=_get_rate_limit_key)


# ---------------------------------------------------------------------------
# Application Lifecycle
# ---------------------------------------------------------------------------

@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    Startup: pre-load Gemini context cache so the first analysis request
    does not incur cache creation latency.
    Shutdown: no cleanup required (Gemini cache is server-side).
    """
    logger.info("startup_begin", version=settings.app_version)
    try:
        gemini = GeminiService()
        global _gemini_service  # noqa: PLW0603
        _gemini_service = gemini
        cache = await gemini.get_or_create_cache()
        logger.info("startup_cache_loaded", cache_name=cache.name)
    except Exception as exc:
        # Log but do not crash — the app can still serve requests without
        # the pre-loaded cache (it will be created on the first analysis).
        logger.error("startup_cache_load_failed", error=str(exc))
    logger.info("startup_complete")
    yield
    logger.info("shutdown")


# ---------------------------------------------------------------------------
# FastAPI Application
# ---------------------------------------------------------------------------

app = FastAPI(
    title="ApexAI Athletics Coaching API",
    description="Gemini-powered AI video coaching microservice for CrossFit athletes",
    version=settings.app_version,
    lifespan=lifespan,
    docs_url="/v1/docs" if settings.debug else None,
    redoc_url="/v1/redoc" if settings.debug else None,
)

# Rate limiter integration
app.state.limiter = limiter
app.add_exception_handler(RateLimitExceeded, _rate_limit_exceeded_handler)

# CORS — restrict to known origins in production
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins,
    allow_credentials=True,
    allow_methods=["GET", "POST"],
    allow_headers=["Authorization", "Content-Type"],
)


# ---------------------------------------------------------------------------
# Exception Handlers
# ---------------------------------------------------------------------------

@app.exception_handler(HTTPException)
async def custom_http_exception_handler(request: Request, exc: HTTPException):
    """Wraps all HTTP errors in the standard error envelope."""
    error_map = {
        400: "BAD_REQUEST",
        401: "UNAUTHORIZED",
        403: "FORBIDDEN",
        404: "NOT_FOUND",
        409: "CONFLICT",
        413: "PAYLOAD_TOO_LARGE",
        422: "VALIDATION_ERROR",
        429: "RATE_LIMITED",
        500: "INTERNAL_SERVER_ERROR",
        503: "SERVICE_UNAVAILABLE",
    }
    code = error_map.get(exc.status_code, "ERROR")
    return JSONResponse(
        status_code=exc.status_code,
        content=ErrorResponse(
            error=ErrorDetail(code=code, message=str(exc.detail))
        ).model_dump(),
    )


# ---------------------------------------------------------------------------
# Background Task: Video Analysis Pipeline
# ---------------------------------------------------------------------------

async def run_analysis_pipeline(
    analysis_id: str,
    video_path: Path,
    movement_type: str,
    athlete_id: str,
    video_id: str,
    gemini: GeminiService,
    supabase: Client,
) -> None:
    """
    Background task that orchestrates the full Gemini analysis pipeline.

    Pipeline stages:
      1. analyzing_video  — Gemini 1.5 Pro processes the video
      2. generating_corrections — Gemini 2.0 Flash creates corrective images
      3. finalizing — write results to Supabase

    Status is updated in _analysis_jobs at each stage so the polling
    endpoint can report granular progress to the Android client.
    """

    def update_status(
        progress: float,
        stage: str,
        status: AnalysisStatus = AnalysisStatus.PROCESSING,
        error_message: str | None = None,
    ) -> None:
        _analysis_jobs[analysis_id].update({
            "status": status.value,
            "progress": progress,
            "stage": stage,
            "error_message": error_message,
        })

    try:
        # Stage 1: Gemini video analysis
        update_status(0.05, "analyzing_video")
        logger.info(
            "analysis_pipeline_start",
            analysis_id=analysis_id,
            movement_type=movement_type,
        )

        analysis_data = await gemini.analyze_video(
            video_path=video_path,
            movement_type=movement_type,
            athlete_id=athlete_id,
            video_id=video_id,
        )

        update_status(0.60, "generating_corrections")

        # Stage 2: Generate corrective images for each fault (Gemini Flash)
        faults = analysis_data.get("faults", [])
        for i, fault in enumerate(faults):
            try:
                image_bytes = await gemini.generate_correction_image(
                    fault_description=fault.get("description", ""),
                    movement_type=movement_type,
                    fault_timestamp_ms=fault.get("timestamp_ms", 0),
                    video_path=video_path,
                )

                # Upload the corrective image to Supabase Storage
                correction_path = f"corrections/{athlete_id}/{analysis_id}/fault_{i}.png"
                supabase.storage.from_(settings.corrections_bucket_name).upload(
                    path=correction_path,
                    file=image_bytes,
                    file_options={"content-type": "image/png"},
                )

                # Get a signed URL (1 hour TTL — the Android client should
                # re-fetch if the report is opened after expiry)
                signed_url_response = supabase.storage.from_(
                    settings.corrections_bucket_name
                ).create_signed_url(correction_path, expires_in=3600)

                fault["corrected_image_url"] = signed_url_response.get("signedURL", "")
                fault["_storage_path"] = correction_path

                progress = 0.60 + (0.30 * ((i + 1) / max(len(faults), 1)))
                update_status(progress, "generating_corrections")

            except Exception as fault_img_err:
                # A failing image generation is non-fatal — the fault is
                # still reported without the corrective image.
                logger.warning(
                    "correction_image_failed",
                    fault_index=i,
                    error=str(fault_img_err),
                )
                fault["corrected_image_url"] = None

        update_status(0.90, "finalizing")

        # Stage 3: Write coaching report to Supabase
        report_id = str(uuid.uuid4())

        report_row = {
            "id": report_id,
            "video_id": video_id,
            "user_id": athlete_id,
            "movement_type": movement_type,
            "overall_assessment": analysis_data.get("overall_assessment", ""),
            "rep_count": analysis_data.get("rep_count", 0),
            "analysis_confidence": analysis_data.get("analysis_confidence", AnalysisConfidence.MEDIUM.value),
            "prompt_version": analysis_data.get("prompt_version", "v1.0"),
            "global_cues": analysis_data.get("global_cues", []),
        }

        supabase.table("coaching_reports").insert(report_row).execute()

        # Write movement faults as child rows
        fault_rows = []
        for fault in faults:
            fault_rows.append({
                "id": str(uuid.uuid4()),
                "report_id": report_id,
                "description": fault.get("description", ""),
                "severity": fault.get("severity", "MINOR"),
                "timestamp_ms": fault.get("timestamp_ms", 0),
                "cue": fault.get("cue", ""),
                "corrected_image_url": fault.get("corrected_image_url"),
                "affected_joints": fault.get("affected_joints", []),
            })

        if fault_rows:
            supabase.table("movement_faults").insert(fault_rows).execute()

        # Update video status to 'complete'
        supabase.table("video_uploads").update(
            {"status": "complete"}
        ).eq("id", video_id).execute()

        # Store the report_id in the job so the status endpoint can
        # redirect the client to the correct report URL
        _analysis_jobs[analysis_id].update({
            "status": AnalysisStatus.COMPLETE.value,
            "progress": 1.0,
            "stage": "finalizing",
            "report_id": report_id,
        })

        logger.info(
            "analysis_pipeline_complete",
            analysis_id=analysis_id,
            report_id=report_id,
        )

    except Exception as exc:
        logger.exception(
            "analysis_pipeline_failed",
            analysis_id=analysis_id,
            error=str(exc),
        )
        update_status(
            progress=_analysis_jobs.get(analysis_id, {}).get("progress", 0.0),
            stage="error",
            status=AnalysisStatus.ERROR,
            error_message=str(exc),
        )
        # Update video status to 'error'
        try:
            supabase.table("video_uploads").update(
                {"status": "error", "error_message": str(exc)[:500]}
            ).eq("id", video_id).execute()
        except Exception:
            pass  # Best-effort cleanup

    finally:
        # Always clean up the temporary video file
        try:
            video_path.unlink(missing_ok=True)
        except Exception:
            pass


# ---------------------------------------------------------------------------
# Endpoints
# ---------------------------------------------------------------------------

@app.get("/v1/health", response_model=HealthCheckResponse, tags=["System"])
async def health_check() -> HealthCheckResponse:
    """
    Health check endpoint. Does not require authentication.
    Used by Cloud Run / Railway / load balancer health probes.
    """
    cache_loaded = "crossfit_knowledge_base" in _gemini_service._cache_store if _gemini_service else False
    return HealthCheckResponse(
        status="ok",
        version=settings.app_version,
        gemini_cache_loaded=cache_loaded,
        uptime_seconds=time.monotonic() - _start_time,
    )


@app.post(
    "/v1/analyze-video",
    status_code=status.HTTP_202_ACCEPTED,
    response_model=AnalyzeVideoResponse,
    responses={
        400: {"model": ErrorResponse},
        401: {"model": ErrorResponse},
        413: {"model": ErrorResponse},
        429: {"model": ErrorResponse},
        503: {"model": ErrorResponse},
    },
    tags=["Coaching"],
)
@limiter.limit("10/hour")
async def analyze_video(
    request: Request,
    background_tasks: BackgroundTasks,
    video: UploadFile = File(..., description="Video file (video/mp4, max 500MB)"),
    movement_type: str = Form(..., min_length=1, max_length=100),
    athlete_id: str = Form(..., description="Supabase user UUID"),
    current_user_id: str = Depends(get_current_user_id),
    gemini: GeminiService = Depends(get_gemini_service),
) -> AnalyzeVideoResponse:
    """
    Upload a video for Gemini AI coaching analysis.

    The video is saved to a temporary file and analysis runs as a
    background task. Returns 202 Accepted immediately with an analysis_id
    the client uses to poll status.

    The athlete_id in the form body must match the JWT sub claim to prevent
    one user from submitting analysis under another user's ID.
    """
    # Verify the form athlete_id matches the authenticated user
    if athlete_id != current_user_id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="athlete_id does not match authenticated user",
        )

    # Validate movement_type against known CrossFit movements
    _ALLOWED_MOVEMENT_TYPES = {
        "BACK_SQUAT", "FRONT_SQUAT", "OVERHEAD_SQUAT",
        "DEADLIFT", "SUMO_DEADLIFT",
        "CLEAN", "POWER_CLEAN", "HANG_CLEAN", "HANG_POWER_CLEAN",
        "SNATCH", "POWER_SNATCH", "HANG_SNATCH", "HANG_POWER_SNATCH",
        "CLEAN_AND_JERK", "PUSH_JERK", "SPLIT_JERK",
        "THRUSTER", "PUSH_PRESS", "STRICT_PRESS",
        "PULL_UP", "CHEST_TO_BAR", "MUSCLE_UP",
        "BURPEE", "BOX_JUMP", "DOUBLE_UNDER",
        "KETTLEBELL_SWING", "WALL_BALL", "TOES_TO_BAR",
        "HANDSTAND_PUSH_UP", "HANDSTAND_WALK",
        "ROWING", "ASSAULT_BIKE", "SKI_ERG",
        "RUN", "LUNGE", "STEP_UP",
    }
    if movement_type.upper() not in _ALLOWED_MOVEMENT_TYPES:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Unknown movement type: '{movement_type}'. Must be one of the supported CrossFit movements.",
        )
    movement_type = movement_type.upper()

    # Validate content type
    allowed_types = {"video/mp4", "video/quicktime", "video/x-m4v"}
    if video.content_type not in allowed_types:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Invalid video format. Allowed types: {', '.join(allowed_types)}",
        )

    # Stream to temp file and check size
    suffix = Path(video.filename or "upload.mp4").suffix or ".mp4"
    with tempfile.NamedTemporaryFile(suffix=suffix, delete=False) as tmp:
        tmp_path = Path(tmp.name)

    total_bytes = 0
    max_size = 500 * 1024 * 1024  # 500 MB

    try:
        async with aiofiles.open(tmp_path, "wb") as f:
            while chunk := await video.read(1024 * 1024):  # 1 MB chunks
                total_bytes += len(chunk)
                if total_bytes > max_size:
                    tmp_path.unlink(missing_ok=True)
                    raise HTTPException(
                        status_code=status.HTTP_413_REQUEST_ENTITY_TOO_LARGE,
                        detail=f"Video exceeds maximum size of 500 MB",
                    )
                await f.write(chunk)
    except HTTPException:
        raise
    except Exception as exc:
        tmp_path.unlink(missing_ok=True)
        logger.error("video_upload_save_failed", error=str(exc))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to save uploaded video",
        ) from exc

    # Create video_uploads record in Supabase
    supabase = get_supabase_client()
    video_id = str(uuid.uuid4())

    try:
        supabase.table("video_uploads").insert({
            "id": video_id,
            "user_id": athlete_id,
            "storage_path": f"videos/{athlete_id}/{video_id}{suffix}",
            "movement_type": movement_type,
            "file_size_bytes": total_bytes,
            "status": "analyzing",
        }).execute()
    except Exception as exc:
        tmp_path.unlink(missing_ok=True)
        logger.error("video_record_creation_failed", error=str(exc))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to create video record",
        ) from exc

    analysis_id = str(uuid.uuid4())

    # Initialize job status — user_id stored for ownership enforcement on status/report endpoints
    _analysis_jobs[analysis_id] = {
        "user_id": athlete_id,
        "status": AnalysisStatus.PROCESSING.value,
        "progress": 0.01,
        "stage": "analyzing_video",
        "video_id": video_id,
        "report_id": None,
        "error_message": None,
    }

    # Queue the background analysis task
    background_tasks.add_task(
        run_analysis_pipeline,
        analysis_id=analysis_id,
        video_path=tmp_path,
        movement_type=movement_type,
        athlete_id=athlete_id,
        video_id=video_id,
        gemini=gemini,
        supabase=supabase,
    )

    logger.info(
        "analysis_job_queued",
        analysis_id=analysis_id,
        video_id=video_id,
        movement_type=movement_type,
        file_size_bytes=total_bytes,
    )

    return AnalyzeVideoResponse(
        analysis_id=analysis_id,
        status=AnalysisStatus.PROCESSING,
        estimated_seconds=45,
        poll_url=f"/v1/coaching/status/{analysis_id}",
    )


@app.get(
    "/v1/coaching/status/{analysis_id}",
    response_model=AnalysisStatusResponse,
    responses={
        401: {"model": ErrorResponse},
        404: {"model": ErrorResponse},
    },
    tags=["Coaching"],
)
async def get_analysis_status(
    analysis_id: str,
    current_user_id: str = Depends(get_current_user_id),
) -> AnalysisStatusResponse:
    """
    Poll the status of a video analysis job.

    The Android client polls this endpoint every 3 seconds until
    status == 'complete', then fetches the full report.
    """
    job = _analysis_jobs.get(analysis_id)
    if job is None or job.get("user_id") != current_user_id:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Analysis job {analysis_id} not found",
        )

    return AnalysisStatusResponse(
        analysis_id=analysis_id,
        status=AnalysisStatus(job["status"]),
        progress=job.get("progress", 0.0),
        stage=job.get("stage"),
        error_message=job.get("error_message"),
    )


@app.get(
    "/v1/coaching/report/{analysis_id}",
    response_model=CoachingReportResponse,
    responses={
        401: {"model": ErrorResponse},
        404: {"model": ErrorResponse},
        409: {"model": ErrorResponse},
    },
    tags=["Coaching"],
)
async def get_coaching_report(
    analysis_id: str,
    authorization: Annotated[str | None, Header()] = None,
    current_user_id: str = Depends(get_current_user_id),
) -> CoachingReportResponse:
    """
    Retrieve a completed coaching report.

    Returns 409 Conflict if the analysis is still in progress —
    the client should continue polling /coaching/status/{analysis_id}.
    """
    job = _analysis_jobs.get(analysis_id)
    if job is None or job.get("user_id") != current_user_id:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Analysis job {analysis_id} not found",
        )

    if job["status"] == AnalysisStatus.PROCESSING.value:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Analysis is still in progress. Poll /coaching/status for updates.",
        )

    if job["status"] == AnalysisStatus.ERROR.value:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Analysis failed: {job.get('error_message', 'Unknown error')}",
        )

    report_id = job.get("report_id")
    if report_id is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Report ID not available — analysis may have failed",
        )

    # Use user-scoped client for reads so RLS enforces ownership automatically
    user_token = authorization.split(" ", 1)[1] if authorization else ""
    read_client = get_user_supabase_client(user_token)

    try:
        report_response = (
            read_client.table("coaching_reports")
            .select("*")
            .eq("id", report_id)
            .single()
            .execute()
        )
    except Exception as exc:
        logger.error("report_fetch_failed", report_id=report_id, error=str(exc))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to retrieve coaching report",
        ) from exc

    if report_response.data is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"Coaching report {report_id} not found",
        )

    # Fetch associated movement faults
    try:
        faults_response = (
            read_client.table("movement_faults")
            .select("*")
            .eq("report_id", report_id)
            .order("severity")  # CRITICAL first (alphabetical matches intent here)
            .execute()
        )
    except Exception as exc:
        logger.error("faults_fetch_failed", report_id=report_id, error=str(exc))
        faults_response = type("obj", (object,), {"data": []})()

    report_data = report_response.data
    faults_data = faults_response.data or []

    # Build the response
    faults = [
        MovementFaultResponse(
            id=f["id"],
            description=f["description"],
            severity=f["severity"],
            timestamp_ms=f["timestamp_ms"],
            cue=f["cue"],
            corrected_image_url=f.get("corrected_image_url"),
            affected_joints=f.get("affected_joints", []),
        )
        for f in faults_data
    ]

    return CoachingReportResponse(
        id=report_data["id"],
        video_id=report_data["video_id"],
        movement_type=report_data["movement_type"],
        overall_assessment=report_data.get("overall_assessment", ""),
        rep_count=report_data.get("rep_count", 0),
        analysis_confidence=AnalysisConfidence(
            report_data.get("analysis_confidence", AnalysisConfidence.MEDIUM.value)
        ),
        faults=faults,
        global_cues=report_data.get("global_cues", []),
        prompt_version=report_data.get("prompt_version", "v1.0"),
        disclaimer=AI_DISCLAIMER,
        created_at=datetime.fromisoformat(report_data["created_at"]),
    )


@app.post(
    "/v1/generate-correction-image",
    response_model=GenerateCorrectionImageResponse,
    responses={
        400: {"model": ErrorResponse},
        401: {"model": ErrorResponse},
        404: {"model": ErrorResponse},
    },
    tags=["Coaching"],
)
async def generate_correction_image(
    request_body: GenerateCorrectionImageRequest,
    current_user_id: str = Depends(get_current_user_id),
    gemini: GeminiService = Depends(get_gemini_service),
) -> GenerateCorrectionImageResponse:
    """
    Generate a corrected posture image for a specific movement fault
    using Gemini 2.0 Flash.

    This endpoint is called by the Android client when viewing a fault
    that does not yet have a corrected_image_url. It can also be used
    to regenerate an image.
    """
    supabase = get_supabase_client()

    # Verify the fault belongs to a report owned by the current user
    try:
        fault_response = (
            supabase.table("movement_faults")
            .select("*, coaching_reports!inner(user_id, movement_type)")
            .eq("id", request_body.fault_id)
            .eq("report_id", request_body.report_id)
            .single()
            .execute()
        )
    except Exception as exc:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Fault not found",
        ) from exc

    if fault_response.data is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Fault not found",
        )

    report_user_id = fault_response.data["coaching_reports"]["user_id"]
    if report_user_id != current_user_id:
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="Access denied",
        )

    # Generate the corrective image
    image_bytes = await gemini.generate_correction_image(
        fault_description=request_body.fault_description,
        movement_type=request_body.movement_type,
        fault_timestamp_ms=request_body.fault_timestamp_ms,
    )

    # Upload to Supabase Storage
    storage_path = (
        f"corrections/{current_user_id}/"
        f"{request_body.report_id}/{request_body.fault_id}.png"
    )

    try:
        supabase.storage.from_(settings.corrections_bucket_name).upload(
            path=storage_path,
            file=image_bytes,
            file_options={"content-type": "image/png", "upsert": "true"},
        )
    except Exception as exc:
        logger.error("correction_image_upload_failed", error=str(exc))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Failed to upload corrective image to storage",
        ) from exc

    # Generate signed URL
    signed_url_response = supabase.storage.from_(
        settings.corrections_bucket_name
    ).create_signed_url(storage_path, expires_in=3600)

    signed_url = signed_url_response.get("signedURL", "")

    # Update the fault row with the image URL
    supabase.table("movement_faults").update(
        {"corrected_image_url": signed_url}
    ).eq("id", request_body.fault_id).execute()

    logger.info(
        "correction_image_generated",
        fault_id=request_body.fault_id,
        storage_path=storage_path,
    )

    return GenerateCorrectionImageResponse(
        fault_id=request_body.fault_id,
        corrected_image_url=signed_url,
        storage_path=storage_path,
    )


@app.post(
    "/v1/cache/refresh",
    response_model=CacheRefreshResponse,
    responses={
        401: {"model": ErrorResponse},
        503: {"model": ErrorResponse},
    },
    tags=["System"],
)
async def refresh_context_cache(
    admin_user_id: str = Depends(require_admin_user),
    gemini: GeminiService = Depends(get_gemini_service),
) -> CacheRefreshResponse:
    """
    Refreshes the Gemini CrossFit knowledge base context cache.

    Call this endpoint after updating the movement standards knowledge base.
    The existing cache is evicted and a new one is created.

    Restricted to users with app_metadata.role == 'admin' in their Supabase JWT.
    """
    try:
        cache_metadata = await gemini.force_refresh_cache()
    except Exception as exc:
        logger.error("cache_refresh_failed", error=str(exc))
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail=f"Failed to refresh Gemini context cache: {exc}",
        ) from exc

    return CacheRefreshResponse(
        cache_name=cache_metadata["cache_name"],
        model=cache_metadata["model"],
        token_count=cache_metadata.get("token_count", 0),
        expires_at=datetime.fromisoformat(cache_metadata["expires_at"]),
        refreshed_at=datetime.fromisoformat(cache_metadata["refreshed_at"]),
    )


# ---------------------------------------------------------------------------
# Account Deletion (Law 25, Art. 28 — Right to Erasure)
# ---------------------------------------------------------------------------

@app.delete(
    "/v1/account",
    status_code=status.HTTP_204_NO_CONTENT,
    responses={
        401: {"model": ErrorResponse},
        500: {"model": ErrorResponse},
    },
    tags=["Account"],
)
async def delete_account(
    current_user_id: str = Depends(get_current_user_id),
) -> None:
    """
    Permanently deletes the authenticated user's account and all associated data.

    This endpoint fulfils the right-to-erasure requirement under Quebec's Law 25
    (Loi 25, Art. 28) and PIPEDA Principle 4.3.8.

    Deletion order:
      1. All files under videos/{user_id}/ in the videos Storage bucket
      2. All files under corrections/{user_id}/ in the corrections Storage bucket
      3. All database rows (ON DELETE CASCADE handles child tables)
      4. The Supabase Auth user record

    An erasure event is logged for audit purposes before deletion begins.
    """
    supabase = get_supabase_client()

    logger.info("account_erasure_initiated", user_id=current_user_id)

    try:
        # 1. Delete video files from Storage
        try:
            video_files = supabase.storage.from_(settings.video_bucket_name).list(
                path=f"videos/{current_user_id}"
            )
            if video_files:
                paths = [f"videos/{current_user_id}/{f['name']}" for f in video_files]
                supabase.storage.from_(settings.video_bucket_name).remove(paths)
        except Exception as exc:
            logger.warning("erasure_video_storage_failed", user_id=current_user_id, error=str(exc))

        # 2. Delete correction images from Storage
        try:
            correction_files = supabase.storage.from_(settings.corrections_bucket_name).list(
                path=f"corrections/{current_user_id}"
            )
            if correction_files:
                paths = [f"corrections/{current_user_id}/{f['name']}" for f in correction_files]
                supabase.storage.from_(settings.corrections_bucket_name).remove(paths)
        except Exception as exc:
            logger.warning("erasure_correction_storage_failed", user_id=current_user_id, error=str(exc))

        # 3. Delete database rows (ON DELETE CASCADE propagates to child tables)
        supabase.table("profiles").delete().eq("id", current_user_id).execute()

        # 4. Delete the Auth user (requires service role)
        supabase.auth.admin.delete_user(current_user_id)

        logger.info("account_erasure_complete", user_id=current_user_id)

    except Exception as exc:
        logger.error("account_erasure_failed", user_id=current_user_id, error=str(exc))
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Account deletion failed. Please contact support.",
        ) from exc
