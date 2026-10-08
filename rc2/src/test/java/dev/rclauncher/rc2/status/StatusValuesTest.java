package dev.rclauncher.rc2.status;
import org.junit.Test;
import static org.junit.Assert.*;

public class StatusValuesTest {
    @Test public void batteryScaleAndLevelMustBeValid() {
        assertEquals("67%",StatusValues.battery(67,100));
        assertEquals("50%",StatusValues.battery(1,2));
        assertEquals("—",StatusValues.battery(1,0));
        assertEquals("—",StatusValues.battery(-1,100));
        assertEquals("—",StatusValues.battery(101,100));
    }
    @Test public void temperatureUsesTenthsOfCelsiusAndRejectsMissingOrImplausibleValues() {
        assertEquals("30.0°C",StatusValues.temperature(300));
        assertEquals("0.0°C",StatusValues.temperature(0));
        assertEquals("80.0°C",StatusValues.temperature(800));
        assertEquals("—",StatusValues.temperature(-1));
        assertEquals("—",StatusValues.temperature(801));
    }
}
