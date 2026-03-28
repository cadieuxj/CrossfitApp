/**
 * ApexAI Athletics — Supabase Edge Function: calculate-readiness
 * ==============================================================
 * Accepts Health Connect biometric data posted by the Android client,
 * persists it as a health_snapshot, then calculates and returns the
 * ACWR composite readiness score.
 *
 * This function is invoked via:
 *   POST https://{PROJECT_REF}.supabase.co/functions/v1/calculate-readiness
 *
 * Auth: Requires Supabase JWT in Authorization: Bearer header.
 *       The user ID is extracted from the JWT — never trusted from the request body.
 *
 * Request body:
 * {
 *   "hrv_rmssd": 68,                    // int, ms (optional)
 *   "sleep_duration_minutes": 420,       // int (optional)
 *   "deep_sleep_minutes": 90,            // int (optional)
 *   "rem_sleep_minutes": 100,            // int (optional)
 *   "resting_hr": 52,                    // int, bpm (optional)
 *   "captured_at": "2026-03-28T07:00:00Z" // ISO 8601 (required)
 * }
 *
 * Success Response (200):
 * {
 *   "success": true,
 *   "data": {
 *     "snapshot_id": "uuid",
 *     "readiness": {
 *       "acwr": 1.12,
 *       "zone": "OPTIMAL",
 *       "acute_load": 2240.5,
 *       "chronic_load": 1998.3,
 *       "hrv_rmssd": 68,
 *       "sleep_minutes": 420,
 *       "resting_hr": 52,
 *       "recommendation": "...",
 *       "calculated_at": "2026-03-28T08:30:00Z"
 *     }
 *   }
 * }
 *
 * The Android ReadinessViewModel calls this function after syncing
 * Health Connect data. The returned readiness object maps directly
 * to ReadinessScore domain entity.
 */

import { createClient, SupabaseClient } from "https://esm.sh/@supabase/supabase-js@2.47.0";

// ---------------------------------------------------------------------------
// Types
// ---------------------------------------------------------------------------

interface HealthConnectPayload {
  hrv_rmssd?: number;
  sleep_duration_minutes?: number;
  deep_sleep_minutes?: number;
  rem_sleep_minutes?: number;
  resting_hr?: number;
  captured_at: string;
}

interface ReadinessResult {
  acwr: number;
  zone: "UNDERTRAINED" | "OPTIMAL" | "CAUTION" | "HIGH_RISK";
  acute_load: number;
  chronic_load: number;
  hrv_rmssd: number | null;
  sleep_minutes: number | null;
  resting_hr: number | null;
  hrv_component: number | null;
  sleep_component: number | null;
  recommendation: string;
  calculated_at: string;
}

interface SuccessResponse<T> {
  success: true;
  data: T;
}

interface ErrorResponse {
  success: false;
  error: {
    code: string;
    message: string;
  };
}

type ApiResponse<T> = SuccessResponse<T> | ErrorResponse;

// ---------------------------------------------------------------------------
// Constants
// ---------------------------------------------------------------------------

// ACWR zone thresholds from CLAUDE.md and ARCHITECTURE_PLAN.md
const ACWR_UNDERTRAINED_MAX = 0.8;
const ACWR_OPTIMAL_MAX = 1.3;
const ACWR_CAUTION_MAX = 1.5;

// HRV thresholds for component scoring (population-level guidelines)
const HRV_EXCELLENT_THRESHOLD = 70;  // ms RMSSD
const HRV_ADEQUATE_THRESHOLD = 40;

// Sleep thresholds for component scoring
const SLEEP_OPTIMAL_MINUTES = 480;   // 8 hours
const SLEEP_ADEQUATE_MINUTES = 360;  // 6 hours

// Minimum chronic workload to prevent division-by-zero on new users
const MIN_CHRONIC_LOAD = 0.01;

// ---------------------------------------------------------------------------
// Validation
// ---------------------------------------------------------------------------

/**
 * Validates and sanitizes the Health Connect payload.
 * Returns a typed, validated payload or throws with a descriptive message.
 */
function validatePayload(raw: unknown): HealthConnectPayload {
  if (typeof raw !== "object" || raw === null) {
    throw new Error("Request body must be a JSON object");
  }

  const body = raw as Record<string, unknown>;

  // captured_at is required — without it we cannot enforce the per-day
  // UNIQUE constraint on health_snapshots.
  if (!body.captured_at || typeof body.captured_at !== "string") {
    throw new Error("captured_at is required and must be an ISO 8601 string");
  }

  const capturedAt = new Date(body.captured_at);
  if (isNaN(capturedAt.getTime())) {
    throw new Error(`captured_at '${body.captured_at}' is not a valid ISO 8601 date`);
  }

  // Validate numeric fields if provided
  const numericFields = [
    "hrv_rmssd",
    "sleep_duration_minutes",
    "deep_sleep_minutes",
    "rem_sleep_minutes",
    "resting_hr",
  ] as const;

  for (const field of numericFields) {
    if (body[field] !== undefined) {
      const value = Number(body[field]);
      if (isNaN(value) || value < 0) {
        throw new Error(`${field} must be a non-negative number, got: ${body[field]}`);
      }
    }
  }

  // Domain-specific range validation
  if (body.hrv_rmssd !== undefined) {
    const hrv = Number(body.hrv_rmssd);
    if (hrv < 1 || hrv > 300) {
      throw new Error(`hrv_rmssd ${hrv} is outside valid range (1–300 ms)`);
    }
  }

  if (body.resting_hr !== undefined) {
    const hr = Number(body.resting_hr);
    if (hr < 20 || hr > 250) {
      throw new Error(`resting_hr ${hr} is outside valid range (20–250 bpm)`);
    }
  }

  if (body.sleep_duration_minutes !== undefined) {
    const sleep = Number(body.sleep_duration_minutes);
    if (sleep > 1440) {
      throw new Error(`sleep_duration_minutes ${sleep} exceeds 24 hours (1440 min)`);
    }
  }

  return {
    hrv_rmssd: body.hrv_rmssd !== undefined ? Number(body.hrv_rmssd) : undefined,
    sleep_duration_minutes: body.sleep_duration_minutes !== undefined
      ? Number(body.sleep_duration_minutes)
      : undefined,
    deep_sleep_minutes: body.deep_sleep_minutes !== undefined
      ? Number(body.deep_sleep_minutes)
      : undefined,
    rem_sleep_minutes: body.rem_sleep_minutes !== undefined
      ? Number(body.rem_sleep_minutes)
      : undefined,
    resting_hr: body.resting_hr !== undefined ? Number(body.resting_hr) : undefined,
    captured_at: body.captured_at as string,
  };
}

// ---------------------------------------------------------------------------
// ACWR Calculation
// ---------------------------------------------------------------------------

/**
 * Calculates Acute:Chronic Workload Ratio from the athlete's results table.
 *
 * Formula (from CLAUDE.md):
 *   W_acute  = SUM(score_numeric × rpe_or_5) for past 7 days
 *   W_chronic = SUM(score_numeric × rpe_or_5) for past 28 days ÷ 4
 *   ACWR = W_acute / W_chronic
 *
 * The 28-day sum divided by 4 gives the equivalent 7-day average,
 * making the ratio dimensionally consistent.
 */
async function calculateAcwr(
  supabase: SupabaseClient,
  userId: string,
): Promise<{ acuteLoad: number; chronicLoad: number; acwr: number }> {
  const now = new Date().toISOString();

  const sevenDaysAgo = new Date(Date.now() - 7 * 24 * 60 * 60 * 1000).toISOString();
  const twentyEightDaysAgo = new Date(Date.now() - 28 * 24 * 60 * 60 * 1000).toISOString();

  // Acute load: past 7 days
  const { data: acuteData, error: acuteError } = await supabase
    .from("results")
    .select("score_numeric, rpe")
    .eq("user_id", userId)
    .gte("completed_at", sevenDaysAgo)
    .not("score_numeric", "is", null)
    .gt("score_numeric", 0);

  if (acuteError) {
    throw new Error(`Failed to fetch acute workload data: ${acuteError.message}`);
  }

  const acuteLoad = (acuteData || []).reduce((sum, row) => {
    const load = (row.score_numeric ?? 0) * (row.rpe ?? 5);
    return sum + load;
  }, 0);

  // Chronic load: past 28 days (sum / 4 = weekly average)
  const { data: chronicData, error: chronicError } = await supabase
    .from("results")
    .select("score_numeric, rpe")
    .eq("user_id", userId)
    .gte("completed_at", twentyEightDaysAgo)
    .not("score_numeric", "is", null)
    .gt("score_numeric", 0);

  if (chronicError) {
    throw new Error(`Failed to fetch chronic workload data: ${chronicError.message}`);
  }

  const chronicSum = (chronicData || []).reduce((sum, row) => {
    const load = (row.score_numeric ?? 0) * (row.rpe ?? 5);
    return sum + load;
  }, 0);

  const chronicLoad = Math.max(chronicSum / 4.0, MIN_CHRONIC_LOAD);
  const acwr = acuteLoad / chronicLoad;

  return {
    acuteLoad: Math.round(acuteLoad * 100) / 100,
    chronicLoad: Math.round(chronicLoad * 100) / 100,
    acwr: Math.round(acwr * 10000) / 10000,
  };
}

//
Continuing the Edge Function from the `calculateAcwr` call onward.

---

## File: `supabase/functions/calculate-readiness/index.ts` (continued)
