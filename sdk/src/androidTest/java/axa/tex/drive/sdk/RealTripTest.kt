package axa.tex.drive.sdk


import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.filters.LargeTest
import axa.tex.drive.sdk.acquisition.SensorServiceFake
import axa.tex.drive.sdk.acquisition.score.ScoreV1
import axa.tex.drive.sdk.acquisition.score.model.ScoreStatus
import axa.tex.drive.sdk.core.Platform
import axa.tex.drive.sdk.core.TexConfig
import axa.tex.drive.sdk.core.TexService
import axa.tex.drive.sdk.core.logger.LogType
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.junit.After
import org.junit.Assert
import org.junit.Assert.*
import org.junit.Test
import org.koin.core.context.stopKoin
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


class RealTripTest  {
    private val tripLogFileName = "trip_location_test.csv"
    val rxScheduler = Schedulers.io() //Schedulers.single() Schedulers.trampoline() io Schedulers.newThread()


    @After
    fun teardown() {
        stopKoin()
    }

    @Test
    @LargeTest
    fun testTexServiceInitializationAPIV1() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sensorService = SensorServiceFake(context, rxScheduler)
        assertNotNull(sensorService)
        val doneSignal = CountDownLatch(930) // Number of GPS point used for the trip
        val scoreSignal = CountDownLatch(1)
        val isAPIV2 = false
        val appName = "APP-TEST"//"youdrive_france_prospect"
        var config = TexConfig.Builder(context, appName, "22910000",sensorService, rxScheduler, isAPIV2 = isAPIV2).enableTrackers().platformHost(Platform.PRODUCTION).build()
        TexConfig.config!!.isRetrievingScoreAutomatically = false
        assertNotNull(config)

        var service = TexService.configure(config)
        assertNotNull(service)

        service.logStream().subscribeOn(AndroidSchedulers.mainThread()).subscribe({ it ->
            println(it.toString())
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
                val scoreV1 = score.score as ScoreV1
                assertNotNull("TripId null: ", scoreV1.tripId)
                assert(scoreV1.tripId!!.value == tripId.value)
                scoreSignal.countDown()
            }
        }, {throwable ->
            assertFalse("Exception: "+throwable,true)
            scoreSignal.countDown()
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

        scoreRetriever!!.getAvailableScoreListener()?.subscribe({
            assertNotNull(it)
            assert(it!! == tripId!!.value)
            it?.let { score ->
                scoreRetriever?.retrieveScore(it, appName, Platform.PRODUCTION.generateUrl(isAPIV2), true, isAPIV2, delay = 12)
            }
        })

        val endTripTime = sensorService!!.loadTrip(context, 100L)
       // val endTripTime = loadTrip(sensorService!!, timeStart)// 57 600 000 = 16 Hour  86400000 = 24 Hour
        println("-doneSignal await")
        doneSignal.await()
        println("-Thread sleep 12s")
        Thread.sleep(1000)  // 93 sec
        println("-stop trip")
        service!!.getTripRecorder().stopTrip(System.currentTimeMillis()- (86400000-3600000))
        println("-Thread sleep 1s")
        Thread.sleep(1000)
        println("-scoreSignal await")
        scoreSignal.await()
    }

    @Test
    @LargeTest
    fun testTexServiceInitializationAPIV2() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sensorService = SensorServiceFake(context, rxScheduler)
        assertNotNull(sensorService)
        val doneSignal = CountDownLatch(930) // Number of GPS point used for the trip
        val scoreSignal = CountDownLatch(1)
        val isAPIV2 = true
        val appName = "youdrive_france_prospect"//"youdrive_france_prospect"
        var config = TexConfig.Builder(context, appName, "22910000",sensorService, rxScheduler, isAPIV2 = isAPIV2).enableTrackers().platformHost(Platform.TESTING).build()
        TexConfig.config!!.isRetrievingScoreAutomatically = false
        assertNotNull(config)

        var service = TexService.configure(config)
        assertNotNull(service)

        service.logStream().subscribeOn(AndroidSchedulers.mainThread()).subscribe({ it ->
            println(it.toString())
            assert(it.type!= LogType.ERROR)
        })
        val timeStart = System.currentTimeMillis() - 86400000// 50000000
        val tripId =  service!!.getTripRecorder().startTrip(timeStart)
        assertNotNull(tripId)
        assertNotNull(tripId!!.value)

        val scoreRetriever = service!!.scoreRetriever()
        scoreRetriever!!.getScoreListener()!!.subscribe ( {
            println("-SCORE RETRIEVED")
            assertNotNull(it)
            it?.let { score ->
                assertNull("Error in Score: "+score.scoreError, score.scoreError)
                assertNotNull("Score null: ", score.score)
                println("-SCORE is found "+score.score!!.status)
                Assert.assertEquals(ScoreStatus.found, score.score!!.status)
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

        scoreRetriever!!.getAvailableScoreListener()?.subscribe({
            println("-AVAILAIBLE Score Listener Trip ID : "+it)
            assertNotNull(it)
            println("-")
            println("-")
            assertEquals(tripId!!.value, it!!)
            it?.let { score ->
                println("-retrieveScore : "+it)
                scoreRetriever?.retrieveScore(it, appName, Platform.TESTING.generateUrl(isAPIV2), true, isAPIV2, delay = 12)
            }
        })

        val endTripTime = sensorService!!.loadTrip(context, 100L)
        // val endTripTime = loadTrip(sensorService!!, timeStart)// 57 600 000 = 16 Hour  86400000 = 24 Hour
        println("-doneSignal await")
        doneSignal.await()
        println("-Thread sleep 12s")
        Thread.sleep(1000)  // 93 sec
        println("-stop trip")
        service!!.getTripRecorder().stopTrip(System.currentTimeMillis()- (86400000-3600000))
        println("-Thread sleep 1s")
        Thread.sleep(1000)
        println("-scoreSignal await")
        scoreSignal.await(150, TimeUnit.SECONDS)
        println("scoreSignal.done")
    }
}