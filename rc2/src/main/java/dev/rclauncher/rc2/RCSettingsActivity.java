package dev.rclauncher.rc2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.role.RoleManager;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import dev.rclauncher.rc2.core.Action;
import dev.rclauncher.rc2.core.EdgeGesture;
import dev.rclauncher.rc2.core.GestureConfig;
import dev.rclauncher.rc2.settings.SettingsStore;

public final class RCSettingsActivity extends Activity {
    private SettingsStore settings;
    private LinearLayout list;
    private final ExecutorService io=Executors.newSingleThreadExecutor();
    private String export="";
    private TextView serviceStatus;
    @Override protected void onCreate(Bundle state) {
        settings=((RCApplication)getApplication()).settings();
        android.content.res.Configuration cfg=new android.content.res.Configuration(getResources().getConfiguration());cfg.setLocale(new Locale(settings.text("language")));getResources().updateConfiguration(cfg,getResources().getDisplayMetrics());
        super.onCreate(state);build();
    }
    private void build() {
        LinearLayout root=UI.column(this);root.setBackgroundColor(UI.BG);root.setPadding(UI.dp(this,24),UI.dp(this,16),UI.dp(this,24),UI.dp(this,12));setContentView(root);
        root.addView(UI.text(this,getString(R.string.settings),26,UI.TEXT));
        ScrollView scroll=new ScrollView(this);list=UI.column(this);scroll.addView(list);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        serviceStatus=UI.text(this,"",16,UI.ACCENT);UI.add(list,serviceStatus,44);
        UI.add(list,UI.button(this,getString(R.string.accessibility),()->publicSettings(Settings.ACTION_ACCESSIBILITY_SETTINGS)),52);
        toggle(R.string.enable_navigation,"navigation");
        UI.add(list,UI.text(this,getString(R.string.navigation_description),15,UI.MUTED),76);
        choose(R.string.presets,new String[]{getString(R.string.floating),getString(R.string.edge),getString(R.string.bar)},new String[]{"floating","edge","bar"},"preset");
        toggle(R.string.left_handle,"left");toggle(R.string.right_handle,"right");toggle(R.string.bottom_handle,"bottom");
        range(R.string.position,"position",0,100,0.01f);
        range(R.string.thickness,"thickness",8,24,1);range(R.string.length,"length",64,180,1);
        range(R.string.bottom_width,"bottomWidth",96,240,1);range(R.string.sensitivity,"distance",32,72,1);
        range(R.string.hold,"hold",550,1000,1);range(R.string.opacity,"opacity",20,100,0.01f);
        toggle(R.string.fly_guard,"flyGuard");toggle(R.string.recents,"systemRecents");
        UI.add(list,UI.button(this,getString(R.string.preview),this::preview),52);
        UI.add(list,UI.button(this,getString(R.string.reset),()->{settings.resetNavigation();build();}),52);
        UI.add(list,UI.text(this,getString(R.string.bindings),20,UI.TEXT),48);
        for(String key:new String[]{"fly","files","gallery","browser"}) {
            int label=key.equals("fly")?R.string.fly:key.equals("files")?R.string.files:key.equals("gallery")?R.string.gallery:R.string.browser;
            UI.add(list,UI.button(this,getString(label)+" · "+getString(R.string.choose),()-> {
                startActivity(new Intent(this,RCLauncher.class).putExtra("destination","BINDING").putExtra("binding",key));
            }),52);
        }
        choose(R.string.language,new String[]{getString(R.string.vi),getString(R.string.en)},new String[]{"vi","en"},"language");
        toggle(R.string.reduce_motion,"reduceMotion");
        UI.add(list,UI.button(this,getString(R.string.role_home),this::homeRole),52);
        UI.add(list,UI.button(this,getString(R.string.diagnostics),this::diagnostics),52);
        UI.add(list,UI.button(this,getString(R.string.export_config),()->export(settings.exportJson(),"rc-config.json",10)),52);
        UI.add(list,UI.button(this,getString(R.string.import_config),()-> {
            try {startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("application/json").addCategory(Intent.CATEGORY_OPENABLE),11);}
            catch(RuntimeException e) {toast(R.string.missing);}
        }),52);
        UI.add(root,UI.button(this,getString(R.string.back),this::finish),52);
    }
    private void toggle(int label,String key) {
        Switch control=new Switch(this);control.setText(label);control.setTextColor(UI.TEXT);control.setTextSize(16);control.setChecked(settings.flag(key));
        control.setOnCheckedChangeListener((b,value)-> {
            try {settings.put(key,value);}catch(IllegalArgumentException e) {toast(R.string.import_failed);control.setChecked(!value);}
        });UI.add(list,control,52);
    }
    private void choose(int label,String[] labels,String[] values,String key) {
        UI.add(list,UI.button(this,getString(label)+" · "+settings.text(key),()->new AlertDialog.Builder(this).setTitle(label).setItems(labels,(d,which)-> {
            try {settings.put(key,values[which]);if(key.equals("language"))recreate();else build();}
            catch(IllegalArgumentException e) {toast(R.string.import_failed);}
        }).setNegativeButton(R.string.close,null).show()),52);
    }
    private void range(int label,String key,int low,int high,float scale) {
        TextView value=UI.text(this,getString(label)+" · "+settings.number(key),16,UI.MUTED);UI.add(list,value,32);
        SeekBar slider=new SeekBar(this);slider.setMax(high-low);slider.setProgress(Math.round(settings.number(key)/scale)-low);
        slider.setContentDescription(getString(label));slider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s,int p,boolean user) {value.setText(getString(label)+" · "+((p+low)*scale));}
            public void onStartTrackingTouch(SeekBar s) {}
            public void onStopTrackingTouch(SeekBar s) {try {settings.put(key,(s.getProgress()+low)*scale);}catch(IllegalArgumentException e) {toast(R.string.import_failed);}}
        });UI.add(list,slider,52);
    }
    private void publicSettings(String action) {
        try {
            Intent i=new Intent(action);if(i.resolveActivity(getPackageManager())==null)i=new Intent(Settings.ACTION_SETTINGS);
            startActivity(i);
        }catch(RuntimeException e) {toast(R.string.missing);}
    }
    private void homeRole() {
        RoleManager roles=getSystemService(RoleManager.class);
        if(roles==null||!roles.isRoleAvailable(RoleManager.ROLE_HOME)) {toast(R.string.role_unavailable);return;}
        try {startActivityForResult(roles.createRequestRoleIntent(RoleManager.ROLE_HOME),12);}catch(RuntimeException e) {toast(R.string.role_unavailable);}
    }
    private void preview() {
        LinearLayout box=UI.column(this);box.setPadding(UI.dp(this,16),UI.dp(this,16),UI.dp(this,16),UI.dp(this,16));
        TextView instructions=UI.text(this,getString(R.string.dry_run),16,UI.MUTED);box.addView(instructions);
        TextView outcome=UI.text(this,"—",20,UI.ACCENT);box.addView(outcome);
        for(EdgeGesture.Edge edge:EdgeGesture.Edge.values()) {
            TextView test=UI.text(this,edge.name(),18,UI.TEXT);test.setBackground(UI.shape(this,UI.SURFACE,14));UI.add(box,test,72);
            final EdgeGesture[] recognizer={null};float density=getResources().getDisplayMetrics().density;
            test.setOnTouchListener((v,event)-> {
                if(event.getActionMasked()==MotionEvent.ACTION_DOWN) {
                    recognizer[0]=new EdgeGesture(edge,new GestureConfig(ViewConfiguration.get(this).getScaledTouchSlop()/density,settings.number("distance"),350,550,(long)settings.number("hold"),1.5f),settings.flag("flyGuard"));
                    recognizer[0].down(event.getX()/density,event.getY()/density,event.getEventTime(),true);
                }else if(event.getActionMasked()==MotionEvent.ACTION_MOVE&&recognizer[0]!=null)recognizer[0].move(event.getX()/density,event.getY()/density,event.getEventTime());
                else if(event.getActionMasked()==MotionEvent.ACTION_UP&&recognizer[0]!=null) {Action a=recognizer[0].up(event.getX()/density,event.getY()/density,event.getEventTime());outcome.setText(a==null?"—":a.name());recognizer[0]=null;}
                else if(event.getActionMasked()==MotionEvent.ACTION_CANCEL||event.getActionMasked()==MotionEvent.ACTION_POINTER_DOWN) {if(recognizer[0]!=null)recognizer[0].cancel();recognizer[0]=null;}
                return true;
            });
        }
        new AlertDialog.Builder(this).setTitle(R.string.preview).setView(box).setPositiveButton(R.string.close,null).show();
    }
    private void diagnostics() {
        RoleManager roles=getSystemService(RoleManager.class);
        android.app.ActivityManager.MemoryInfo memory=new android.app.ActivityManager.MemoryInfo();getSystemService(android.app.ActivityManager.class).getMemoryInfo(memory);
        String report="RC Launcher "+BuildConfig.VERSION_NAME+"\nProfile: "+(android.os.Build.DEVICE.toLowerCase(Locale.ROOT).contains("rc331")?"RC2":"Generic landscape")+
                "\nAPI: "+android.os.Build.VERSION.SDK_INT+"\nABI: "+String.join(", ",android.os.Build.SUPPORTED_ABIS)+
                "\nDisplay: "+getResources().getDisplayMetrics().widthPixels+" x "+getResources().getDisplayMetrics().heightPixels+
                "\nDensity: "+getResources().getDisplayMetrics().density+"\nHOME held: "+(roles!=null&&roles.isRoleHeld(RoleManager.ROLE_HOME))+
                "\nService: "+RCNavigationService.state+"\nAvailable RAM: "+memory.availMem/(1024*1024)+" MB\nSystem Recents UI: NOT_RUN\nFlight compatibility: NOT_RUN";
        new AlertDialog.Builder(this).setTitle(R.string.diagnostics).setMessage(report).setPositiveButton(R.string.export_report,(d,w)->export(report,"rc-diagnostics.txt",10)).setNegativeButton(R.string.close,null).show();
    }
    private void export(String data,String name,int request) {
        export=data;
        try {startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).setType(name.endsWith("json")?"application/json":"text/plain").addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_TITLE,name),request);}
        catch(RuntimeException e) {toast(R.string.missing);}
    }
    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data);if(result!=RESULT_OK||data==null||data.getData()==null)return;
        if(request!=10&&request!=11)return;
        android.net.Uri uri=data.getData();String output=export;
        io.submit(()-> {
            try {
                if(request==10) {try(OutputStream out=getContentResolver().openOutputStream(uri,"wt")) {if(out==null)throw new IllegalStateException();out.write(output.getBytes(StandardCharsets.UTF_8));}}
                else {
                    String raw;
                    try(InputStream in=getContentResolver().openInputStream(uri);ByteArrayOutputStream bytes=new ByteArrayOutputStream()) {
                        if(in==null)throw new IllegalStateException();byte[] buffer=new byte[4096];int count;
                        while((count=in.read(buffer))!=-1) {if(bytes.size()+count>65536)throw new IllegalArgumentException();bytes.write(buffer,0,count);}raw=bytes.toString("UTF-8");
                    }
                    String config=raw;runOnUiThread(()->{try {settings.importJson(config);build();toast(R.string.saved);}catch(Exception e) {toast(R.string.import_failed);}});return;
                }runOnUiThread(()->toast(R.string.saved));
            }catch(Exception e) {runOnUiThread(()->toast(R.string.import_failed));}
        });
    }
    private void toast(int id) {Toast.makeText(this,id,Toast.LENGTH_LONG).show();}
    @Override protected void onResume() {
        super.onResume();String state=RCNavigationService.state;
        if(RCNavigationService.instance==null&&state.equals("disabled")) {
            android.view.accessibility.AccessibilityManager manager=getSystemService(android.view.accessibility.AccessibilityManager.class);
            boolean enabled=manager.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK).stream().anyMatch(s->s.getResolveInfo().serviceInfo.packageName.equals(getPackageName()));
            if(enabled)state="enabled_not_bound";
        }
        serviceStatus.setText(getString(state.equals("bound")?R.string.nav_enabled:state.equals("enabled_not_bound")?R.string.nav_unbound:state.equals("policy_blocked")?R.string.nav_failed:state.equals("interrupted")?R.string.nav_interrupted:R.string.nav_disabled));
    }
    @Override protected void onDestroy() {io.shutdownNow();super.onDestroy();}
}
