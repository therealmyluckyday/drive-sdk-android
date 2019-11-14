package com.axa.tex.drive.demo

import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import axa.tex.drive.sdk.acquisition.TripRecorder
import axa.tex.drive.sdk.acquisition.model.Fix
import axa.tex.drive.sdk.acquisition.model.TexUser
import axa.tex.drive.sdk.core.TexConfig
import axa.tex.drive.sdk.core.TexService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest


import java.util.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest : KoinTest {

    var tripRecorder: TripRecorder? = null;
    private var lastLocation: Fix? = null;

    @Before
    fun beforeTest() {
        val appContext = InstrumentationRegistry.getTargetContext()
        val user = TexUser("appId", "FFFDIHOVA3131IJA1")
        val config: TexConfig = TexConfig.Builder(user, appContext, "clientIdToto").enableBatteryTracker().enableLocationTracker().enableMotionTracker().build()
        tripRecorder = TexService.configure(config)?.getTripRecorder()
        tripRecorder?.locationObservable()?.subscribe { fix -> lastLocation = fix }
        tripRecorder?.startTrip(Date().time)

        Thread.sleep(5000)
    }


    @Test
    fun testPackage() {
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("axa.tex.drive.demo", appContext.packageName)
    }

    @Test
    fun testLastLocation() {
        assertTrue(lastLocation != null)
    }


    @Test
    fun testIsRecording() {
        assertTrue(tripRecorder?.isRecording() == true)
        tripRecorder?.stopTrip(Date().time)
        Thread.sleep(1000)
        assertTrue(tripRecorder?.isRecording() == false)
        tripRecorder?.startTrip(Date().time)
    }
}
