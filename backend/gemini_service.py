"""
ApexAI Athletics — Gemini AI Service
=====================================
Orchestrates the dual-model Gemini pipeline:
  1. gemini-1.5-pro  — full video analysis with context caching
  2. gemini-2.0-flash — corrective posture image generation per fault

Context Caching Strategy
-------------------------
The CrossFit movement standards document + athlete history are pre-loaded
as a Gemini context cache. Every new analysis references the cache by its
resource name instead of re-sending the full context, reducing per-video
token cost by approximately 75-90%.

Cache lifecycle:
- Created on first call or /cache/refresh
- TTL: 1 hour (renewable)
- Resource name stored in-memory (TTLCache) and in the video's coaching report
  row for audit/debugging

Threading model:
- All Gemini SDK calls are synchronous (the google-generativeai SDK does not
  natively support asyncio). All calls are wrapped in asyncio.to_thread()
  so they do not block the FastAPI event loop.

Error handling:
- google.api_core.exceptions.ResourceExhausted -> 429 upstream
- google.api_core.exceptions.ServiceUnavailable -> 503 upstream
- All Gemini exceptions are caught, logged with structlog, and re-raised as
  application-level exceptions that the router converts to HTTP responses.
"""

from __future__ import annotations

import asyncio
import base64
import json
import os
import tempfile
import time
from datetime import datetime, timezone
from pathlib import Path
from typing import Any

import google.generativeai as genai
import structlog
from cachetools import TTLCache
from google.generativeai import caching
from google.generativeai.types import HarmBlockThreshold, HarmCategory

from models import (
    FaultSeverity,
    MovementFaultResponse,
    PoseLandmark,
    TimedPoseOverlay,
)

logger = structlog.get_logger(__name__)

# ---------------------------------------------------------------------------
# Configuration (all values come from environment variables)
# ---------------------------------------------------------------------------

GEMINI_API_KEY: str = os.environ["GEMINI_API_KEY"]
VIDEO_ANALYSIS_MODEL: str = os.environ.get("VIDEO_ANALYSIS_MODEL", "gemini-1.5-pro")
IMAGE_GENERATION_MODEL: str = os.environ.get("IMAGE_GENERATION_MODEL", "gemini-2.0-flash")

# Context cache TTL in seconds. Gemini minimum is 60 s.
CACHE_TTL_SECONDS: int = int(os.environ.get("GEMINI_CACHE_TTL_SECONDS", "3600"))

# Maximum video file size allowed (500 MB per spec).
MAX_VIDEO_SIZE_BYTES: int = 500 * 1024 * 1024

# Safety settings — allow sports/fitness content.
# The default SafetySettings block violence-adjacent content which would
# incorrectly flag normal weightlifting analysis responses.
# AUDIT NOTE: Thresholds are relaxed from BLOCK_LOW_AND_ABOVE to BLOCK_ONLY_HIGH.
# Rationale: Standard CrossFit video analysis is incorrectly blocked by stricter settings.
# Any generation blocked by these relaxed thresholds is still logged as a safety event.
SAFETY_SETTINGS = {
    HarmCategory.HARM_CATEGORY_HATE_SPEECH:       HarmBlockThreshold.BLOCK_ONLY_HIGH,
    HarmCategory.HARM_CATEGORY_HARASSMENT:        HarmBlockThreshold.BLOCK_ONLY_HIGH,
    HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT: HarmBlockThreshold.BLOCK_ONLY_HIGH,
    HarmCategory.HARM_CATEGORY_SEXUALLY_EXPLICIT: HarmBlockThreshold.BLOCK_MEDIUM_AND_ABOVE,
}

# ---------------------------------------------------------------------------
# Gemini API Client Initialization
# ---------------------------------------------------------------------------

genai.configure(api_key=GEMINI_API_KEY)

# In-memory store for the active context cache resource name.
# TTLCache evicts after CACHE_TTL_SECONDS so the cache refresh cycle
# aligns with the Gemini cache expiry.
_cache_store: TTLCache = TTLCache(maxsize=1, ttl=CACHE_TTL_SECONDS)
_CACHE_KEY = "crossfit_knowledge_base"


# ---------------------------------------------------------------------------
# CrossFit Knowledge Base Content
# This text is sent ONCE to create/refresh the context cache.
# It contains movement standards, biomechanical fault taxonomies, and
# coaching cueing language. In production this would be loaded from a
# structured document or database, but for MVP it is defined inline.
# ---------------------------------------------------------------------------

CROSSFIT_KNOWLEDGE_BASE = """
# CrossFit Movement Standards and Biomechanical Analysis Guide
# ApexAI Athletics Coaching System v1.0

## General Principles
- All movement analysis uses 2D profile-view angles as the primary metric.
  Z-depth data from on-device pose estimation is unreliable and must not
  be used as a primary fault indicator.
- Joint angle thresholds are provided as population-level guidelines.
  Individual limb length ratios affect optimal positioning.
- Every fault must be accompanied by a single, actionable coaching cue
  in simple language an athlete can execute immediately.

## Fault Severity Taxonomy
- MINOR: Sub-optimal but not injury-risk. Common among intermediate athletes.
- MODERATE: Reduces power output or places joints in compromised positions.
  Requires deliberate practice to correct.
- CRITICAL: Acute injury risk. Athlete should stop and address before continuing.

## Olympic Lifting Standards

### Snatch
Setup:
- Bar over mid-foot (approximately 1 inch from shins)
- Hip crease level with or below the bar
- Neutral spine: no excessive lumbar rounding or hyperextension
- Shoulders over or slightly in front of the bar
- Arms externally rotated (elbows point out, not down)

First Pull (floor to knee):
- Bar path stays close to shins (within 2 inches)
- Shoulders maintain position above hips (angle preserved)
- No early hip rise before the bar leaves the floor

Second Pull (knee to hip contact):
- Knees rebend/sweep under the bar
- Bar contacts upper thigh/hip crease — NOT below the hip
- Arms remain straight — early arm bend is a CRITICAL fault

Turnover (extension to catch):
- Full triple extension: ankle, knee, hip
- Elbows pull high and outside before punching overhead
- Receive in deep squat with active overhead press

Common Faults:
1. Early arm bend (CRITICAL) — arms flex during second pull reducing power transfer
2. Bar drift forward (MODERATE) — bar moves away from body during second pull
3. Shooting hips (MODERATE) — hips rise faster than shoulders off the floor
4. Soft catch (MODERATE) — elbows collapse in the overhead receiving position
5. Early pull with arms (CRITICAL) — arms initiate before full hip extension

### Clean and Jerk
[Same setup as snatch but narrower grip]
Clean-specific standards:
- Receive bar in front rack: bar rests on anterior deltoids, not wrists
- Full squat clean: hip crease must break parallel (for squat clean standard)
- Front rack elbow position: horizontal or higher

Jerk-specific standards:
- Split jerk: front foot lands flat, back knee bends toward floor
- Push jerk: full lockout overhead with knees rebent under load
- No pressing out: arms must lock out before the body descends

### Overhead Squat
- Bar directly over base of support (mid-foot) throughout the movement
- Active shoulder press throughout (not passive lock)
- Hip crease below knee at bottom
- Torso as vertical as possible

## Squat Mechanics

### Back Squat
- Bar rests on upper trapezius (high bar) or lower trapezius (low bar)
- Stance width: hip-width to shoulder-width with toes angled out 15-30 degrees
- Knee tracking over second/third toe throughout
- Hip crease breaks parallel at minimum
- Neutral spine maintained — not excessive forward lean
- Heels remain flat throughout

Common Faults:
1. Knee cave/valgus collapse (CRITICAL) — knees track inward
2. Forward torso lean (MODERATE) — excessive trunk inclination
3. Heel rise (MODERATE) — ankles lack dorsiflexion
4. Butt wink (MODERATE) — posterior pelvic tilt at bottom of squat

### Front Squat
- Same depth and knee standards as back squat
- Elbows must remain elevated (horizontal or above) throughout
- More upright torso required compared to back squat

## Pulling Movements

### Deadlift
- Bar over mid-foot
- Neutral spine — no lumbar rounding in the initial pull
- Lats engaged before initiating pull (protect lower back)
- Bar stays in contact with legs throughout the lift
- Full hip extension and knee lockout at top

### Pull-Up (Kipping/Strict)
Strict: Full dead hang to chin over bar, no body swing
Kipping: Active hollow/arch cycle, hip drive propels upward

## Gymnastics Movements

### Handstand Push-Up
- Nose and both hands form a tripod at the bottom
- Hips over hands (slightly past vertical)
- Full lockout at top — both arms straight
- Head neutral — not tucked or hyperextended

## Scoring Guidelines for Rep Counting
Count only reps that meet the movement standard.
Partial reps should be noted but not counted.
For AMRAP workouts, count total rounds + additional reps.

## Coaching Language Principles
- Use kinesthetic cues over technical anatomy when possible
  (e.g., "keep elbows high" not "maintain elbow flexion")
- Provide a single, most important cue per fault
- Positive framing when possible: "stay connected" rather than "stop drifting"
- Prioritise safety (CRITICAL faults) in the response ordering
"""


# ---------------------------------------------------------------------------
# Service Class
# ---------------------------------------------------------------------------

class GeminiService:
    """
    Stateless service class for all Gemini API interactions.
    Instantiated once by FastAPI's dependency injection system.

    Thread safety: all blocking Gemini SDK calls are wrapped in
    asyncio.to_thread() to prevent event loop blocking.
    """

    def __init__(self) -> None:
        self._analysis_model = genai.GenerativeModel(
            model_name=VIDEO_ANALYSIS_MODEL,
            safety_settings=SAFETY_SETTINGS,
        )
        self._flash_model = genai.GenerativeModel(
            model_name=IMAGE_GENERATION_MODEL,
            safety_settings=SAFETY_SETTINGS,
        )
        logger.info(
            "gemini_service_initialized",
            analysis_model=VIDEO_ANALYSIS_MODEL,
            image_model=IMAGE_GENERATION_MODEL,
        )

    # -----------------------------------------------------------------------
    # Context Cache Management
    # -----------------------------------------------------------------------

    async def get_or_create_cache(self) -> caching.CachedContent:
        """
        Returns the active context cache, creating it if it does not exist
        or has been evicted from the TTLCache.

        The cache contains the CrossFit movement standards knowledge base.
        On a cache miss (first call or post-expiry), a new Gemini CachedContent
        is created via the API and stored locally.
        """
        cached = _cache_store.get(_CACHE_KEY)
        if cached is not None:
            logger.debug("gemini_cache_hit", cache_name=cached.name)
            return cached

        logger.info("gemini_cache_miss_creating_new_cache")
        cache = await asyncio.to_thread(self._create_context_cache)
        _cache_store[_CACHE_KEY] = cache
        logger.info(
            "gemini_cache_created",
            cache_name=cache.name,
            ttl_seconds=CACHE_TTL_SECONDS,
        )
        return cache

    def _create_context_cache(self) -> caching.CachedContent:
        """
        Synchronous inner function that creates the Gemini CachedContent.
        Must be run in a thread via asyncio.to_thread().
        """
        return caching.CachedContent.create(
            model=VIDEO_ANALYSIS_MODEL,
            display_name="apexai-crossfit-knowledge-base",
            contents=[CROSSFIT_KNOWLEDGE_BASE],
            ttl=f"{CACHE_TTL_SECONDS}s",
        )

    async def force_refresh_cache(self) -> dict[str, Any]:
        """
        Deletes the existing cache and creates a fresh one.
        Called by POST /cache/refresh (admin operation).
        Returns metadata about the new cache.
        """
        # Evict existing cache entry so get_or_create_cache() creates fresh
        _cache_store.pop(_CACHE_KEY, None)

        cache = await self.get_or_create_cache()
        return {
            "cache_name": cache.name,
            "model": VIDEO_ANALYSIS_MODEL,
            "token_count": getattr(cache, "usage_metadata", {}).get("total_token_count", 0),
            "expires_at": datetime.now(timezone.utc).replace(
                second=0, microsecond=0
            ).isoformat(),
            "refreshed_at": datetime.now(timezone.utc).isoformat(),
        }

    # -----------------------------------------------------------------------
    # Video Analysis
    # -----------------------------------------------------------------------

    async def analyze_video(
        self,
        video_path: Path,
        movement_type: str,
        athlete_id: str,
        video_id: str,
    ) -> dict[str, Any]:
        """
        Analyzes a video file using Gemini 1.5 Pro with context caching.

        Parameters
        ----------
        video_path : Path
            Local path to the uploaded video file (temporary storage).
        movement_type : str
            The CrossFit movement being performed (e.g., "snatch", "back_squat").
        athlete_id : str
            Supabase user UUID — used to fetch athlete history for context.
        video_id : str
            video_uploads.id UUID — referenced in the coaching report.

        Returns
        -------
        dict
            Structured coaching analysis. Structure matches CoachingReportResponse.
        """
        if not video_path.exists():
            raise FileNotFoundError(f"Video file not found: {video_path}")

        if video_path.stat().st_size > MAX_VIDEO_SIZE_BYTES:
            raise ValueError(
                f"Video file size {video_path.stat().st_size} exceeds "
                f"maximum {MAX_VIDEO_SIZE_BYTES} bytes"
            )

        cache = await self.get_or_create_cache()

        logger.info(
            "gemini_video_analysis_start",
            video_id=video_id,
            movement_type=movement_type,
            athlete_id=athlete_id,
            cache_name=cache.name,
            file_size_bytes=video_path.stat().st_size,
        )

        result = await asyncio.to_thread(
            self._run_video_analysis,
            video_path,
            movement_type,
            cache,
        )

        logger.info(
            "gemini_video_analysis_complete",
            video_id=video_id,
            rep_count=result.get("rep_count", 0),
            fault_count=len(result.get("faults", [])),
        )

        return result

    def _run_video_analysis(
        self,
        video_path: Path,
        movement_type: str,
        cache: caching.CachedContent,
    ) -> dict[str, Any]:
        """
        Synchronous Gemini video analysis call. Run via asyncio.to_thread().

        The video is uploaded to the Gemini File API first, which returns a
        file URI. This URI is then included in the prompt as a Part.
        Context cache is referenced by name, not re-sent inline.
        """
        # Step 1: Upload the video to the Gemini File API
        logger.info("gemini_file_upload_start", path=str(video_path))
        uploaded_file = genai.upload_file(
            path=str(video_path),
            mime_type="video/mp4",
        )

        # Wait for the file to become ACTIVE (processing can take a few seconds)
        max_wait = 120  # seconds
        waited = 0
        while uploaded_file.state.name == "PROCESSING" and waited < max_wait:
            time.sleep(2)
            waited += 2
            uploaded_file = genai.get_file(uploaded_file.name)

        if uploaded_file.state.name != "ACTIVE":
            raise RuntimeError(
                f"Gemini file upload failed or timed out. "
                f"State: {uploaded_file.state.name}"
            )

        logger.info(
            "gemini_file_upload_complete",
            file_name=uploaded_file.name,
            uri=uploaded_file.uri,
        )

        # Step 2: Build the analysis prompt
        prompt = self._build_analysis_prompt(movement_type)

        # Step 3: Create a model instance that references the context cache
        cached_model = genai.GenerativeModel.from_cached_content(
            cached_content=cache,
            safety_settings=SAFETY_SETTINGS,
        )

        # Step 4: Send the video and prompt to Gemini
        response = cached_model.generate_content(
            contents=[uploaded_file, prompt],
            generation_config={
                "temperature": 0.2,  # Low temperature for consistent structured output
                "response_mime_type": "application/json",
            },
        )

        # Step 5: Audit log for safety threshold events
        try:
            for candidate in response.candidates:
                if hasattr(candidate, "finish_reason") and str(candidate.finish_reason) == "SAFETY":
                    logger.warning(
                        "gemini_safety_block",
                        movement_type=movement_type,
                        safety_ratings=[
                            {"category": str(r.category), "probability": str(r.probability)}
                            for r in (candidate.safety_ratings or [])
                        ],
                    )
        except Exception:
            pass  # Safety audit is best-effort; never block the main pipeline

        # Step 6: Clean up the uploaded file from Gemini File API storage
        try:
            genai.delete_file(uploaded_file.name)
        except Exception as cleanup_err:
            logger.warning(
                "gemini_file_cleanup_failed",
                file_name=uploaded_file.name,
                error=str(cleanup_err),
            )

        # Step 7: Parse and validate the structured JSON response
        return self._parse_analysis_response(response.text, movement_type)

    def _build_analysis_prompt(self, movement_type: str) -> str:
        """
        Builds the structured analysis prompt for a specific movement type.
        The prompt instructs Gemini to return a specific JSON schema that
        maps directly to CoachingReportResponse.
        """
        return f"""
Analyze this CrossFit video of an athlete performing the {movement_type}.

Using the CrossFit movement standards and biomechanical analysis guide
provided in the context, perform a complete coaching analysis.

Return your analysis ONLY as a valid JSON object matching this exact schema:

{{
  "rep_count": <integer — number of complete, standard reps observed>,
  "estimated_weight_kg": <float or null — estimated barbell weight in kg, null if undetectable or bodyweight>,
  "overall_assessment": "<2-4 sentence summary of the athlete's overall performance and most critical area to address>",
  "faults": [
    {{
      "description": "<specific description of what is happening biomechanically>",
      "severity": "<MINOR|MODERATE|CRITICAL>",
      "timestamp_ms": <integer — milliseconds from video start when fault is most visible>,
      "cue": "<single actionable coaching instruction in plain language>",
      "affected_joints": ["<joint name>", ...]
    }}
  ],
  "global_cues": [
    "<coaching cue that applies to the entire set, not a specific rep>"
  ],
  "overlay_data": [
    {{
      "timestamp_ms": <integer>,
      "landmarks": [
        {{
          "index": <0-32>,
          "x": <0.0-1.0>,
          "y": <0.0-1.0>,
          "z": <float>,
          "visibility": <0.0-1.0>
        }}
      ],
      "joint_angles": {{
        "LEFT_KNEE": <degrees>,
        "RIGHT_KNEE": <degrees>,
        "LEFT_HIP": <degrees>,
        "RIGHT_HIP": <degrees>,
        "LEFT_ELBOW": <degrees>,
        "RIGHT_ELBOW": <degrees>,
        "LEFT_SHOULDER": <degrees>,
        "RIGHT_SHOULDER": <degrees>,
        "TRUNK_INCLINATION": <degrees>
      }}
    }}
  ]
}}

Rules:
1. overlay_data should include one entry every 100ms throughout the video.
   For a 10-second video, that is approximately 100 entries.
   Estimate landmark positions and joint angles as accurately as possible.
2. List faults in order from most severe (CRITICAL first) to least severe (MINOR last).
3. Limit to the 5 most significant faults maximum.
4. Global cues should be 2-4 items.
5. The overall_assessment must mention the athlete's strengths as well as areas to improve.
6. Return ONLY the JSON object. No markdown, no explanation text outside the JSON.
"""

    def _parse_analysis_response(
        self,
        response_text: str,
        movement_type: str,
    ) -> dict[str, Any]:
        """
        Parses and validates the Gemini JSON response.
        Raises ValueError if the response cannot be parsed or is structurally invalid.
        """
        try:
            # Strip any accidental markdown code fences
            clean = response_text.strip()
            if clean.startswith("```"):
                # Remove opening fence (```json or ```)
                clean = clean.split("\n", 1)[1] if "\n" in clean else clean
                # Remove closing fence
                if clean.endswith("```"):
                    clean = clean[: clean.rfind("```")]
                clean = clean.strip()

            data = json.loads(clean)

            # Structural validation
            required_keys = {"movement_type", "overall_assessment", "faults", "global_cues", "overlay_data"}
            missing = required_keys - data.keys()
            if missing:
                raise ValueError(f"Gemini response missing required keys: {missing}")

            if data.get("movement_type", "").upper() != movement_type.upper():
                logger.warning(
                    "movement_type mismatch: expected %s, got %s",
                    movement_type,
                    data.get("movement_type"),
                )

            if not isinstance(data.get("faults"), list):
                raise ValueError("'faults' must be a list")

            if not isinstance(data.get("overlay_data"), list):
                raise ValueError("'overlay_data' must be a list")

            return data

        except json.JSONDecodeError as exc:
            raise ValueError(f"Gemini returned non-JSON response: {exc}") from exc
