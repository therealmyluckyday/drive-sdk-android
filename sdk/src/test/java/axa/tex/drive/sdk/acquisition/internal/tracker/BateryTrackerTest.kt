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

        val fakeBatterySensor = FakeBatterySensor()
        val batteryTracker = BatteryTracker(fakeBatterySensor)
        batteryTracker.enableTracking()
        Assert.assertTrue(fakeBatterySensor.trackingEnabled);
        Assert.assertFalse(fakeBatterySensor.trackingDisable);
    }

    @Test
    fun batteryTrackerCorrectlyDisabled() {

        val fakeBatterySensor = FakeBatterySensor()
        val batteryTracker = BatteryTracker(fakeBatterySensor)
        batteryTracker.enableTracking()
        Assert.assertTrue(fakeBatterySensor.trackingEnabled);
        Assert.assertFalse(fakeBatterySensor.trackingDisable);

        batteryTracker.disableTracking()

        Assert.assertFalse(fakeBatterySensor.trackingEnabled);
        Assert.assertTrue(fakeBatterySensor.trackingDisable);
    }

    @Test
    fun batteryFixProvider(){
        val state = BatteryState.plugged
        val level = 20
        val time = Date().time
        val signal = CountDownLatch(1)
        val fakeBatterySensor = FakeBatterySensor()
        fakeBatterySensor.batteryState = state
        fakeBatterySensor.batteryLevel = level
        fakeBatterySensor.timestamp = time
        val batteryTracker = BatteryTracker(fakeBatterySensor)

        var failed = false
        val batteryFixProducer = batteryTracker.provideFixProducer() as Observable<Fix>
        batteryFixProducer.subscribe { fix ->
            try {
                val batteryFix = (fix as BatteryFix)
                Assert.assertTrue(batteryFix.state == state  && batteryFix.level == level && batteryFix.timestamp == time)
                signal.countDown()
            }catch (e : Throwable){
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