package axa.tex.drive.sdk.acquisition.internal.sensor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.app.ActivityCompat
import axa.tex.drive.sdk.acquisition.model.Fix
import axa.tex.drive.sdk.acquisition.model.LocationFix
import axa.tex.drive.sdk.automode.internal.Automode
import axa.tex.drive.sdk.automode.internal.tracker.SpeedFilter
import axa.tex.drive.sdk.acquisition.SensorService
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.logger.LoggerFactory
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject


internal class LocationSensor : TexSensor, KoinComponentCallbacks {

    private var lastLocation: Location? = null
    internal val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
    private var context: Context? = null
    private val fixProducer: PublishSubject<List<Fix>> = PublishSubject.create()
    private val speedFilter: SpeedFilter

    var isEnable: Boolean = false
    var canBeEnabled: Boolean = true

    private val sensorService: SensorService
    var automode: Automode

    override fun disableSensor() {
        LOGGER.info("disabling tracker", "override fun disableSensor()")

        enableTracking(false)
    }

    override fun enableSensor() {
            LOGGER.info("enabling tracker"+canBeEnabled, "override fun enableSensor()")
        if (canBeEnabled && !isEnable) {
            LOGGER.info("enabling tracker", "override fun enableSensor()")
            enableTracking(true)
        }
    }

    override fun isEnabled(): Boolean {
        return isEnable
    }

    override fun canBeEnabled(): Boolean {
        return canBeEnabled
    }


    constructor(automode: Automode, sensorService: SensorService, speedFilter: SpeedFilter, context: Context?, canBeEnabled: Boolean = true) {
        LOGGER.info("context "+context, "constructor")
        this.context = context
        this.canBeEnabled = canBeEnabled
        this.automode = automode
        this.sensorService = sensorService
        this.speedFilter = speedFilter
        LOGGER.info(" rxScheduler"+speedFilter.rxScheduler, "constructor")
    }

    override fun producer(): Observable<List<Fix>> {
        return fixProducer
    }

    private fun enableTracking(track: Boolean) {
        val funcName = "enableTracking"
        LOGGER.info("location Tracker enabling: $track", funcName)
        if (isEnable != track) {
            isEnable = track
            if (track) {
                LOGGER.info("location Tracker enabled", funcName)
                subscribeGPSSTream()
                checkPermission()
                sensorService.activelyScanSpeed()
            } else {
                sensorService.stopSpeedScanning()
            }
            speedFilter.collectionEnabled = track
            LOGGER.info("location Tracker enabled: $track", funcName)
        } else {
            LOGGER.error("Already done: $track", funcName)
        }
    }

    fun subscribeGPSSTream() {
        val funcName = "subscribeGPSSTream"
        LOGGER.info("subscribeGPSSTream "+speedFilter.rxScheduler, funcName)
        LOGGER.info("speedFilter "+speedFilter, funcName)
        val scheduler: Scheduler = if (speedFilter.rxScheduler != null) speedFilter.rxScheduler!! else Schedulers.io()
        speedFilter.gpsStream.subscribeOn(scheduler).observeOn(Schedulers.io())!!.subscribe ({
            LOGGER.info("Got new location fix ", funcName)
            if (speedFilter.collectionEnabled) {
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

        LOGGER.info("checkPermission "+Build.VERSION.SDK_INT, "checkPermission")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            val hasForegroundLocationPermission = ActivityCompat.checkSelfPermission(this.context!!,
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    LOGGER.info("hasForegroundLocationPermission "+hasForegroundLocationPermission, "checkPermission")
        }
    }
}