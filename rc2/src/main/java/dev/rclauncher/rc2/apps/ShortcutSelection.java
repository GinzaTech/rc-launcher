package dev.rclauncher.rc2.apps;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Deterministic actual-app suggestions. Never invent an absent handler. */
public final class ShortcutSelection {
    private ShortcutSelection() {}
    public static List<String> choose(List<String> available,List<String> pins,List<String> bindings,String fly,String ownPackage,int limit) {
        List<String> result=new ArrayList<>();Set<String> packages=new HashSet<>();
        for(List<String> group:List.of(pins,bindings,available)) {
            for(String component:group) {
                String pkg=component.split("/",2)[0];
                boolean explicit=group==pins;
                if(result.size()>=limit)return result;
                if(!available.contains(component)||component.equals(fly)||pkg.equals(ownPackage)||result.contains(component))continue;
                if(!explicit&&packages.contains(pkg))continue;
                result.add(component);packages.add(pkg);
            }
        }return result;
    }
}
