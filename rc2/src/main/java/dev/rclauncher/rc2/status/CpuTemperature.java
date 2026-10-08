package dev.rclauncher.rc2.status;
import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Read-only, unprivileged CPU cluster sensors. No shell, root or thermal writes. */
public final class CpuTemperature {
    private final List<File> sensors=new ArrayList<>();
    private boolean discovered=false;
    private String read(File file) throws java.io.IOException {
        return new String(Files.readAllBytes(file.toPath()),StandardCharsets.UTF_8).trim();
    }
    public String sample() {
        if(!discovered) {
            discovered=true;File[] zones=new File("/sys/class/thermal").listFiles();
            if(zones!=null)for(File zone:zones) {
                if(!zone.getName().startsWith("thermal_zone"))continue;
                try {
                    String type=read(new File(zone,"type"));
                    if(type.startsWith("cpuss-")&&type.endsWith("-usr"))sensors.add(new File(zone,"temp"));
                }catch(Exception ignored) { /* Not publicly readable on this ROM. */ }
            }
        }
        int maximum=-1;
        for(File sensor:sensors)try {
            int value=Integer.parseInt(read(sensor));
            if(value>=1000&&value<=150000)maximum=Math.max(maximum,value);
        }catch(Exception ignored) { /* Keep other valid sensors; no fabricated temperature. */ }
        return ThermalValues.celsius(Integer.toString(maximum));
    }
}
