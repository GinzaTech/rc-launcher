package dev.rclauncher.rc2;

import android.accessibilityservice.AccessibilityService;
import android.app.role.RoleManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import dev.rclauncher.rc2.core.Action;
import dev.rclauncher.rc2.core.EdgeGesture;
import dev.rclauncher.rc2.core.GestureConfig;
import dev.rclauncher.rc2.settings.SettingsStore;

/** One event-driven service; never reads nodes, text, screenshots, or physical keys. */
public final class RCNavigationService extends AccessibilityService implements SettingsStore.Listener {
    public static volatile RCNavigationService instance;
    public static volatile String state="disabled";
    private final Handler main=new Handler(Looper.getMainLooper());
    private final List<View> windows=new ArrayList<>();
    private final List<EdgeGesture> recognizers=new ArrayList<>();
    private WindowManager wm;
    private SettingsStore settings;
    private View menu;
    private boolean menuIsBar=false;
    private boolean screenOn=true,flyHint=false;
    private long hiddenUntil=0;
    private final Runnable restore=()->{hiddenUntil=0;rebuild();};
    private final Runnable collapse=()->{closeMenu();rebuild();};
    private final BroadcastReceiver screen=new BroadcastReceiver() {
        @Override public void onReceive(Context c,Intent i) {
            screenOn=!Intent.ACTION_SCREEN_OFF.equals(i.getAction());
            if(screenOn)rebuild();else {cancelGestures();removeWindows();}
        }
    };
    @Override protected void onServiceConnected() {
        instance=this;state="bound";wm=getSystemService(WindowManager.class);settings=((RCApplication)getApplication()).settings();
        settings.listen(this);IntentFilter f=new IntentFilter(Intent.ACTION_SCREEN_OFF);f.addAction(Intent.ACTION_SCREEN_ON);registerReceiver(screen,f);
        screenOn=getSystemService(android.os.PowerManager.class).isInteractive();rebuild();
    }
    @Override public void changed() {main.post(this::rebuild);}
    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        if(event.getEventType()!=AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED||event.getPackageName()==null||settings==null)return;
        String p=event.getPackageName().toString();
        if(p.equals(getPackageName())||p.equals("android")||p.contains("permissioncontroller")||p.contains("inputmethod")||p.equals("com.miui.securitycenter"))return;
        String component=settings.text("binding_fly");
        flyHint=!component.isEmpty()&&p.equals(component.split("/")[0]);
    }
    @Override public void onInterrupt() {state="interrupted";cancelGestures();closeMenu();}
    @Override public void onSystemActionsChanged() { /* Availability is read at user dispatch, never polled. */ }
    @Override public void onConfigurationChanged(Configuration c) {super.onConfigurationChanged(c);rebuild();}
    @Override public boolean onUnbind(Intent intent) {release();return super.onUnbind(intent);}
    @Override public void onDestroy() {release();super.onDestroy();}
    private void release() {
        cancelGestures();removeWindows();main.removeCallbacksAndMessages(null);
        if(settings!=null)settings.unlisten(this);
        try {unregisterReceiver(screen);}catch(IllegalArgumentException ignored) {}
        if(instance==this)instance=null;state="disabled";
    }
    private void cancelGestures() {for(EdgeGesture g:recognizers)g.cancel();recognizers.clear();}
    private Rect usable() {
        android.view.WindowMetrics metrics=wm.getCurrentWindowMetrics();Rect r=new Rect(metrics.getBounds());
        android.graphics.Insets inset=metrics.getWindowInsets().getInsetsIgnoringVisibility(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());
        r.left+=inset.left;r.top+=inset.top;r.right-=inset.right;r.bottom-=inset.bottom;return r;
    }
    private void add(View view,int width,int height,int x,int y,boolean temporary) {
        WindowManager.LayoutParams p=new WindowManager.LayoutParams(width,height,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,PixelFormat.TRANSLUCENT);
        p.gravity=Gravity.TOP|Gravity.LEFT;p.x=x;p.y=y;
        try {wm.addView(view,p);windows.add(view);if(temporary)menu=view;}
        catch(RuntimeException e) {state="policy_blocked";removeWindows();Toast.makeText(this,R.string.nav_failed,Toast.LENGTH_LONG).show();}
    }
    private void removeWindows() {
        closeMenu();for(View v:new ArrayList<>(windows))try {wm.removeViewImmediate(v);}catch(RuntimeException ignored) {}
        windows.clear();
    }
    public void closeMenu() {
        main.removeCallbacks(collapse);
        if(menu!=null) {View old=menu;menu=null;windows.remove(old);try {wm.removeViewImmediate(old);}catch(RuntimeException ignored) {}}
        menuIsBar=false;
    }
    public void rebuild() {
        if(wm==null||settings==null)return;cancelGestures();removeWindows();
        if(!screenOn||!settings.flag("navigation")||android.os.SystemClock.elapsedRealtime()<hiddenUntil)return;
        Rect r=usable();float position=settings.number("position");String preset=settings.text("preset");
        if(preset.equals("bar")) {bar();return;}
        if(preset.equals("floating")) {
            Button b=UI.button(this,getString(R.string.menu),()->execute(Action.MENU));b.setAlpha(settings.number("opacity"));
            b.setOnLongClickListener(v->{execute(Action.BAR);return true;});
            int size=UI.dp(this,56);add(b,size,size,r.right-size,r.top+Math.round((r.height()-size)*position),false);return;
        }
        int thickness=UI.dp(this,settings.number("thickness")),length=UI.dp(this,settings.number("length"));
        int y=r.top+Math.round((r.height()-length)*position);
        if(settings.flag("left"))handle(EdgeGesture.Edge.LEFT,thickness,length,r.left,y);
        if(settings.flag("right"))handle(EdgeGesture.Edge.RIGHT,thickness,length,r.right-thickness,y);
        if(settings.flag("bottom")) {
            int width=UI.dp(this,settings.number("bottomWidth"));handle(EdgeGesture.Edge.BOTTOM,width,thickness,r.left+Math.round((r.width()-width)*position),r.bottom-thickness);
        }
    }
    private void handle(EdgeGesture.Edge edge,int width,int height,int x,int y) {
        View view=new View(this);view.setBackground(UI.shape(this,UI.ACCENT,8));view.setAlpha(settings.number("opacity"));
        view.setContentDescription(edge==EdgeGesture.Edge.LEFT?getString(R.string.back):edge==EdgeGesture.Edge.RIGHT?getString(R.string.menu):getString(R.string.home));
        final EdgeGesture[] g={null};final int[] pointer={-1};
        float density=getResources().getDisplayMetrics().density;
        view.setOnTouchListener((v,event)-> {
            switch(event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    pointer[0]=event.getPointerId(0);
                    g[0]=new EdgeGesture(edge,new GestureConfig(ViewConfiguration.get(this).getScaledTouchSlop()/density,
                            settings.number("distance"),350,550,(long)settings.number("hold"),1.5f),settings.flag("flyGuard")&&flyHint);
                    recognizers.add(g[0]);g[0].down(event.getRawX()/density,event.getRawY()/density,event.getEventTime(),true);return true;
                case MotionEvent.ACTION_MOVE:
                    if(g[0]!=null)g[0].move(event.getRawX()/density,event.getRawY()/density,event.getEventTime());return true;
                case MotionEvent.ACTION_UP:
                    if(g[0]!=null&&event.getPointerId(0)==pointer[0]) {
                        Action action=g[0].up(event.getRawX()/density,event.getRawY()/density,event.getEventTime());recognizers.remove(g[0]);g[0]=null;
                        if(action!=null)execute(action);
                    }return true;
                case MotionEvent.ACTION_POINTER_DOWN:case MotionEvent.ACTION_CANCEL:
                    if(g[0]!=null) {g[0].cancel();recognizers.remove(g[0]);g[0]=null;}return true;
                default:return true;
            }
        });
        add(view,width,height,x,y,false);
    }
    private boolean available(int action) {return getSystemActions().stream().anyMatch(a->a.getId()==action);}
    public boolean execute(Action action) {
        if(action==Action.CLOSE) {closeMenu();return true;}
        if(action==Action.MENU) {if(menu!=null&&!menuIsBar) {closeMenu();return true;}else showMenu();return menu!=null;}
        if(action==Action.BAR) {closeMenu();bar();return menu!=null;}
        if(action==Action.BACK&&menu!=null) {closeMenu();return true;}
        closeMenu();
        boolean success;
        switch(action) {
            case BACK:success=available(GLOBAL_ACTION_BACK)&&performGlobalAction(GLOBAL_ACTION_BACK);break;
            case HOME:
                RoleManager role=getSystemService(RoleManager.class);
                success=role!=null&&role.isRoleHeld(RoleManager.ROLE_HOME)&&available(GLOBAL_ACTION_HOME)&&performGlobalAction(GLOBAL_ACTION_HOME);
                if(!success)success=open("HOME");break;
            case SWITCHER:
                success=settings.flag("systemRecents")&&available(GLOBAL_ACTION_RECENTS)&&performGlobalAction(GLOBAL_ACTION_RECENTS);
                if(!success)success=open("SWITCHER");break;
            case ALL_APPS:success=open("DRAWER");break;
            case FLY:success=open("FLY");break;
            default:success=false;
        }
        if(!success)Toast.makeText(this,R.string.action_failed,Toast.LENGTH_SHORT).show();
        else if(state.equals("interrupted"))state="bound";
        return success;
    }
    private boolean open(String destination) {
        try {startActivity(new Intent(this,RCLauncher.class).putExtra("destination",destination).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP));return true;}
        catch(RuntimeException e) {return false;}
    }
    private void bar() {
        closeMenu();Rect r=usable();LinearLayout row=new LinearLayout(this);row.setBackground(UI.shape(this,UI.SURFACE,14));
        for(Action a:new Action[]{Action.BACK,Action.HOME,Action.MENU}) {
            int label=a==Action.BACK?R.string.back:a==Action.HOME?R.string.home:R.string.menu;
            row.addView(UI.button(this,getString(label),()-> {if(a==Action.BACK) {closeMenu();execute(Action.BACK);}else execute(a);}),new LinearLayout.LayoutParams(0,-1,1));
        }
        int w=Math.min(UI.dp(this,360),r.width()),h=UI.dp(this,56);add(row,w,h,r.centerX()-w/2,r.bottom-h,true);
        menuIsBar=true;
        if(!settings.text("preset").equals("bar"))main.postDelayed(collapse,5000);
    }
    private void showMenu() {
        closeMenu();Rect r=usable();ScrollView scroll=new ScrollView(this);LinearLayout list=UI.column(this);list.setPadding(UI.dp(this,12),UI.dp(this,12),UI.dp(this,12),UI.dp(this,12));
        list.setBackground(UI.shape(this,UI.SURFACE,20));scroll.addView(list);
        UI.add(list,UI.button(this,getString(R.string.back),()->{closeMenu();execute(Action.BACK);}),52);
        UI.add(list,UI.button(this,getString(R.string.home),()->execute(Action.HOME)),52);
        UI.add(list,UI.button(this,getString(R.string.fly),()->execute(Action.FLY)),52);
        UI.add(list,UI.button(this,getString(R.string.all_apps),()->execute(Action.ALL_APPS)),52);
        UI.add(list,UI.button(this,getString(R.string.switcher),()->execute(Action.SWITCHER)),52);
        UI.add(list,UI.button(this,getString(R.string.files),()->{closeMenu();open("FILES");}),52);
        UI.add(list,UI.button(this,getString(R.string.system_settings),()->{closeMenu();open("SYSTEM_SETTINGS");}),52);
        UI.add(list,UI.button(this,getString(R.string.tools),()->{closeMenu();open("TOOLS");}),52);
        UI.add(list,UI.button(this,getString(R.string.settings),()-> {closeMenu();startActivity(new Intent(this,RCSettingsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}),52);
        UI.add(list,UI.button(this,getString(R.string.temporary_hide),()-> {hiddenUntil=android.os.SystemClock.elapsedRealtime()+30000;removeWindows();main.postDelayed(restore,30000);}),52);
        UI.add(list,UI.button(this,getString(R.string.close),this::closeMenu),52);
        int w=Math.min(UI.dp(this,360),r.width()),h=Math.min(UI.dp(this,480),Math.round(r.height()*0.8f));
        add(scroll,w,h,r.right-w,r.top+Math.max(0,(r.height()-h)/2),true);
        main.postDelayed(collapse,5000);
    }
}
