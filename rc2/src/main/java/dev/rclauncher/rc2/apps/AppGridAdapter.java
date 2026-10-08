package dev.rclauncher.rc2.apps;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import dev.rclauncher.rc2.UI;
import java.util.List;
import java.util.ArrayList;

/** Framework GridView recycles cells without AndroidX in the RC runtime. */
public final class AppGridAdapter extends BaseAdapter {
    public interface Selection {void selected(AppEntry entry,boolean longPress);}
    private List<AppEntry> entries=new ArrayList<>();
    private final AppRepository apps;
    private final Selection selection;
    public AppGridAdapter(AppRepository apps,Selection selection) {this.apps=apps;this.selection=selection;}
    public void submit(List<AppEntry> entries) {this.entries=new ArrayList<>(entries);notifyDataSetChanged();}
    @Override public boolean hasStableIds() {return true;}
    @Override public long getItemId(int position) {
        long id=0xcbf29ce484222325L;
        for(char c:entries.get(position).key.toString().toCharArray())id=(id^c)*0x100000001b3L;
        return id;
    }
    @Override public int getCount() {return entries.size();}
    @Override public AppEntry getItem(int position) {return entries.get(position);}
    @Override public View getView(int position,View convertView,ViewGroup parent) {
        Holder h;
        if(convertView==null) {
            Context c=parent.getContext();LinearLayout box=UI.column(c);box.setGravity(Gravity.CENTER);box.setPadding(UI.dp(c,8),UI.dp(c,8),UI.dp(c,8),UI.dp(c,8));
            box.setBackground(UI.shape(c,UI.SURFACE,14));box.setFocusable(true);box.setLayoutParams(new AbsListView.LayoutParams(-1,UI.dp(c,102)));
            ImageView icon=new ImageView(c);box.addView(icon,new LinearLayout.LayoutParams(UI.dp(c,40),UI.dp(c,40)));
            TextView label=UI.text(c,"",15,UI.TEXT);label.setGravity(Gravity.CENTER);label.setMaxLines(2);label.setEllipsize(android.text.TextUtils.TruncateAt.END);box.addView(label,new LinearLayout.LayoutParams(-1,-2));
            h=new Holder(icon,label);box.setTag(h);convertView=box;
        }else h=(Holder)convertView.getTag();
        AppEntry entry=entries.get(position);h.label.setText(entry.label);convertView.setContentDescription(entry.label+" · "+entry.key.componentName.getPackageName());
        apps.icon(entry,h.icon);convertView.setOnClickListener(v->selection.selected(entry,false));
        convertView.setOnLongClickListener(v->{selection.selected(entry,true);return true;});return convertView;
    }
    private static final class Holder {
        final ImageView icon;final TextView label;
        Holder(ImageView icon,TextView label) {this.icon=icon;this.label=label;}
    }
}
