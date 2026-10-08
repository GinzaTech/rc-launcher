package dev.rclauncher.rc2;

import android.app.Application;
import dev.rclauncher.rc2.apps.AppRepository;
import dev.rclauncher.rc2.settings.SettingsStore;

/** No launcher/workspace/Quickstep initialization on Accessibility rebind. */
public final class RCApplication extends Application {
    private AppRepository apps;
    private SettingsStore settings;
    public synchronized SettingsStore settings() {
        if(settings==null)settings=new SettingsStore(this);
        return settings;
    }
    public synchronized AppRepository apps() {
        if(apps==null)apps=new AppRepository(this);
        return apps;
    }
    @Override public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if(apps!=null)apps.trim(level);
    }
}
