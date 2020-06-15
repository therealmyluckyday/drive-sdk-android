package axa.tex.drive.sdk.acquisition

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import axa.tex.drive.sdk.automode.internal.tracker.SpeedFilter
import axa.tex.drive.sdk.core.ActivityRecognitionService
import axa.tex.drive.sdk.core.logger.LoggerFactory
import io.reactivex.Scheduler


class SensorServiceImpl: SensorService {
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

    override fun speedFilter() : SpeedFilter {
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

    @Throws(PermissionException::class)
    override fun requestForLocationPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                val exception = PermissionException("need permission.ACCESS_FINE_LOCATION")
                throw exception
            }

            if (context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                val exception = PermissionException("need permission.ACCESS_COARSE_LOCATION")
                throw exception
            }

            if ((Build.VERSION.SDK_INT >= 29) && (context.checkSelfPermission(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                val exception = PermissionException("need permission.ACCESS_BACKGROUND_LOCATION")
                throw exception
            }
        }
    }
}