---
name: ApexAI Athletics — Design System Tokens
description: Established color palette, typography scale, spacing, and component conventions for ApexAI Athletics
type: project
---

## Color System (Dark Athletic Theme)

### Brand Colors
- Primary Electric Blue: #00D4FF
- Primary Blue Dark (pressed): #00A8CC
- Accent Neon Green: #39FF14
- Accent Orange (warning/high intensity): #FF6B35

### Neutral Scale
- Background Deep Black: #0A0A0F
- Surface Dark: #12121A
- Surface Elevated: #1A1A26
- Surface Card: #1E1E2E
- Border Subtle: #2A2A3E
- Border Visible: #3A3A55

### Text Colors
- Text Primary: #F0F0FF
- Text Secondary: #9090B0
- Text Disabled: #505068
- Text On-Blue: #000000 (black on electric blue backgrounds)

### Semantic Colors
- Success: #39FF14 (reuses accent neon green)
- Warning: #FFB800
- Error/Danger: #FF3B5C
- Info: #00D4FF (reuses primary blue)

### Readiness Zone Colors
- OPTIMAL (0.8–1.3): #39FF14
- CAUTION (1.3–1.5): #FFB800
- HIGH_RISK (>1.5): #FF3B5C
- UNDERTRAINED (<0.8): #9090B0

### Fault Severity Colors
- MINOR: #FFB800
- MODERATE: #FF6B35
- CRITICAL: #FF3B5C

## Typography Scale (font: Inter, fallback: system-ui sans-serif)

| Token | Size | Weight | Line Height | Usage |
|---|---|---|---|---|
| DisplayLarge | 48sp | 800 | 52sp | Splash logo, major KPI numbers |
| DisplayMedium | 36sp | 700 | 40sp | Readiness score number |
| HeadlineLarge | 28sp | 700 | 34sp | Screen titles |
| HeadlineMedium | 24sp | 600 | 30sp | Section headers, card titles |
| HeadlineSmall | 20sp | 600 | 26sp | Sub-section headers |
| TitleLarge | 18sp | 600 | 24sp | Card titles, list item primaries |
| TitleMedium | 16sp | 500 | 22sp | Tab labels, button labels |
| BodyLarge | 16sp | 400 | 24sp | Primary body copy |
| BodyMedium | 14sp | 400 | 20sp | Secondary body, descriptions |
| BodySmall | 12sp | 400 | 16sp | Captions, timestamps |
| LabelLarge | 14sp | 600 | 20sp | Badge labels, chip labels |
| LabelSmall | 11sp | 500 | 14sp | Overlines, metadata |

Letter spacing: HeadlineLarge+ uses -0.5px; body uses 0px; LabelSmall uses +0.5px

## Spacing Scale (base unit: 4dp)
4, 8, 12, 16, 20, 24, 32, 40, 48, 64, 80, 96dp

## Corner Radii
- Small: 8dp (chips, badges)
- Medium: 12dp (cards, inputs)
- Large: 16dp (bottom sheets, modals)
- XLarge: 24dp (FAB, dialogs)
- Full: 50% (avatar, circular indicators)

## Elevation / Shadow
Uses color-based elevation (dark theme): elevated surfaces use Surface Elevated (#1A1A26) at dp 8+; no drop shadows on dark backgrounds.

## Icon Library
Lucide Icons (Compose port). Size conventions:
- Nav bar icons: 24dp
- Action icons in cards/buttons: 20dp
- Inline text icons: 16dp

## Motion
- Micro interactions (button press, toggle): 150ms EaseInOut
- Screen transitions (slide): 300ms EaseInOut
- Modal/bottom sheet: 350ms EaseOut
- Skeleton shimmer: 1200ms linear infinite
- Score ring fill animation: 800ms EaseOut on first load
