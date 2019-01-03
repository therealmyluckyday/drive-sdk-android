package axa.tex.drive.sdk.automode.internal

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast
import axa.tex.drive.sdk.automode.AutoMode
import axa.tex.drive.sdk.automode.AutoModeState
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener

private const val ACTIVITY_DETECTION_INTERVAL = (10 * 1000).toLong()
private const val ACTIVITY_CONFIDENCE = 75
private const val ACTIVITY_INTENT_ACTION = "axa.tex.drive.sdk.automode.ACTIVITY_DETECTED"
private const val PENDING_INTENT_REQUEST_CODE = 484


class ActivityTracker : AutoModeState {
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
    }


    override fun scan(autoMode: AutoMode) {

        activityReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                val result = ActivityRecognitionResult.extractResult(intent)
                val activity = result.mostProbableActivity
                Toast.makeText(context, result.mostProbableActivity.toString(), Toast.LENGTH_LONG).show()
                if (activity.type != DetectedActivity.IN_VEHICLE) {
                    return
                }
                if (activity.confidence < ACTIVITY_CONFIDENCE) {
                    // mLogger.trace("Not enough confidence for car activity: {}", activity.confidence)
                    return
                }
                // mLogger.debug("Activity Receiver: going to speed scan mode")
                val inVehicle = InVehicleState(context)
                autoMode.setCurrentState(inVehicle)
                autoMode.setFormerState(this@ActivityTracker)
                stopScan()
            }
        }

        context.registerReceiver(activityReceiver, IntentFilter(ACTIVITY_INTENT_ACTION))
        val task = activityRecognitionClient.requestActivityUpdates(ACTIVITY_DETECTION_INTERVAL, pendingIntent)
        task.addOnSuccessListener(OnSuccessListener { print("Connection succeeded!") })
        task.addOnFailureListener(OnFailureListener { print("Unable to connect!") })
    }

    override fun stopScan() {
        if (activityReceiver != null) {
            context.unregisterReceiver(activityReceiver)
        }

        activityRecognitionClient
                .removeActivityUpdates(pendingIntent)
    }
}