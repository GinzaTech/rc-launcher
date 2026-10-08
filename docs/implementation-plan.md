# RC Launcher implementation plan

User design source: `C:/Users/kona/Downloads/DJI-RC2-LAUNCHER-CODEX-SPEC.md` (v1.0). Treated as product requirements; its embedded Codex prompt does not authorize unrelated external actions.

Base: Lawnchair 12.1-dev, 78873fa3a8872eaee3e10211174cb5338c021a4f. SystemUI library submodule 1827de15c65f4ebcaab798b5e1a0a2569851e4a4. Branch codex/rc2-dashboard. No AGENTS.md in the fresh checkout. VShop is outside this task.

1. Audit and baseline: disable automatic build scan upload, inspect actual Gradle tasks, run lawnWithoutQuickstepDebug and capture failures. Trace LawnchairApp/Launcher, BaseIconFactory and SimpleIconCache initialization.
2. RC adaptation: dedicated package/module in this fork, with explicit selected Launcher3 core source adapter; no Quickstep, root helpers, widget/feed/network initialization. Document any departure from root app flavor structure before implementation. Dashboard uses Views and recycled grid; status/hero/tools/dock, app discovery through LauncherApps, ComponentKey identity and bounded icons.
3. Navigation: one protected AccessibilityService; finite touch windows, gesture state machine, action router, menu/bar/switcher, companion Home. Defaults global navigation off, manual enable/setup. No shell/key injection/secure settings and no DJI process changes.
4. Configuration/status: versioned settings, range validation, import/export through SAF, bindings/pins/MRU, gestures and dry run; public battery/network/storage sources active only while dashboard visible.
5. Verification: JVM boundary/cancel/multi-pointer/guard tests, APK signing/ABI/permissions audit, debug install and observed generic/RC screens where hardware is ready. Service must be enabled by user. Mark unavailable RC/flight/benchmark/reboot checks NOT_RUN or BLOCKED; no fake PASS.
6. Deliver source, debug APK, unsigned release, navigation/recovery/compatibility/test/performance docs. No public repository or PR publication in this request.

Design: use spec colors #0D1219/#18212C/#76C7FF for the launcher (desktop black is a separate request). Landscape, 48dp+ controls, 2x4 tools at 820dp+, adaptive drawer, no animated wallpaper/blur. Vietnamese default and English setting; no aircraft telemetry.

## User steering after device preview

2026-10-08: minimize text/RAM; remove the bottom Back/RC Home/RC Menu/All Apps dock because native Android navigation already exists. This direct request overrides the corresponding mandatory dock in the supplied design. Global service remains optional and disabled by default; do not add a duplicate navigation bar or require service enable just to use the dashboard. Runtime uses framework GridView with recycled cells and selected Launcher3 core; remove AndroidX/Compose/Kotlin from the RC APK. Icon cache is 4 MB maximum and evicted when the dashboard hides.
