package com.axa.tex.drive.demo

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import axa.tex.drive.sdk.acquisition.TripRecorder

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import org.koin.test.KoinTest

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest :KoinTest{

    var tripRecorder : TripRecorder? = null;

    @Before
    fun beforeTest() {

         tripRecorder = TripRecorder(InstrumentationRegistry.getTargetContext());
         tripRecorder?.track();

    }

    @Test
    fun testTrackerInjection() {

        val appContext = InstrumentationRegistry.getTargetContext()


        assertEquals("com.axa.dil.tex", appContext.packageName)
        Thread.sleep(5000)
        tripRecorder?.numberOfTracker()!!
        var numberTracker :Int = tripRecorder?.numberOfTracker()!!
        assertEquals(3, numberTracker)
    }
}
