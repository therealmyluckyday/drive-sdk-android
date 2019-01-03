package axa.tex.drive.sdk.automode.internal

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import axa.tex.drive.sdk.automode.AutoMode
import axa.tex.drive.sdk.automode.AutoModeState

private const val LOCATION_ACCURACY_THRESHOLD = 20
private const val SPEED_START_THRESHOLD = 20 * 0.28f

class SpeedTracking : AutoModeState {
    override fun stopScan() {
    }

    private var context: Context? = null
    private var locationManager: LocationManager? = null

    constructor(context: Context?) {
        this.context = context
        locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
    }

    override fun scan(autoMode: AutoMode) {
        val speedLocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {

                if (location.accuracy > LOCATION_ACCURACY_THRESHOLD) {
                    return
                }
                if (location.speed > SPEED_START_THRESHOLD) {

                    locationManager?.removeUpdates(this)
                    autoMode.setCurrentState(Driving())
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