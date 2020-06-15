package axa.tex.drive.sdk


import android.location.Location
import androidx.test.InstrumentationRegistry.getTargetContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import axa.tex.drive.sdk.acquisition.SensorService
import axa.tex.drive.sdk.acquisition.SensorServiceFake
import axa.tex.drive.sdk.acquisition.SensorServiceImpl
import axa.tex.drive.sdk.acquisition.TripRecorder
import axa.tex.drive.sdk.acquisition.model.Fix
import axa.tex.drive.sdk.acquisition.model.TexUser
import axa.tex.drive.sdk.core.Platform
import axa.tex.drive.sdk.core.TexConfig
import axa.tex.drive.sdk.core.TexService
import io.reactivex.schedulers.Schedulers
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import java.util.*


@RunWith(AndroidJUnit4::class)
class TextInstrumentedTest : KoinTest {

    private val tripLogFileName = "trip_location_test.csv"
    var sensorService: SensorServiceFake? = null
    var tripRecorder: TripRecorder? = null
    private var lastLocation: Fix? = null
    val rxScheduler = Schedulers.single()

    @Before
    fun beforeTest() {
        val appContext = InstrumentationRegistry.getInstrumentation().getTargetContext()
        sensorService = SensorServiceFake(appContext, rxScheduler)
        val user = TexUser("appId", "FFFDIHOVA3131IJA1")
        val config: TexConfig = TexConfig.Builder(user, appContext, "clientIDToto",sensorService!!).enableBatteryTracker().enableLocationTracker().enableMotionTracker().build()
        tripRecorder = TexService.configure(config).getTripRecorder()
        tripRecorder?.locationObservable()?.subscribe { fix -> lastLocation = fix }
        tripRecorder?.startTrip(Date().time)

        var newLocation = Location("")
        val latitude = 10.0
        newLocation.latitude = latitude
        val longitude = 10.0
        newLocation.longitude = longitude
        val accuracy = 1.0F
        newLocation.accuracy = accuracy
        val speed = 10.0F
        newLocation.speed = speed // 20.F
        val bearing = 1.0F
        newLocation.bearing = bearing
        val altitude = 10.0
        newLocation.altitude =altitude
        newLocation.time = Date().time
        sensorService?.forceLocationChanged(newLocation)
        //Thread.sleep(1000)
    }


    @Test
    fun testPackage() {
        val appContext = InstrumentationRegistry.getInstrumentation().getTargetContext()
        Assert.assertEquals("axa.tex.drive.sdk.test", appContext.packageName)
    }

    @Test
    fun testLastLocation() {
        Thread.sleep(1000)
        Assert.assertTrue(lastLocation != null)
    }


    @Test
    fun testIsRecording() {
        Assert.assertTrue(tripRecorder!!.isRecording())
        tripRecorder?.stopTrip(Date().time)
        Thread.sleep(100)
        Assert.assertFalse(tripRecorder!!.isRecording())
        tripRecorder?.startTrip(Date().time)
        Thread.sleep(100)
        Assert.assertTrue(tripRecorder!!.isRecording())
    }

}