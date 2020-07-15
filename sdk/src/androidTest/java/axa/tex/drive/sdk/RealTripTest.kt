package axa.tex.drive.sdk


import android.content.Context
import android.location.Location
import android.os.Build.VERSION_CODES.N
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.os.SystemClock.*
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import axa.tex.drive.sdk.acquisition.SensorServiceFake
import axa.tex.drive.sdk.automode.internal.tracker.model.TexLocation
import axa.tex.drive.sdk.core.CertificateAuthority
import axa.tex.drive.sdk.core.Platform
import axa.tex.drive.sdk.core.TexConfig
import axa.tex.drive.sdk.core.TexService
import axa.tex.drive.sdk.core.logger.LogType
import io.reactivex.schedulers.Schedulers
import junit.framework.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.stopKoin
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.concurrent.CountDownLatch


class RealTripTest  {
    private val tripLogFileName = "trip_location_test.csv"
    var sensorService: SensorServiceFake? = null
    val rxScheduler = Schedulers.io() //Schedulers.single() Schedulers.trampoline() io Schedulers.newThread()
    var service: TexService? = null
    private var config: TexConfig? = null

    @Before
    fun setup() {

    }

    @After
    fun teardown() {
        stopKoin()
    }

    @Test
    @LargeTest
    fun testTexServiceInitialization() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        sensorService = SensorServiceFake(context, rxScheduler)
        assertNotNull(sensorService)

        val doneSignal = CountDownLatch(700)
        val scoreSignal = CountDownLatch(1)

        val appName = "APP-TEST"
        config = TexConfig.Builder(context, appName, "22910000",sensorService!!, rxScheduler).enableTrackers().platformHost(Platform.PRODUCTION).build()
        TexConfig.config!!.isRetrievingScoreAutomatically = false
        assertNotNull(config)

        service = TexService.configure(config!!)
        assertNotNull(service)

        service!!.logStream().subscribeOn(rxScheduler).subscribe({ it ->
            //println(it.description)
            assert(it.type!= LogType.ERROR)
        })

        val scoreRetriever = service!!.scoreRetriever()
        scoreRetriever!!.getScoreListener()!!.subscribe ( {
            println("ScoreWorker result" )
            it?.let { score ->
                println("ScoreWorker result" + score)
                scoreSignal.countDown()
            }
        }, {throwable ->
            print(throwable)
        })

        scoreRetriever?.getAvailableScoreListener()?.subscribe({
            it?.let { score ->
                scoreRetriever?.retrieveScore(it, appName, Platform.PRODUCTION, true, delay = 12)
            }
        })

        service!!.getTripRecorder().endedTripListener()?.subscribe ( {
            println(it)
        }, {throwable ->
            println(throwable)
        })
        service!!.getTripRecorder().tripProgress()?.subscribe({
           println(doneSignal.count)
            doneSignal.countDown()
        })

        println(Thread.currentThread().getName()+": startTrip")
        val tripId =  service!!.getTripRecorder().startTrip(Date().time)
        assert(tripId != null)

       //print("activateAutomode")
       //val autoModeHandler = service!!.automodeHandler()
       //autoModeHandler.activateAutomode(context,false, isSimulatedDriving = false)

        val endTripTime = loadTrip(sensorService!!)

        println(Thread.currentThread().getName()+": await")
        doneSignal.await()

        // 6 seconds trip too short
       // sleep(6000)

        println(Thread.currentThread().getName()+": stopTrip")
        service!!.getTripRecorder().stopTrip(endTripTime)
        scoreSignal.await()
    }

    fun loadTrip(sensorService: SensorServiceFake): Long {
        println(Thread.currentThread().getName()+": loadTrip")
        val inputStream: InputStream = InstrumentationRegistry.getInstrumentation().getContext().getAssets().open(tripLogFileName)
        assert(inputStream!=null)
        val noTime: Long = 0
        var newTime: Long = noTime
        if (inputStream !== null ) {
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
        } else {
            fail("File not found")
        }
        assertFalse(newTime == noTime)
        return newTime
    }
    private fun sendLocationLineStringToSpeedFilter(line: String, time: Long, sensorService: SensorServiceFake) : Long {
        //println(Thread.currentThread().getName()+": sendLocationLineStringToSpeedFilter: "+line)
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