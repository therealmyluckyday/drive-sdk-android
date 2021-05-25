package axa.tex.drive.sdk.automode


import android.location.Location
import axa.tex.drive.sdk.acquisition.SensorServiceFake
import axa.tex.drive.sdk.automode.internal.Automode
import axa.tex.drive.sdk.automode.internal.states.DrivingState
import axa.tex.drive.sdk.automode.internal.states.IdleState
import axa.tex.drive.sdk.automode.internal.states.InVehicleState
import axa.tex.drive.sdk.automode.internal.states.TrackingState
import axa.tex.drive.sdk.automode.internal.tracker.LOCATION_ACCURACY_THRESHOLD
import axa.tex.drive.sdk.automode.internal.tracker.SPEED_MOVEMENT_THRESHOLD
import axa.tex.drive.sdk.core.CertificateAuthority.Companion.LOGGER
import axa.tex.drive.sdk.core.logger.LogType
import axa.tex.drive.sdk.core.logger.LoggerFactory
import com.google.android.gms.location.DetectedActivity
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.koin.test.KoinTest
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.stubbing.Answer
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
    fun testFromIdleStateToTrackingStateUsingEnable() {
        Assert.assertTrue(automode!!.getCurrentState() is IdleState)

        automode!!.getCurrentState().enable()

        Assert.assertTrue(automode!!.getCurrentState() is TrackingState)
    }

    // Tracking State

    @Test
    fun testFromTrackingStateToInVehicleStateUsingGoNext() {
        val currentState = TrackingState(automode!!)
        automode?.setCurrentState(currentState)
        Assert.assertTrue(automode!!.getCurrentState() is TrackingState)

        currentState.goNext()

        Assert.assertTrue(automode!!.getCurrentState() is InVehicleState)
    }

    @Test
    fun testFromTrackingStateToInVehicleStateUsingEnable() {
        val trackingState = TrackingState(automode!!)
        automode?.setCurrentState(trackingState)
        Assert.assertTrue(automode!!.getCurrentState() is TrackingState)
        automode!!.getCurrentState().enable()

        val confidence = 100
        sensorServiceFake.speedFilter().activityStream.onNext(DetectedActivity(DetectedActivity.IN_VEHICLE, confidence))
        Thread.sleep(100)
        val newLocation = getFakeLocation()
        sensorServiceFake.forceLocationChanged(newLocation)

        //Assert.assertTrue("Current State Automode" + automode!!.getCurrentState() + "\n", automode!!.getCurrentState() is InVehicleState)
    }

    @Test
    fun testFromTrackingStateToInVehicleStateUsingEnable_Fail_With_not_IN_VEHICLE_Activity() {
        if (automode == null) {
            assert(false)
            return
        }
        if (automode != null) {
        val trackingState = TrackingState(automode!!)
        automode!!.setCurrentState(trackingState)
        Assert.assertTrue(automode!!.getCurrentState() is TrackingState)
        automode!!.getCurrentState().enable()
        val confidence = 100

        sensorServiceFake.speedFilter().activityStream.onNext(DetectedActivity(DetectedActivity.ON_BICYCLE, confidence))
        Thread.sleep(100)
        var newLocation = getFakeLocation()
        sensorServiceFake.forceLocationChanged(newLocation)
        Assert.assertTrue("Current State Automode" + automode!!.getCurrentState() + "\n", automode!!.getCurrentState() == trackingState)
        sensorServiceFake.speedFilter().activityStream.onNext(DetectedActivity(DetectedActivity.ON_FOOT, confidence))
        Thread.sleep(100)
        newLocation = getFakeLocation()
        sensorServiceFake.forceLocationChanged(newLocation)
        Assert.assertTrue("Current State Automode" + automode!!.getCurrentState() + "\n", automode!!.getCurrentState() == trackingState)
        sensorServiceFake.speedFilter().activityStream.onNext(DetectedActivity(DetectedActivity.RUNNING, confidence))
        Thread.sleep(100)
        newLocation = getFakeLocation()
        sensorServiceFake.forceLocationChanged(newLocation)
        Assert.assertTrue("Current State Automode" + automode!!.getCurrentState() + "\n", automode!!.getCurrentState() == trackingState)
        sensorServiceFake.speedFilter().activityStream.onNext(DetectedActivity(DetectedActivity.STILL, confidence))
        Thread.sleep(100)
        newLocation = getFakeLocation()
        sensorServiceFake.forceLocationChanged(newLocation)
        Assert.assertTrue("Current State Automode" + automode!!.getCurrentState() + "\n", automode!!.getCurrentState() == trackingState)
        sensorServiceFake.speedFilter().activityStream.onNext(DetectedActivity(DetectedActivity.TILTING, confidence))
        Thread.sleep(100)
        newLocation = getFakeLocation()
        sensorServiceFake.forceLocationChanged(newLocation)
        Assert.assertTrue("Current State Automode" + automode!!.getCurrentState() + "\n", automode!!.getCurrentState() == trackingState)
        sensorServiceFake.speedFilter().activityStream.onNext(DetectedActivity(DetectedActivity.UNKNOWN, confidence))
        Thread.sleep(100)
        newLocation = getFakeLocation()
        sensorServiceFake.forceLocationChanged(newLocation)
        Assert.assertTrue("Current State Automode" + automode!!.getCurrentState() + "\n", automode!!.getCurrentState() == trackingState)
        sensorServiceFake.speedFilter().activityStream.onNext(DetectedActivity(DetectedActivity.WALKING, confidence))
        Thread.sleep(100)
        newLocation = getFakeLocation()
        sensorServiceFake.forceLocationChanged(newLocation)
        Assert.assertTrue("Current State Automode" + automode!!.getCurrentState() + "\n", automode!!.getCurrentState() == trackingState)

        }
    }

    @Test
    fun testFromTrackingStateToInVehicleStateUsingEnable_Fail_With_low_speed_GPS() {
        val trackingState = TrackingState(automode!!)
        automode?.setCurrentState(trackingState)
        Assert.assertTrue(automode!!.getCurrentState() is TrackingState)
        automode!!.getCurrentState().enable()

        val confidence = 100
        sensorServiceFake.speedFilter().activityStream.onNext(DetectedActivity(DetectedActivity.IN_VEHICLE, confidence))
        Thread.sleep(100)
        var mockedLocation: Location = mock(Location::class.java)
        val speed = SPEED_MOVEMENT_THRESHOLD - 0.1F
        `when`(mockedLocation.speed).thenReturn(speed)
        val accuracy = LOCATION_ACCURACY_THRESHOLD + 0.1F
        `when`(mockedLocation.accuracy).thenReturn(accuracy)
        sensorServiceFake.forceLocationChanged(mockedLocation)

        Assert.assertTrue("Current State Automode" + automode!!.getCurrentState() + "\n", automode!!.getCurrentState() == trackingState)
    }

    // IN Vehicule State


    @Test
    fun testFromInVehicleStateToDrivingStateUsingEnable_Fail_SPEED_MOVEMENT_NOT_THRESHOLD() {
        val currentState = InVehicleState(automode!!)
        automode?.setCurrentState(currentState)
        Assert.assertTrue(automode!!.getCurrentState() is InVehicleState)
        automode!!.getCurrentState().enable()

        var mockedLocation: Location = mock(Location::class.java)
        val speed = SPEED_MOVEMENT_THRESHOLD - 0.1F
        `when`(mockedLocation.speed).thenReturn(speed)
        val accuracy = LOCATION_ACCURACY_THRESHOLD+0.0F
        `when`(mockedLocation.accuracy).thenReturn(accuracy)

        sensorServiceFake.forceLocationChanged(mockedLocation)
        Thread.sleep(100)

        Assert.assertTrue("Current State Automode" + automode!!.getCurrentState() + "\n", automode!!.getCurrentState() == currentState)
    }

    @Test
    fun testFromInVehicleStateToDrivingStateUsingEnable_Fail_LOCATION_ACCURACY_THRESHOLD() {
        val currentState = InVehicleState(automode!!)
        automode?.setCurrentState(currentState)
        Assert.assertTrue(automode!!.getCurrentState() is InVehicleState)
        automode!!.getCurrentState().enable()

        var mockedLocation: Location = mock(Location::class.java)
        val speed = SPEED_MOVEMENT_THRESHOLD
        `when`(mockedLocation.speed).thenReturn(speed)
        val accuracy = LOCATION_ACCURACY_THRESHOLD + 0.1F
        `when`(mockedLocation.accuracy).thenReturn(accuracy)

        sensorServiceFake.forceLocationChanged(mockedLocation)
        Thread.sleep(100)

        Assert.assertTrue("Current State Automode" + automode!!.getCurrentState() + "\n", automode!!.getCurrentState() == currentState)
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
    fun testFromInVehicleStateToDrivingStateUsingEnable() {
        val currentState = InVehicleState(automode!!)
        automode?.setCurrentState(currentState)
        Assert.assertTrue(automode!!.getCurrentState() is InVehicleState)
        automode!!.getCurrentState().enable()

        val newLocation = getFakeLocation()
        LOGGER.info(":TEST Speed of ${newLocation.speed} >= ${SPEED_MOVEMENT_THRESHOLD} reached with ${newLocation.accuracy} <= $LOCATION_ACCURACY_THRESHOLD of accuracy", function = "TEST")
        sensorServiceFake.forceLocationChanged(newLocation)
        Thread.sleep(100)
        sensorServiceFake.forceLocationChanged(newLocation)

        Thread.sleep(100)
        Assert.assertTrue("Current State Automode" + automode!!.getCurrentState() + "\n", automode!!.getCurrentState() is DrivingState)
    }


    // DRIVING STATE
    @Test
    fun testFromDrivingStateToIdleStateUsingGoNext() {
        val currentState = DrivingState(automode!!)
        automode?.setCurrentState(currentState)
        Assert.assertTrue(automode!!.getCurrentState() is DrivingState)

        currentState.goNext()

        Assert.assertTrue(automode!!.getCurrentState() is TrackingState)
    }

    @Test
    fun testFromDrivingStateToIdleStateUsingEnable() {
        val currentState = DrivingState(automode!!)
        automode?.setCurrentState(currentState)
        Assert.assertTrue(automode!!.getCurrentState() is DrivingState)
        automode!!.getCurrentState().enable()

        val newLocation = getFakeLocation()
        LOGGER.info(Date().toString() + ":IS Speed of ${newLocation.speed} >= ${SPEED_MOVEMENT_THRESHOLD} reached with ${newLocation.accuracy} <= $LOCATION_ACCURACY_THRESHOLD of accuracy", function = "next")
        sensorServiceFake.forceLocationChanged(newLocation)
        Thread.sleep(100)
        sensorServiceFake.forceLocationChanged(newLocation)

        //Assert.assertTrue("Current State Automode" + automode!!.getCurrentState() + "\n", automode!!.getCurrentState() is IdleState)
    }

    @Test
    fun testFromDrivingStateToIdleStateUsingEnable_Fail_watchSpeed() {
        val currentState = mock(DrivingState::class.java)
        `when`(currentState.getTime()).thenReturn(System.currentTimeMillis() - 6000)
        automode?.setCurrentState(currentState)
        Assert.assertTrue(automode!!.getCurrentState() is DrivingState)
        automode?.acceptableStopDuration = 0
        currentState.lastMvtTime = System.currentTimeMillis() - 6000
        automode!!.getCurrentState().enable()

        val newLocation = getFakeLocation()
        LOGGER.info(Date().toString() + ":IS Speed of ${newLocation.speed} >= ${SPEED_MOVEMENT_THRESHOLD} reached with ${newLocation.accuracy} <= $LOCATION_ACCURACY_THRESHOLD of accuracy", function = "next")
        //sensorServiceFake.forceLocationChanged(newLocation)
        Thread.sleep(100)
        //sensorServiceFake.forceLocationChanged(newLocation)

        Assert.assertFalse("Current State Automode" + automode!!.getCurrentState() + "\n", automode!!.getCurrentState() is IdleState)
    }

    @Test
    fun testFromDrivingStateToIdleStateUsingEnable_Fail_watchGPS() {
       // val mockDrivingState = mock(DrivingState::class.java, Mockito.RETURNS_DEEP_STUBS)
        val currentState = DrivingState(automode!!)
        val answer = Answer {
            val drivingState = it as DrivingState
            println("testFromDrivingStateToIdleStateUsingEnable_Fail_watchGPS drivingState.goNext()")
            drivingState.goNext()
            null
        }
       // `when`(mockDrivingState.stop(ArgumentMatchers.anyString())).thenAnswer(answer)
/*
        doAnswer {
            val drivingState = it as DrivingState
            drivingState.goNext()
            null // or you can type return@doAnswer null

        }.`when`(mockDrivingState).stop(ArgumentMatchers.anyString())*/
        //doNothing().`when`(mockDrivingState.stop(ArgumentMatchers.anyString()))
        /*`when`(mockDrivingState.getTime()).thenReturn(System.currentTimeMillis() - 6000)
        doCallRealMethod().`when`(mockDrivingState).enable()
        doCallRealMethod().`when`(mockDrivingState).watchGPS()
        doNothing().`when`(mockDrivingState).watchSpeed()
        doCallRealMethod().`when`(mockDrivingState).goNext()*/
        automode?.setCurrentState(currentState)
        Assert.assertTrue(automode!!.getCurrentState() is DrivingState)
        automode?.timeToWaitForGps = 1000
        currentState.lastMvtTime = System.currentTimeMillis() - 6000

        //doAnswer(answer).`when`(currentState).stop("No gps:Stop driving. from watchGPS")

        //doThrow(Exception::class.java).`when`(mockDrivingState).stop(ArgumentMatchers.anyString())
        automode!!.getCurrentState().enable()
        //Mockito.verify(mockDrivingState).stop(ArgumentMatchers.anyString())

        //val newLocation = getFakeLocation()
        //LOGGER.info(Date().toString() + ":IS Speed of ${newLocation.speed} >= ${SPEED_MOVEMENT_THRESHOLD} reached with ${newLocation.accuracy} <= $LOCATION_ACCURACY_THRESHOLD of accuracy", function = "next")
        //sensorServiceFake.forceLocationChanged(newLocation)
        //Thread.sleep(1000)
        //sensorServiceFake.forceLocationChanged(newLocation)
        //verify(mockDrivingState).stop(ArgumentMatchers.anyString())
        //Assert.assertTrue("Current State Automode" + automode!!.getCurrentState() + "\n", automode!!.getCurrentState() is IdleState)
    }

    // AUTOMODE NOMINAL FLOW
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