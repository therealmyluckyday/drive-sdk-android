package axa.tex.drive.sdk.automode


import android.location.Location
import axa.tex.drive.sdk.acquisition.SensorService
import axa.tex.drive.sdk.acquisition.SensorServiceFake
import axa.tex.drive.sdk.automode.internal.Automode
import axa.tex.drive.sdk.automode.internal.states.DrivingState
import axa.tex.drive.sdk.automode.internal.states.IdleState
import axa.tex.drive.sdk.automode.internal.states.InVehicleState
import axa.tex.drive.sdk.automode.internal.states.TrackingState
import axa.tex.drive.sdk.automode.internal.tracker.LOCATION_ACCURACY_THRESHOLD
import axa.tex.drive.sdk.automode.internal.tracker.SPEED_MOVEMENT_THRESHOLD
import axa.tex.drive.sdk.automode.internal.tracker.SpeedFilter
import axa.tex.drive.sdk.core.CertificateAuthority.Companion.LOGGER
import axa.tex.drive.sdk.core.logger.LogType
import axa.tex.drive.sdk.core.logger.LoggerFactory
import com.google.android.gms.location.DetectedActivity
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.inject
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import java.util.*

class AutomaticModeTest : KoinTest {
    internal var rxScheduler: Scheduler = Schedulers.single()
    internal var sensorServiceFake: SensorServiceFake = SensorServiceFake()
    internal var automode: Automode? = null
    
    fun getFakeLocation(): Location {
        var mockedLocation: Location = mock(Location::class.java)
        val speed = SPEED_MOVEMENT_THRESHOLD + 100
        `when`(mockedLocation.speed).thenReturn(speed)
        LOGGER.info(Date().toString() + ":IS  ${mockedLocation.latitude} ${mockedLocation.longitude} ${mockedLocation.accuracy} ${mockedLocation.speed} ${mockedLocation.bearing} ${mockedLocation.altitude} ${mockedLocation.time}", function = "fakeLocation")
        return mockedLocation
    }

    @Before
    fun beforeTest() {
        automode = Automode(sensorServiceFake, rxScheduler)
        assert(automode != null)
        LoggerFactory().logger.getLogStream().subscribeOn(rxScheduler)/*.observeOn(AndroidSchedulers.mainThread())*/.subscribe({ it ->
            assert(it.type != LogType.ERROR)
        })
    }

    @Test
    fun testFromIdleStateToTrackingStateUsingNext() {
        Assert.assertTrue(automode!!.getCurrentState() is IdleState)

        automode!!.next()

        Assert.assertTrue(automode!!.getCurrentState() is TrackingState)
    }


    @Test
    fun testFromTrackingStateToInVehicleStateUsingGoNext() {
        val currentState = TrackingState(automode!!)
        automode?.setCurrentState(currentState)
        Assert.assertTrue(automode!!.getCurrentState() is TrackingState)

        currentState.goNext()

        Assert.assertTrue(automode!!.getCurrentState() is InVehicleState)
    }

    @Test
    fun testFromTrackingStateToInVehicleStateUsingNext() {
        val trackingState = TrackingState(automode!!)
        automode?.setCurrentState(trackingState)
        Assert.assertTrue(automode!!.getCurrentState() is TrackingState)
        trackingState.disable(false)
        automode?.next()

        val confidence = 100
        sensorServiceFake.speedFilter().activityStream.onNext(DetectedActivity(DetectedActivity.IN_VEHICLE, confidence))
        Thread.sleep(100)
        val newLocation = getFakeLocation()
        sensorServiceFake.forceLocationChanged(newLocation)

        Assert.assertTrue("Current State Automode" + automode!!.getCurrentState() + "\n", automode!!.getCurrentState() is InVehicleState)
    }


    @Test
    fun testFromInVehicleStateToDrivingStateUsingGoNext() {
        val currentState = InVehicleState(automode!!)
        automode?.setCurrentState(currentState)
        Assert.assertTrue(automode!!.getCurrentState() is InVehicleState)

        currentState.goNext()

        Assert.assertTrue(automode!!.getCurrentState() is DrivingState)
    }

    @Test
    fun testFromInVehicleStateToDrivingStateUsingNext() {
        val currentState = InVehicleState(automode!!)
        automode?.setCurrentState(currentState)
        Assert.assertTrue(automode!!.getCurrentState() is InVehicleState)
        currentState.disable(false)
        automode!!.next()

        val newLocation = getFakeLocation()
        LOGGER.info(Date().toString() + ":IS Speed of ${newLocation.speed} >= ${SPEED_MOVEMENT_THRESHOLD} reached with ${newLocation.accuracy} <= $LOCATION_ACCURACY_THRESHOLD of accuracy", function = "next")
        sensorServiceFake.forceLocationChanged(newLocation)
        Thread.sleep(100)
        sensorServiceFake.forceLocationChanged(newLocation)

        Assert.assertTrue("Current State Automode" + automode!!.getCurrentState() + "\n", automode!!.getCurrentState() is DrivingState)
    }

    @Test
    fun testFromIdleStateToDrivingStateUsingGoNext() {
        Assert.assertTrue(automode!!.getCurrentState() is IdleState)
        automode!!.getCurrentState().goNext()
        Assert.assertTrue(automode!!.getCurrentState() is TrackingState)
        automode!!.getCurrentState().goNext()
        Assert.assertTrue(automode!!.getCurrentState() is InVehicleState)
        automode!!.getCurrentState().goNext()
        Assert.assertTrue(automode!!.getCurrentState() is DrivingState)
    }
}