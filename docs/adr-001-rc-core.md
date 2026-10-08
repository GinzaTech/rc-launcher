# ADR 001: isolate the RC runtime from Lawnchair privileged initialization

Status: implementation decision, 2026-10-08.

The checked-out Lawnchair entry inherits QuickstepLauncher; LawnchairApp initializes QuickStepContract, and the iconloader library imports Lawnchair icon preferences directly. SimpleIconCache transitively requires that factory. Copying the withoutQuickstep flavor name into a new manifest would not remove that runtime coupling. Baseline tasks exist and a baseline APK build is being checked independently.

Use a dedicated `rc2` app module in the upstream fork. Adapt selected Launcher3 app identity/index/icon code with source attribution and a public LauncherApps repository, rather than initialize the full Lawnchair Application/Workspace/widget/database graph for a fixed dashboard. Preserve the full upstream source and submodule in the fork. ComponentKey and ColorExtractor are reused from the pinned submodule; the app-model/icon adapter has current-user and bounded-memory semantics. No placeholder Quickstep classes or fake success methods are added.

This is an explicit adaptation of the fork with a separate entry, not a skin applied to the installed app.lawnchair APK. Package dev.rclauncher.rc2(.debug) can coexist with the user's current Lawnchair. Existing Home stays until companion/service paths are verified and the user chooses HOME through Android UI. Gradle tasks are `:rc2:assembleDebug`, `:rc2:testDebugUnitTest`, `:rc2:lintDebug`, and `:rc2:assembleRelease`; root Lawnchair variants remain for baseline comparison only.

Tradeoff: the RC dashboard does not inherit upstream draggable workspace/widgets. These are excluded by the supplied design. The repository and APK reports must clearly name the selected core adaptation, and must not call the unchanged full Lawnchair variant no-Quickstep verified.
