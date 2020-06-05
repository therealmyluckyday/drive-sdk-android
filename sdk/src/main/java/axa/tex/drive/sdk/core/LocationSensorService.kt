package axa.tex.drive.sdk.core

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.app.ActivityCompat
import axa.tex.drive.sdk.automode.internal.tracker.SpeedFilter
import axa.tex.drive.sdk.automode.internal.tracker.model.TexLocation
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.logger.LoggerFactory
import io.reactivex.Scheduler
import io.reactivex.subjects.PublishSubject
import org.koin.android.ext.android.inject

class LocationSensorService: LocationListener, KoinComponentCallbacks {
    val locationManager: LocationManager
    internal val speedFilter: SpeedFilter by inject()
    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
    private var context: Context

    constructor(context: Context, scheduler: Scheduler) {
        this.context = context
        this.locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    fun passivelyScanSpeed() {
        if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        this.locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0L, 0.0f, this)
    }

    fun activelyScanSpeed() {
        try {
            // Request location updates
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0.0f, this)
            //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0.0f, this)
        } catch(ex: SecurityException) {
            LOGGER.error("Security Exception, no location available", "activelyScanSpeed")
        }
    }

    fun stopSpeedScanning() {
        locationManager.removeUpdates(this)
    }

    // LocationListener,
    override fun onLocationChanged(location: Location) {
        //  mc hdf fprint("trip location changed \n")
        //LOGGER.info("\"Location ", "onLocationChanged")
        val texLocation = TexLocation(location.latitude.toFloat(), location.longitude.toFloat(), location.accuracy, location.speed, location.bearing, location.altitude.toFloat(), location.time)
        speedFilter.gpsStream.onNext(texLocation)
        speedFilter.locations.onNext(location)
    }
    //LocationListener,
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        LOGGER.info("\"Status $status", "onStatusChanged")
        print("LocationSensorService"+ "\"Status $status")
    }
    //LocationListener,
    override fun onProviderEnabled(provider: String?) {
        LOGGER.info("", "onProviderEnabled")
        print("LocationSensorService"+ "\"provider $provider")
    }
    // LocationListener,
    override fun onProviderDisabled(provider: String?) {
        LOGGER.info("", "onProviderDisabled")
        print("LocationSensorService"+ "\"provider $provider")
    }

}