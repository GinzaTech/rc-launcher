# Upstream audit / ADR

Base/submodule và quyết định: ADR 001 + implementation-plan.md. Gradle 8.4, AGP 8.1.3, JDK17, baseline Kotlin1.9.20. Tasks được đọc trước build. Automatic build scan upload bị gỡ ở settings.gradle trước khi chạy Gradle.

Baseline assembleLawnWithoutQuickstepDebug FAIL ở processResources: bg_overview_clear_all_button không có. LawnchairLauncher kế thừa QuickstepLauncher; LawnchairApp gọi QuickStepContract trong onCreate. BaseIconFactory của iconloaderlib import Lawnchair icon preferences; không thể mang thư viện nguyên trạng vào một service nhẹ rồi coi là không phụ thuộc Lawnchair.

RC module dùng entry riêng, public LauncherApps, source identity/color đã adapt. Root Gradle chỉ bỏ dependency AndroidX mặc định cho :rc2; các app/libraries upstream khác giữ nguyên. GridView framework có recycled cells thay RecyclerView sau yêu cầu giảm RAM. Debug APK không chứa Kotlin/Compose/AndroidX/native lib, không Quickstep service/provider/widget/root/network helper.

Merged APK permissions: ACCESS_NETWORK_STATE. Accessibility service protected BIND_ACCESSIBILITY_SERVICE, canRetrieveWindowContent/canPerformGestures/filterKeys/touchExploration false. No INTERNET, QUERY_ALL_PACKAGES, SYSTEM_ALERT_WINDOW, USAGE_STATS, Location, all-files, root/secure-setting permissions. All source/addons của upstream không được coi là runtime của module RC.

min30/target32/compile34 theo baseline thiết kế; generic app ngoài RC chưa được chứng nhận. API30/32 tương thích và ABI-neutral được kiểm tra artifact; không giả đang hỗ trợ mọi firmware.
