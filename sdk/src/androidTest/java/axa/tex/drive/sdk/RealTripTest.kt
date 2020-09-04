package axa.tex.drive.sdk


import android.content.Context
import android.location.Location
import io.reactivex.android.schedulers.AndroidSchedulers;
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import axa.tex.drive.sdk.acquisition.SensorServiceFake
import axa.tex.drive.sdk.core.Platform
import axa.tex.drive.sdk.core.TexConfig
import axa.tex.drive.sdk.core.TexService
import axa.tex.drive.sdk.core.logger.LogType
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.koin.core.context.stopKoin
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.CountDownLatch


class RealTripTest  {
    private val tripLogFileName = "trip_location_test.csv"
    val rxScheduler = Schedulers.io() //Schedulers.single() Schedulers.trampoline() io Schedulers.newThread()


    @After
    fun teardown() {
        stopKoin()
    }

    @Test
    @LargeTest
    fun testTexServiceInitialization() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sensorService = SensorServiceFake(context, rxScheduler)
        assertNotNull(sensorService)
        val doneSignal = CountDownLatch(935) // Number of GPS point used for the trip
        val scoreSignal = CountDownLatch(1)

        val appName = "APP-TEST"
        var config = TexConfig.Builder(context, appName, "22910000",sensorService, rxScheduler).enableTrackers().platformHost(Platform.PRODUCTION).build()
        TexConfig.config!!.isRetrievingScoreAutomatically = false
        assertNotNull(config)

        var service = TexService.configure(config)
        assertNotNull(service)

        service.logStream().subscribeOn(AndroidSchedulers.mainThread()).subscribe({ it ->
            println(it.description)
            assert(it.type!= LogType.ERROR)
        })
        val timeStart = System.currentTimeMillis() - 86400000// 50000000
        val tripId =  service!!.getTripRecorder().startTrip(timeStart)
        assertNotNull(tripId)
        assertNotNull(tripId!!.value)

        val scoreRetriever = service!!.scoreRetriever()
        scoreRetriever!!.getScoreListener()!!.subscribe ( {
            assertNotNull(it)
            it?.let { score ->
                assertNull("Error in Score: "+score.scoreError, score.scoreError)
                assertNotNull("Score null: ", score.score)
                assertNotNull("TripId null: ", score.score!!.tripId)
                assert(score.score!!.tripId!!.value == tripId.value)
                scoreSignal.countDown()
            }
        }, {throwable ->
            assertFalse("Exception: "+throwable,true)
        })

        service!!.getTripRecorder().tripProgress()?.subscribe({
            assertNotNull(it)
            assert(it!!.currentTripId.value == tripId!!.value)
            doneSignal.countDown()
        })

        service!!.getTripRecorder().endedTripListener()?.subscribe ( {
            assertNotNull(it)
            assert(it!! == tripId!!.value)
        }, {throwable ->
            assertFalse("Exception: "+throwable,true)
        })

        scoreRetriever?.getAvailableScoreListener()?.subscribe({
            assertNotNull(it)
            assert(it!! == tripId!!.value)
            it?.let { score ->
                scoreRetriever?.retrieveScore(it, appName, Platform.PRODUCTION, true, delay = 12)
            }
        })

        val endTripTime = sensorService!!.loadTrip(context, 1L)
       // val endTripTime = loadTrip(sensorService!!, timeStart)// 57 600 000 = 16 Hour  86400000 = 24 Hour
        doneSignal.await()
        Thread.sleep(1000)
        service!!.getTripRecorder().stopTrip(System.currentTimeMillis()- (86400000-3600000))

        Thread.sleep(1000)
        scoreSignal.await()
    }
}