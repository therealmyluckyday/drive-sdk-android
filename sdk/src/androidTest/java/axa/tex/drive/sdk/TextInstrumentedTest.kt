package axa.tex.drive.sdk


import androidx.test.InstrumentationRegistry.getTargetContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import axa.tex.drive.sdk.acquisition.TripRecorder
import axa.tex.drive.sdk.acquisition.model.Fix
import axa.tex.drive.sdk.acquisition.model.TexUser
import axa.tex.drive.sdk.core.TexConfig
import axa.tex.drive.sdk.core.TexService
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import java.util.*


@RunWith(AndroidJUnit4::class)
class TextInstrumentedTest : KoinTest {

    var tripRecorder: TripRecorder? = null
    private var lastLocation: Fix? = null

    @Before
    fun beforeTest() {
        val appContext = getTargetContext()
        val user = TexUser("appId", "FFFDIHOVA3131IJA1")
        val config: TexConfig = TexConfig.Builder(user, appContext, "clientIDToto").enableBatteryTracker().enableLocationTracker().enableMotionTracker().build()
        tripRecorder = TexService.configure(config).getTripRecorder()
        tripRecorder?.locationObservable()?.subscribe { fix -> lastLocation = fix }
        tripRecorder?.startTrip(Date().time)

        Thread.sleep(5000)
    }


    @Test
    fun testPackage() {
        val appContext = getTargetContext()
        Assert.assertEquals("axa.tex.drive.sdk.test", appContext.packageName)
    }

    @Test
    fun testLastLocation() {
        Assert.assertTrue(lastLocation != null)
    }


    @Test
    fun testIsRecording() {
        Assert.assertTrue(tripRecorder?.isRecording() == true)
        tripRecorder?.stopTrip(Date().time)
        Thread.sleep(1000)
        Assert.assertTrue(tripRecorder?.isRecording() == false)
        tripRecorder?.startTrip(Date().time)
    }

}