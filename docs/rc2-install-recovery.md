# Cài, nâng cấp và khôi phục

Firmware cần sẵn đường cài APK. DJI không chính thức hỗ trợ app bên thứ ba trên RC2; dự án không đưa root/unlock/flash vào APK/setup. Phiên này dùng đường ADB mà người dùng đã có và Allow trên tay.

Cài APK debug đã ký:

```powershell
adb -s <serial> install -r artifacts/RC-Launcher-1.0-preview3-debug.apk
adb -s <serial> shell am start -n dev.rclauncher.rc2.debug/dev.rclauncher.rc2.RCLauncher
```

Chọn đúng ADB transport đã device. RC này cần cầu ADB của app PC khi server chuẩn offline; không dùng serial/cổng ví dụ như kết nối thật. App PC có Cài RC Launcher/Mở RC Launcher; artifact SHA pin. Cùng signer/version tăng giữ cấu hình. Không force uninstall/pm clear để sửa lỗi; bản khác signer/hash cần đánh giá riêng.

Package riêng, không ghi đè Lawnchair hay DJI. Đã chạy companion, chưa thay HOME. Nhấn Home Android có thể về launcher/cầu Home cũ; RC Launcher luôn mở được từ app icon. Nếu muốn chọn HOME mới, dùng lựa chọn trong RC Settings và Android role UI nếu firmware có; không hứa firmware chấp nhận.

Navigation service optional/off. Nếu bật, tự cấp qua UI Trợ năng; reset vùng hoặc tắt config trong app khi overlay gây vướng. Không ghi enabled_accessibility_services qua shell. Tạm ẩn có restore30s; không ẩn biểu tượng app.

Gỡ chỉ app này nếu người dùng muốn:

```powershell
adb -s <serial> uninstall dev.rclauncher.rc2.debug
```

Không gỡ/disable DJI/Lawnchair, không flash hay xóa flight logs. Release unsigned phải được ký bởi người quản lý khóa trước khi cài; không gọi nó là APK cài được.
