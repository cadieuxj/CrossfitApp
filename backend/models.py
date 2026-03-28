"""
ApexAI Athletics — Pydantic Models for FastAPI Microservice
===========================================================
All request/response shapes for the coaching pipeline.
These models are the authoritative API contract between
the FastAPI microservice and the Android client.

Design notes:
- All UUIDs are typed as str (validated by Pydantic's uuid validator on input).
- Timestamps use datetime for proper JSON ISO-8601 serialization.
- Optional fields are explicit; never use bare None defaults in required fields.
- Error responses follow the project envelope: {"success": false, "error": {...}}
"""

from __future__ import annotations

import uuid
from datetime import datetime
from enum import Enum
from typing import Any

from pydantic import BaseModel, ConfigDict, Field, field_validator


# ---------------------------------------------------------------------------
# Shared Enums — must stay in sync with PostgreSQL CHECK constraints
# ---------------------------------------------------------------------------

class WorkoutType(str, Enum):
    AMRAP = "AMRAP"
    EMOM = "EMOM"
    RFT = "RFT"
    TABATA = "TABATA"


class ScoringType(str, Enum):
    REPS = "REPS"
    TIME = "TIME"
    LOAD = "LOAD"
    ROUNDS_PLUS_REPS = "ROUNDS_PLUS_REPS"


class FaultSeverity(str, Enum):
    MINOR = "MINOR"
    MODERATE = "MODERATE"
    CRITICAL = "CRITICAL"


class AnalysisConfidence(str, Enum):
    HIGH = "HIGH"
    MEDIUM = "MEDIUM"
    LOW = "LOW"


class AnalysisStatus(str, Enum):
    PROCESSING = "processing"
    COMPLETE = "complete"
    ERROR = "error"


class AnalysisStage(str, Enum):
    UPLOADING = "uploading"
    ANALYZING_VIDEO = "analyzing_video"
    GENERATING_CORRECTIONS = "generating_corrections"
    FINALIZING = "finalizing"


# ---------------------------------------------------------------------------
# Pose / Overlay Data Shapes
# PoseLandmark and TimedPoseOverlay are retained for on-device use only.
# They are NOT populated by Gemini (LLMs cannot produce valid pose coordinates).
# Real 3D pose data is produced by DepthPoseFuser (ARCore + MediaPipe) on-device.
# ---------------------------------------------------------------------------

class PoseLandmark(BaseModel):
    """Single BlazePose landmark. Indices follow MediaPipe topology (0-32)."""
    model_config = ConfigDict(frozen=True)

    index: int = Field(..., ge=0, le=32, description="BlazePose landmark index")
    x: float = Field(..., ge=0.0, le=1.0, description="Normalized horizontal position")
    y: float = Field(..., ge=0.0, le=1.0, description="Normalized vertical position")
    z: float = Field(default=0.0, description="Depth estimate — unreliable on mobile")
    visibility: float = Field(..., ge=0.0, le=1.0, description="Landmark confidence")


class TimedPoseOverlay(BaseModel):
    """
    Pose data for a single video frame.
    The Android client uses timestamp_ms to synchronize with Media3 playback.
    """
    model_config = ConfigDict(frozen=True)

    timestamp_ms: int = Field(..., ge=0, description="Frame timestamp in milliseconds")
    landmarks: list[PoseLandmark]
    joint_angles: dict[str, float] = Field(
        default_factory=dict,
        description="Joint name -> angle in degrees. Keys match JointAngle enum on Android."
    )


# ---------------------------------------------------------------------------
# Movement Fault
# ---------------------------------------------------------------------------

class MovementFaultResponse(BaseModel):
    """
    A single biomechanical fault detected in the video analysis.
    corrected_image_url is populated by a follow-up Gemini Flash call
    and may be null immediately after analysis completes.
    """
    id: str = Field(default_factory=lambda: str(uuid.uuid4()))
    description: str = Field(..., min_length=1, max_length=1000)
    severity: FaultSeverity
    timestamp_ms: int = Field(..., ge=0)
    cue: str = Field(..., min_length=1, max_length=500, description="Athlete-facing coaching instruction")
    corrected_image_url: str | None = None
    affected_joints: list[str] = Field(default_factory=list)


# ---------------------------------------------------------------------------
# Coaching Report (full response returned to Android)
# ---------------------------------------------------------------------------

AI_DISCLAIMER = (
    "AI-generated analysis · Not coach-validated · "
    "CRITICAL flags should be reviewed with a qualified coach. "
    "Score based on population research · Not individually calibrated."
)


class CoachingReportResponse(BaseModel):
    """
    Complete Gemini coaching analysis result.
    Returned by GET /coaching/report/{analysis_id}.

    Notes:
    - overlay_data removed: LLMs cannot produce valid pose coordinates.
      Real 3D pose data is produced by DepthPoseFuser on-device.
    - estimated_weight_kg removed: unverifiable from monocular video.
    - analysis_confidence added: HIGH/MEDIUM/LOW based on video clarity.
    - disclaimer added: Quebec Law 25 compliance requirement.
    """
    id: str
    video_id: str
    movement_type: str
    overall_assessment: str
    rep_count: int = Field(..., ge=0)
    analysis_confidence: AnalysisConfidence = AnalysisConfidence.MEDIUM
    faults: list[MovementFaultResponse]
    global_cues: list[str]
    prompt_version: str = "v1.0"
    disclaimer: str = AI_DISCLAIMER
    created_at: datetime


# ---------------------------------------------------------------------------
# Analysis Submission
# ---------------------------------------------------------------------------

class AnalyzeVideoResponse(BaseModel):
    """
    Response to POST /analyze-video (202 Accepted).
    The Android client polls /coaching/status/{analysis_id} at 3-second
    intervals until status == 'complete'.
    """
    analysis_id: str
    status: AnalysisStatus = AnalysisStatus.PROCESSING
    estimated_seconds: int = Field(default=45, ge=0)
    poll_url: str


class AnalysisStatusResponse(BaseModel):
    """
    Response to GET /coaching/status/{analysis_id}.
    progress is a float 0.0–1.0 for the Android upload/analysis progress bar.
    """
    analysis_id: str
    status: AnalysisStatus
    progress: float = Field(..., ge=0.0, le=1.0)
    stage: AnalysisStage | None = None
    error_message: str | None = None


# ---------------------------------------------------------------------------
# Correction Image Generation
# ---------------------------------------------------------------------------

class GenerateCorrectionImageRequest(BaseModel):
    """
    Request body for POST /generate-correction-image.
    fault_timestamp_ms is used to extract the specific frame for Gemini Flash.
    """
    report_id: str = Field(..., description="coaching_reports.id UUID")
    fault_id: str = Field(..., description="movement_faults.id UUID")
    fault_timestamp_ms: int = Field(..., ge=0)
    fault_description: str = Field(..., min_length=1, max_length=1000)
    movement_type: str

    @field_validator("report_id", "fault_id")
    @classmethod
    def validate_uuid(cls, v: str) -> str:
        try:
            uuid.UUID(v)
        except ValueError as exc:
            raise ValueError(f"Invalid UUID: {v}") from exc
        return v


class GenerateCorrectionImageResponse(BaseModel):
    """
    Response to POST /generate-correction-image.
    The corrected_image_url is a Supabase Storage signed URL (1-hour TTL).
    """
    fault_id: str
    corrected_image_url: str
    storage_path: str


# ---------------------------------------------------------------------------
# Context Cache Management
# ---------------------------------------------------------------------------

class CacheRefreshResponse(BaseModel):
    """Response to POST /cache/refresh."""
    cache_name: str = Field(..., description="Gemini API resource name for the new cache")
    model: str
    token_count: int
    expires_at: datetime
    refreshed_at: datetime


# ---------------------------------------------------------------------------
# Standard Error Envelope
# Used for all 4xx and 5xx responses. The Android client checks
# success == false and displays error.message to the user.
# ---------------------------------------------------------------------------

class ErrorDetail(BaseModel):
    code: str = Field(..., description="Machine-readable error code, e.g. 'VIDEO_TOO_LARGE'")
    message: str = Field(..., description="Human-readable error message for display")


class ErrorResponse(BaseModel):
    success: bool = False
    error: ErrorDetail


# ---------------------------------------------------------------------------
# Health Check
# ---------------------------------------------------------------------------

class HealthCheckResponse(BaseModel):
    status: str = "ok"
    version: str
    gemini_cache_loaded: bool
    uptime_seconds: float
