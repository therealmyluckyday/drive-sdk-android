package axa.tex.drive.sdk.acquisition.internal.tracker

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import axa.tex.drive.sdk.acquisition.internal.tracker.fake.FakeLocationTracker
import axa.tex.drive.sdk.acquisition.model.LocationFix
import axa.tex.drive.sdk.acquisition.model.Fix
import axa.tex.drive.sdk.core.logger.LoggerFactory
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject


class LocationTracker : LocationListener, Tracker {
    internal val LOGGER = LoggerFactory.getLogger().logger
    private val fixProducer: PublishSubject<Fix> = PublishSubject.create()
    private var context : Context? = null;
    private var locationManager: LocationManager? = null
    private var isEnabled : Boolean
    private var fakeLocationTracker: FakeLocationTracker? = null

    constructor(context: Context?, isEnabled : Boolean = false, fakeLocationTracker: FakeLocationTracker? = null) {
        LOGGER.info("Creating location tracker", "LocationTracker", "constructor(context: Context?, isEnabled : Boolean = false, fakeLocationTracker: FakeLocationTracker? = null)")
        this.isEnabled = isEnabled
        locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        this.context = context
        this.fakeLocationTracker = fakeLocationTracker
        LOGGER.info("Location tracker created", "LocationTracker", "constructor(context: Context?, isEnabled : Boolean = false, fakeLocationTracker: FakeLocationTracker? = null)")

        if(fakeLocationTracker == null){
            LOGGER.info("Uses real sensor for tracking location", "LocationTracker", "constructor(context: Context?, isEnabled : Boolean = false, fakeLocationTracker: FakeLocationTracker? = null)")

        }else{
            LOGGER.info("Uses fake sensor for tracking location", "LocationTracker", "constructor(context: Context?, isEnabled : Boolean = false, fakeLocationTracker: FakeLocationTracker? = null)")

        }

    }

    constructor(context: Context, isEnabled : Boolean = false) {
        this.isEnabled = isEnabled
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        this.context = context
    }


    override fun disableTracking() {
        LOGGER.info("Disable locationTracking", "LocationTracker", "disableTracking")

        isEnabled = false
        enableTracking(false)



    }

    override fun enableTracking() {
        LOGGER.info("Enabling locationTracking", "LocationTracker", "override fun enableTracking()")
        isEnabled = true
        enableTracking(true)
    }

    private fun enableTracking(track: Boolean) {
        if(fakeLocationTracker != null){
            val fakeLocations = fakeLocationTracker?.provideFixProducer() as Observable<Fix>
            fakeLocations.subscribe {fix  ->
                val fakeLocation = fix as LocationFix
                LOGGER.info("Sending fake location", "LocationTracker", "private fun enableTracking(track: Boolean)")
                fixProducer.onNext(fakeLocation)
            }
            fakeLocationTracker?.enableTracking()
        }else {
            if (track) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (context?.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            context?.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        LOGGER.error("Permissions for location are not available", "LocationTracker", "private fun enableTracking(track: Boolean)")
                        return;
                    }
                }

                locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, this)
                LOGGER.info("location Tracker enabled", "LocationTracker", "private fun enableTracking(track: Boolean)")
            } else {
                locationManager?.removeUpdates(this)
                LOGGER.info("location Tracker disabled", "LocationTracker", "private fun enableTracking(track: Boolean)")
            }
        }
    }

    override fun provideFixProducer(): Any {
        return fixProducer
    }

    override fun onLocationChanged(location: Location) {
        LOGGER.info("Receives location from sensor", "LocationTracker", "override fun onLocationChanged(location: Location)")
        var speed = location.speed
        if(!location.hasSpeed()){
            speed = -1f
        }
        val locationFix = LocationFix(location.latitude,
                location.longitude,
                location.accuracy,
                speed,
                location.bearing,
                location.altitude,
                location.time)
        LOGGER.info("Sending location", "LocationTracker", "override fun onLocationChanged(location: Location)")
        fixProducer.onNext(locationFix)
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
    }

    override fun onProviderEnabled(provider: String?) {
    }

    override fun onProviderDisabled(provider: String?) {
    }

    override fun isEnabled() : Boolean{
        return isEnabled
    }
}