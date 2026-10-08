# Điều hướng

Người dùng đã yêu cầu bỏ dock nội bộ vì RC có thanh điều hướng Android. Dashboard nhận Back Android để đóng search/drawer rồi về dashboard, không có nút Back/Home/Menu/All Apps trùng ở đáy. Biểu tượng ứng dụng và ô Launcher/Ứng dụng vẫn là đường vào/khôi phục.

Dịch vụ tùy chọn mặc định off. Nếu tự bật qua Accessibility Settings, config UI có nút nổi 56dp, cử chỉ cạnh hoặc bar. Chỉ bật một preset. Idle windows có bounds nhỏ, TYPE_ACCESSIBILITY_OVERLAY; không fullscreen touch capture, không đọc text/nodes/screenshots/phím vật lý. Config tắt không tự bật service; service enabled_not_bound khác bound.

Left inward swipe → Back; right tap/inward swipe → RC Menu; right hold → bar; bottom swipe → RC Home; hold → Switcher. Fly Guard thay bottom semantics bằng giữ đủ ngưỡng Home, không gán Switcher ở 350ms. Cancel/multi-pointer/hướng lệch/reset service không dispatch. Dry-run trong Settings chỉ hiển thị action. Unit tests kiểm tra boundary/hold/cancel/đổi đồng hồ.

Home companion mở explicit RCLauncher; khi thực sự giữ HOME role mới thử system HOME. Menu là control center riêng, không phải menu app Android. Recents mặc định không thử: RC Switcher dùng history tối đa12 và app ghim, không task list/process kill/thumbnail giả. System Recents API return true chưa là UI PASS.

Service chưa được bật trên RC ở phiên này; các global gesture/menu/Back/overlay/Recents đều NOT_RUN. User đã chọn native bar có sẵn. Không ghi secure settings, keyevent hoặc root trong APK để làm navigation. ADB được dùng bên ngoài APK cho cài/quan sát ở mặt đất.
