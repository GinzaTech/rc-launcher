# Hiệu năng quan sát

RC331, API30, density2.5, 1920x1080; APK debug không LeakCanary. Số đo tức thời, chưa chuẩn hóa benchmark/p95/release.

- APK bản đầu có thư viện UI: 7.671.993 byte. Preview3 framework Views/GridView: 135.292 byte debug; 73.391 byte release unsigned/R8.
- Dashboard warm snapshot: PSS71.053KB (~69,4MiB), RSS115.152KB, swapPSS227KB. Java heap PSS4.111KB, nativeheap8.215KB, graphics29.152KB, còn lại code/shared system mappings. Không lấy APK135KB làm RAM.
- Ngay sau cài/cold: PSS93.181KB. Sau mở Fly snapshot khác: PSS91.741KB; snapshot này không phải idle ổn định nên không tuyên bố mức RAM background cố định.
- am start cold preview3: TotalTime803ms; warm bring-to-front636ms một lần. Đây là Android activity timing, không median/p95, không đo riêng input-interactive/frame latency.
- Cache icon cap4MB, 96px ARGB bitmap; model chỉ label/component/info, không giữ bitmap trong mọi entry. Evict cache/dừng queue/status receiver/network callbacks khi dashboard ẩn. Service off nên không có idle overlay workload.
- Không network request chủ động, không bitmap wallpaper/blur/widget/feed/Quickstep/root helper. Runtime APK không AndroidX/Compose/Kotlin/native .so. Nền dùng màu, tile native.

Chưa đo jank/fps, CPU idle/wakeup bằng trace, soak15/30min, benchmark release, font scale/rotation matrix hoặc tác động lên video preview. Không kết luận an toàn bay/tiết kiệm31MB từ hai snapshot khác điều kiện. Ngân sách trong file spec vẫn là target.
