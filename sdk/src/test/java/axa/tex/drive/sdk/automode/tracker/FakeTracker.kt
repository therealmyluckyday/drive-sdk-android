package axa.tex.drive.sdk.automode.tracker

import axa.tex.drive.sdk.automode.internal.tracker.SpeedFilter
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.automode.internal.tracker.TexActivityTracker
import axa.tex.drive.sdk.automode.internal.tracker.model.TexLocation
import axa.tex.drive.sdk.automode.internal.tracker.model.TexActivity
import axa.tex.drive.sdk.automode.internal.tracker.model.TexSpeed
import axa.tex.drive.sdk.automode.internal.tracker.model.Where
import org.koin.android.ext.android.inject

abstract class FakeTracker : TexActivityTracker, KoinComponentCallbacks {
    private val activities = mutableListOf<TexActivity>()
    private val speeds = mutableListOf<TexSpeed>()
    var stopScanning = false

    private val filterer: SpeedFilter by inject()

     fun setFromIdleToTrackingStateData(){
        activities.add(TexActivity(Where.ON_FOOT,50))
        activities.add(TexActivity(Where.WALKING,20))
        activities.add(TexActivity(Where.WALKING,100))
        activities.add(TexActivity(Where.IN_VEHICLE,25))
        activities.add(TexActivity(Where.IN_VEHICLE,32))
        activities.add(TexActivity(Where.ON_FOOT,100))
        activities.add(TexActivity(Where.IN_VEHICLE,50))
        activities.add(TexActivity(Where.IN_VEHICLE,53))
        activities.add(TexActivity(Where.IN_VEHICLE,95))
        activities.add(TexActivity(Where.ON_FOOT,50))

        speeds.add(TexSpeed(0.0f, 21.0f))
        speeds.add(TexSpeed(1.0f, 21.0f))
        speeds.add(TexSpeed(1.10f, 10.0f))
        speeds.add(TexSpeed(0.298f, 5.0f))
        speeds.add(TexSpeed(1.0f, 25.0f))

    }

     fun setFromIdleToInVehicleStateData(){
        setFromIdleToTrackingStateData()
        speeds.add(TexSpeed(5.0f, 25.0f))

    }

     fun setFromIdleToDrivingStateData(){
        setFromIdleToInVehicleStateData()
        speeds.add(TexSpeed(5.0f, 10.0f))

    }

    fun setFromDrivingToLongStopData(){
        setFromIdleToDrivingStateData()
        val stop = TexSpeed(0.0f, 10.0f)
        for(i in 1..1000){
            speeds.add(stop)
        }
    }

    constructor(){
/*
    Fake Classe
    // this is a Fake method
    */
    }

    override fun checkWhereAmI() {
        for (activity in activities){
            if (activity.where == Where.IN_VEHICLE && !stopScanning) {
                filterer.activityInput.onNext(activity)
            }
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
                filterer.locationInput.onNext(speed)
                val texLocation = TexLocation(0f, 0f, speed.accuracy, speed.speed, 0f, 10f, 15151551515)
                filterer.gpsStream.onNext(texLocation)
            }
        }
    }
}