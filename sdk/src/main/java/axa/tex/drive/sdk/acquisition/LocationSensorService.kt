package axa.tex.drive.sdk.acquisition

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.HandlerThread
import android.os.Looper
import androidx.core.app.ActivityCompat
import axa.tex.drive.sdk.automode.internal.tracker.SpeedFilter
import axa.tex.drive.sdk.automode.internal.tracker.model.TexLocation
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.logger.LoggerFactory
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.tasks.OnCompleteListener
import io.reactivex.Scheduler
import org.koin.android.ext.android.inject


class LocationSensorService: LocationCallback, LocationListener, KoinComponentCallbacks {
    private val fusedLocationClient: FusedLocationProviderClient?
    val locationManager: LocationManager
    internal val speedFilter: SpeedFilter by inject()
    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
    private var context: Context
    val looper : Looper
    private val locationRequest: LocationRequest = LocationRequest()
    private var locationCallback: LocationCallback = LocationCallback()

    constructor(context: Context, scheduler: Scheduler, mFusedLocationClient: FusedLocationProviderClient?) {
        this.context = context
        this.locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        this.fusedLocationClient = mFusedLocationClient
        val handlerThread = HandlerThread("MyHandlerThread")
        handlerThread.start()
        // Now get the Looper from the HandlerThread
        // NOTE: This call will block until the HandlerThread gets control and initializes its Looper
        // Now get the Looper from the HandlerThread
        // NOTE: This call will block until the HandlerThread gets control and initializes its Looper

        looper = handlerThread.looper

        locationRequest.setFastestInterval(0)
        locationRequest.setInterval(1000)
        locationRequest.setPriority(PRIORITY_HIGH_ACCURACY)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                val locations = locationResult?.locations
                if (locations != null && locations!!.isNotEmpty()) {
                    LOGGER.info("\"Location callback", "onLocationResult")
                    val location = locations[0]
                    sendLocation(location)
                }
            }
        }
    }

    fun passivelyScanSpeed() {
        LOGGER.info("\"", "passivelyScanSpeed")
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
        LOGGER.info("\"", "activelyScanSpeed")
        try {
            // Request location updates
            if (ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                LOGGER.error("checkSelfPermission", "activelyScanSpeed")
                return
            }

            fusedLocationClient!!.requestLocationUpdates(locationRequest, locationCallback, looper)
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0.0f, this, looper)
            LOGGER.info("requestLocationUpdates", "activelyScanSpeed")
        } catch (ex: Exception) {
            LOGGER.error("Security Exception, no location available" + ex, "activelyScanSpeed")
        }
    }

    fun stopSpeedScanning() {
        LOGGER.info("\"STOP SCANNING ", "stopSpeedScanning")
        locationManager.removeUpdates(this)
        fusedLocationClient!!.removeLocationUpdates(locationCallback)
    }

    fun sendLocation(location: Location) {
        LOGGER.info("\"Location ", "sendLocation")
        val texLocation = TexLocation(location.latitude.toFloat(), location.longitude.toFloat(), location.accuracy, location.speed, location.bearing, location.altitude.toFloat(), location.time)
        speedFilter.gpsStream.onNext(texLocation)
        speedFilter.locations.onNext(location)
    }

    // LocationListener,
    override fun onLocationChanged(location: Location) {
        LOGGER.info("\"Location ", "onLocationChanged")
        sendLocation(location)
    }
    //LocationListener,
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        LOGGER.info("\"Status $status", "onStatusChanged")
    }
    //LocationListener,
    override fun onProviderEnabled(provider: String?) {
        LOGGER.info("", "onProviderEnabled")
    }
    // LocationListener,
    override fun onProviderDisabled(provider: String?) {
        LOGGER.info("", "onProviderDisabled")
    }

    // LocationCallback
    override fun onLocationAvailability(p0: LocationAvailability?) {
        super.onLocationAvailability(p0)
    }
}