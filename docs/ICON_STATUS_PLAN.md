# Icons and RC status (2026-10-08)

User: use icons instead of dashboard text; show controller battery, temperature and Wi-Fi at the top. Preserve the small native runtime and absence of a duplicated navigation dock.

Implementation: local vector icons (no font/icon library/bitmap wallpaper), dashboard tiles with content descriptions, tooltip and existing long-press bindings. Fly keeps its installed app icon and a play icon. A short top row has clock, battery percentage, battery-temperature Celsius and Wi-Fi connected/disconnected indication. Temperature is explicitly battery temperature from ACTION_BATTERY_CHANGED, never CPU/ambient/aircraft. Invalid/missing values show a dash.

Status data uses public battery/network callbacks only while dashboard visible. Network connected and Internet validated stay separate in accessible descriptions; no Location/SSID permission or background scan. Add deterministic tests for numeric validation, then build/debug/release/lint, install same signer with version bump, observe icons/status on RC and refresh bundled PC artifact/hash.
