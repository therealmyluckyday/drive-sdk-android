package axa.tex.drive.sdk.automode.internal

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import axa.tex.drive.sdk.automode.AutoMode
import axa.tex.drive.sdk.automode.AutoModeState
import java.util.*
import kotlin.concurrent.schedule

private const val SPEED_START_THRESHOLD = 20 * 0.28f
private const val LOCATION_ACCURACY_THRESHOLD = 20
private const val TIME_TO_WAIT_FOR_SPEED = 1000 * 60 * 3L
private const val TIME_TO_WAIT_FOR_GPS = 1000 * 60 * 4L

 internal class Driving : AutoModeState {

    private var context: Context
    private var locationManager: LocationManager? = null
    private var speedWatcher: TimerTask? = null
    private var gpsWatcher: TimerTask? = null
    var speedLocationListener: LocationListener? = null

    constructor(context: Context) {
        log(context, "${Date().toString()}  [Driving [ Vrooooo...oooommm ]\n")
        this.context = context
        locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?


    }

    private fun watchSpeed(autoMode: AutoMode, timeForScanning: Long) {
        speedWatcher = Timer("Time for scanning", false).schedule(timeForScanning) {
            log(context, "${Date().toString()}  [Driving: low Speed, Going back to IDLE state ]\n")
            autoMode.setCurrentState(IdleState(context))
            locationManager?.removeUpdates(speedLocationListener)
        }
    }

    private fun cancelSpeedWatcher() {
        if (speedWatcher != null) {
            speedWatcher?.cancel()
            speedWatcher = null
        }
    }

    private fun watchGps(autoMode: AutoMode, timeForGps: Long) {
        gpsWatcher = Timer("Time for gps", false).schedule(timeForGps) {
            log(context, "${Date().toString()}  [Driving: NO gps, Going back to IDLE state]\n")
            autoMode.setCurrentState(IdleState(context))
            locationManager?.removeUpdates(speedLocationListener)
        }
    }

    private fun cancelGpsWatcher() {
        if (gpsWatcher != null) {
            gpsWatcher?.cancel()
            gpsWatcher = null
        }
    }

    override fun stopScan() {

    }


    override fun scan(autoMode: AutoMode) {
        speedLocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                cancelGpsWatcher()
                watchGps(autoMode, TIME_TO_WAIT_FOR_GPS)
                log(context, "${Date().toString()}  [ Driving My Speed = ${location.speed} and the location accuracy : ${location.accuracy}]\n")
                if (location.accuracy > LOCATION_ACCURACY_THRESHOLD) {
                    log(context, "${Date().toString()}  [ Driving : Locations with low accuracy= ${location.accuracy}] \n")
                    return
                }
                //Speed  < 10km/h
                if (location.speed < SPEED_START_THRESHOLD) {
                    log(context, "${Date().toString()}  [Driving: speed is less than 10 kms/h:  ${location.speed} ]\n")
                    watchSpeed(autoMode, TIME_TO_WAIT_FOR_SPEED)
                } else {
                    cancelSpeedWatcher()
                }
            }

            override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
            override fun onProviderEnabled(s: String) {}
            override fun onProviderDisabled(s: String) {}
        }

        try {
            locationManager?.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0L, 0f, speedLocationListener)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun state(): AutoModeState.State {
        return AutoModeState.State.DRIVING
    }
}
