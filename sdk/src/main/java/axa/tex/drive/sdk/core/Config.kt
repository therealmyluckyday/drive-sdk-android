package axa.tex.drive.sdk.core

import axa.tex.drive.sdk.core.internal.Constants

internal class Config{
    var batteryTrackerEnabled : Boolean = false
    var locationTrackerEnabled : Boolean = false
    var motionTrackerEnabled : Boolean = false
    var appName : String = Constants.DEFAULT_APP_NAME
    var clientId : String = Constants.DEFAULT_CLIENT_ID

    constructor(){

    }

    constructor(batteryTrackerEnabled : Boolean,
                locationTrackerEnabled : Boolean,
                motionTrackerEnabled : Boolean,
                appName : String, clientId : String){
        this.batteryTrackerEnabled = batteryTrackerEnabled
        this.locationTrackerEnabled = locationTrackerEnabled
        this.motionTrackerEnabled = motionTrackerEnabled
        this.appName = appName
        this.clientId = clientId
    }
}