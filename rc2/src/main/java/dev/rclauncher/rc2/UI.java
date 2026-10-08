package dev.rclauncher.rc2;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.ImageView;

public final class UI {
    public static final int BG=Color.rgb(13,18,25),SURFACE=Color.rgb(24,33,44),TEXT=Color.rgb(245,247,250),MUTED=Color.rgb(185,197,212),ACCENT=Color.rgb(118,199,255);
    public static int dp(Context c,float value) {return Math.round(value*c.getResources().getDisplayMetrics().density);}
    public static GradientDrawable shape(Context c,int color,int radius) {
        GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(c,radius));d.setStroke(dp(c,1),Color.rgb(58,75,97));return d;
    }
    public static TextView text(Context c,String value,int size,int color) {
        TextView v=new TextView(c);v.setText(value);v.setTextSize(size);v.setTextColor(color);v.setGravity(Gravity.CENTER_VERTICAL);return v;
    }
    public static Button button(Context c,String label,Runnable action) {
        Button b=new Button(c);b.setText(label);b.setTextColor(TEXT);b.setTextSize(16);b.setAllCaps(false);b.setMinHeight(dp(c,52));b.setMinimumHeight(dp(c,52));
        b.setPadding(dp(c,12),dp(c,6),dp(c,12),dp(c,6));b.setMaxLines(2);b.setContentDescription(label);
        StateListDrawable states=new StateListDrawable();states.addState(new int[]{android.R.attr.state_pressed},shape(c,Color.rgb(38,53,73),14));
        states.addState(new int[]{android.R.attr.state_focused},shape(c,Color.rgb(38,53,73),14));states.addState(new int[]{},shape(c,SURFACE,14));b.setBackground(states);
        b.setOnClickListener(v->action.run());return b;
    }
    public static LinearLayout column(Context c) {LinearLayout l=new LinearLayout(c);l.setOrientation(LinearLayout.VERTICAL);return l;}
    public static ImageButton iconButton(Context c,int drawable,String label,Runnable action) {
        ImageButton b=new ImageButton(c);b.setImageResource(drawable);b.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        b.setPadding(dp(c,14),dp(c,14),dp(c,14),dp(c,14));b.setMinimumHeight(dp(c,52));b.setFocusable(true);
        b.setContentDescription(label);b.setTooltipText(label);
        StateListDrawable states=new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed},shape(c,Color.rgb(38,53,73),14));
        states.addState(new int[]{android.R.attr.state_focused},shape(c,Color.rgb(38,53,73),14));
        states.addState(new int[]{},shape(c,SURFACE,14));b.setBackground(states);b.setOnClickListener(v->action.run());
        return b;
    }
    public static StateListDrawable primaryBackground(Context c) {
        StateListDrawable states=new StateListDrawable();
        states.addState(new int[]{android.R.attr.state_pressed},shape(c,Color.rgb(90,180,242),14));
        states.addState(new int[]{android.R.attr.state_focused},shape(c,Color.rgb(166,219,255),14));
        states.addState(new int[]{},shape(c,ACCENT,14));return states;
    }
    public static void add(LinearLayout parent,View child,int height) {
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(parent.getContext(),height));p.bottomMargin=dp(parent.getContext(),8);parent.addView(child,p);
    }
}
