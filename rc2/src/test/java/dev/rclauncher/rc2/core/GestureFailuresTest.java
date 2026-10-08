package dev.rclauncher.rc2.core;
import org.junit.Test;
import static org.junit.Assert.*;

public class GestureFailuresTest {
    private EdgeGesture left() {return new EdgeGesture(EdgeGesture.Edge.LEFT,new GestureConfig(8,48,350,550,650,1.5f),false);}
    @Test public void canceledStreamsAndNegativeClockNeverNavigate() {
        EdgeGesture g=left();g.down(0,0,100,true);g.move(60,0,50);assertNull(g.up(60,0,200));
        g.down(0,0,300,true);g.move(60,0,400);g.cancel();assertNull(g.up(60,0,500));
    }
    @Test public void tooFastOrExpiredSwipeDoesNotCommit() {
        for(int t:new int[]{79,701,3001}) {
            EdgeGesture g=left();g.down(0,0,0,true);g.move(48,0,30);assertNull(g.up(48,0,t));
        }
    }
    @Test public void newDownAfterCancelAndIndependentGesturesWork() {
        EdgeGesture g=left();g.down(0,0,0,true);g.cancel();
        g.down(0,0,100,true);g.move(48,0,200);assertEquals(Action.BACK,g.up(48,0,300));
        g.down(0,0,700,true);g.move(48,0,800);assertEquals(Action.BACK,g.up(48,0,900));
    }
    @Test public void canceledHoldDoesNotCommitAfterReconnect() {
        EdgeGesture g=new EdgeGesture(EdgeGesture.Edge.BOTTOM,new GestureConfig(8,48,350,550,650,1.5f),true);
        g.down(0,0,0,true);g.move(0,-50,100);g.cancel();assertNull(g.up(0,-50,751));
    }
}
