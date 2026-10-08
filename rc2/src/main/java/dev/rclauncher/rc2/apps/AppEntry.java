package dev.rclauncher.rc2.apps;
import android.content.pm.LauncherActivityInfo;
import com.android.launcher3.util.ComponentKey;

/** Launcher3 component/profile identity, independent of workspace/Quickstep. */
public final class AppEntry {
    public final ComponentKey key;
    public final String label;
    public final LauncherActivityInfo info;
    public AppEntry(LauncherActivityInfo info) {
        this.info=info;key=new ComponentKey(info.getComponentName(),info.getUser());label=info.getLabel().toString();
    }
    public String component() {return key.componentName.flattenToString();}
}
