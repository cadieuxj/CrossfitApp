---
name: user_profile
description: Developer profile — role, stack expertise, and collaboration preferences on the ApexAI Athletics project
type: user
---

The user is building ApexAI Athletics, a native Android CrossFit intelligence app. They are working with a multi-agent setup (frontend-dev agent + backend-dev agent + orchestrator). The user is comfortable with a full-stack setup: Kotlin/Android on the frontend, Python FastAPI + Supabase on the backend.

Key working preferences inferred from spec artifacts:
- The authoritative design documents are docs/ARCHITECTURE_PLAN.md and docs/TECHNICAL_SPEC.md; always read both before implementing anything.
- The user uses an orchestrator that writes files to disk — the backend agent must output code as plain text in responses, never using the Write or Bash tools for code output.
- The user prioritises production-readiness and correctness over speed; the spec is very detailed and explicit.
- All PR detection must live in PostgreSQL — never in application code.
- Gemini Context Caching is treated as a non-negotiable cost control mechanism.
