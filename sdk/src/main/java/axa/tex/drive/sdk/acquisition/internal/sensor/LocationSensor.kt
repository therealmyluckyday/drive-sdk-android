package axa.tex.drive.sdk.acquisition.internal.sensor

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import axa.tex.drive.sdk.acquisition.model.Fix
import axa.tex.drive.sdk.acquisition.model.LocationFix
import axa.tex.drive.sdk.automode.internal.Automode
import axa.tex.drive.sdk.automode.internal.tracker.AutoModeTracker
import axa.tex.drive.sdk.automode.internal.tracker.TexActivityTracker
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.logger.LoggerFactory
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject


internal class LocationSensor : TexSensor, LocationListener, KoinComponentCallbacks {


    private var lastLocation: Location? = null
    internal val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
    private var context: Context? = null;
    private var locationManager: LocationManager? = null
    private val fixProducer: PublishSubject<List<Fix>> = PublishSubject.create()

    var isEnable: Boolean = true
    var canBeEnabled: Boolean = true

    private var autoModeTracker: AutoModeTracker? = null
    var automode: Automode

    override fun disableSensor() {
        LOGGER.info("disabling tracker", "override fun disableSensor()")

        enableTracking(false)
    }

    override fun enableSensor() {
        if (canBeEnabled) {
            enableTracking(true)
        }
    }

    override fun isEnabled(): Boolean {
        return isEnable
    }


    constructor(automode: Automode, autoModeTracker: TexActivityTracker, context: Context?, canBeEnabled: Boolean = true) {
        locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        this.context = context
        this.canBeEnabled = canBeEnabled
        this.automode = automode
        this.autoModeTracker = autoModeTracker as AutoModeTracker
    }

    override fun producer(): Observable<List<Fix>> {
        return fixProducer
    }

    override fun onLocationChanged(location: Location) {
        var computedSpeed = 0.0
        if (this.lastLocation != null)
            computedSpeed = Math.sqrt(
                    Math.pow(location.longitude - lastLocation?.longitude!!, 2.0) + Math.pow(location.latitude - lastLocation!!.latitude, 2.0)
            ) / (location.time - this.lastLocation!!.time)

        this.lastLocation = location


        var speed = location.speed
        if (!location.hasSpeed()) {
            speed = (-1.0).toFloat()
        }
        val locationFix = LocationFix(location.latitude,
                location.longitude,
                location.accuracy,
                speed,
                location.bearing,
                location.altitude,
                location.time)
        LOGGER.info("Sending location", "override fun onLocationChanged(location: Location)")
        fixProducer.onNext(listOf(locationFix))
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }

    private fun enableTracking(track: Boolean) {

        if (track) {
            //============================================================================================================================
            autoModeTracker?.speedFilter?.gpsStream?.subscribe {
                if (autoModeTracker?.speedFilter!!.collectionEnabled) {
                    val locationFix: LocationFix = LocationFix(it.latitude.toDouble(),
                            it.longitude.toDouble(),
                            it.accuracy,
                            it.speed,
                            it.bearing,
                            it.altitude.toDouble(),
                            it.time)
                    LOGGER.info("${Date().toString()} Got new location fix ", "private fun enableTracking(track: Boolean)")
                    if (locationFix != null) {
                        fixProducer.onNext(listOf(locationFix))
                    }
                }
            }
            //============================================================================================================================
            if (Build.VERSION.SDK_INT >= 23) {
                if (context?.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        context?.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
            }
            autoModeTracker?.activelyScanSpeed()
            autoModeTracker?.speedFilter?.collectionEnabled = true
            LOGGER.info("location Tracker enabled", "private fun enableTracking(track: Boolean)")
        } else {
            //locationManager?.removeUpdates(this)
            autoModeTracker?.speedFilter?.collectionEnabled = false
            autoModeTracker?.stopSpeedScanning()
            LOGGER.info("Location tracker disabled", "private fun enableTracking(track: Boolean)")
        }

    }
}