package axa.tex.drive.sdk.automode


import axa.tex.drive.sdk.automode.internal.tracker.SpeedFilter
import axa.tex.drive.sdk.automode.tracker.FromIdleDrivingState
import axa.tex.drive.sdk.automode.tracker.FromIdleToInVehicleState
import axa.tex.drive.sdk.automode.tracker.FromIdleTrackingState
import axa.tex.drive.sdk.automode.internal.tracker.TexActivityTracker
import axa.tex.drive.sdk.automode.tracker.FromDrivingToLongStop
import axa.tex.drive.sdk.automode.internal.Automode
import axa.tex.drive.sdk.automode.internal.states.DrivingState
import axa.tex.drive.sdk.automode.internal.states.IdleState
import axa.tex.drive.sdk.automode.internal.states.InVehicleState
import axa.tex.drive.sdk.automode.internal.states.TrackingState
import org.junit.Assert
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.inject
import org.koin.dsl.module
import org.koin.test.KoinTest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class AutomaticModeTest : KoinTest {


    @Test
    fun testFromIdleToInVehicleState() {
        val myModule = module {
            single { SpeedFilter() }
            single { AutomodeHandler() }
            single { FromIdleToInVehicleState() as TexActivityTracker }
            single { Automode(get()) }
        }
        startKoin {
            // module list
            modules(listOf(myModule))
        }
        val automode: Automode by inject()
        Assert.assertTrue(automode.getCurrentState() is IdleState)
        automode.next()
        Assert.assertTrue(automode.getCurrentState() is InVehicleState)
        stopKoin()
    }

    @Test
    fun testFromIdleToTrackingState() {
        val myModule = module{
            single { SpeedFilter() }
            single { AutomodeHandler() }
            single { FromIdleTrackingState() as TexActivityTracker }
            single { Automode(get()) }
        }
        startKoin {
            // module list
            modules(listOf(myModule))
        }
        val automode: Automode by inject()
        Assert.assertTrue(automode.getCurrentState() is IdleState)
        automode.next()
        Assert.assertTrue(automode.getCurrentState() is TrackingState)
        stopKoin()
    }

    @Test
    fun testFromIdleDriving() {
        val idleToDrivingModule = module {
            single { SpeedFilter() }
            single { AutomodeHandler() }
            single { FromIdleDrivingState() as TexActivityTracker }
            single { Automode(get()) }
        }
        startKoin {
            // module list
            modules(listOf(idleToDrivingModule))
        }
        val automode: Automode by inject()
        Assert.assertTrue(automode.getCurrentState() is IdleState)
        automode.next()
        Assert.assertTrue(automode.getCurrentState() is DrivingState)
        stopKoin()
    }


    @Test
    fun testFromDrivingToIdleAfterNoGps() {
        val drivingToIdleAfterNoGps = module {
            single { SpeedFilter() }
            single { AutomodeHandler() }
            single { FromIdleDrivingState() as TexActivityTracker }
            single { Automode(get()) }
        }
        startKoin {
            // module list
            modules(listOf(drivingToIdleAfterNoGps))
        }
        val automode: Automode by inject()
        automode.timeToWaitForGps = 100
        Assert.assertTrue(automode.getCurrentState() is IdleState)
        automode.next()
        automode.states[AutomodeHandler.State.IDLE]?.disable(true)
        Assert.assertTrue(automode.getCurrentState() is DrivingState)
        val signal = CountDownLatch(1)
        automode.autoModeHandler.state.subscribe {

            signal.countDown()
            Assert.assertTrue(automode.getCurrentState() is IdleState)
        }
        signal.await()

        stopKoin()
    }

    @Test
    fun testFromDrivingToIdleAfterLongStop() {
        val idleToDrivingModule =  module {
            single { SpeedFilter() }
            single { FromDrivingToLongStop() as TexActivityTracker }
            single { AutomodeHandler() }
            single { Automode(get()) }
        }
        startKoin {
            // module list
            modules(listOf(idleToDrivingModule))
        }
        val automode: Automode by inject()
        automode.acceptableStopDuration = 500
        Assert.assertTrue(automode.getCurrentState() is IdleState)
        automode.next()
        Assert.assertTrue(automode.getCurrentState() is DrivingState)
        val signal = CountDownLatch(1)

        automode.autoModeHandler.state.subscribe {
            signal.countDown()
            Assert.assertTrue(!it)
        }
        signal.await(1, TimeUnit.MICROSECONDS)

        stopKoin()
    }

}