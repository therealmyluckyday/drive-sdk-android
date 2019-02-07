package axa.tex.drive.sdk.automode.internal

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
import axa.tex.drive.sdk.automode.AutoMode
import axa.tex.drive.sdk.automode.AutoModeState
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import java.util.*

private const val ACTIVITY_DETECTION_INTERVAL = (10 * 1000).toLong()
private const val ACTIVITY_CONFIDENCE = 75
private const val ACTIVITY_INTENT_ACTION = "axa.tex.drive.sdk.automode.ACTIVITY_DETECTED"
private const val PENDING_INTENT_REQUEST_CODE = 484
private const val SPEED_MOVEMENT_THRESHOLD = 10 * 0.28f // in m/s


 class ActivityTracker : AutoModeState {

    private var locationManager: LocationManager? = null

    override fun state(): AutoModeState.State {
        return AutoModeState.State.TRACKING_ACTIVITY
    }

    private var activityReceiver: BroadcastReceiver? = null
    private var context: Context
    private var activityRecognitionClient: ActivityRecognitionClient
    private var pendingIntent: PendingIntent

    constructor(context: Context) {
        val activityIntent = Intent(ACTIVITY_INTENT_ACTION)
        this.context = context
        activityRecognitionClient = ActivityRecognitionClient(context)
        pendingIntent = PendingIntent.getBroadcast(
                context,
                PENDING_INTENT_REQUEST_CODE,
                activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
    }

    //Just for test the simulator does not give speed for location.
    private var speedLocationListener: LocationListener? = null

    override fun scan(autoMode: AutoMode) {
        activityReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                val result = ActivityRecognitionResult.extractResult(intent)
                val activity = result.mostProbableActivity
                Toast.makeText(context, result.mostProbableActivity.toString(), Toast.LENGTH_LONG).show()
                log(context, "${Date().toString()} ${result.mostProbableActivity} \n")
                if (activity.type != DetectedActivity.IN_VEHICLE) {
                    //Not in vehicle.
                    return
                }
                if (activity.confidence < ACTIVITY_CONFIDENCE) {
                    //Not enough confidence for car activity
                    return
                }
                //Activity Receiver: going to speed scan mode
                val inVehicle = InVehicleState(autoMode, context)
                autoMode.setCurrentState(inVehicle)
                autoMode.setFormerState(this@ActivityTracker)
                locationManager?.removeUpdates(speedLocationListener)
                stopScan()
                log(context, "IN VEHICLE \n")
            }
        }

        context.registerReceiver(activityReceiver, IntentFilter(ACTIVITY_INTENT_ACTION))
        val task = activityRecognitionClient.requestActivityUpdates(ACTIVITY_DETECTION_INTERVAL, pendingIntent)
        task.addOnSuccessListener(OnSuccessListener { print("Connection succeeded!") })
        task.addOnFailureListener(OnFailureListener { print("Unable to connect!") })

        speedLocationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (location.speed >= SPEED_MOVEMENT_THRESHOLD) {
                    log(context, "${Date().toString()} Activity tracking [Vehicle speed : ] \n")
                    locationManager?.removeUpdates(speedLocationListener)
                    val inVehicle = InVehicleState(autoMode, context)
                    autoMode.setCurrentState(inVehicle)
                    autoMode.setFormerState(this@ActivityTracker)
                    stopScan()
                    log(context, "IN VEHICLE \n")
                }
                println(location)
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
        if (activityReceiver != null) {
            try {
                context.unregisterReceiver(activityReceiver)
            } catch (e: IllegalArgumentException) {
                //Receiver is already unregistered
                e.printStackTrace()
            }
        }
        activityRecognitionClient
                .removeActivityUpdates(pendingIntent)
    }

}