package axa.tex.drive.sdk.automode.internal.tracker

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.widget.Toast
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.automode.internal.tracker.model.TexLocation
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import axa.tex.drive.sdk.automode.internal.tracker.model.TexSpeed
import axa.tex.drive.sdk.automode.internal.tracker.model.TexActivity
import axa.tex.drive.sdk.automode.internal.tracker.model.Where
import io.reactivex.subjects.PublishSubject
import org.koin.android.ext.android.inject


private const val ACTIVITY_INTENT_ACTION = "axa.tex.drive.sdk.automode.ACTIVITY_DETECTED"
private const val PENDING_INTENT_REQUEST_CODE = 484
private const val ACTIVITY_DETECTION_INTERVAL = (10 * 1000).toLong()

@SuppressLint("MissingPermission")
internal class AutoModeTracker : LocationListener, TexActivityTracker, KoinComponentCallbacks{

    private val locationManager: LocationManager
    internal val speedFilter: SpeedFilter by inject()
    internal val locations: PublishSubject<Location> = PublishSubject.create()

    private lateinit var activityRecognitionClient: ActivityRecognitionClient
    private lateinit var pendingIntent: PendingIntent
    private lateinit var activityReceiver: BroadcastReceiver
    private var context : Context




    constructor(context: Context) {
        this.context = context
        this.locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager


        //speedFilter = SpeedFilter()

    }

    override fun onLocationChanged(location: Location) {
        val texSpeed = TexSpeed(location.speed, location.accuracy)
        speedFilter.locationInput.onNext(texSpeed)
        val texLocation = TexLocation(location.latitude.toFloat(),location.longitude.toFloat(),location.accuracy,location.speed,location.bearing, location.altitude.toFloat(),location.time)
        speedFilter.gpsStream.onNext(texLocation)
        speedFilter.locations.onNext(location)
        locations.onNext(location)

    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String?) {}
    override fun onProviderDisabled(provider: String?) {}

    override fun passivelyScanSpeed() {
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0L, 0.0f, this)
    }

    override fun activelyScanSpeed() {
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0.0f, this)
    }

    @SuppressLint("MissingPermission")
    override fun stopSpeedScanning() {
        locationManager.removeUpdates(this)
    }

    override fun checkWhereAmI(){

        activityRecognitionClient = ActivityRecognitionClient(context)
        val activityIntent = Intent(ACTIVITY_INTENT_ACTION)
        pendingIntent = PendingIntent.getBroadcast(
                context,
                PENDING_INTENT_REQUEST_CODE,
                activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)


        activityReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                val result = ActivityRecognitionResult.extractResult(intent)
                val activity = result.mostProbableActivity
                Toast.makeText(context, result.mostProbableActivity.toString(), Toast.LENGTH_LONG).show()

                if (activity.type == DetectedActivity.IN_VEHICLE) {
                    speedFilter.activityInput.onNext(TexActivity(Where.IN_VEHICLE, activity.confidence))
                }
            }
        }
        context.registerReceiver(activityReceiver, IntentFilter(ACTIVITY_INTENT_ACTION))
        val task = activityRecognitionClient.requestActivityUpdates(ACTIVITY_DETECTION_INTERVAL, pendingIntent)
        task.addOnSuccessListener(OnSuccessListener { print("Connection succeeded!") })
        task.addOnFailureListener(OnFailureListener { print("Unable to connect!") })

    }

    override fun stopActivityScanning(){
        activityRecognitionClient.removeActivityUpdates(pendingIntent)
    }

}