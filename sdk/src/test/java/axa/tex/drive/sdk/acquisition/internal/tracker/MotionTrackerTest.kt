package axa.tex.drive.sdk.acquisition.internal.tracker

import axa.tex.drive.sdk.acquisition.internal.tracker.fake.FakeMotionSensor
import org.junit.Assert
import org.junit.Test

class MotionTrackerTest {

    @Test
    fun motionTrackerCorrectlyEnabled() {

        val motionSensor = FakeMotionSensor()
        val motionTracker = MotionTracker(FakeMotionSensor())
        motionTracker.enableTracking()
        Assert.assertTrue(motionTracker.isEnabled());
        Assert.assertFalse(!motionTracker.isEnabled());
    }

    @Test
    fun motionTrackerCorrectlyDisabled() {


        val motionTracker = MotionTracker(FakeMotionSensor())
        motionTracker.enableTracking()
        Assert.assertTrue(motionTracker.isEnabled());
        Assert.assertFalse(!motionTracker.isEnabled());

        motionTracker.disableTracking()

        Assert.assertFalse(motionTracker.isEnabled());
        Assert.assertTrue(!motionTracker.isEnabled());
    }
}