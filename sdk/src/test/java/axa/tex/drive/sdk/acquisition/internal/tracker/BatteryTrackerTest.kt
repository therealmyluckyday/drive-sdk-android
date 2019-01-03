package axa.tex.drive.sdk.acquisition.internal.tracker

import axa.tex.drive.sdk.acquisition.internal.tracker.fake.FakeBatterySensor
import axa.tex.drive.sdk.acquisition.model.BatteryFix
import axa.tex.drive.sdk.acquisition.model.BatteryState
import axa.tex.drive.sdk.acquisition.model.Fix
import io.reactivex.Observable
import org.junit.Assert
import org.junit.Test
import java.util.*
import java.util.concurrent.CountDownLatch

class BatteryTrackerTest {
    @Test
    fun batteryTrackerCorrectlyEnabled() {


        val batteryTracker = BatteryTracker(FakeBatterySensor())
        batteryTracker.enableTracking()
        Assert.assertTrue(batteryTracker.isEnabled());
        Assert.assertFalse(!batteryTracker.isEnabled());
    }

    @Test
    fun batteryTrackerCorrectlyDisabled() {

        val batteryTracker = BatteryTracker(FakeBatterySensor())
        batteryTracker.enableTracking()
        Assert.assertTrue(batteryTracker.isEnabled());
        Assert.assertFalse(!batteryTracker.isEnabled());

        batteryTracker.disableTracking()

        Assert.assertFalse(batteryTracker.isEnabled());
        Assert.assertTrue(!batteryTracker.isEnabled());
    }

    @Test
    fun batteryFixProvider() {
        val state = BatteryState.unplugged
        val level = 20
        val time = Date().time
        val signal = CountDownLatch(1)

        val batteryTracker = BatteryTracker(FakeBatterySensor(level, state, time))

        var failed = false
        val batteryFixProducer = batteryTracker.provideFixProducer() as Observable<List<Fix>>
        batteryFixProducer.subscribe { fix ->
            try {
                val batteryFix = (fix as List<Fix>).first() as BatteryFix
                Assert.assertTrue(batteryFix.state == state && batteryFix.level == level && batteryFix.timestamp == time)
                signal.countDown()
            } catch (e: Throwable) {
                e.printStackTrace()
                failed = true
                signal.countDown()
            }

        }



        batteryTracker.enableTracking()
        signal.await()
        Assert.assertTrue(!failed)
    }
}