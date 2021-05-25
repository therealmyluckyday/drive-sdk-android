package axa.tex.drive.sdk.acquisition.internal.tracker


import axa.tex.drive.sdk.acquisition.internal.tracker.fake.FakeLocationSensor
import axa.tex.drive.sdk.acquisition.model.LocationFix
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
        locationProducer.doOnError {
            println(it.printStackTrace())
            signal.countDown()
        }
        .subscribe ({ fixes ->
            try {
                val fix = fixes.first()
                Assert.assertTrue("fix is LocationFix", fix is LocationFix)
                val locationFix = (fix as LocationFix)
                Assert.assertTrue("latitude : ${locationFix.latitude}",locationFix.latitude.toString() == "12.0")
                Assert.assertTrue("longitude : |${locationFix.longitude}|1.88282|",locationFix.longitude.toString() == "1.88282")
                isCalled = true
                signal.countDown()
            } catch (e: Throwable) {
                signal.countDown()
                print(e.printStackTrace())
            }

        }, {throwable ->
            print(throwable)
            Assert.assertNull(throwable.printStackTrace().toString(), throwable)
            signal.countDown()
        })
        locationTracker.enableTracking()
        signal.await()
        Assert.assertTrue("isCalled", isCalled)
    }
}