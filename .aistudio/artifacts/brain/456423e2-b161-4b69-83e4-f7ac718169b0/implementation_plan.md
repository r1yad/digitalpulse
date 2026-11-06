# DigitalPulse: Liquid Glass Personal Digital Monitor

A complete, feature-rich native Android application written **exclusively in Java** using the Android SDK, architected with an ultra-premium **Liquid Glass** visual design system. Replaces conventional Material card layouts with translucent frosted surfaces, layered floating glass panels, subtle luminous borders, organic curves, and responsive glass charts, backed by Room persistence, MVVM architecture, and battery-efficient background telemetry.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following architectural and design decisions govern this build:

- **Liquid Glass Visual Language**: Universal design system overhaul across all modules, dialogs, bottom sheets, and navigation elements. Translucent surfaces (`#1AFFFFFF` / `#0DFFFFFF` on dark, `#E6FFFFFF` / `#CCFFFFFF` on light), frosted glass effects, subtle luminous border strokes (`#26FFFFFF` / `#15000000`), and ambient gradient lighting behind content.
- **Blur & Transparency Strategy (Native Android Java)**:
  - On **Android 12+ (API 31+)**: Leverage hardware-accelerated `RenderEffect.createBlurEffect(radiusX, radiusY, Shader.TileMode.CLAMP)` on backdrop containers.
  - On **Android 8.0–11 (API 26–30)**: Graceful high-performance fallback using multi-layered `GradientDrawable`, alpha-composited surface washes, and soft ambient radial glows. Zero jank, no external fragile dependencies.
- **Floating Liquid Navigation Capsule**: Detached, translucent pill-shaped bottom navigation bar with rounded 28dp corners, subtle luminous glow on active icons, smooth tab indicators, and full WindowInsets edge-to-edge support.
- **Pure Java Native Architecture**: 100% Java source files (`.java`), custom Glass View components (`GlassCardView`, `GlassFloatingNav`, `GlassMetricPill`, `GlassChartView`), Room database DAOs/Entities, MVVM with `LiveData`/`ViewModel`, and WorkManager background telemetry. No Kotlin or WebViews.
- **Universal Application**: Redesign is applied consistently across **all** modules: Home Dashboard, Network Monitor, Screen Time Analytics, Notification Saver, Clipboard Manager, Battery Monitor, Automation Rules, and Onboarding.

---

## 1. Overview & Core Concept

**DigitalPulse** combines comprehensive device telemetry with a futuristic Liquid Glass interface. It offers live monitoring and deep historical insights for:
1. **Screen Time & App Limits**: Usage tracking, launch counters, session averages, and 75%/90%/100% smart warnings.
2. **Network Bandwidth & Speed**: Cellular vs. Wi-Fi analytics, per-app data consumption, billing cycles, and CSV export.
3. **Notification Saver**: Searchable local archive via `NotificationListenerService`, app grouping, bookmarks, and automated cleanup.
4. **Clipboard Manager**: 24-hour temporary auto-expiring clipboard, favorites, categorization (links, text, emails), and manual quick-add.
5. **Battery Analytics**: Live battery temperature, voltage, charging state, cycle history, and high-temperature alerts.
6. **Automation Engine**: User-configured trigger rules (screen time thresholds, high data usage alerts, battery overheat warnings).

---

## 2. User Experience & Visual Design

### Liquid Glass Design System Specifications

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Liquid Glass Layer Hierarchy                    │
│                                                                        │
│  [Layer 0] Ambient Mesh Gradient Backdrop (Aurora Orbs: Cyan & Violet) │
│                                  │                                     │
│  [Layer 1] Base Frosted Surface (Dark Charcoal: #101216 / 85% alpha)   │
│                                  │                                     │
│  [Layer 2] Floating Glass Card Panels (Translucent: #15FFFFFF fill)   │
│            Border: 1dp Luminous Stroke (#2AFFFFFF / #64B5F6 accent)   │
│            Corner Radius: 20dp - 28dp pill curves                      │
│                                  │                                     │
│  [Layer 3] High-Contrast Typography & Glass Interactive Controls       │
│            (Titles: #FFFFFF, Subtitles: #B0BEC5, Accents: #4FC3F7)    │
│                                  │                                     │
│  [Layer 4] Floating Glass Bottom Navigation Pill (Detached 16dp inset) │
└────────────────────────────────────────────────────────────────────────┘
```

### Color & Lighting Palette

#### Dark Liquid Glass (Primary Experience)
- **Base Background**: Deep Obsidian / Charcoal (`#0B0E14`) with ambient radial mesh orbs (`#1E2640` and `#1A3038`).
- **Glass Panel Fill**: `rgba(255, 255, 255, 0.07)` to `rgba(255, 255, 255, 0.12)`.
- **Luminous Glass Borders**: `rgba(255, 255, 255, 0.18)` stroke with focused gradient highlights.
- **Glow Accents**: Electric Cyan (`#00E5FF`), Aurora Violet (`#B388FF`), and Neon Emerald (`#00E676`).
- **Text & Content**: Pure White (`#FFFFFF`) primary, Mist Silver (`#B0BEC5`) secondary, Slate Muted (`#78909C`).

#### Light Liquid Glass (Daytime Mode)
- **Base Background**: Luminous Frost White (`#F0F4F8`) with soft sky and lilac ambient blurs.
- **Glass Panel Fill**: `rgba(255, 255, 255, 0.70)` with subtle specular shine.
- **Luminous Glass Borders**: `rgba(255, 255, 255, 0.90)` top edge, `rgba(0, 0, 0, 0.06)` bottom edge.
- **Text & Content**: Deep Navy/Charcoal (`#0F172A`) primary, Slate Gray (`#475569`) secondary.

### Core Glass Components
1. **GlassCardLayout**: Custom ViewGroup implementing rounded organic corners (20dp), subtle specular edge lighting, internal padding, and alpha glass washes.
2. **GlassFloatingNav**: Floating bottom capsule elevated 16dp from the bottom edge with active pill indicators and soft icon glows.
3. **GlassChartView**: Custom 60 FPS Java Canvas view rendering smooth cubic bezier spline curves, gradient area fills (`#00E5FF` fading to transparent), and luminous touch scrubber tooltips.
4. **GlassSearchField**: Translucent pill search box with illuminated glass borders and clear button.
5. **GlassBottomSheet / Dialog**: Floating modal sheets with blurred translucent backdrop and soft drop shadows.

---

## 3. Key User Flows & Modules

### A. Home Dashboard
- **Glass Header**: Date, time, and dynamic greeting ("Good Morning / Evening") over an ambient aurora glow.
- **Activity Glass Ring**: Visual composite score combining Screen Time, Data Usage, and Battery Level.
- **Key Metric Trio**: 3 floating glass metric cards:
  - Screen Time: Today's duration with % change vs. yesterday.
  - Data Used: Cellular + Wi-Fi split with live indicator.
  - Battery Health: Level percentage, temperature badge, and charging status.
- **Interactive Daily Trend Chart**: Liquid spline curve of hourly screen time activity.
- **Most-Used App Highlight**: Glass pill displaying top app icon, name, and usage duration.
- **Quick Action Dock**: Direct glass buttons to Clipboard, Battery Details, Rules Engine, and System Settings.

### B. Network Monitor
- Real-time speed indicator (download & upload speeds updated every second).
- Cellular vs. Wi-Fi breakdown with custom billing cycle tracker and data allowance progress bar.
- Per-application network consumption ranked list in glass cards.
- Export network telemetry history to CSV.

### C. Screen Time Monitor
- Daily, weekly, and monthly screen time statistics from `UsageStatsManager`.
- Per-app launch counters and average session duration.
- Smart App Usage Warning configurations (75%, 90%, 100% threshold alerts).

### D. Notification Saver
- Secure local archive captured via `NotificationListenerService`.
- Grouping by app, instant search, bookmarking, and read/unread flags.
- Notification volume analytics and customizable retention auto-pruning.

### E. Clipboard History Manager
- Privacy-conscious clipboard capture with explicit 3-tier lifecycle:
  1. *Temporary*: Automatically pruned after 24 hours.
  2. *Favorite*: Pinned and protected from deletion.
  3. *Permanent*: Retained until explicitly removed.
- Category filters (Links, Emails, Code snippets, Plain text) and manual quick-add dialog.

### F. Battery Monitor
- Live battery temperature, voltage, health, and charging history log.
- Visual battery level discharge curve and high-temperature warning alerts.

### G. Automation & Custom Rules
- Rule creator for custom triggers (e.g. "Alert when YouTube > 2h", "Warn when Mobile Data > 2GB", "Alert when Battery Temp > 40°C").
- Toggleable enable/disable switch on every rule card.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────────────────────┐
│                     DigitalPulse Glass Architecture                    │
│                                                                        │
│   MainActivity (Edge-to-Edge WindowInsets, Floating Glass Nav)        │
│   ┌───────────────────┬───────────────────┬──────────────────────┐    │
│   │ HomeGlassFragment │ NetGlassFragment  │ UsageGlassFragment   │    │
│   ├───────────────────┼───────────────────┼──────────────────────┤    │
│   │ NotifGlassFragment│ AnalyticsFragment │ Glass Dialogs/Sheets │    │
│   └───────────────────┴───────────────────┴──────────────────────┘    │
│                                    │                                   │
│                                    ▼                                   │
│                      Pure Java ViewModels (MVVM)                       │
│    DashboardViewModel | NetworkViewModel | UsageViewModel              │
│    NotificationViewModel | ClipboardViewModel | BatteryViewModel       │
│                                    │                                   │
│                                    ▼                                   │
│                        Repositories & Listeners                        │
│    • UsageStatsRepository (UsageStatsManager & UsageEvents)            │
│    • NetworkStatsRepository (NetworkStatsManager & TrafficStats)       │
│    • DigitalPulseNotificationService (NotificationListenerService)    │
│    • ClipboardRepository (ClipboardManager + 24h Purge Worker)         │
│    • BatteryTelemetryReceiver (BatteryManager broadcasts)              │
│                                    │                                   │
│                                    ▼                                   │
│                        Room Local Database (Java)                      │
│    Entities:                                                           │
│      - AppUsageRecord       - NetworkUsageRecord                       │
│      - NotificationRecord   - ClipboardRecord                          │
│      - BatteryLogRecord     - UsageLimitRule      - AutomationRule     │
│                                    │                                   │
│                                    ▼                                   │
│                     WorkManager Scheduled Workers                      │
│    • PeriodicTelemetryWorker (Snapshots usage & network)               │
│    • ClipboardPruneWorker (Enforces 24-hour expiration)                │
│    • RuleEvaluationWorker (Evaluates limits and fires warnings)        │
└────────────────────────────────────────────────────────────────────────┘
```

### GitHub Actions CI/CD Workflow (`.github/workflows/android-build.yml`)
- Triggers on push to `main`, `pull_request`, and manual `workflow_dispatch`.
- Configures JDK 17, Android SDK platform-tools 36, and Gradle cache.
- Runs `gradle assembleDebug` and optionally `assembleRelease` with signing secrets.
- Uploads compiled APKs as downloadable workflow artifacts.

---

## Verification Plan

### Automated & Build Verification
1. **Gradle Build Verification**: Execute `compile_applet` to verify Java 11/17 compilation, Room annotationProcessor, Material 3, and WorkManager dependencies without errors.
2. **Resource Integrity**: Ensure all layout XML files, drawable vectors, style attributes, and color resources compile cleanly.

### Manual & Interactive Verification
1. **Liquid Glass Aesthetic**: Verify translucent panel backgrounds, luminous borders, organic curves, and dark/light theme switching.
2. **Floating Capsule Navigation**: Verify the bottom navigation pill floats cleanly above the system gesture bar without clipping or overlap.
3. **Smooth Canvas Charts**: Verify spline curve rendering and interactive scrub points for hourly usage and battery discharge.
4. **Data Integrity**: Verify real values are displayed from Android `UsageStatsManager`, `NetworkStatsManager`, `BatteryManager`, and `NotificationListenerService`.
