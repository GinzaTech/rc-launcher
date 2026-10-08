package dev.rclauncher.rc2.settings;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Small versioned config. Validation completes before atomic preference commit. */
public final class SettingsStore {
    public interface Listener {void changed();}
    private final SharedPreferences preferences;
    private final List<Listener> listeners=new ArrayList<>();
    private JSONObject data;
    public SettingsStore(Context context) {
        preferences=context.getSharedPreferences("rc-config",Context.MODE_PRIVATE);
        try {data=validate(new JSONObject(preferences.getString("config","{}")));}
        catch(JSONException|IllegalArgumentException e) {data=defaults();}
    }
    private static JSONObject defaults() {
        JSONObject d=new JSONObject();
        try {
            d.put("schema",1).put("navigation",false).put("preset","floating").put("flyGuard",true)
             .put("systemRecents",false).put("language","vi").put("reduceMotion",true)
             .put("left",false).put("right",true).put("bottom",false)
             .put("position",0.5).put("thickness",12).put("length",96).put("bottomWidth",144)
             .put("distance",48).put("hold",650).put("opacity",0.55)
             .put("pins",new JSONArray()).put("history",new JSONArray());
        } catch(JSONException e) {throw new IllegalStateException(e);}
        return d;
    }
    public static JSONObject validate(JSONObject input) throws JSONException {
        input=new JSONObject(input.toString());
        int schema=input.optInt("schema",1);
        if(schema!=1 || (input.has("schema")&&(!(input.get("schema") instanceof Number)||input.getDouble("schema")!=1)) || input.toString().length()>65536)throw new IllegalArgumentException("Unsupported config schema/size");
        JSONObject result=defaults();
        java.util.Iterator<String> keys=input.keys();
        while(keys.hasNext()) {
            String key=keys.next();
            if(!result.has(key) && !List.of("binding_fly","binding_files","binding_gallery","binding_browser").contains(key))throw new IllegalArgumentException("Unknown config field");
            result.put(key,input.get(key));
        }
        for(String key:new String[]{"navigation","flyGuard","systemRecents","reduceMotion","left","right","bottom"})
            if(!(result.get(key) instanceof Boolean))throw new IllegalArgumentException("Invalid boolean");
        if(!List.of("floating","edge","bar").contains(result.getString("preset")))throw new IllegalArgumentException("Invalid preset");
        if(!List.of("vi","en").contains(result.getString("language")))throw new IllegalArgumentException("Invalid language");
        checkRange(result,"position",0,1);checkRange(result,"opacity",0.2,1);checkRange(result,"thickness",8,24);
        checkRange(result,"length",64,180);checkRange(result,"bottomWidth",96,240);checkRange(result,"distance",32,72);checkRange(result,"hold",550,1500);
        if(result.getString("preset").equals("edge") && !result.getBoolean("left") && !result.getBoolean("right") && !result.getBoolean("bottom"))
            throw new IllegalArgumentException("At least one visible recovery handle is required");
        for(String key:new String[]{"pins","history"}) {
            JSONArray list=result.getJSONArray(key);
            if(list.length()>(key.equals("history")?12:24))throw new IllegalArgumentException("Too many apps");
            for(int i=0;i<list.length();i++)component(list.getString(i));
        }
        keys=result.keys();
        while(keys.hasNext()) {String k=keys.next();if(k.startsWith("binding_"))component(result.getString(k));}
        return result;
    }
    private static void checkRange(JSONObject d,String key,double low,double high) throws JSONException {
        Object raw=d.get(key);if(!(raw instanceof Number))throw new IllegalArgumentException("Invalid number");
        double v=((Number)raw).doubleValue();if(!Double.isFinite(v)||v<low||v>high)throw new IllegalArgumentException("Out of range");
    }
    private static void component(String value) {
        if(value.length()>512 || !value.matches("[A-Za-z_][A-Za-z0-9_]*(?:\\.[A-Za-z_][A-Za-z0-9_]*)*/\\.?[A-Za-z_$][A-Za-z0-9_$]*(?:\\.[A-Za-z_$][A-Za-z0-9_$]*)*"))
            throw new IllegalArgumentException("Invalid component");
    }
    public synchronized String text(String key) {return data.optString(key,"");}
    public synchronized boolean flag(String key) {return data.optBoolean(key,false);}
    public synchronized float number(String key) {return (float)data.optDouble(key,0);}
    public synchronized List<String> list(String key) {
        JSONArray array=data.optJSONArray(key);List<String> out=new ArrayList<>();
        if(array!=null)for(int i=0;i<array.length();i++)out.add(array.optString(i));
        return Collections.unmodifiableList(out);
    }
    public synchronized String exportJson() {return data.toString();}
    public void importJson(String raw) throws JSONException {
        if(raw.length()>65536)throw new IllegalArgumentException("Config too large");
        commit(validate(new JSONObject(raw)));
    }
    public void put(String key,Object value) {
        try {JSONObject next=new JSONObject(exportJson());next.put(key,value);commit(validate(next));}
        catch(JSONException e) {throw new IllegalArgumentException(e);}
    }
    public void resetNavigation() {
        try {
            JSONObject next=new JSONObject(exportJson()),d=defaults();
            for(String k:List.of("navigation","preset","left","right","bottom","position","thickness","length","bottomWidth","distance","hold","opacity"))next.put(k,d.get(k));
            commit(validate(next));
        } catch(JSONException e) {throw new IllegalStateException(e);}
    }
    private void commit(JSONObject next) {
        synchronized(this) {data=next;preferences.edit().putString("config",next.toString()).apply();}
        for(Listener l:new ArrayList<>(listeners))l.changed();
    }
    public void listen(Listener l) {listeners.add(l);}
    public void unlisten(Listener l) {listeners.remove(l);}
    public void record(String component) {
        List<String> previous=list("history"),next=new ArrayList<>();next.add(component);
        for(String p:previous)if(!p.equals(component)&&next.size()<12)next.add(p);
        put("history",new JSONArray(next));
    }
    public void togglePin(String component) {
        List<String> next=new ArrayList<>(list("pins"));
        if(!next.remove(component)) {if(next.size()>=24)throw new IllegalArgumentException("Pin limit");next.add(component);}
        put("pins",new JSONArray(next));
    }
}
