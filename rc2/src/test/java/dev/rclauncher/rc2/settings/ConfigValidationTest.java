package dev.rclauncher.rc2.settings;
import org.json.JSONObject;
import org.json.JSONArray;
import org.junit.Test;
import static org.junit.Assert.*;

public class ConfigValidationTest {
    @Test public void oldEmptySettingsMigrateToCurrentDefaultsWithoutEnablingService() throws Exception {
        JSONObject result=SettingsStore.validate(new JSONObject());
        assertEquals(1,result.getInt("schema"));assertFalse(result.getBoolean("navigation"));
        assertEquals("floating",result.getString("preset"));assertEquals("vi",result.getString("language"));
    }
    @Test public void invalidEnumsRangesAndFutureVersionsFailBeforeCommit() throws Exception {
        String[] invalid={"{\"schema\":2}","{\"position\":1.01}","{\"opacity\":0.19}","{\"thickness\":25}","{\"preset\":\"inject\"}","{\"navigation\":\"true\"}","{\"binding_shell\":\"x/.y\"}","{\"hold\":349}"};
        for(String json:invalid) {
            try {SettingsStore.validate(new JSONObject(json));fail(json);}catch(IllegalArgumentException expected) {}
        }
    }
    @Test public void acceptedConfigDoesNotMutateInputAndKeepsBindings() throws Exception {
        JSONObject input=new JSONObject().put("binding_fly","dji.go.v5/.Main").put("position",0.25);
        String previous=input.toString();JSONObject result=SettingsStore.validate(input);
        assertEquals(previous,input.toString());assertEquals("dji.go.v5/.Main",result.getString("binding_fly"));
        assertEquals(0.25,result.getDouble("position"),0.001);
    }
    @Test public void malformedComponentsAndTooManyHistoryItemsAreRejected() throws Exception {
        for(String component:new String[]{"dji.go.v5/.Main;id","bad", "x/y/z", "../.Main"}) {
            try {SettingsStore.validate(new JSONObject().put("binding_fly",component));fail(component);}catch(IllegalArgumentException expected) {}
        }
        JSONArray list=new JSONArray();for(int i=0;i<13;i++)list.put("app.pkg/.Main"+i);
        try {SettingsStore.validate(new JSONObject().put("history",list));fail();}catch(IllegalArgumentException expected) {}
    }
    @Test public void noEdgeRegionsCannotBecomeAnInvisibleNavigationTrap() throws Exception {
        JSONObject input=new JSONObject().put("preset","edge").put("left",false).put("right",false).put("bottom",false);
        try {SettingsStore.validate(input);fail();}catch(IllegalArgumentException expected) {}
    }
}
