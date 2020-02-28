package axa.tex.drive.sdk.automode


import axa.tex.drive.sdk.automode.internal.tracker.SpeedFilter
import axa.tex.drive.sdk.automode.tracker.FromIdleDrivingState
import axa.tex.drive.sdk.automode.tracker.FromIdleToInVehicleState
import axa.tex.drive.sdk.automode.tracker.FromIdleTrackingState
import axa.tex.drive.sdk.automode.internal.tracker.TexActivityTracker
import axa.tex.drive.sdk.automode.tracker.FromDrivingToLongStop
import axa.tex.drive.sdk.automode.internal.Automode
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
    fun testFromIdleToInVehicleState() {
        val myModule = module {
            single { SpeedFilter() }
            single { AutomodeHandler() }
            single { FromIdleToInVehicleState() as TexActivityTracker }
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
    fun testFromIdleToTrackingState() {
        val myModule = module{
            single { SpeedFilter() }
            single { AutomodeHandler() }
            single { FromIdleTrackingState() as TexActivityTracker }
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
            single { AutomodeHandler() }
            single { FromIdleDrivingState() as TexActivityTracker }
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
            single { AutomodeHandler() }
            single { FromIdleDrivingState() as TexActivityTracker }
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
            single { FromDrivingToLongStop() as TexActivityTracker }
            single { AutomodeHandler() }
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