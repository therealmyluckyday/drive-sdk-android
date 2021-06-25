package com.axa.tex.drive.demo
import android.location.Location
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import axa.tex.drive.sdk.acquisition.SensorServiceFake
import axa.tex.drive.sdk.acquisition.TripRecorder
import axa.tex.drive.sdk.acquisition.model.Fix
import axa.tex.drive.sdk.acquisition.model.TexUser
import axa.tex.drive.sdk.core.TexConfig
import axa.tex.drive.sdk.core.TexService
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import java.util.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

@RunWith(AndroidJUnit4::class)
@MediumTest
class ExampleInstrumentedTest {

    var tripRecorder: TripRecorder? = null;
    private var lastLocation: Fix? = null;

    @Before
    fun beforeTest() {
        val appContext = InstrumentationRegistry.getInstrumentation().getTargetContext()
        val user = TexUser("appId", "FFFDIHOVA3131IJA1")
        val config: TexConfig = TexConfig.Builder(user, appContext, "clientToto", isAPIV2 = false).enableBatteryTracker().enableLocationTracker().enableMotionTracker().build()
        tripRecorder = TexService.configure(config)?.getTripRecorder()
        tripRecorder?.locationObservable()?.subscribe { fix -> lastLocation = fix }
        tripRecorder?.startTrip(Date().time)

        Thread.sleep(5000)
    }


    @Test
    fun testPackage() {
        val appContext = InstrumentationRegistry.getInstrumentation().getTargetContext()
        Assert.assertEqual("axa.tex.drive.demo", appContext.packageName)
    }

    @Test
    fun testLastLocation() {
        assertTrue(lastLocation != null)
    }


    @Test
    fun testIsRecording() {
        assertTrue(tripRecorder?.isRecording() == true)
        tripRecorder?.stopTrip(Date().time)
        Thread.sleep(1000)
        assertTrue(tripRecorder?.isRecording() == false)
        tripRecorder?.startTrip(Date().time)
    }
}
