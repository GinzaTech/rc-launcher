package dev.rclauncher.rc2.status;
import java.util.Locale;

/** Public battery broadcast values, validated before displaying numeric data. */
public final class StatusValues {
    private StatusValues() {}
    public static String battery(int level,int scale) {
        if(scale<=0||level<0||level>scale)return "—";
        return Math.round(level*100f/scale)+"%";
    }
    public static String temperature(int tenthsCelsius) {
        if(tenthsCelsius<0||tenthsCelsius>800)return "—";
        return String.format(Locale.ROOT,"%.1f°C",tenthsCelsius/10f);
    }
}
