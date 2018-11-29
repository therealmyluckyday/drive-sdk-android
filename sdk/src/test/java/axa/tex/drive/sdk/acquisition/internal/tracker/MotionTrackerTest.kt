package axa.tex.drive.sdk.acquisition.internal.tracker

import axa.tex.drive.sdk.acquisition.internal.tracker.fake.FakeLocationTracker
import axa.tex.drive.sdk.acquisition.internal.tracker.fake.FakeMotionSensor
import org.junit.Assert
import org.junit.Test

class MotionTrackerTest {

    @Test
    fun motionTrackerCorrectlyEnabled() {

        val fakeMotionSensor = FakeMotionSensor()
        val motionTracker = MotionTracker(fakeMotionSensor = fakeMotionSensor)
        motionTracker.enableTracking()
        Assert.assertTrue(fakeMotionSensor.trackingEnabled);
        Assert.assertFalse(fakeMotionSensor.trackingDisable);
    }

    @Test
    fun motionTrackerCorrectlyDisabled() {

        val fakeMotionSensor = FakeMotionSensor()
        val motionTracker = MotionTracker(fakeMotionSensor = fakeMotionSensor)
        motionTracker.enableTracking()
        Assert.assertTrue(fakeMotionSensor.trackingEnabled);
        Assert.assertFalse(fakeMotionSensor.trackingDisable);

        motionTracker.disableTracking()

        Assert.assertFalse(fakeMotionSensor.trackingEnabled);
        Assert.assertTrue(fakeMotionSensor.trackingDisable);
    }
}