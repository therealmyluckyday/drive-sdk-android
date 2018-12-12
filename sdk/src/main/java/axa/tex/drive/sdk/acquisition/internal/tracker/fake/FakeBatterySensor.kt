package axa.tex.drive.sdk.acquisition.internal.tracker.fake

import axa.tex.drive.sdk.acquisition.model.BatteryFix
import axa.tex.drive.sdk.acquisition.model.BatteryState
import java.util.*


class FakeBatterySensor{

    var trackingEnabled : Boolean = false
    var trackingDisable : Boolean = true

     var batteryLevel : Int = 1
    var batteryState : BatteryState = BatteryState.plugged
     var timestamp: Long = 12

    internal fun  enableTracking(){
        disableOrEnableTracking(true)
    }

    internal fun  disableTracking(){
        disableOrEnableTracking(false);
    }

    private fun  disableOrEnableTracking(enable : Boolean){
        trackingDisable = !enable
        trackingEnabled = enable
    }

    internal fun provideFix() : BatteryFix{
       return BatteryFix(batteryLevel,batteryState, timestamp)
    }


}