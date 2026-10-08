package dev.rclauncher.rc2.core;
public final class GestureConfig {
    public final float slop,distance,ratio;
    public final long hold,longPress,guardHold;
    public GestureConfig(float slop,float distance,long hold,long longPress,long guardHold,float ratio) {
        if(slop<=0 || distance<32 || distance>72 || distance<=slop || hold<250 || hold>500 ||
                longPress<400 || longPress>900 || guardHold<hold || guardHold>1500 || ratio<1.2f || ratio>2.5f)
            throw new IllegalArgumentException("Invalid gesture thresholds");
        this.slop=slop;this.distance=distance;this.hold=hold;this.longPress=longPress;this.guardHold=guardHold;this.ratio=ratio;
    }
}
