package axa.tex.drive.sdk


import android.content.Context
import android.location.Location
import android.os.SystemClock
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import axa.tex.drive.sdk.acquisition.SensorService
import axa.tex.drive.sdk.acquisition.SensorServiceFake
import axa.tex.drive.sdk.acquisition.TripRecorder
import axa.tex.drive.sdk.acquisition.model.Fix
import axa.tex.drive.sdk.core.Platform
import axa.tex.drive.sdk.core.TexConfig
import axa.tex.drive.sdk.core.TexService
import axa.tex.drive.sdk.core.logger.LogType
import axa.tex.drive.sdk.core.tools.FileManager
import io.reactivex.schedulers.Schedulers
import junit.framework.Assert.*
import org.junit.Test
import java.io.*
import java.util.*


//@RunWith(AndroidJUnit4ClassRunner::class)
class TextInstrumented2Test  {

    private val tripLogFileName = "trip_location_test.csv"
    var sensorService: SensorServiceFake? = null
    var tripRecorder: TripRecorder? = null
    private var lastLocation: Fix? = null
    val rxScheduler = Schedulers.io()
    val context = ApplicationProvider.getApplicationContext<Context>()

    var service: TexService? = null
    private var config: TexConfig? = null
    @Test
    @LargeTest
    fun testTexServiceInitialization() {

        val context = ApplicationProvider.getApplicationContext<Context>()
        sensorService = SensorServiceFake(context, rxScheduler)
        assertNotNull(sensorService)

        config = TexConfig.Builder(context, "APP-TEST", "22910000",sensorService!!, rxScheduler ).enableTrackers().platformHost(Platform.PRODUCTION).build()
        assertNotNull(config)

        service = TexService.configure(config!!)
        assertNotNull(service)

        service!!.logStream().subscribeOn(rxScheduler).subscribe({ it ->
            assert(it.type!= LogType.ERROR)
        })

        val tripId =  service!!.getTripRecorder().startTrip(Date().time)
        assert(tripId != null)
        SystemClock.sleep(1000)
        val endTripTime = loadTrip(sensorService!!)
        SystemClock.sleep(7000)
        service!!.getTripRecorder().stopTrip(endTripTime)
        SystemClock.sleep(7000)
    }

    fun loadTrip(sensorService: SensorServiceFake): Long {
        val inputStream: InputStream = InstrumentationRegistry.getInstrumentation().getContext().getAssets().open(tripLogFileName)
        assert(inputStream!=null)
        val noTime: Long = 0
        var newTime: Long = noTime
        if (inputStream !== null ) {
            Thread {
                try {
                    val bufferedReader = inputStream.bufferedReader()
                    var lineIndex = 0
                    var line = bufferedReader.readLine()
                    newTime = System.currentTimeMillis() - 86400000
                    assertNotNull(line)
                    while (line != null) {
                        newTime = sendLocationLineStringToSpeedFilter(line, newTime, sensorService)
                        line = bufferedReader.readLine()
                        lineIndex++
                    }
                    bufferedReader.close()
                } catch (e: IOException) {
                    fail("Fail : "+e);
                }
            }.start()
        } else {
            fail("File not found")
        }
        assertFalse(newTime == noTime)
        return newTime
    }
    private fun sendLocationLineStringToSpeedFilter(line: String, time: Long, sensorService: SensorServiceFake) : Long {
        val locationDetails = line.split(",")
        var newLocation = Location("")
        val latitude = locationDetails[0].toDouble()
        newLocation.latitude = latitude
        val longitude = locationDetails[1].toDouble()
        newLocation.longitude = longitude
        val accuracy = locationDetails[2].toFloat()
        newLocation.accuracy = accuracy
        val speed = locationDetails[3].toFloat()
        newLocation.speed = speed // 20.F
        val bearing = locationDetails[4].toFloat()
        newLocation.bearing = bearing
        val altitude = locationDetails[5].toDouble()
        newLocation.altitude = altitude
        val delay = locationDetails[6].toLong()
        newLocation.time = 1 + time
        sensorService.forceLocationChanged(newLocation)
        return newLocation.time
    }

}