package axa.tex.drive.sdk.acquisition.internal.tracker

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import axa.tex.drive.sdk.acquisition.model.LocationFix
import axa.tex.drive.sdk.acquisition.model.Data
import io.reactivex.subjects.PublishSubject


class LocationTracker : LocationListener, Tracker {


    private val fixProducer: PublishSubject<Data> = PublishSubject.create()
    private val context : Context;
    private var locationManager: LocationManager? = null
    private var isEnabled : Boolean

    constructor(context: Context, isEnabled : Boolean = false) {
        this.isEnabled = isEnabled
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        this.context = context
    }


    override fun disableTracking() {
        enableTracking(false)
    }

    override fun enableTracking() {
        enableTracking(true)
    }

    private fun enableTracking(track: Boolean) {
        if(track) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            }

            locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, this)
        }else{
            locationManager?.removeUpdates(this)
        }
    }

    override fun provideFixProducer(): Any {
        return fixProducer
    }

    override fun onLocationChanged(location: Location) {

        val locationFix = LocationFix(location.latitude, location.longitude, location.accuracy, location.speed, location.bearing, location.altitude, location.time);
        fixProducer.onNext(Data(locationFix.timestamp,location=locationFix))
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