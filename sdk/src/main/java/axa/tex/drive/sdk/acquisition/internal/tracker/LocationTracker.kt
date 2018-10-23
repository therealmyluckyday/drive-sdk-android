package axa.tex.drive.sdk.acquisition.internal.tracker

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import axa.tex.drive.sdk.acquisition.internal.tracker.fake.FakeLocationTracker
import axa.tex.drive.sdk.acquisition.internal.tracker.fake.model.FakeLocation
import axa.tex.drive.sdk.acquisition.model.LocationFix
import axa.tex.drive.sdk.acquisition.model.Fix
import axa.tex.drive.sdk.core.internal.utils.Utils
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject


class LocationTracker : LocationListener, Tracker {

    private val fixProducer: PublishSubject<Fix> = PublishSubject.create()
    private var context : Context? = null;
    private var locationManager: LocationManager? = null
    private var isEnabled : Boolean
    private var fakeLocationTracker: FakeLocationTracker? = null

    constructor(context: Context?, isEnabled : Boolean = false, fakeLocationTracker: FakeLocationTracker? = null) {
        this.isEnabled = isEnabled
        locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        this.context = context
        this.fakeLocationTracker = fakeLocationTracker
    }

    constructor(context: Context, isEnabled : Boolean = false) {
        this.isEnabled = isEnabled
        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        this.context = context
    }


    override fun disableTracking() {
        isEnabled = false
        enableTracking(false)
    }

    override fun enableTracking() {
        isEnabled = true
        enableTracking(true)
    }

    private fun enableTracking(track: Boolean) {
        if(fakeLocationTracker != null){
            val fakeLocations = fakeLocationTracker?.provideFixProducer() as Observable<Fix>
            fakeLocations.subscribe {fix  ->
                val fakeLocation = fix as LocationFix
                fixProducer.onNext(fakeLocation)
            }
            fakeLocationTracker?.enableTracking()
        }else {
            if (track) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (context?.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            context?.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }

                locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, this)
            } else {
                locationManager?.removeUpdates(this)
            }
        }
    }

    override fun provideFixProducer(): Any {
        return fixProducer
    }

    override fun onLocationChanged(location: Location) {

        val locationFix = LocationFix(location.latitude,
                location.longitude,
                location.accuracy,
                location.speed,
                location.bearing,
                location.altitude,
                location.time)
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