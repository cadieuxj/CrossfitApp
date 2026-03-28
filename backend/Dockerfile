# =============================================================================
# ApexAI Athletics — FastAPI Microservice Dockerfile
# Multi-stage build: builder installs deps, runtime is minimal.
# Target: Cloud Run / Railway / Fly.io
# =============================================================================

# Stage 1: Builder
FROM python:3.11-slim AS builder

WORKDIR /app

# Install build dependencies for packages that compile C extensions
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir --prefix=/install -r requirements.txt

# Stage 2: Runtime
FROM python:3.11-slim AS runtime

WORKDIR /app

# ffmpeg is required by gemini_service._extract_video_frame()
RUN apt-get update && apt-get install -y --no-install-recommends \
    ffmpeg \
    && rm -rf /var/lib/apt/lists/*

# Copy installed packages from builder
COPY --from=builder /install /usr/local

# Copy application source
COPY main.py models.py gemini_service.py ./

# Non-root user for security
RUN adduser --disabled-password --gecos "" appuser
USER appuser

# Cloud Run requires listening on $PORT (default 8080)
ENV PORT=8080

EXPOSE ${PORT}

CMD ["sh", "-c", "uvicorn main:app --host 0.0.0.0 --port ${PORT} --workers 2 --log-level info"]
