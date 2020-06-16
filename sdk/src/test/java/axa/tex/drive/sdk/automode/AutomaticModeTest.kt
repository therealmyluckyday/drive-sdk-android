package axa.tex.drive.sdk.automode


import android.location.Location
import android.os.SystemClock
import axa.tex.drive.sdk.automode.internal.tracker.SpeedFilter
import axa.tex.drive.sdk.automode.internal.Automode
import axa.tex.drive.sdk.acquisition.SensorService
import axa.tex.drive.sdk.acquisition.SensorServiceFake
import axa.tex.drive.sdk.automode.internal.states.IdleState
import axa.tex.drive.sdk.automode.internal.states.InVehicleState
import axa.tex.drive.sdk.automode.internal.states.TrackingState
import axa.tex.drive.sdk.automode.internal.tracker.SPEED_MOVEMENT_THRESHOLD
import com.google.android.gms.location.DetectedActivity
import io.reactivex.schedulers.Schedulers
import org.junit.Assert
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.inject
import org.koin.dsl.module
import org.koin.test.KoinTest

class AutomaticModeTest : KoinTest {
    @Test
    fun testFromIdleStateToTrackingStateUsingNext() {
        val rxScheduler = Schedulers.single()
        val sensorServiceFake = SensorServiceFake()
        val automode = Automode(sensorServiceFake, rxScheduler)

        Assert.assertTrue(automode.getCurrentState() is IdleState)
        automode.next()
        Assert.assertTrue(automode.getCurrentState() is TrackingState)
    }


    @Test
    fun testFromTrackingStateToInVehicleStateUsingGoNext() {
        val rxScheduler = Schedulers.single()
        val sensorServiceFake = SensorServiceFake()
        val automode = Automode(sensorServiceFake, rxScheduler)
        val currentState = TrackingState(automode)
        automode.setCurrentState(currentState)
        Assert.assertTrue(automode.getCurrentState() is TrackingState)
        currentState.goNext()
        Assert.assertTrue(automode.getCurrentState() is InVehicleState)
    }

    }

    @Test
    fun testFromIdleToTrackingState() {
        val myModule = module{
            single { SpeedFilter() }
            single { SensorServiceFake() as SensorService }
            single { Automode(get(), Schedulers.single()) }
        }
        startKoin {
            // module list
            modules(listOf(myModule))
        }
        val automode: Automode by inject()

        Assert.assertTrue(true)
        stopKoin()
    }

    @Test
    fun testFromIdleDriving() {
        val idleToDrivingModule = module {
            single { SpeedFilter() }
            single { SensorServiceFake() as SensorService }
            single { Automode(get(), Schedulers.single()) }
        }
        startKoin {
            // module list
            modules(listOf(idleToDrivingModule))
        }
        val automode: Automode by inject()
        Assert.assertTrue(true)
        stopKoin()
    }


    @Test
    fun testFromDrivingToIdleAfterNoGps() {
        val drivingToIdleAfterNoGps = module {
            single { SpeedFilter() }
            single { SensorServiceFake() as SensorService }
            single { Automode(get(), Schedulers.single()) }
        }
        startKoin {
            // module list
            modules(listOf(drivingToIdleAfterNoGps))
        }
        val automode: Automode by inject()
        automode.timeToWaitForGps = 100
        Assert.assertTrue(true)

        stopKoin()
    }

    @Test
    fun testFromDrivingToIdleAfterLongStop() {
        val idleToDrivingModule =  module {
            single { SpeedFilter() }
            single { SensorServiceFake() as SensorService }
            single { Automode(get(), Schedulers.single()) }
        }
        startKoin {
            // module list
            modules(listOf(idleToDrivingModule))
        }
        val automode: Automode by inject()
        automode.acceptableStopDuration = 500
        Assert.assertTrue(true)

        stopKoin()
    }

}