package dev.rclauncher.rc2.apps;
import android.os.SystemClock;
import dev.rclauncher.rc2.settings.SettingsStore;

/** Coalesces only the same launch, never throttles unrelated navigation. */
public final class LaunchCoordinator {
    private final AppRepository apps;
    private final SettingsStore settings;
    private String last="";
    private long time=0;
    public LaunchCoordinator(AppRepository apps,SettingsStore settings) {this.apps=apps;this.settings=settings;}
    public boolean launch(String component) {
        long now=SystemClock.elapsedRealtime();
        if(component.equals(last)&&now-time<500)return true;
        boolean success=apps.launch(component);
        if(success) {last=component;time=now;settings.record(component);}
        return success;
    }
}
