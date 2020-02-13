package axa.tex.drive.sdk.acquisition.internal.sensor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
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
    private var context: Context? = null
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
        LOGGER.info("Sending location", "onLocationChanged")
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
        fixProducer.onNext(listOf(locationFix))
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        LOGGER.info("status: $status", "onStatusChanged")
    }

    override fun onProviderEnabled(provider: String?) {
        LOGGER.info("provider $provider", "onProviderEnabled")
    }

    override fun onProviderDisabled(provider: String?) {
        LOGGER.info("$provider", "onProviderDisabled")
    }

    private fun enableTracking(track: Boolean) {
        val funcName = "enableTracking"
        if (track) {
            subscribeGPSSTream()
            checkPermission()
            autoModeTracker?.activelyScanSpeed()
        } else {
            autoModeTracker?.stopSpeedScanning()
        }
        autoModeTracker?.speedFilter?.collectionEnabled = track
        LOGGER.info("location Tracker enabled: $track", funcName)
    }

    fun subscribeGPSSTream() {
        val funcName = "subscribeGPSSTream"
        autoModeTracker?.speedFilter?.gpsStream?.subscribe ({
            if (autoModeTracker?.speedFilter!!.collectionEnabled) {
                val locationFix: LocationFix = LocationFix(it.latitude.toDouble(),
                        it.longitude.toDouble(),
                        it.accuracy,
                        it.speed,
                        it.bearing,
                        it.altitude.toDouble(),
                        it.time)
                LOGGER.info("Got new location fix ", funcName)
                fixProducer.onNext(listOf(locationFix))
            }
        }, {throwable ->
            LOGGER.error("throwable $throwable", funcName)
        })
    }

    fun checkPermission() {
        if (Build.VERSION.SDK_INT >= 23 ) {
            val hasForegroundLocationPermission = ActivityCompat.checkSelfPermission(this.context!!,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

            if (Build.VERSION.SDK_INT >= 29 ) {
                val hasBackgroundLocationPermission = ActivityCompat.checkSelfPermission(this.context!!,
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
                if (!hasBackgroundLocationPermission || !hasForegroundLocationPermission) {
                    return
                }
            } else {
                if (!hasForegroundLocationPermission) {
                    return
                }
            }
        }
    }
}