package axa.tex.drive.sdk.core

import android.content.Context
import android.location.Location
import axa.tex.drive.sdk.automode.internal.tracker.SpeedFilter
import axa.tex.drive.sdk.automode.internal.tracker.TexActivityTracker
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.logger.LoggerFactory
import io.reactivex.Scheduler

interface SensorService {

}

class SensorServiceImpl: TexActivityTracker, KoinComponentCallbacks {
    internal val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
    private var context: Context
    private var locationSensorService: LocationSensorService
    private var activityRecognitionService: ActivityRecognitionService

    constructor(context: Context, scheduler: Scheduler) {
        this.context = context
        this.locationSensorService = LocationSensorService(context, scheduler)
        this.activityRecognitionService = ActivityRecognitionService(context, scheduler)
    }

    fun forceLocationChanged(location: Location) {
        locationSensorService.onLocationChanged(location)
    }

    fun speedFilter() : SpeedFilter {
        return this.locationSensorService.speedFilter
    }

    override fun passivelyScanSpeed() {
        this.locationSensorService.passivelyScanSpeed()
    }

    override fun activelyScanSpeed() {
        this.locationSensorService.activelyScanSpeed()
    }

    override fun stopSpeedScanning() {
        this.locationSensorService.stopSpeedScanning()
    }

    override fun checkWhereAmI() {
        LOGGER.info("\"" , "checkWhereAmI")
        this.activityRecognitionService.startActivityScanning(locationSensorService.speedFilter.activityStream)
    }

    override fun stopActivityScanning() {
        LOGGER.info("\"" , "stopActivityScanning")
        this.activityRecognitionService.stopActivityScanning()
    }
}