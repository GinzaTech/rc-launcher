package dev.rclauncher.rc2.core;

import org.junit.Test;
import static org.junit.Assert.*;

public class EdgeGestureTest {
    private EdgeGesture gesture(EdgeGesture.Edge edge, boolean guard) {
        return new EdgeGesture(edge, new GestureConfig(8,48,350,550,650,1.5f), guard);
    }
    @Test public void rightTapIsOneMenuLeftTapDoesNothing() {
        EdgeGesture r=gesture(EdgeGesture.Edge.RIGHT,false);r.down(0,0,0,true);
        assertEquals(Action.MENU,r.up(0,0,100));assertNull(r.up(0,0,110));
        EdgeGesture l=gesture(EdgeGesture.Edge.LEFT,false);l.down(0,0,0,true);assertNull(l.up(0,0,100));
    }
    @Test public void commitBoundaryAndDirectionAreExact() {
        for(int distance:new int[]{47,48,49}) {
            EdgeGesture g=gesture(EdgeGesture.Edge.LEFT,false);g.down(0,0,0,true);g.move(distance,0,100);
            assertEquals(distance<48?null:Action.BACK,g.up(distance,0,200));
        }
        EdgeGesture g=gesture(EdgeGesture.Edge.LEFT,false);g.down(0,0,0,true);g.move(-60,0,100);assertNull(g.up(-60,0,200));
    }
    @Test public void outsideDownCannotStealMoveAndCancelNeverCommits() {
        EdgeGesture g=gesture(EdgeGesture.Edge.BOTTOM,false);g.down(0,0,0,false);g.move(0,-100,100);assertNull(g.up(0,-100,200));
        g.down(0,0,300,true);g.move(0,-100,400);g.cancel();assertNull(g.up(0,-100,800));
    }
    @Test public void normalUpAndHoldChooseExactlyOneDestination() {
        EdgeGesture g=gesture(EdgeGesture.Edge.BOTTOM,false);g.down(0,0,0,true);g.move(0,-50,100);
        assertEquals(Action.HOME,g.up(0,-50,200));
        g.down(0,0,1000,true);g.move(0,-50,1100);assertEquals(Action.SWITCHER,g.up(0,-50,1450));
        assertNull(g.up(0,-50,1500));
    }
    @Test public void flyGuardDoesNotOpenSwitcherAt350msOrHomeBefore650ms() {
        for(int hold:new int[]{349,350,649,650,651}) {
            EdgeGesture g=gesture(EdgeGesture.Edge.BOTTOM,true);g.down(0,0,0,true);g.move(0,-50,100);
            assertEquals(hold<650?null:Action.HOME,g.up(0,-50,100+hold));
        }
    }
    @Test public void diagonalAndMultiPointerCancellationRejects() {
        EdgeGesture g=gesture(EdgeGesture.Edge.LEFT,false);g.down(0,0,0,true);g.move(50,80,100);assertNull(g.up(50,80,200));
        g.down(0,0,300,true);g.move(50,0,400);g.cancel();assertNull(g.up(50,0,450));
    }
    @Test public void longPressReturnsBarOnceAndMovementWhileHeldCancels() {
        EdgeGesture g=gesture(EdgeGesture.Edge.RIGHT,false);g.down(0,0,0,true);assertEquals(Action.BAR,g.up(0,0,550));assertNull(g.up(0,0,600));
        g=gesture(EdgeGesture.Edge.BOTTOM,true);g.down(0,0,0,true);g.move(0,-50,100);g.move(30,-80,500);assertNull(g.up(30,-80,900));
    }
    @Test public void invalidConfigDoesNotCreateGesture() {
        try { new GestureConfig(8,5,350,550,650,1.5f);fail(); } catch(IllegalArgumentException expected) {}
        try { new GestureConfig(8,48,650,550,350,1.5f);fail(); } catch(IllegalArgumentException expected) {}
    }
    @Test public void movingBackBelowThresholdBeforeReleaseDoesNotCommit() {
        EdgeGesture g=gesture(EdgeGesture.Edge.LEFT,false);g.down(0,0,0,true);g.move(48,0,100);
        assertNull(g.up(47,0,200));
    }
}
