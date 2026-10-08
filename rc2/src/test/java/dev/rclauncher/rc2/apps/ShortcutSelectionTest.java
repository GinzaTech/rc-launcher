package dev.rclauncher.rc2.apps;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class ShortcutSelectionTest {
    @Test public void picksExplicitAppsFirstAndAvoidsDefaultDuplicatePackages() {
        List<String> available=List.of("settings/.A","settings/.B","files/.Main","photos/.Main","fly/.Main","self/.Main");
        assertEquals(List.of("files/.Main","settings/.A","photos/.Main"),
                ShortcutSelection.choose(available,List.of("files/.Main"),List.of("settings/.A"),"fly/.Main","self",8));
    }
    @Test public void absentAppsAreNotInventedAndExplicitAliasesAreRespected() {
        assertEquals(List.of("settings/.A","settings/.B"),ShortcutSelection.choose(List.of("settings/.A","settings/.B"),
                List.of("missing/.Main","settings/.A","settings/.B"),List.of(),"","self",8));
        assertEquals(List.of(),ShortcutSelection.choose(List.of(),List.of("missing/.Main"),List.of(),"","self",8));
    }
    @Test public void limitsSlotsAndNeverRepeatsTheConfiguredFlyOrOwnLauncher() {
        assertEquals(List.of("files/.Main"),ShortcutSelection.choose(List.of("fly/.Main","self/.Main","files/.Main","photos/.Main"),List.of(),List.of(),"fly/.Main","self",1));
    }
}
