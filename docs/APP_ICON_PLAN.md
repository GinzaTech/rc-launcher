# Actual app icons and temperature sources (2026-10-08)

User corrects dashboard: icons must come from installed/chosen apps, not the generic vector tool set. Keep status indicators and the native navigation bar; no caption text on usual dashboard tiles.

Replace the generic tools grid by actual launchable app shortcuts. Prefer user pins and configured bindings, then installed utility apps. Deduplicate packages for default suggestions so Settings aliases do not create duplicate icons; explicit pins may retain different activities. Do not invent browser/utility apps absent on the RC. Keep a small own-app icon button for internal launcher settings and the app drawer as product controls, not an impersonated third-party app. App icon cache96px/4MB remains shared and label/component/user identity stays with each icon.

Temperature question: current battery driver dji-primary-battery reports300 (30C); sysfs cpuss/cpu nodes report roughly87–95C and are readable from app UID without root. Public thermal HAL returns no temperature entries. These are driver reports, not a validated case-temperature measurement; do not assert battery30C equals controller temperature or that firmware calibration is verified. Display CPU and Pin separately, use read-only sysfs only when type/unit/read permission checks succeed, otherwise dash. Refresh only while visible and retain no background polling.

Verify selection defaults/bounds, thermal parsing/ranges, install same signer with version bump, capture actual icons/status, then update bundled PC artifact/hash and local guides.
