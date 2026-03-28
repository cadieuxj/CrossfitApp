---
name: feedback_agent_sequencing
description: Agent pipeline execution order, what has run, and sequencing rules for this project
type: feedback
---

Do NOT re-run agents that have already completed. All 8 prior agent runs have produced persistent output in docs/, app/, backend/, and supabase/.

**Why:** Re-running completed agents would overwrite fixes that were already applied from code review and security audit rounds.

**How to apply:** Always check project_state_2026_03_28.md for the completed agent list before invoking any agent. Only invoke agents for work not yet done.

## Completed Agents (do not re-run)
1. app-spec-writer → docs/TECHNICAL_SPEC.md
2. android-architecture-planner → docs/ARCHITECTURE_PLAN.md
3. ui-ux-designer → docs/UI_UX_DESIGN.md
4. backend-dev → supabase/, backend/
5. android-frontend-coder → app/src/main/kotlin/ (88 files) + res/
6. android-qa-engineer → tests + CI/CD
7. code-review-advisor → docs/CODE_REVIEW.md + P0/P1 fixes applied
8. quebec-security-auditor → docs/SECURITY_AUDIT.md + HIGH fixes applied

## Remaining Agent Work (next phases)
- android-frontend-coder (second pass): fix build blockers B-01/B-02/B-03/R-01
- backend-dev (second pass): fix CORS wildcard, upgrade python-jose
- crossfit-athlete-evaluator: UX evaluation after build blockers resolved
- scientific-peer-reviewer: validate ACWR algorithm and MediaPipe angle math
