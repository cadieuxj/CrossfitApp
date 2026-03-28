Now I have all the context I need. Let me produce the full audit report:

---

# Security Audit Report

**Date:** 2026-03-28
**Auditor:** Quebec Security Auditor (claude-sonnet-4-6)
**Scope:** ApexAI Athletics Android application — full pre-launch audit covering backend microservice, Supabase schema, Android client, and regulatory compliance
**Jurisdiction Focus:** Quebec, Canada (Loi 25 / Law 25 primary; PIPEDA/CPPA federal; CASL)
**App Status:** Pre-production (initial commit, not yet deployed to production or the Google Play Store)

---

## Executive Summary

ApexAI Athletics is a technically well-structured CrossFit coaching application. The codebase demonstrates competent security engineering in several areas: Row Level Security (RLS) is enabled and properly scoped on all user-owned database tables, JWT authentication is correctly implemented with audience validation, input allowlisting is used for movement types, and Android backup exclusions are properly configured.

However, the application has **critical gaps that must be resolved before a single Quebec resident can onboard.** The most serious concern is not a code vulnerability — it is a complete absence of the legal privacy infrastructure required by Quebec's Law 25 (Loi 25). There is no privacy policy, no consent mechanism at registration or for health/video data collection, no Privacy Impact Assessment, no data retention policy, and no right-to-erasure mechanism. These are not enhancements; they are mandatory legal requirements with penalties up to $25,000,000 CAD or 4% of worldwide turnover.

On the technical side, the CORS configuration defaults to a wildcard (`*`) that must be restricted before deployment, the cache refresh endpoint is dangerously under-protected, the rate limiter operates on IP address rather than authenticated user identity (making it bypassable), and a hardcoded mock URI exists in production recording code.

**18 confirmed findings are reported:** 6 Critical, 11 High, 9 Medium, 5 Low.

---

## Findings Summary Table

| ID | Severity | Category | Title |
|----|----------|----------|-------|
| C-01 | CRITICAL | API Security | CORS wildcard allows any origin to make credentialed requests |
| C-02 | CRITICAL | API Security | Rate limiter keyed on IP, not authenticated user identity |
| C-03 | CRITICAL | Broken Access Control | /v1/cache/refresh accessible to all authenticated users |
| C-04 | CRITICAL | Broken Access Control | In-memory job store has no ownership binding |
| C-05 | CRITICAL | Android / Injection | Video URI passed raw through navigation — deep link injection |
| C-06 | CRITICAL | Logic / Data Integrity | VisionViewModel emits hardcoded mock URI in stopRecording() |
| H-01 | HIGH | Law 25 | No privacy policy or Terms of Service at registration |
| H-02 | HIGH | Law 25 / Health Data | No explicit consent for Health Connect data collection |
| H-03 | HIGH | Law 25 / Cross-Border | No disclosure or consent for video transmission to Google Gemini |
| H-04 | HIGH | Law 25 / Retention | No data retention or destruction policy |
| H-05 | HIGH | Law 25 | No right-to-erasure mechanism |
| H-06 | HIGH | Credentials / Key Mgmt | Service role key is the sole write credential — no scoping |
| H-07 | HIGH | AI Safety / Audit | Gemini safety thresholds relaxed with no compensating audit log |
| H-08 | HIGH | Law 25 | No Privacy Impact Assessment (PIA) documented |
| H-09 | HIGH | Authentication | Password minimum length of 6 characters |
| H-10 | HIGH | Android Storage | FileProvider exposes entire app files directory |
| H-11 | HIGH | Observability / PII | Sentry SDK present with no PII scrubbing configuration |
| M-01 | MEDIUM | Android / Deep Links | Custom URI scheme deep links are hijackable |
| M-02 | MEDIUM | Android | RECORD_AUDIO permission lacks in-app disclosure |
| M-03 | MEDIUM | Storage | Signed URL TTL with no revocation mechanism |
| M-04 | MEDIUM | Error Handling | Internal exception messages written to database error_message column |
| M-05 | MEDIUM | API Security | calculate_readiness() RPC has no independent rate limit |
| M-06 | MEDIUM | Dependency | python-jose 3.3.0 — known algorithm confusion vulnerabilities |
| M-07 | MEDIUM | Authentication | No account lockout or brute-force protection beyond Supabase defaults |
| M-08 | MEDIUM | Authentication | No email verification enforcement before account access |
| M-09 | MEDIUM | Android Backup | data_extraction_rules.xml does not exclude the file domain for device transfer |
| L-01 | LOW | Information Disclosure | Health check leaks internal Gemini cache state |
| L-02 | LOW | Android | Missing security headers on API responses |
| L-03 | LOW | CASL | No unsubscribe mechanism for commercial electronic messages |
| L-04 | LOW | Android | RECORD_AUDIO in manifest but no audio-specific disclosure |
| L-05 | LOW | Data Governance | movements.instructions column has no IP protection |

---

## Detailed Findings

---

### C-01 — CORS Wildcard Allows Any Origin to Make Credentialed Requests

**Severity:** CRITICAL
**Category:** API Security / Security Misconfiguration
**File:** `backend/main.py`, line 110

**Description:**
The `cors_origins` setting defaults to `["*"]`, and `allow_credentials=True` is set on the CORS middleware simultaneously. Browsers do not normally honour both at once for security reasons, but the wildcard default clearly signals that this was intended to be restricted in production and was not. If a production deployment ships with this default, any web origin can make credentialed cross-origin requests to the API. This is particularly serious because the API handles biometric video data of Quebec residents.

**Evidence:**
```python
# main.py line 110
cors_origins: list[str] = ["*"]  # Restrict in production

# main.py lines 262-268
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins,
    allow_credentials=True,        # Credentialed requests permitted
    allow_methods=["GET", "POST"],
    allow_headers=["Authorization", "Content-Type"],
)
```

**Impact:**
A malicious web page visited by an authenticated athlete could make API calls on their behalf, potentially exfiltrating coaching reports, health data analysis, or triggering resource-intensive video analysis jobs on the victim's account.

**Remediation:**
Set `CORS_ORIGINS` to the specific production origin(s) in the deployment environment and remove the `["*"]` default entirely. Replace with an explicit allowlist with no fallback:

```python
cors_origins: list[str]  # No default — must be explicitly configured
```

Add a startup validation that fails fast if `cors_origins` is empty or contains `"*"` in non-debug mode.

**References:** OWASP A05:2021 Security Misconfiguration; CWE-942

---

### C-02 — Rate Limiter Keyed on IP, Not Authenticated User Identity

**Severity:** CRITICAL
**Category:** API Security / Broken Access Control
**File:** `backend/main.py`, line 214

**Description:**
The rate limiter for the `/v1/analyze-video` endpoint uses `get_remote_address` as the key function. This means rate limits are enforced per source IP address, not per authenticated user. An attacker behind a different IP (or using proxies, residential VPNs, or Tor exit nodes) can bypass the 10 requests/hour limit entirely. More critically, a single malicious actor controlling many IPs faces no per-identity restriction, and a legitimate user behind a shared NAT (corporate network, university) may be rate-limited by another user's activity.

**Evidence:**
```python
# main.py line 214
limiter = Limiter(key_func=get_remote_address)

# main.py line 512
@limiter.limit("10/hour")
async def analyze_video(..., current_user_id: str = Depends(get_current_user_id)):
```

The `current_user_id` is available as a dependency but is never passed to the limiter.

**Impact:**
Abuse of the Gemini 1.5 Pro video analysis pipeline (which is expensive both in cost and latency) is not effectively constrained per user. A malicious authenticated user can register multiple accounts and submit 10 analysis requests per hour per account from any IP. This also affects Law 25 compliance indirectly — resource exhaustion attacks can cause availability failures that delay breach notifications.

**Remediation:**
Replace the rate limiter key function with one that extracts the authenticated user ID from the JWT. Since the JWT is already validated by the time the rate limit decorator runs, use a custom key function:

```python
def get_authenticated_user_key(request: Request) -> str:
    # Extract from the already-validated JWT stored in request state,
    # or parse the Authorization header directly for the limiter key.
    auth = request.headers.get("authorization", "")
    if auth.lower().startswith("bearer "):
        token = auth.split(" ", 1)[1]
        try:
            payload = jwt.decode(token, settings.supabase_jwt_secret,
                                 algorithms=["HS256"], audience="authenticated")
            return payload.get("sub", get_remote_address(request))
        except Exception:
            pass
    return get_remote_address(request)

limiter = Limiter(key_func=get_authenticated_user_key)
```

**References:** OWASP A04:2021 Insecure Design; CWE-770

---

### C-03 — /v1/cache/refresh Accessible to All Authenticated Users

**Severity:** CRITICAL
**Category:** Broken Access Control
**File:** `backend/main.py`, lines 919–956

**Description:**
The `/v1/cache/refresh` endpoint, which evicts and recreates the Gemini CrossFit knowledge base context cache, is protected only by authentication — any valid user JWT can trigger it. The code comments acknowledge this: *"Note: In production, this should be restricted to admin users. For MVP the check is that the caller is authenticated."* This is not acceptable in a production API. Any authenticated athlete can repeatedly call this endpoint, causing continuous cache eviction and expensive re-creation API calls to Gemini, constituting a Denial-of-Service vector. Furthermore, repeated cache refreshes would disrupt all concurrent analysis pipelines.

**Evidence:**
```python
# main.py line 938-939
# Note: In production, this should be restricted to admin users.
# For MVP the check is that the caller is authenticated.
async def refresh_context_cache(
    current_user_id: str = Depends(get_current_user_id),
    ...
```

**Impact:**
Any authenticated user can trigger Gemini API costs and disrupt service for all users. This is a financial and availability risk. The endpoint effectively constitutes an abuse path for billing attacks against the Gemini API key.

**Remediation:**
Implement an admin role check. Since Supabase Auth supports custom claims via the `app_metadata` field, add an `is_admin` or `role: admin` claim to the JWT for privileged users, and verify it in a new `require_admin_user` dependency:

```python
async def require_admin_user(
    current_user_id: str = Depends(get_current_user_id),
    authorization: Annotated[str | None, Header()] = None,
) -> str:
    # Decode already-validated token to check role claim
    token = authorization.split(" ")[1]
    payload = jwt.decode(token, settings.supabase_jwt_secret,
                         algorithms=["HS256"], audience="authenticated")
    app_metadata = payload.get("app_metadata", {})
    if app_metadata.get("role") != "admin":
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN,
                            detail="Admin access required")
    return current_user_id
```

**References:** OWASP A01:2021 Broken Access Control; CWE-285

---

### C-04 — In-Memory Job Store Has No Ownership Binding

**Severity:** CRITICAL
**Category:** Broken Access Control / Information Disclosure
**File:** `backend/main.py`, line 122; lines 668–691

**Description:**
The `_analysis_jobs` dictionary stores video analysis job state in-memory, keyed by `analysis_id` (a UUID). The `/v1/coaching/status/{analysis_id}` endpoint requires authentication but does **not verify that the job belongs to the requesting user.** Any authenticated user who can enumerate or guess an `analysis_id` UUID can poll the status of another user's video analysis job, learning the analysis stage, progress, and error messages. The coaching report endpoint (`/v1/coaching/report/{analysis_id}`) does enforce user ownership when fetching from Supabase, but the status polling endpoint does not.

**Evidence:**
```python
# main.py lines 678-691
async def get_analysis_status(
    analysis_id: str,
    current_user_id: str = Depends(get_current_user_id),
) -> AnalysisStatusResponse:
    job = _analysis_jobs.get(analysis_id)
    if job is None:
        raise HTTPException(...)
    # NO check that job belongs to current_user_id
    return AnalysisStatusResponse(...)
```

The job dictionary also does not store the owning user ID, making the check impossible to implement without a schema change.

**Impact:**
Cross-user information leakage. An attacker can learn that another user is actively analysing a video (activity inference), what movement they are performing, and at what stage the analysis is. In the context of an athlete's biometric video data, this constitutes a privacy breach under Law 25 (Loi 25, Art. 63.1).

**Remediation:**
Store the `user_id` in the job dictionary at creation time, and enforce ownership on the status endpoint:

```python
# At job creation (analyze_video):
_analysis_jobs[analysis_id] = {
    "user_id": athlete_id,   # ADD THIS
    "status": ...,
    ...
}

# In get_analysis_status:
if job["user_id"] != current_user_id:
    raise HTTPException(status_code=status.HTTP_404_NOT_FOUND,
                        detail=f"Analysis job {analysis_id} not found")
```

Return 404 (not 403) to avoid confirming the existence of the job to a non-owner.

**References:** OWASP A01:2021 Broken Access Control; OWASP API3:2023 Broken Object Property Level Authorization; CWE-639; Loi 25, Art. 63.1

---

### C-05 — Video URI Passed Raw Through Navigation — Deep Link Injection Risk

**Severity:** CRITICAL
**Category:** Android Security / Injection
**File:** `app/src/main/kotlin/com/apexai/crossfit/core/ui/navigation/AppNavigation.kt`, lines 299–315

**Description:**
The video URI recorded by the camera is URL-encoded and passed as a path parameter in the navigation route (`NavRoutes.VISION_REVIEW_PATTERN`). On the receiving end, it is URL-decoded with `java.net.URLDecoder.decode(it, "UTF-8")` and passed directly as a string to `RecordingReviewScreen`. Navigation routes in Jetpack Navigation Compose are strings, and if this URI can be influenced by an external input (e.g., a deep link), an attacker could inject an arbitrary URI pointing to a sensitive `content://` provider or `file://` path. Additionally, `NavRoutes.DEEP_LINK_COACHING_REPORT` is registered and the coaching report screen receives `analysisId` without further sanitization.

**Evidence:**
```kotlin
// AppNavigation.kt lines 302-305
val videoUri = backStackEntry.arguments?.getString("videoUri")?.let {
    java.net.URLDecoder.decode(it, "UTF-8")   // Raw decode, no validation
} ?: ""
RecordingReviewScreen(videoUri = videoUri, ...)
```

The deep link `apexai://coaching/report/{analysisId}` passes `analysisId` directly without UUID format validation at the navigation layer.

**Impact:**
A malicious application or web page using the `apexai://` scheme could trigger navigation to a crafted URI, potentially causing the app to read from or display content from an unintended source. If `RecordingReviewScreen` passes the URI to a video player or file reader, path traversal or content provider injection may result.

**Remediation:**
- Validate that the decoded `videoUri` conforms to an expected pattern (e.g., starts with `content://com.apexai.crossfit.fileprovider/` or is a known media store URI) before passing to the player.
- For `analysisId` received from deep links, validate UUID format before any API call.
- Consider using HTTPS deep links (`https://apexai.app/coaching/report/{id}`) with Android App Links (Digital Asset Links) instead of the `apexai://` custom scheme, which cannot be verified by the OS and can be registered by any installed app.

**References:** OWASP A03:2021 Injection; CWE-926 Improper Export of Android Application Components; Android App Links documentation

---

### C-06 — VisionViewModel Emits Hardcoded Mock URI in stopRecording()

**Severity:** CRITICAL
**Category:** Business Logic / Data Integrity
**File:** `app/src/main/kotlin/com/apexai/crossfit/feature/vision/presentation/camera/VisionViewModel.kt`, line 170

**Description:**
The `stopRecording()` function emits a hardcoded mock URI `"content://mock/recording.mp4"` instead of the actual recorded video file URI from CameraX's `VideoCapture` use case. The `videoRecorder` and `currentRecording` variables are initialized but `videoRecorder` is never set and `currentRecording` is never populated from a real CameraX recording session. This means in the current state, the "recorded" video that gets uploaded to the AI pipeline is a mock URI pointing to no real file.

**Evidence:**
```kotlin
// VisionViewModel.kt lines 166-172
private fun stopRecording() {
    _uiState.update { it.copy(isRecording = false, cameraState = CameraState.READY) }
    // Emit a synthetic URI for review — actual recording uses CameraX VideoCapture
    viewModelScope.launch {
        _effects.send(VisionEffect.NavigateToReview("content://mock/recording.mp4"))
    }
}
```

```kotlin
// Declared but never assigned:
private var videoRecorder: androidx.camera.video.Recorder? = null
private var currentRecording: androidx.camera.video.Recording? = null
```

Furthermore, `bindCameraUseCases()` binds only `Preview` and `ImageAnalysis` — the `VideoCapture` use case is never bound.

**Impact:**
The core feature of the application — video analysis — is non-functional in its current form. Any test or QA that accepted this screen as "working" was validating a mock. When the mock URI is eventually replaced with a real file URI, the access control and consent issues documented elsewhere become immediately exploitable. This also represents a significant risk if the code ships to production in this state: users will believe their video was analysed when it was not.

**Remediation:**
Implement the full CameraX `VideoCapture` use case:
1. Add `VideoCapture<Recorder>` to `bindCameraUseCases()` alongside `Preview` and `ImageAnalysis`.
2. In `startRecording()`, create a `Recording` instance using `videoRecorder?.prepareRecording()` targeting a `MediaStoreOutputOptions` or `FileOutputOptions`.
3. In `stopRecording()`, call `currentRecording?.stop()` and collect the `VideoRecordEvent.Finalize` event to obtain the real `outputResults.outputUri`.
4. Remove the hardcoded `"content://mock/recording.mp4"`.

**References:** CWE-440 Expected Behavior Violation; Android CameraX VideoCapture documentation

---

### H-01 — No Privacy Policy or Terms of Service at Registration

**Severity:** HIGH
**Category:** Quebec Law 25 (Loi 25) — Mandatory Consent
**Files:** `RegisterScreen.kt`, `LoginScreen.kt`

**Description:**
Neither the login nor registration screens present a privacy policy, Terms of Service, or any statement about how personal information is used. Quebec's Law 25 (Loi 25, Art. 8) requires that individuals be informed of the purposes for which their personal information is collected, the name of the person responsible for the protection of personal information, and their rights under the law — all before or at the time of collection. This is not present anywhere in the reviewed codebase.

**Impact:**
Operating without a privacy policy visible at the point of collection is a direct violation of Law 25, Art. 8, and PIPEDA Principle 2 (Identifying Purposes). The Commission d'accès à l'information (CAI) can issue administrative monetary penalties of up to $25,000,000 CAD or 4% of worldwide turnover. For a pre-launch app, this is a showstopper.

**Remediation:**
1. Draft a Privacy Policy in both French and English (Law 25 requires French as the language of commerce in Quebec; PIPEDA is bilingual).
2. Add a checkbox or affirmative click-wrap at registration: "I have read and agree to the [Privacy Policy] and [Terms of Service]." Both links must be functional before the register button is enabled.
3. Store consent with a timestamp and policy version in the user's `profiles` row or a dedicated `consents` table.
4. Ensure the privacy policy explicitly covers: data categories collected, purposes, third-party sharing (Google Gemini, Supabase), retention periods, the identity and contact information of your Privacy Officer, and how to exercise rights (access, correction, erasure, portability).

**References:** Loi 25, Art. 8, 14; PIPEDA Schedule 1, Principle 2; OWASP Top 10 — Data Privacy

---

### H-02 — No Explicit Consent for Health Connect Data Collection

**Severity:** HIGH
**Category:** Quebec Law 25 / Sensitive Health Data
**File:** `HealthConnectDataSource.kt`

**Description:**
The app reads HRV, sleep stages, and resting heart rate from Android Health Connect and stores them in Supabase's `health_snapshots` table. Health data is a category of **sensitive personal information** under Law 25 (Loi 25, Art. 12). The current implementation only calls `checkPermissions()` to verify that the Android OS permission has been granted — it does not present any app-layer consent explaining what the data is used for, where it is stored, or who it is shared with. Android Health Connect permission grants do not constitute informed consent under Law 25; they only establish the technical capability to read data.

**Evidence:**
```kotlin
// HealthConnectDataSource.kt lines 29-38
suspend fun checkPermissions(): Boolean = runCatching {
    val required = setOf(
        HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(RestingHeartRateRecord::class),
    )
    val granted = client.permissionController.getGrantedPermissions()
    required.all { it in granted }
}.getOrDefault(false)
```

No consent screen precedes the permission request. No explanation of server-side storage or use is provided.

**Impact:**
Collecting sensitive health data without explicit informed consent is a violation of Law 25, Art. 12. Storing health data on Supabase servers (outside Quebec) without documenting cross-border transfer safeguards compounds this into a cross-border transfer violation (Law 25, Art. 17).

**Remediation:**
Before requesting Health Connect permissions, display a consent screen that:
- Names each data type being requested (HRV, sleep, resting heart rate)
- Explains the specific purpose (ACWR readiness score calculation)
- Discloses that data is stored on Supabase servers (with jurisdiction information)
- Is presented in French first (or with a language toggle) for Quebec users
- Records the user's explicit affirmative consent with timestamp and data type list

Implement the Health Connect permission rationale screen as required by the [Health Connect app guidelines](https://developer.android.com/health-and-fitness/guides/health-connect/permissions-data-sources).

**References:** Loi 25, Art. 12, 14, 17; Google Health Connect Policy Requirements

---

### H-03 — No Disclosure or Consent for Video Transmission to Google Gemini

**Severity:** HIGH
**Category:** Quebec Law 25 / Cross-Border Transfer / Biometric Data
**Files:** `gemini_service.py`, `VisionViewModel.kt`

**Description:**
When an athlete uploads a video for analysis, that video — which contains their identifiable image, body shape, and movement patterns — is transmitted to Google's Gemini File API and processed on Google's servers, which are located outside Canada (primarily in the United States). This constitutes a cross-border transfer of sensitive personal information under Law 25, Art. 17. Furthermore, biometric data (body position, movement, potentially face) is being processed by a third-party AI. No disclosure of this is made to the user anywhere in the app, and no cross-border transfer agreement or Privacy Impact Assessment (PIA) documents the adequacy of protection measures.

**Evidence:**
```python
# gemini_service.py lines 398-416
uploaded_file = genai.upload_file(
    path=str(video_path),
    mime_type="video/mp4",
)
# Video is now on Google's servers outside Canada
```

**Impact:**
This is arguably the highest-risk Law 25 violation in the application. Transferring biometric video data outside Quebec without the conditions of Art. 17 being met (either the receiving jurisdiction offers equivalent protection, or the user has been explicitly informed and given a meaningful choice) can result in the maximum penalty tier. Google's Gemini API Terms of Service and privacy practices must be assessed, documented, and disclosed.

**Remediation:**
1. Before any video is uploaded for analysis, display a disclosure consent screen explaining: "Your video will be sent to Google's Gemini AI service (servers located outside Canada) for analysis. Google's [Privacy Policy link] applies. Do you consent?"
2. Store this consent with timestamp and version.
3. Conduct and document a Privacy Impact Assessment (PIA) for the Gemini integration specifically.
4. Negotiate or document reliance on Google's standard data processing terms for the cross-border transfer adequacy requirement.
5. Assess whether Gemini data retention policies require explicit deletion requests and integrate with `genai.delete_file()` (already called, but confirm this is surfaced to users as part of their right-to-erasure).

**References:** Loi 25, Art. 12, 17, 63.3; PIPEDA Accountability Principle

---

### H-04 — No Data Retention or Destruction Policy

**Severity:** HIGH
**Category:** Quebec Law 25 / Data Governance
**File:** `supabase/migrations/001_initial_schema.sql`

**Description:**
Law 25 (Loi 25, Art. 23) requires that personal information be destroyed or anonymised when the purpose for which it was collected has been fulfilled. No retention schedule is defined anywhere in the codebase. The `health_snapshots` table accumulates daily indefinitely, the `video_uploads` table accumulates indefinitely (and the actual videos appear to remain in Supabase Storage even after analysis completes — there is no lifecycle policy or deletion trigger), `coaching_reports` with body position overlay data (biometric JSONB) accumulate indefinitely, and `results` and `personal_records` accumulate indefinitely.

**Impact:**
Perpetual storage of health and biometric data beyond its useful purpose violates Law 25, Art. 23. The Supabase "videos" Storage bucket is particularly concerning: videos are saved to a temp file, processed, but the `storage_path` column in `video_uploads` suggests the original video is also stored in the bucket — this is never cleaned up in the code.

**Remediation:**
1. Define a retention schedule for each data category (e.g., health snapshots retained for 18 months; video files deleted from storage within 7 days of analysis completion; coaching reports retained for the life of the account).
2. Implement automated deletion: a PostgreSQL scheduled job (pg_cron) or a backend cron task to delete expired health_snapshots and video files.
3. Delete the raw video from Supabase Storage after analysis is confirmed complete (the temp file is deleted, but the storage bucket copy is not).
4. Document the retention policy in the Privacy Policy.

**References:** Loi 25, Art. 23; PIPEDA Principle 5 (Limiting Use, Disclosure, and Retention)

---

### H-05 — No Right-to-Erasure Mechanism

**Severity:** HIGH
**Category:** Quebec Law 25 — Right to Be Forgotten
**File:** `backend/main.py` (absence of DELETE endpoints)

**Description:**
Law 25 (Loi 25, Art. 28) provides individuals the right to have their personal information deleted when it is no longer necessary for the purpose for which it was collected, or when the consent on which collection was based is withdrawn. The entire FastAPI microservice has zero DELETE endpoints. While Supabase's `ON DELETE CASCADE` constraints would handle referential integrity if a user were deleted from `auth.users`, there is no user-facing mechanism or admin API to exercise the right of erasure. The Supabase service role is available to the backend, but no erasure workflow exists.

**Impact:**
Non-compliance with Law 25, Art. 28. Any Quebec resident who requests deletion of their data must be accommodated. Without a mechanism, the organization cannot comply with deletion requests within the legally required timeframe.

**Remediation:**
1. Create a `DELETE /v1/account` endpoint that, for the authenticated user: deletes all files from the `videos` and `corrections` storage buckets under their user path, deletes all database rows (the cascade will handle children), deletes the Supabase Auth user.
2. Create a user-facing "Delete Account" UI flow in the Profile screen.
3. Create an operational procedure for off-band erasure requests received by email or through the Privacy Officer.
4. Log erasure events for audit purposes.

**References:** Loi 25, Art. 28; PIPEDA Principle 4.3.8

---

### H-06 — Service Role Key Is the Sole Write Credential

**Severity:** HIGH
**Category:** Credential Management / Key Management
**File:** `backend/main.py`, line 138; `backend/.env.example`

**Description:**
The `SUPABASE_SERVICE_ROLE_KEY` is a super-credential that bypasses all Row Level Security policies on the entire Supabase database. The FastAPI microservice uses this key for all server-side writes. While the code does apply manual user-ID filtering on reads (`eq("user_id", current_user_id)`), a single miscoded query, a logic error in any future endpoint, or a code injection vulnerability would grant full, unrestricted access to the personal information of all users — including their health data and biometric video data.

**Evidence:**
```python
def get_supabase_client() -> Client:
    return create_client(settings.supabase_url, settings.supabase_service_role_key)
```

This same client is used for both writes (appropriate) and reads (the report fetch and fault ownership check), meaning any future developer who adds a read query to a new endpoint with a bug may inadvertently expose cross-user data.

**Impact:**
The blast radius of any server-side vulnerability is maximised by using the service role key for all operations. Under Law 25, a breach caused by over-privileged credentials would be difficult to mitigate in the breach notification to the CAI.

**Remediation:**
- For read operations that should be user-scoped, use a Supabase client authenticated with the user's own JWT (by passing it to the Supabase client constructor), allowing RLS to enforce ownership automatically.
- Reserve the service role client exclusively for operations that legitimately require bypassing RLS (writing `coaching_reports`, updating `video_uploads` status — both already well-identified in comments).
- Consider creating a custom PostgreSQL role with INSERT/UPDATE permissions only on the specific tables the service needs to write to, and using that instead of the full service role where possible.

**References:** CWE-250 Execution with Unnecessary Privileges; OWASP A01:2021; Principle of Least Privilege

---

### H-07 — Gemini Safety Thresholds Relaxed with No Compensating Audit Log
