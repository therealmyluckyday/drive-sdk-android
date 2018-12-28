package axa.tex.drive.sdk


import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import axa.tex.drive.sdk.acquisition.TripRecorder
import axa.tex.drive.sdk.acquisition.internal.tracker.DEFAULT_MOTION_AGE_AFTER_ACCELERATION
import axa.tex.drive.sdk.acquisition.internal.tracker.DEFAULT_OLDER_MOTION_AGE
import axa.tex.drive.sdk.acquisition.model.Fix
import axa.tex.drive.sdk.acquisition.model.TexUser
import axa.tex.drive.sdk.core.TexConfig
import axa.tex.drive.sdk.core.TexService
import io.reactivex.Observable
import org.junit.Assert

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Before
import org.koin.test.KoinTest


@RunWith(AndroidJUnit4::class)
class TextInstrumentedTest : KoinTest {

    var tripRecorder: TripRecorder? = null;
    private var lastLocation: Fix? = null;

    @Before
    fun beforeTest() {
        val appContext = InstrumentationRegistry.getTargetContext()
        val user = TexUser("appId", "FFFDIHOVA3131IJA1")
        val config: TexConfig = TexConfig.Builder(user, appContext).enableBatteryTracker().enableLocationTracker().enableMotionTracker().build(appContext);
        tripRecorder = TexService.configure(config)?.getTripRecorder();
        tripRecorder?.locationObservable()?.subscribe { fix -> lastLocation = fix }
        tripRecorder?.track()

        Thread.sleep(5000)
    }


    @Test
    fun testPackage() {
        val appContext = InstrumentationRegistry.getTargetContext()
        Assert.assertEquals("axa.tex.drive.sdk.test", appContext.packageName)
    }

    @Test
    fun testLastLocation() {
        Assert.assertTrue(lastLocation != null)
    }


    @Test
    fun testIsRecording() {
        Assert.assertTrue(tripRecorder?.isRecording() == true)
        tripRecorder?.stopTracking();
        Thread.sleep(1000)
        Assert.assertTrue(tripRecorder?.isRecording() == false)
        tripRecorder?.track();
    }


    private fun testMotionBuffer(fixes: List<Fix>): Boolean {
        Assert.assertFalse(fixes.isEmpty())
        Assert.assertTrue((fixes.first().timestamp() - fixes.last().timestamp()) <= DEFAULT_OLDER_MOTION_AGE + DEFAULT_MOTION_AGE_AFTER_ACCELERATION)
        return false;
    }

    @Test
    fun testMotionTracking() {
        val appContext = InstrumentationRegistry.getTargetContext()
        val motionTracker = MotionTracker(appContext, true)
        val fixData = motionTracker.provideFixProducer() as Observable<Any>
        fixData.subscribe { fixes ->
            Assert.assertFalse((fixes as List<Fix>).isEmpty());
            Assert.assertTrue((fixes as List<Fix>).first().timestamp() - (fixes as List<Fix>).first().timestamp() <=
                    DEFAULT_OLDER_MOTION_AGE + DEFAULT_MOTION_AGE_AFTER_ACCELERATION)
        }
    }
}