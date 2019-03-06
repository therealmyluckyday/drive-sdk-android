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
import org.koin.dsl.module.Module
import org.koin.dsl.module.module
import org.koin.standalone.StandAloneContext
import org.koin.standalone.inject
import org.koin.test.KoinTest
import java.util.concurrent.CountDownLatch

class AutomaticModeTest : KoinTest {


    @Test
    fun testFromIdleToInVehicleState() {
        val myModule:  Module = module(definition = {
            single { SpeedFilter() }
            single { AutomodeHandler() }
            single { FromIdleToInVehicleState() as TexActivityTracker }
            single { Automode(get()) }
        })
        StandAloneContext.startKoin(listOf(myModule))
        val automode: Automode by inject()
        Assert.assertTrue(automode.getCurrentState() is IdleState)
        automode.next()
        Assert.assertTrue(automode.getCurrentState() is InVehicleState)
        StandAloneContext.stopKoin()
    }

    @Test
    fun testFromIdleToTrackingState() {
        val myModule:  Module = module(definition = {
            single { SpeedFilter() }
            single { AutomodeHandler() }
            single { FromIdleTrackingState() as TexActivityTracker }
            single { Automode(get()) }
        })
        StandAloneContext.startKoin(listOf(myModule))
        val automode: Automode by inject()
        Assert.assertTrue(automode.getCurrentState() is IdleState)
        automode.next()
        Assert.assertTrue(automode.getCurrentState() is TrackingState)
        StandAloneContext.stopKoin()
    }

    @Test
    fun testFromIdleDriving() {
        val idleToDrivingModule:  Module = module(definition = {
            single { SpeedFilter() }
            single { AutomodeHandler() }
            single { FromIdleDrivingState() as TexActivityTracker }
            single { Automode(get()) }
        })
        StandAloneContext.startKoin(listOf(idleToDrivingModule))
        val automode: Automode by inject()
        Assert.assertTrue(automode.getCurrentState() is IdleState)
        automode.next()
        Assert.assertTrue(automode.getCurrentState() is DrivingState)
        StandAloneContext.stopKoin()
    }


    @Test
    fun testFromDrivingToIdleAfterNoGps() {
        val drivingToIdleAfterNoGps:  Module = module(definition = {
            single { SpeedFilter() }
            single { AutomodeHandler() }
            single { FromIdleDrivingState() as TexActivityTracker }
            single { Automode(get()) }
        })
        StandAloneContext.startKoin(listOf(drivingToIdleAfterNoGps))
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

        StandAloneContext.stopKoin()
    }

    @Test
    fun testFromDrivingToIdleAfterLongStop() {
        val idleToDrivingModule:  Module = module(definition = {
            single { SpeedFilter() }
            single { FromDrivingToLongStop() as TexActivityTracker }
            single { AutomodeHandler() }
            single { Automode(get()) }
        })
        StandAloneContext.startKoin(listOf(idleToDrivingModule))
        val automode: Automode by inject()
        automode.acceptableStopDuration = 500
        Assert.assertTrue(automode.getCurrentState() is IdleState)
        automode.next()
        Assert.assertTrue(automode.getCurrentState() is DrivingState)
        val signal = CountDownLatch(1)
        automode.autoModeHandler.state.subscribe {
            signal.countDown()
            Assert.assertTrue(it == AutomodeHandler.State.IDLE)
        }
        signal.await()
        StandAloneContext.stopKoin()
    }

}