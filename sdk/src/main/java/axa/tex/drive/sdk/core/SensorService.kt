package axa.tex.drive.sdk.core

import android.content.Context
import axa.tex.drive.sdk.automode.internal.tracker.TexActivityTracker
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.logger.LoggerFactory
import io.reactivex.Scheduler

class SensorService: TexActivityTracker, KoinComponentCallbacks {
    internal val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
    private var context: Context
    var locationSensorService: LocationSensorService
    var activityRecognitionService: ActivityRecognitionService

    constructor(context: Context, scheduler: Scheduler) {
        this.context = context
        this.locationSensorService = LocationSensorService(context, scheduler)
        this.activityRecognitionService = ActivityRecognitionService(context, scheduler)
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