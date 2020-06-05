package axa.tex.drive.sdk.automode.tracker

import android.app.Activity
import axa.tex.drive.sdk.automode.internal.tracker.SpeedFilter
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.automode.internal.tracker.model.TexLocation
import axa.tex.drive.sdk.core.SensorService
import org.koin.android.ext.android.inject

open class FakeTracker : SensorService, KoinComponentCallbacks {
    private val activities = mutableListOf<Activity>()
    private val speeds = mutableListOf<TexLocation>()
    var stopScanning = false

    private val filterer: SpeedFilter by inject()

     fun setFromIdleToTrackingStateData(){


    }

     fun setFromIdleToInVehicleStateData(){
        setFromIdleToTrackingStateData()

    }

     fun setFromIdleToDrivingStateData(){
        setFromIdleToInVehicleStateData()

    }

    fun setFromDrivingToLongStopData(){
        setFromIdleToDrivingStateData()
        for(i in 1..1000){
        }
    }

    constructor(){
/*
    Fake Classe
    // this is a Fake method
    */
    }

    override fun speedFilter(): SpeedFilter {
        return filterer
    }

    override fun checkWhereAmI() {
        for (activity in activities){

        }
    }

    override fun stopActivityScanning() {
/*
    Fake Classe
    // this is a Fake method
    */
    }

    override fun stopSpeedScanning() {
/*
    Fake Classe
    // this is a Fake method
    */
    }

    override fun passivelyScanSpeed() {
        provideSpeed()
    }

    override fun activelyScanSpeed() {
        provideSpeed()
    }

    private fun provideSpeed(){
        for (speed in speeds){
            if(!stopScanning) {
                val texLocation = TexLocation(0f, 0f, speed.accuracy, speed.speed, 0f, 10f, 15151551515)
                filterer.gpsStream.onNext(texLocation)
            }
        }
    }
}