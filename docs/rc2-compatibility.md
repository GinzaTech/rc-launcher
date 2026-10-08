# Tương thích firmware

| Firmware/build | API / ABI | Cài | Dashboard | Fly picker/launch | Service/overlay | Global Back/Home/Menu | HOME role | System Recents | Reboot/soak | Fly conflict |
|---|---|---|---|---|---|---|---|---|---|---|
| V14.00.00.04 test-keys, incremental4 / RC331 | 30 / arm64-v8a, armeabi-v7a, armeabi (APK ABI-neutral) | PASS preview3 | PASS, 1920x1080, density2.5, dock bỏ theo yêu cầu | PASS chọn gói dji.go.v5 và mở trang chủ | NOT_RUN/off | NOT_RUN | NOT_RUN/companion | NOT_RUN | NOT_RUN | NOT_RUN |

Không dùng firmware từ nghiên cứu cộng đồng để gắn PASS. Native navigation đã có từ công việc trước; không coi đó là service mới của RC Launcher. Phone Android15 Fly tiếng Việt là sản phẩm riêng ở rc2-viet, không phải thử launcher API30.

2026-10-08 integration update: RC2 Việt Homebridge3.0 installed; nativeHome entered RC Launcher and after reboot startup foregroundRC Launcher observed (boot_completed1/uptime278.75s). No agent amstart was sent after reboot before that observation. Later extraSettings/USB checks timed out; not markedPASS.
