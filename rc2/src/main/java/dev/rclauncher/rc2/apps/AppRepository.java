package dev.rclauncher.rc2.apps;

import android.content.Context;
import android.content.ComponentName;
import android.content.pm.LauncherApps;
import android.content.pm.LauncherActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.os.UserHandle;
import android.util.LruCache;
import android.widget.ImageView;
import com.android.launcher3.icons.ColorExtractor;
import com.android.launcher3.util.ComponentKey;
import java.lang.ref.WeakReference;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Executors;

/** Public LauncherApps model adapter with component/profile identity and 4 MB cap. */
public final class AppRepository {
    public interface Listener {void loaded(List<AppEntry> apps);}
    private final LauncherApps launcher;
    private final Handler main=new Handler(Looper.getMainLooper());
    private final ThreadPoolExecutor worker=(ThreadPoolExecutor)Executors.newFixedThreadPool(1);
    private final LruCache<ComponentKey,Bitmap> icons=new LruCache<ComponentKey,Bitmap>(4*1024*1024) {
        @Override protected int sizeOf(ComponentKey key,Bitmap icon) {return icon.getAllocationByteCount();}
    };
    private volatile boolean dirty=true,visible=false;
    private volatile List<AppEntry> apps=Collections.emptyList();
    private Listener listener;
    private Future<?> loading;
    private int generation=0;
    private final LauncherApps.Callback callback=new LauncherApps.Callback() {
        private void invalidate() {dirty=true;icons.evictAll();if(visible)load();}
        @Override public void onPackageRemoved(String p,UserHandle u) {invalidate();}
        @Override public void onPackageAdded(String p,UserHandle u) {invalidate();}
        @Override public void onPackageChanged(String p,UserHandle u) {invalidate();}
        @Override public void onPackagesAvailable(String[] p,UserHandle u,boolean replacing) {invalidate();}
        @Override public void onPackagesUnavailable(String[] p,UserHandle u,boolean replacing) {invalidate();}
    };
    public AppRepository(Context context) {
        launcher=context.getSystemService(LauncherApps.class);
        launcher.registerCallback(callback,main);
    }
    public void start(Listener l) {listener=l;visible=true;load();}
    public void stop() {
        visible=false;listener=null;generation++;
        if(loading!=null)loading.cancel(true);
        worker.getQueue().clear();
        icons.evictAll();
    }
    public void load() {
        if(!visible)return;
        if(!dirty) {if(listener!=null)listener.loaded(apps);return;}
        if(loading!=null && !loading.isDone())return;
        final int token=generation;
        loading=worker.submit(()-> {
            List<AppEntry> result=new ArrayList<>();
            try {
                for(LauncherActivityInfo info:launcher.getActivityList(null,Process.myUserHandle())) {
                    if(Thread.currentThread().isInterrupted())return;
                    if(launcher.isActivityEnabled(info.getComponentName(),info.getUser()))result.add(new AppEntry(info));
                }
                result.sort(Comparator.comparing(e->normalize(e.label)));
                List<AppEntry> stable=Collections.unmodifiableList(result);
                main.post(()-> {if(token==generation&&visible) {apps=stable;dirty=false;if(listener!=null)listener.loaded(stable);}});
            } catch(RuntimeException e) {main.post(()->{if(token==generation&&listener!=null)listener.loaded(apps);});}
        });
    }
    public List<AppEntry> snapshot() {return apps;}
    public static String normalize(String text) {
        return Normalizer.normalize(text,Normalizer.Form.NFD).replaceAll("\\p{M}+","").toLowerCase(Locale.ROOT).replace('đ','d');
    }
    public List<AppEntry> search(String query) {
        String q=normalize(query);List<AppEntry> result=new ArrayList<>();
        for(AppEntry a:apps)if(normalize(a.label+" "+a.key.componentName.getPackageName()).contains(q))result.add(a);
        return result;
    }
    public AppEntry find(String component) {for(AppEntry a:apps)if(a.component().equals(component))return a;return null;}
    public void icon(AppEntry entry,ImageView view) {
        view.setTag(entry.key.toString());Bitmap cached=icons.get(entry.key);
        if(cached!=null) {view.setImageBitmap(cached);return;}
        view.setImageResource(dev.rclauncher.rc2.R.drawable.rc_icon);
        WeakReference<ImageView> reference=new WeakReference<>(view);int token=generation;
        worker.submit(()-> {
            if(!visible||token!=generation)return;
            try {
                Drawable drawable=entry.info.getBadgedIcon(0);
                Bitmap bitmap=Bitmap.createBitmap(96,96,Bitmap.Config.ARGB_8888);
                drawable.setBounds(0,0,96,96);drawable.draw(new Canvas(bitmap));
                // Reuse the pinned Launcher3 color extractor for the icon backdrop.
                int color=new ColorExtractor().findDominantColorByHue(bitmap);
                if(!visible||token!=generation)return;
                icons.put(entry.key,bitmap);
                main.post(()-> {
                    ImageView target=reference.get();
                    if(visible&&token==generation&&target!=null&&entry.key.toString().equals(target.getTag())) {
                        target.setImageBitmap(bitmap);
                        if(!(target instanceof android.widget.ImageButton))target.setBackgroundColor((color&0x00ffffff)|0x22000000);
                    }
                });
            } catch(RuntimeException e) { /* Placeholder remains; do not fail drawer for one OEM icon. */ }
        });
    }
    public boolean launch(String component) {
        try {
            ComponentName c=ComponentName.unflattenFromString(component);
            if(c==null||!launcher.isActivityEnabled(c,Process.myUserHandle()))return false;
            launcher.startMainActivity(c,Process.myUserHandle(),null,null);return true;
        }
        catch(RuntimeException e) {return false;}
    }
    public void trim(int level) {if(level>=android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW)icons.evictAll();}
}
