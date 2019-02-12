package axa.tex.drive.sdk.automode.internal

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import axa.tex.drive.sdk.automode.AutoMode
import axa.tex.drive.sdk.automode.AutoModeState
import java.util.*
import kotlin.concurrent.schedule

private const val LOCATION_ACCURACY_THRESHOLD = 20
private const val SPEED_START_THRESHOLD = 20 * 0.28f

internal class SpeedTracking : AutoModeState {
    override fun stopScan() {
    }

    private var context: Context
    private var locationManager: LocationManager? = null
    private val timeForScanning = 3 * 1000 * 60L
    private var scanningStop = false


    constructor(autoMode: AutoMode, context: Context) {
        this.context = context
        locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        log(context, "${Date().toString()}  [SpeedTracking : Wait for speed of 5.6 kms/h ]\n")
        Timer("Time for scanning", false).schedule(timeForScanning) {
            if (scanningStop) {
                //Nothing to do
                autoMode.setCurrentState(InVehicleState(autoMode, context))
                autoMode.setFormerState(this@SpeedTracking)
            } else {

            }
        }
    }

    override fun scan(autoMode: AutoMode) {
        val speedLocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                Toast.makeText(context, "Speed tracking: speed = ${location.speed}", Toast.LENGTH_SHORT).show()
                if (location.accuracy > LOCATION_ACCURACY_THRESHOLD) {
                    return
                }
                if (location.speed > SPEED_START_THRESHOLD) {
                    log(context, "${Date().toString()}  [SpeedTracking : 5.6 kms/h  reached]\n")
                    scanningStop = false
                    locationManager?.removeUpdates(this)
                    autoMode.setFormerState(this@SpeedTracking)
                    if (context != null) {
                        autoMode.setCurrentState(Driving(context))
                    }
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
        return AutoModeState.State.SCANNING_SPEED
    }
}