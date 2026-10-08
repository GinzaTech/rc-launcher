# Kiểm thử

Ngày 08/10/2026, version1.0-preview3 code3. Build debug/release unsigned, 18 JVM tests và lint PASS (0 errors,18 warnings). Warnings gồm old target baseline32, app settings platform switch, font/text concatenation/RTL và metadata; không suppress để che lỗi.

Gesture: boundary47/48/49, rebound dưới ngưỡng, một action/UP, tap/right-hold, hướng/diagonal, pointer cancel, outside DOWN, negative/expired monotonic time, Fly Guard350/649/650, canceled hold/reconnect, hai stream độc lập. Config: schema/default migration, enums/ranges/boolean type, component/shell syntax/history limit, giữ input/config trước, trap zero-edge regions. Hai regression RED đã được quan sát (rebound, component path), sau đó sửa và GREEN.

Trên RC thật: install -r Success, am start Status ok, xem screenshot/hierarchy dashboard. Picker có từng launcher component + profile; đã chọn DJI Fly dji.go.v5 và mở được DJIPureLaunch rồi trang chủ DJIDeviceActivity. Dashboard cuối có tám ô đủ nhìn và không còn dock bốn nút. Pin/Wi-Fi là dữ liệu RC, không drone telemetry.

ARTEMIS observer đã dùng trên điện thoại trước đó. Chẩn đoán ARTEMIS cho RC không thấy transport private và thiếu LLM key; không chạy agent/UI instrumentation qua ARTEMIS trong phiên này. Kiểm chứng RC là ADB trực tiếp qua cầu private, không gọi đó là ARTEMIS pass.

Không thử service/global input, HOME replacement, reboot, font scale1.3, mất cấu hình/process death, tất cả handlers, gamepad/physical controls, bay/soak30min. Đây là preview theo yêu cầu UI nhẹ/dock bỏ; chưa nghiệm thu mọi mục trong đặc tả1.0.

Đã kiểm tra drawer qua ô Ứng dụng và nhập DJI trong ô tìm kiếm trên RC thật; đọc lại UI để xác nhận kết quả rồi dùng Back Android về dashboard. Chi tiết kết quả có trong local verification; không coi đây là UI instrumentation suite.

Preview4: 20 JVM tests, build debug/release và lint0errors15warnings. Đã cài cùng signer lên RC, quan sát 9 ImageButton có content description, pin57%, nhiệt độ pin30.0°C và Wi-Fi connected. Icon vector local, không font/icon-library. Không thay Wi-Fi để thử trạng thái off trên tay; snapshot fallback và numeric range được kiểm tra bằng source/tests.
