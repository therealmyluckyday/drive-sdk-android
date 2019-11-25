package axa.tex.drive.sdk.acquisition.internal.tracker


import axa.tex.drive.sdk.acquisition.internal.tracker.fake.FakeLocationSensor
import axa.tex.drive.sdk.acquisition.model.Fix
import axa.tex.drive.sdk.acquisition.model.LocationFix
import io.reactivex.Observable
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.CountDownLatch

class LocationTrackerTest {

    @Test
    fun locationTrackerCorrectlyEnabled() {
        val locationTracker = LocationTracker(FakeLocationSensor())
        Assert.assertFalse(locationTracker.isEnabled())
        locationTracker.enableTracking()
        Assert.assertTrue(locationTracker.isEnabled())
    }

    @Test
    fun locationTrackerCorrectlyDisable() {
        val locationTracker = LocationTracker(FakeLocationSensor())
        locationTracker.enableTracking()
        Assert.assertTrue(locationTracker.isEnabled())
        locationTracker.disableTracking()
        Assert.assertFalse(locationTracker.isEnabled())
    }


    @Test
    fun locationTrackerCorrectlyProvidesFixes() {
        val locationTracker = LocationTracker(FakeLocationSensor())
        locationTracker.enableTracking()
        val signal = CountDownLatch(1)
        var isCalled= false
        val locationProducer = locationTracker.provideFixProducer()
        locationProducer.subscribe { fixes ->
            try {
                val fix = fixes.first()
                Assert.assertTrue("fix is LocationFix", fix is LocationFix)
                val locationFix = (fix as LocationFix)
                print(fix)
                Assert.assertTrue("latitude : ${locationFix.latitude}",locationFix.latitude.toString() == "12.0")
                Assert.assertTrue("longitude : ${locationFix.longitude}",locationFix.latitude.toString() == "1.88282")
                isCalled = true
                signal.countDown()
            } catch (e: Throwable) {
                isCalled = true
                signal.countDown()
                Assert.assertNull(e.printStackTrace().toString(), e)
            }

        }
        locationTracker.enableTracking()
        signal.await()
        Assert.assertTrue("isCalled", isCalled)
    }
}