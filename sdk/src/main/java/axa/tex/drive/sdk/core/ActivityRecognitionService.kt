package axa.tex.drive.sdk.core

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import axa.tex.drive.sdk.core.logger.LoggerFactory
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import io.reactivex.Scheduler
import io.reactivex.subjects.PublishSubject

private const val ACTIVITY_INTENT_ACTION = "axa.tex.drive.sdk.automode.ACTIVITY_DETECTED"
private const val PENDING_INTENT_REQUEST_CODE = 484
private const val ACTIVITY_DETECTION_INTERVAL = (10 * 1000).toLong()
class ActivityRecognitionService {
    internal val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
    private lateinit var activityRecognitionClient: ActivityRecognitionClient
    private lateinit var pendingIntent: PendingIntent
    private lateinit var activityReceiver: BroadcastReceiver
    private var context: Context

    constructor(context: Context, scheduler: Scheduler) {
        this.context = context
    }

    fun startActivityScanning(activityStream: PublishSubject<DetectedActivity>) {

        LOGGER.info("\"Create activityRecognitionClient", "startActivityScanning")
        activityRecognitionClient = ActivityRecognition.getClient(context)
        val activityIntent = Intent(ACTIVITY_INTENT_ACTION)
        pendingIntent = PendingIntent.getBroadcast(
                context,
                PENDING_INTENT_REQUEST_CODE,
                activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)


        activityReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val result = ActivityRecognitionResult.extractResult(intent)
                result?.also {
                    val activity: DetectedActivity = result.mostProbableActivity
                    LOGGER.info("\"ActivityRecognition: "+result.mostProbableActivity.toString(), "addOnSuccessListener")
                    var isSimulateDriving = true
                    if (isSimulateDriving) {
                        LOGGER.info("\"isSimulateDriving", "startActivityScanning")
                        val confidence = 100
                        activityStream.onNext(DetectedActivity(DetectedActivity.IN_VEHICLE, confidence))
                    }
                    else {
                        activityStream.onNext(activity)
                    }
                }

            }
        }
        context.registerReceiver(activityReceiver, IntentFilter(ACTIVITY_INTENT_ACTION))
        val task = activityRecognitionClient.requestActivityUpdates(ACTIVITY_DETECTION_INTERVAL, pendingIntent)
        task.addOnSuccessListener(OnSuccessListener {
            LOGGER.info("\"Connection succeeded!", "addOnSuccessListener")
        })
        task.addOnFailureListener(OnFailureListener {
            LOGGER.info("\"Connection Failed! " + it, "addOnFailureListener") })

    }

    fun stopActivityScanning() {
        activityRecognitionClient.removeActivityUpdates(pendingIntent)
    }
}