---
name: reference_api_contract
description: FastAPI endpoint contracts and Supabase REST patterns the Android frontend depends on
type: reference
---

## FastAPI Base URL
- Debug: http://10.0.2.2:8000/v1/
- Release: https://api.apexai-athletics.com/v1/

## FastAPI Endpoints
| Method | Path | Description |
|--------|------|-------------|
| POST | /coaching/analyze | Multipart upload; returns 202 with analysis_id |
| GET | /coaching/status/{analysis_id} | Poll status; returns progress float + stage string |
| GET | /coaching/report/{analysis_id} | Full coaching report JSON |
| POST | /coaching/generate-correction | Takes report_id + fault_id; triggers Gemini Flash image |
| POST | /cache/refresh | Refreshes Gemini context cache (CrossFit knowledge base) |

## Auth
FastAPI validates Supabase JWTs using SUPABASE_JWT_SECRET env var (HS256).
Header: Authorization: Bearer {supabase_access_token}

## Rate Limits
- /coaching/analyze: 10 per hour per user
- /coaching/status: no limit
- /coaching/report: no limit

## Supabase RPC
- POST /rpc/calculate_readiness — body: { "p_user_id": "uuid" }
