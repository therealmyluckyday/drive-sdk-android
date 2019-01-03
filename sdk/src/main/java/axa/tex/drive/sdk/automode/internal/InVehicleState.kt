package axa.tex.drive.sdk.automode.internal

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import axa.tex.drive.sdk.automode.AutoMode
import axa.tex.drive.sdk.automode.AutoModeState

//2.77777777778m/s about 3m/s = 10km/h
private const val LOCATION_ACCURACY_THRESHOLD = 20
private const val SPEED_MOVEMENT_THRESHOLD = 10 * 0.28f
private const val SPEED_START_THRESHOLD = 20 * 0.28f

class InVehicleState : AutoModeState {


    private var context: Context? = null
    private var locationManager: LocationManager? = null

    constructor(context: Context) {
        this.context = context
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
    }


    override fun scan(autoMode: AutoMode) {
        val speedLocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {

                if (location.accuracy > LOCATION_ACCURACY_THRESHOLD) {
                    return
                }
                //Speed  > 10km/h
                if (location.speed > SPEED_MOVEMENT_THRESHOLD) {
                    autoMode.setCurrentState(SpeedTracking(context))
                    autoMode.setFormerState(this@InVehicleState)
                    locationManager?.removeUpdates(this)

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

    override fun stopScan() {
    }

    override fun state(): AutoModeState.State {
        return AutoModeState.State.IN_VEHICLE
    }
}