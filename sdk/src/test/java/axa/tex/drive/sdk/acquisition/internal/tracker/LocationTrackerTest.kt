package axa.tex.drive.sdk.acquisition.internal.tracker


import axa.tex.drive.sdk.acquisition.internal.tracker.LocationTracker
import axa.tex.drive.sdk.acquisition.internal.tracker.fake.FakeLocationTracker
import axa.tex.drive.sdk.acquisition.model.Fix
import axa.tex.drive.sdk.acquisition.model.LocationFix
import io.reactivex.Observable
import org.junit.Assert
import org.junit.Test

import java.util.concurrent.CountDownLatch

class LocationTrackerTest {

    @Test
    fun locationTrackerCorrectlyEnabled() {
        val locationTracker : LocationTracker = LocationTracker(context = null,fakeLocationTracker = FakeLocationTracker(false))
        Assert.assertFalse(locationTracker.isEnabled());
        locationTracker.enableTracking()
        Assert.assertTrue(locationTracker.isEnabled());
    }

    @Test
    fun locationTrackerCorrectlyDisable() {
        val locationTracker : LocationTracker = LocationTracker(isEnabled = true,context = null,fakeLocationTracker = FakeLocationTracker(false))
        Assert.assertTrue(locationTracker.isEnabled());
        locationTracker.disableTracking()
        Assert.assertFalse(locationTracker.isEnabled());
    }


    @Test
    fun locationTrackerCorrectlyProvidesFixes() {
        val signal = CountDownLatch(1)
        val fakeLocationTracker = FakeLocationTracker(false);
        val locationTracker : LocationTracker = LocationTracker(context = null,fakeLocationTracker = fakeLocationTracker)
        locationTracker.enableTracking()
        var failed = false
        val locationProducer = locationTracker?.provideFixProducer() as Observable<Fix>
        locationProducer.subscribe { fix ->
           try {
               val locationFix = (fix as LocationFix)
               Assert.assertTrue(locationFix.latitude==12.0  && locationFix.longitude==1.88282)
               signal.countDown()
           }catch (e : Throwable){
               e.printStackTrace()
               failed = true
               signal.countDown()
           }

        }
        fakeLocationTracker.enableTracking()
        signal.await()
        Assert.assertTrue(!failed)
    }
}