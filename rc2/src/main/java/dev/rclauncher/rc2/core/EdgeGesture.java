package dev.rclauncher.rc2.core;

/** Monotonic, DP-based recognition. A pointer stream can commit only on UP. */
public final class EdgeGesture {
    public enum Edge { LEFT, RIGHT, BOTTOM }
    private enum State { IDLE, TRACKING, ARMED, CANCELLED }
    private final Edge edge;
    private final GestureConfig config;
    private final boolean guard;
    private State state=State.IDLE;
    private float x,y,armedX,armedY;
    private long start,armedTime;

    public EdgeGesture(Edge edge,GestureConfig config,boolean guard) { this.edge=edge;this.config=config;this.guard=guard; }
    public void down(float x,float y,long time,boolean inside) {
        cancel();if(!inside)return;
        this.x=x;this.y=y;start=time;state=State.TRACKING;
    }
    public void move(float currentX,float currentY,long time) {
        if(state==State.IDLE || state==State.CANCELLED)return;
        float dx=currentX-x,dy=currentY-y;
        float primary=edge==Edge.LEFT?dx:edge==Edge.RIGHT?-dx:-dy;
        float secondary=edge==Edge.BOTTOM?Math.abs(dx):Math.abs(dy);
        if(time<start || time-start>3000 || primary < -config.slop ||
                (secondary>config.slop && primary<config.ratio*secondary)) { state=State.CANCELLED;return; }
        if(state==State.ARMED) {
            if(primary<config.distance || Math.hypot(currentX-armedX,currentY-armedY)>config.slop*2)state=State.CANCELLED;
        } else if(primary>=config.distance && primary>=config.ratio*secondary) {
            state=State.ARMED;armedTime=time;armedX=currentX;armedY=currentY;
        }
    }
    public Action up(float currentX,float currentY,long time) {
        move(currentX,currentY,time);Action result=null;
        if(state==State.TRACKING && Math.hypot(currentX-x,currentY-y)<=config.slop && time>=start && time-start<=3000) {
            if(edge==Edge.RIGHT)result=time-start>=config.longPress?Action.BAR:Action.MENU;
        } else if(state==State.ARMED && time-start>=80) {
            if(edge==Edge.BOTTOM) {
                long held=time-armedTime;
                if(guard) { if(held>=config.guardHold)result=Action.HOME; }
                else if(held>=config.hold)result=Action.SWITCHER;
                else if(time-start<=700)result=Action.HOME;
            } else if(time-start<=700)result=edge==Edge.LEFT?Action.BACK:Action.MENU;
        }
        cancel();return result;
    }
    public void cancel() {state=State.IDLE;}
}
