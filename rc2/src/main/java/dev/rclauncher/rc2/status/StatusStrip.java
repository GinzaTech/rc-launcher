package dev.rclauncher.rc2.status;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import dev.rclauncher.rc2.UI;
import dev.rclauncher.rc2.R;

/** Three bounded native status chips; no animation or bitmap allocations. */
public final class StatusStrip extends LinearLayout {
    private final TextView clock,battery,temperature;
    private final ImageView wifi;
    private final LinearLayout batteryChip,temperatureChip;
    public StatusStrip(Context context) {
        super(context);setGravity(Gravity.CENTER_VERTICAL);
        clock=UI.text(context,"—",16,UI.MUTED);addView(clock,new LayoutParams(0,-1,1));
        batteryChip=chip(context,R.drawable.rc_battery);battery=UI.text(context,"—",16,UI.TEXT);batteryChip.addView(battery);addView(batteryChip);
        temperatureChip=chip(context,R.drawable.rc_temperature);temperature=UI.text(context,"—",16,UI.TEXT);temperatureChip.addView(temperature);addView(temperatureChip);
        wifi=new ImageView(context);wifi.setImageResource(R.drawable.rc_wifi);wifi.setImageTintList(ColorStateList.valueOf(UI.MUTED));
        LayoutParams p=new LayoutParams(UI.dp(context,32),UI.dp(context,28));p.setMargins(UI.dp(context,14),0,0,0);addView(wifi,p);
        update(new VisibleStatus.Snapshot("—","—","—",null,false));
    }
    private LinearLayout chip(Context c,int icon) {
        LinearLayout chip=new LinearLayout(c);chip.setGravity(Gravity.CENTER_VERTICAL);chip.setPadding(UI.dp(c,14),0,UI.dp(c,14),0);
        ImageView image=new ImageView(c);image.setImageResource(icon);image.setImageTintList(ColorStateList.valueOf(UI.ACCENT));
        LayoutParams p=new LayoutParams(UI.dp(c,28),UI.dp(c,28));p.setMargins(0,0,UI.dp(c,8),0);chip.addView(image,p);
        chip.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);return chip;
    }
    public void update(VisibleStatus.Snapshot snapshot) {
        clock.setText(snapshot.clock);battery.setText(snapshot.battery);
        temperature.setText("CPU "+snapshot.cpu+" · "+getContext().getString(R.string.pin_short)+" "+snapshot.temperature);
        temperature.setTextSize(14);
        String batteryLabel=getContext().getString(R.string.battery)+": "+snapshot.battery;
        batteryChip.setContentDescription(batteryLabel);batteryChip.setTooltipText(batteryLabel);
        String tempLabel="CPU (cpuss, kernel): "+snapshot.cpu+" · "+getContext().getString(R.string.battery_temperature)+" (Android): "+snapshot.temperature;
        temperatureChip.setContentDescription(tempLabel);temperatureChip.setTooltipText(tempLabel);
        boolean connected=Boolean.TRUE.equals(snapshot.wifi);
        wifi.setImageResource(Boolean.FALSE.equals(snapshot.wifi)?R.drawable.rc_wifi_off:R.drawable.rc_wifi);
        wifi.setImageTintList(ColorStateList.valueOf(connected?UI.ACCENT:UI.MUTED));
        wifi.setAlpha(snapshot.wifi==null?0.5f:1);
        String label=snapshot.wifi==null?getContext().getString(R.string.unavailable):getContext().getString(connected?R.string.wifi_connected:R.string.wifi_disconnected);
        if(connected)label+=" · "+getContext().getString(snapshot.internet?R.string.internet:R.string.offline);
        wifi.setContentDescription(label);wifi.setTooltipText(label);
    }
}
