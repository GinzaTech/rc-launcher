package dev.rclauncher.rc2.status;
import org.junit.Test;
import static org.junit.Assert.*;

public class ThermalValuesTest {
    @Test public void interpretsOnlyValidCpuMillidegreeReadings() {
        assertEquals("91.9°C",ThermalValues.celsius("91900"));
        assertEquals("—",ThermalValues.celsius("-274000"));
        assertEquals("—",ThermalValues.celsius("invalid"));
        assertEquals("—",ThermalValues.celsius(""));
        assertEquals("—",ThermalValues.celsius("300"));
    }
}
