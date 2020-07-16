package axa.tex.drive.sdk

import android.content.Context
import android.location.Location
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import axa.tex.drive.sdk.acquisition.SensorServiceFake
import axa.tex.drive.sdk.core.Platform
import axa.tex.drive.sdk.core.TexConfig
import axa.tex.drive.sdk.core.TexService
import axa.tex.drive.sdk.core.logger.LogType
import com.google.android.gms.location.DetectedActivity
import io.reactivex.schedulers.Schedulers
import junit.framework.Assert
import org.junit.After
import org.junit.Test
import org.koin.core.context.stopKoin
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.concurrent.CountDownLatch

class AutomodeIntegratedTest {
    private val tripLogFileName = "trip_location_test.csv"
    var sensorService: SensorServiceFake? = null
    val rxScheduler = Schedulers.io() //Schedulers.single() Schedulers.trampoline() io Schedulers.newThread()
    var service: TexService? = null
    private var config: TexConfig? = null

    @After
    fun teardown() {
        stopKoin()
    }

    @Test
    @LargeTest
    fun testTexServiceInitialization() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sensorServiceFake = SensorServiceFake(context, rxScheduler)
        sensorService = sensorServiceFake
        Assert.assertNotNull(sensorService)

        val doneSignal = CountDownLatch(934) // Number of GPS point used for the trip
        val scoreSignal = CountDownLatch(1)
        val appName = "APP-TEST"
        config = TexConfig.Builder(context, appName, "22910000",sensorService!!, rxScheduler).enableTrackers().platformHost(Platform.PRODUCTION).build()
        TexConfig.config!!.isRetrievingScoreAutomatically = false
        Assert.assertNotNull(config)

        service = TexService.configure(config!!)
        Assert.assertNotNull(service)

        service!!.logStream().subscribeOn(rxScheduler).subscribe({ it ->
            assert(it.type!= LogType.ERROR)
        })

        var endTripTime = Date().time
        val autoModeHandler = service!!.automodeHandler()
        autoModeHandler.activateAutomode(context,false, isSimulatedDriving = false)
        val timeStart = System.currentTimeMillis() - 50000000
        autoModeHandler?.state?.subscribe( {driving ->
            if(driving){
                println("-DRIVING START TRIP : ")
                val tripId =  service!!.getTripRecorder().startTrip(timeStart)
            }else{
                println("-DRIVING STOP TRIP : ")
                service!!.getTripRecorder().stopTrip(endTripTime)
            }
        }, {throwable ->
            print(throwable)
        })

        val confidence = 100
        sensorServiceFake.speedFilter().activityStream.onNext(DetectedActivity(DetectedActivity.IN_VEHICLE, confidence))

        val scoreRetriever = service!!.scoreRetriever()
        scoreRetriever!!.getScoreListener()!!.subscribe ( {
            Assert.assertNotNull(it)
            it?.let { score ->
                Assert.assertNull("Error in Score: " + score.scoreError, score.scoreError)
                Assert.assertNotNull("Score null: ", score.score)
                Assert.assertNotNull("TripId null: ", score.score!!.tripId)
                scoreSignal.countDown()
            }
        }, {throwable ->
            Assert.assertFalse("Exception: " + throwable, true)
        })

        service!!.getTripRecorder().tripProgress()?.subscribe({
            Assert.assertNotNull(it)
            doneSignal.countDown()
        })

        service!!.getTripRecorder().endedTripListener()?.subscribe ( {
            Assert.assertNotNull(it)
            println("-End Trip Listener Trip ID : "+it)
        }, {throwable ->
            Assert.assertFalse("Exception: " + throwable, true)
        })

        scoreRetriever?.getAvailableScoreListener()?.subscribe({
            Assert.assertNotNull(it)
            println("-AVAILAIBLE Score Listener Trip ID : "+it)
            it?.let { score ->
                scoreRetriever?.retrieveScore(it, appName, Platform.PRODUCTION, true, delay = 12)
            }
        })

        sensorServiceFake.speedFilter().activityStream.onNext(DetectedActivity(DetectedActivity.IN_VEHICLE, confidence))
        Thread.sleep(1000)

        sensorServiceFake.infosMethodCalled.subscribeOn(rxScheduler).subscribe( {
            println("-SUBSCRIBE "+ it)
            if (it == "activelyScanSpeed") {
                println("-LOAD TRIP")
                endTripTime = loadTrip(sensorService!!, timeStart)// 57 600 000 = 16 Hour  86400000 = 24 Hour
            }
        })

        sensorServiceFake.speedFilter().activityStream.onNext(DetectedActivity(DetectedActivity.IN_VEHICLE, confidence))
        Thread.sleep(1000)
         //doneSignal.await()

        scoreSignal.await()
    }

    fun loadTrip(sensorService: SensorServiceFake, timeStart: Long): Long {
        val inputStream: InputStream = InstrumentationRegistry.getInstrumentation().getContext().getAssets().open(tripLogFileName)
        assert(inputStream!=null)
        val noTime: Long = 0
        var newTime: Long = noTime

        if (inputStream !== null ) {
            try {
                val bufferedReader = inputStream.bufferedReader()
                var lineIndex = 0
                var line = bufferedReader.readLine()
                newTime = timeStart
                Assert.assertNotNull(line)
                while (line != null) {
                    newTime = sendLocationLineStringToSpeedFilter(line, newTime, sensorService)
                    line = bufferedReader.readLine()
                    lineIndex++
                }
                bufferedReader.close()
            } catch (e: IOException) {
                Assert.fail("Fail : " + e);
            }
        } else {
            Assert.fail("File not found")
        }
        Assert.assertFalse(newTime == noTime)
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
        newLocation.time = delay + time
        sensorService.forceLocationChanged(newLocation)
        return newLocation.time
    }
}