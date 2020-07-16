package axa.tex.drive.sdk.acquisition

import android.content.Context
import android.location.Location
import axa.tex.drive.sdk.automode.internal.tracker.SpeedFilter
import axa.tex.drive.sdk.automode.internal.tracker.model.TexLocation
import io.reactivex.Scheduler
import axa.tex.drive.sdk.core.logger.LoggerFactory
import io.reactivex.subjects.PublishSubject

open class SensorServiceFake : SensorService {
    private var speedFilter = SpeedFilter()
    internal val logger = LoggerFactory().getLogger(this::class.java.name).logger
    val infosMethodCalled: PublishSubject<String> = PublishSubject.create()

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
        logger.info("sensorFake activelyScanSpeed", function = "activelyScanSpeed")
        infosMethodCalled.onNext("activelyScanSpeed")
    }

    override fun stopSpeedScanning() {
    }
    override fun checkWhereAmI() {
        logger.info("sensorFake checkWhereAmI", function = "checkWhereAmI")
        infosMethodCalled.onNext("checkWhereAmI")
    }

    override fun stopActivityScanning() {
    }

    override fun requestForLocationPermission() {

    }
}