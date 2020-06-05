package axa.tex.drive.sdk

import android.content.Context
import android.location.Location
import axa.tex.drive.sdk.automode.internal.tracker.SpeedFilter
import axa.tex.drive.sdk.core.ActivityRecognitionService
import axa.tex.drive.sdk.core.LocationSensorService
import axa.tex.drive.sdk.core.SensorService
import io.reactivex.Scheduler

class SensorServiceFake : SensorService {
    private var speedFilter = SpeedFilter()

    constructor(context: Context, scheduler: Scheduler) {
    }

    fun forceLocationChanged(location: Location) {
        //locationSensorService.onLocationChanged(location)
    }

    override fun speedFilter() : SpeedFilter {
        return this.speedFilter
    }

    override fun passivelyScanSpeed() {
    }

    override fun activelyScanSpeed() {
    }

    override fun stopSpeedScanning() {
    }

    override fun checkWhereAmI() {
    }

    override fun stopActivityScanning() {
    }
}