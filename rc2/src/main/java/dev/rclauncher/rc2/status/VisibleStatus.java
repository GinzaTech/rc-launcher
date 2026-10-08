package dev.rclauncher.rc2.status;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.text.format.DateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import dev.rclauncher.rc2.R;

/** Producers exist only while dashboard is visible; no idle polling. */
public final class VisibleStatus {
    public static final class Snapshot {
        public final String clock,battery,temperature,cpu;
        public final Boolean wifi;
        public final boolean internet;
        public Snapshot(String clock,String battery,String temperature,Boolean wifi,boolean internet) {
            this(clock,battery,temperature,wifi,internet,"—");
        }
        public Snapshot(String clock,String battery,String temperature,Boolean wifi,boolean internet,String cpu) {
            this.clock=clock;this.battery=battery;this.temperature=temperature;this.wifi=wifi;this.internet=internet;this.cpu=cpu;
        }
    }
    public interface Listener {void update(Snapshot snapshot);}
    private final Context context;
    private final Listener listener;
    private final Handler main=new Handler(Looper.getMainLooper());
    private final ExecutorService io=Executors.newSingleThreadExecutor();
    private final ConnectivityManager network;
    private boolean running=false;
    private String battery="—",temperature="—",storage="—";
    private long lastStorage=0;
    private final CpuTemperature cpuSource=new CpuTemperature();
    private String cpu="—";
    private int generation=0;
    private final Runnable thermalRefresh=()->refreshCpu();
    private void refreshCpu() {
        if(!running)return;int token=generation;
        io.submit(()-> {
            String value=cpuSource.sample();
            main.post(()->{if(running&&token==generation) {cpu=value;render();main.postDelayed(thermalRefresh,10000);}});
        });
    }
    private final BroadcastReceiver receiver=new BroadcastReceiver() {
        @Override public void onReceive(Context c,Intent i) {
            if(Intent.ACTION_BATTERY_CHANGED.equals(i.getAction())) {
                int scale=i.getIntExtra("scale",0),level=i.getIntExtra("level",-1),temp=i.getIntExtra("temperature",-1);
                battery=StatusValues.battery(level,scale);
                temperature=StatusValues.temperature(temp);
            }render();
        }
    };
    private final ConnectivityManager.NetworkCallback callback=new ConnectivityManager.NetworkCallback() {
        @Override public void onAvailable(Network n) {main.post(VisibleStatus.this::render);}
        @Override public void onCapabilitiesChanged(Network n,NetworkCapabilities c) {main.post(VisibleStatus.this::render);}
        @Override public void onLost(Network n) {main.post(VisibleStatus.this::render);}
    };
    public VisibleStatus(Context context,Listener listener) {this.context=context;this.listener=listener;network=context.getSystemService(ConnectivityManager.class);}
    public void start() {
        if(running)return;running=true;generation++;
        IntentFilter f=new IntentFilter(Intent.ACTION_BATTERY_CHANGED);f.addAction(Intent.ACTION_TIME_TICK);f.addAction(Intent.ACTION_TIME_CHANGED);f.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        context.registerReceiver(receiver,f);
        try {network.registerNetworkCallback(new NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build(),callback);}
        catch(RuntimeException ignored) {}
        if(android.os.SystemClock.elapsedRealtime()-lastStorage>30000||lastStorage==0) {
            io.submit(()-> {
                try {StatFs stat=new StatFs(context.getFilesDir().getPath());storage=String.format(java.util.Locale.ROOT,"%.1f / %.1f GB",stat.getAvailableBytes()/1e9,stat.getTotalBytes()/1e9);lastStorage=android.os.SystemClock.elapsedRealtime();}
                catch(RuntimeException e) {storage="—";}main.post(this::render);
            });
        }refreshCpu();render();
    }
    private void render() {
        if(!running)return;
        Boolean wifi=false;boolean internet=false;
        try {
            for(Network n:network.getAllNetworks()) {
                NetworkCapabilities c=network.getNetworkCapabilities(n);
                if(c!=null&&c.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    wifi=true;internet|=c.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
                }
            }
        }catch(RuntimeException e) {wifi=null;}
        listener.update(new Snapshot(DateFormat.getTimeFormat(context).format(new Date()),battery,temperature,wifi,internet,cpu));
    }
    public String storage() {return storage;}
    public void stop() {
        if(!running)return;running=false;generation++;
        context.unregisterReceiver(receiver);try {network.unregisterNetworkCallback(callback);}catch(IllegalArgumentException ignored) {}
        main.removeCallbacksAndMessages(null);
    }
    public void close() {stop();io.shutdownNow();}
}
