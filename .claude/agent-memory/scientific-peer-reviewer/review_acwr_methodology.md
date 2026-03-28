---
name: review_acwr_methodology
description: Scientific findings on the ACWR readiness implementation — reviewed 2026-03-28
type: project
---

Review date: 2026-03-28

**Why:** Peer review of ApexAI Athletics v1 scientific methodology.
**How to apply:** Use as baseline when reviewing future ACWR changes or readiness feature additions.

## Confirmed Issues

### Chronic Window Overlap (Critical)
The acute (7-day) and chronic (28-day) windows fully overlap. The chronic average is 28-day total / 4 — this is mathematically the same as using 4 weeks of data where week 4 is identical to the acute period. The original Gabbett (2016) formulation requires distinct, non-overlapping windows OR an exponentially weighted moving average (EWMA). Hulin et al. (2016) and Murray et al. (2017) both validated the non-overlapping model. This is a Moderate-to-Major methodological deviation.

### Load Metric Heterogeneity (Major)
Training load = score_numeric * RPE. But score_numeric is dimensionally inconsistent across workout types:
- For RFT workouts: score_numeric is elapsed time in seconds (lower = better)
- For AMRAP: score_numeric is total reps
- For LOAD: score_numeric is kilograms
Multiplying seconds-elapsed * RPE and adding it to reps * RPE produces a dimensionlessly incoherent sum. This invalidates cross-workout-type ACWR comparisons. Session-RPE (Foster 1998) is the correct method — it requires the athlete to rate the whole session after completion and multiply by session duration in minutes.

### Missing RPE Default Justification
Default RPE of 5 when not recorded is arbitrary. Literature uses mean RPE of logged sessions as the imputation value, not a fixed 5.

### HRV Thresholds Are Population-Level, Not Individual-Baselines
The RMSSD thresholds (40ms, 70ms) are population averages. The scientifically valid approach (Kiviniemi et al. 2007, Plews et al. 2012) uses individual rolling 7-day baseline with coefficient of variation flags, not fixed absolute thresholds.

### Historical ReadinessScore Hardcodes OPTIMAL Zone
In getReadinessHistory(), all historical rows are assigned ReadinessZone.OPTIMAL regardless of actual data. This is a bug that could mislead UI trend displays.

### No New-Athlete Cold Start Handling
With zero results, v_chronic = 0.01 (GREATEST floor). v_acute = 0. ACWR = 0/0.01 = 0.0 -> UNDERTRAINED. This is correct behavior but the recommendation text ("increase load by 10-15% this week") is misleading for a brand new user with zero baseline.
