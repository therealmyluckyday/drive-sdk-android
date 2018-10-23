package axa.tex.drive.sdk.core

internal class Config{
    var batteryTrackerEnabled : Boolean = false
    var locationTrackerEnabled : Boolean = false
    var motionTrackerEnabled : Boolean = false

    constructor(){

    }

    constructor(batteryTrackerEnabled : Boolean,
                locationTrackerEnabled : Boolean,
                motionTrackerEnabled : Boolean){
        this.batteryTrackerEnabled = batteryTrackerEnabled
        this.locationTrackerEnabled = locationTrackerEnabled
        this.motionTrackerEnabled = motionTrackerEnabled
    }
}