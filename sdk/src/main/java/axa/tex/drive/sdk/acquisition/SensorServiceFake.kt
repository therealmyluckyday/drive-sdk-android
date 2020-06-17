package axa.tex.drive.sdk.acquisition

import android.content.Context
import android.location.Location
import axa.tex.drive.sdk.automode.internal.tracker.SpeedFilter
import axa.tex.drive.sdk.automode.internal.tracker.model.TexLocation
import axa.tex.drive.sdk.core.CertificateAuthority.Companion.LOGGER
import io.reactivex.Scheduler

open class SensorServiceFake : SensorService {
    private var speedFilter = SpeedFilter()

    constructor(context: Context, scheduler: Scheduler) {
    }

    constructor() {

    }

    fun forceLocationChanged(location: Location) {
        val texLocation = TexLocation(location.latitude.toFloat(), location.longitude.toFloat(), location.accuracy, location.speed, location.bearing, location.altitude.toFloat(), location.time)
        speedFilter.gpsStream.onNext(texLocation)
        speedFilter.locations.onNext(location)
    }

    override fun speedFilter() : SpeedFilter {
        return this.speedFilter
    }

    override fun passivelyScanSpeed() {
    }

    override fun activelyScanSpeed() {
        LOGGER.info("sensorFake", function = "activelyScanSpeed")
    }

    override fun stopSpeedScanning() {
    }

    override fun checkWhereAmI() {
    }

    override fun stopActivityScanning() {
    }

    override fun requestForLocationPermission() {

    }
}