package dev.rclauncher.rc2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.GridView;
import android.text.Editable;
import android.text.TextWatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import dev.rclauncher.rc2.apps.AppEntry;
import dev.rclauncher.rc2.apps.AppRepository;
import dev.rclauncher.rc2.apps.AppGridAdapter;
import dev.rclauncher.rc2.apps.LaunchCoordinator;
import dev.rclauncher.rc2.apps.ShortcutSelection;
import dev.rclauncher.rc2.settings.SettingsStore;
import dev.rclauncher.rc2.status.VisibleStatus;
import dev.rclauncher.rc2.status.StatusStrip;
import dev.rclauncher.rc2.core.Action;

/** RC dashboard entry. Explicit finite destinations work in companion mode. */
public final class RCLauncher extends Activity {
    private AppRepository apps;
    private SettingsStore settings;
    private LaunchCoordinator launch;
    private VisibleStatus status;
    private StatusStrip statusStrip;
    private LinearLayout root,content;
    private AppGridAdapter adapter;
    private EditText search;
    private String page="HOME",picker="";
    private AlertDialog dialog;
    private boolean visible=false;
    private final Handler main=new Handler(Looper.getMainLooper());
    private final Runnable filtering=()->{if(adapter!=null&&search!=null)adapter.submit(apps.search(search.getText().toString()));};

    @Override protected void onCreate(Bundle state) {
        settings=((RCApplication)getApplication()).settings();
        android.content.res.Configuration cfg=new android.content.res.Configuration(getResources().getConfiguration());
        cfg.setLocale(new Locale(settings.text("language")));getResources().updateConfiguration(cfg,getResources().getDisplayMetrics());
        super.onCreate(state);apps=((RCApplication)getApplication()).apps();launch=new LaunchCoordinator(apps,settings);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        status=new VisibleStatus(this,snapshot->{if(statusStrip!=null)statusStrip.update(snapshot);});
        build();destination(getIntent());
    }
    @Override protected void onNewIntent(Intent intent) {super.onNewIntent(intent);setIntent(intent);destination(intent);}
    private void destination(Intent intent) {
        String d=intent.getStringExtra("destination");if(d==null)d="HOME";
        switch(d) {
            case "DRAWER":drawer("");break;case "SWITCHER":switcher();break;case "FLY":home();openBinding("fly");break;
            case "FILES":home();tool("files");break;case "SYSTEM_SETTINGS":home();tool("system_settings");break;
            case "TOOLS":home();flightTools();break;default:home();
            case "BINDING":
                String key=intent.getStringExtra("binding");
                if(List.of("fly","files","gallery","browser").contains(key==null?"":key))drawer(key);else home();break;
        }
    }
    private void build() {
        root=UI.column(this);root.setBackgroundColor(UI.BG);root.setPadding(UI.dp(this,24),UI.dp(this,12),UI.dp(this,24),UI.dp(this,8));setContentView(root);
        statusStrip=new StatusStrip(this);root.addView(statusStrip,new LinearLayout.LayoutParams(-1,UI.dp(this,44)));
        content=UI.column(this);root.addView(content,new LinearLayout.LayoutParams(-1,0,1));
        // Native Android navigation already exists on this RC; no duplicate dock.

    }
    private void local(Action action) {
        if(action==Action.BACK)onBackPressed();else if(action==Action.HOME)home();else if(action==Action.MENU)menu();else if(action==Action.ALL_APPS)drawer("");
    }
    private void clear() {
        if(dialog!=null) {dialog.dismiss();dialog=null;}
        main.removeCallbacks(filtering);search=null;adapter=null;content.removeAllViews();status.stop();
    }
    private void home() {
        clear();page="HOME";picker="";
        AppEntry configured=apps.find(settings.text("binding_fly"));
        LinearLayout hero=new LinearLayout(this);hero.setGravity(Gravity.CENTER_VERTICAL);
        ImageButton fly=UI.iconButton(this,android.R.drawable.sym_def_app_icon,getString(R.string.fly),()->openBinding("fly"));
        fly.setBackground(UI.primaryBackground(this));fly.setPadding(UI.dp(this,16),UI.dp(this,12),UI.dp(this,16),UI.dp(this,12));
        if(configured!=null) {apps.icon(configured,fly);fly.setContentDescription(configured.label+" · "+configured.key.componentName.getPackageName());}
        fly.setOnLongClickListener(v->{drawer("fly");return true;});
        hero.addView(fly,new LinearLayout.LayoutParams(0,UI.dp(this,88),1));
        Button more=UI.button(this,"⋮",this::dashboardMenu);more.setTextSize(24);more.setContentDescription(getString(R.string.menu));
        LinearLayout.LayoutParams control=new LinearLayout.LayoutParams(UI.dp(this,52),UI.dp(this,52));control.leftMargin=UI.dp(this,12);hero.addView(more,control);
        content.addView(hero,new LinearLayout.LayoutParams(-1,UI.dp(this,96)));
        ScrollView scroll=new ScrollView(this);LinearLayout grid=UI.column(this);scroll.addView(grid);
        int widthDp=(int)(getResources().getDisplayMetrics().widthPixels/getResources().getDisplayMetrics().density);
        int columns=widthDp>=740?4:widthDp>=600?3:2;
        List<String> available=new ArrayList<>(),bindings=new ArrayList<>();
        for(String key:List.of("files","gallery","browser")) {
            String binding=settings.text("binding_"+key);if(!binding.isEmpty())bindings.add(binding);
        }
        for(String pkg:List.of("com.freefcc.app","com.android.documentsui","com.android.gallery3d","com.android.settings","app.lawnchair"))
            for(AppEntry entry:apps.snapshot())if(entry.key.componentName.getPackageName().equals(pkg))available.add(entry.component());
        for(AppEntry entry:apps.snapshot())if(!available.contains(entry.component()))available.add(entry.component());
        List<String> selected=ShortcutSelection.choose(available,settings.list("pins"),bindings,settings.text("binding_fly"),getPackageName(),8);
        LinearLayout row=null;
        for(int i=0;i<selected.size();i++) {
            if(i%columns==0) {row=new LinearLayout(this);grid.addView(row);}
            AppEntry entry=apps.find(selected.get(i));if(entry==null)continue;
            ImageButton tile=UI.iconButton(this,android.R.drawable.sym_def_app_icon,entry.label,()-> {
                if(!launch.launch(entry.component()))Toast.makeText(this,R.string.app_gone,Toast.LENGTH_SHORT).show();
            });
            tile.setImageTintList(null);tile.setContentDescription(entry.label+" · "+entry.key.componentName.getPackageName());tile.setTooltipText(entry.label);apps.icon(entry,tile);
            tile.setOnLongClickListener(v->{shortcutMenu(entry);return true;});
            LinearLayout.LayoutParams cell=new LinearLayout.LayoutParams(0,UI.dp(this,76),1);cell.setMargins(UI.dp(this,4),UI.dp(this,4),UI.dp(this,4),UI.dp(this,4));row.addView(tile,cell);
            if(i==selected.size()-1)for(int empty=(i%columns)+1;empty<columns;empty++)row.addView(new View(this),new LinearLayout.LayoutParams(0,UI.dp(this,76),1));
        }
        if(selected.isEmpty()) {
            Button choose=UI.button(this,"+",()->drawer(""));choose.setContentDescription(getString(R.string.all_apps));grid.addView(choose);
        }
        content.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        if(visible)status.start();
    }
    private void dashboardMenu() {
        dialog=new AlertDialog.Builder(this).setTitle(R.string.menu).setItems(new String[]{getString(R.string.all_apps),getString(R.string.settings)},(d,which)-> {
            if(which==0)drawer("");else startActivity(new Intent(this,RCSettingsActivity.class));
        }).setNegativeButton(R.string.close,null).create();dialog.show();
    }
    private void shortcutMenu(AppEntry entry) {
        dialog=new AlertDialog.Builder(this).setTitle(entry.label).setItems(new String[]{getString(R.string.pin),getString(R.string.all_apps)},(d,which)-> {
            if(which==0) {settings.togglePin(entry.component());home();}else drawer("");
        }).setNegativeButton(R.string.close,null).create();dialog.show();
    }
    private void tool(String key) {
        switch(key) {
            case "all_apps":drawer("");return;case "settings":startActivity(new Intent(this,RCSettingsActivity.class));return;
            case "tools":flightTools();return;
            case "storage":new AlertDialog.Builder(this).setTitle(R.string.storage).setMessage(status.storage()).setPositiveButton(R.string.files,(d,w)->tool("files")).setNegativeButton(R.string.close,null).show();return;
            case "system_settings":publicIntent(new Intent(Settings.ACTION_SETTINGS));return;
            default:
                if(!settings.text("binding_"+key).isEmpty()) {openBinding(key);return;}
                if(key.equals("files"))publicIntent(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*").addCategory(Intent.CATEGORY_OPENABLE));
                else if(key.equals("gallery"))publicIntent(new Intent(Intent.ACTION_VIEW).setType("image/*"));
                else if(key.equals("browser"))drawer("browser");
        }
    }
    private void publicIntent(Intent i) {
        try {if(i.resolveActivity(getPackageManager())==null)throw new IllegalArgumentException();startActivity(i);}
        catch(RuntimeException e) {Toast.makeText(this,R.string.missing,Toast.LENGTH_LONG).show();}
    }
    private void openBinding(String key) {
        String component=settings.text("binding_"+key);
        if(component.isEmpty()) {drawer(key);return;}
        if(!launch.launch(component)) {Toast.makeText(this,R.string.app_gone,Toast.LENGTH_LONG).show();drawer(key);}
    }
    private void drawer(String binding) {
        clear();page="DRAWER";picker=binding;
        TextView title=UI.text(this,getString(binding.isEmpty()?R.string.all_apps:R.string.choose),22,UI.TEXT);content.addView(title);
        search=new EditText(this);search.setTextColor(UI.TEXT);search.setHintTextColor(UI.MUTED);search.setHint(R.string.search);search.setSingleLine(true);
        search.setInputType(android.text.InputType.TYPE_CLASS_TEXT);search.setFocusableInTouchMode(true);content.addView(search,new LinearLayout.LayoutParams(-1,UI.dp(this,52)));
        root.setFocusableInTouchMode(true);root.requestFocus();getWindow().setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        GridView list=new GridView(this);list.setNumColumns(Math.max(2,(int)(getResources().getDisplayMetrics().widthPixels/getResources().getDisplayMetrics().density)/160));list.setVerticalSpacing(UI.dp(this,8));list.setHorizontalSpacing(UI.dp(this,8));
        adapter=new AppGridAdapter(apps,(entry,longPress)-> {
            if(!picker.isEmpty()) {settings.put("binding_"+picker,entry.component());home();return;}
            if(longPress) {settings.togglePin(entry.component());Toast.makeText(this,R.string.saved,Toast.LENGTH_SHORT).show();}
            else if(!launch.launch(entry.component()))Toast.makeText(this,R.string.app_gone,Toast.LENGTH_SHORT).show();
        });list.setAdapter(adapter);content.addView(list,new LinearLayout.LayoutParams(-1,0,1));adapter.submit(apps.snapshot());
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int start,int count,int after) {}
            public void onTextChanged(CharSequence s,int start,int before,int count) {main.removeCallbacks(filtering);main.postDelayed(filtering,150);}
            public void afterTextChanged(Editable s) {}
        });
    }
    private void flightTools() {switchList(settings.list("pins"),R.string.tools);}
    private void switcher() {
        List<String> combined=new ArrayList<>(settings.list("pins"));
        for(String c:settings.list("history"))if(!combined.contains(c))combined.add(c);
        switchList(combined,R.string.switcher);
    }
    private void switchList(List<String> components,int title) {
        clear();page="SWITCHER";content.addView(UI.text(this,getString(title),24,UI.TEXT));content.addView(UI.text(this,getString(R.string.history_hint),14,UI.MUTED));
        GridView list=new GridView(this);list.setNumColumns(4);list.setVerticalSpacing(UI.dp(this,8));list.setHorizontalSpacing(UI.dp(this,8));adapter=new AppGridAdapter(apps,(e,hold)-> {if(hold)settings.togglePin(e.component());else launch.launch(e.component());});list.setAdapter(adapter);
        List<AppEntry> entries=new ArrayList<>();for(String c:components) {AppEntry e=apps.find(c);if(e!=null)entries.add(e);}
        adapter.submit(entries);content.addView(list,new LinearLayout.LayoutParams(-1,0,1));
        Button pin=UI.button(this,getString(R.string.pin),()->drawer(""));content.addView(pin);
    }
    private void menu() {
        String[] names={getString(R.string.fly),getString(R.string.all_apps),getString(R.string.switcher),getString(R.string.settings)};
        dialog=new AlertDialog.Builder(this).setTitle(R.string.menu).setItems(names,(d,which)-> {
            if(which==0)openBinding("fly");else if(which==1)drawer("");else if(which==2)switcher();else startActivity(new Intent(this,RCSettingsActivity.class));
        }).setNegativeButton(R.string.close,null).create();dialog.show();
    }
    @Override public void onBackPressed() {
        if(dialog!=null&&dialog.isShowing()) {dialog.dismiss();dialog=null;return;}
        if(search!=null&&search.hasFocus()) {getSystemService(InputMethodManager.class).hideSoftInputFromWindow(search.getWindowToken(),0);root.requestFocus();return;}
        if(!page.equals("HOME"))home();
    }
    @Override protected void onStart() {
        super.onStart();visible=true;
        apps.start(entries->{if(page.equals("HOME"))home();else if(page.equals("DRAWER")&&adapter!=null)adapter.submit(search==null?entries:apps.search(search.getText().toString()));});
        if(page.equals("HOME"))status.start();
    }
    @Override protected void onStop() {visible=false;apps.stop();status.stop();main.removeCallbacksAndMessages(null);super.onStop();}
    @Override protected void onDestroy() {status.close();if(dialog!=null)dialog.dismiss();super.onDestroy();}
}
