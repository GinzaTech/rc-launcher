package dev.rclauncher.rc2.status;
import java.util.Locale;

public final class ThermalValues {
    private ThermalValues() {}
    public static String celsius(String raw) {
        try {
            int value=Integer.parseInt(raw.trim());
            if(value<1000||value>150000)return "—";
            return String.format(Locale.ROOT,"%.1f°C",value/1000f);
        }catch(RuntimeException e) {return "—";}
    }
}
