package axa.tex.drive.demo

import android.app.Application
import android.widget.Toast
import axa.tex.drive.sdk.acquisition.PermissionException
import axa.tex.drive.sdk.acquisition.TripRecorder
import axa.tex.drive.sdk.automode.AutomodeHandler
import axa.tex.drive.sdk.core.Platform
import axa.tex.drive.sdk.core.TexConfig
import axa.tex.drive.sdk.core.TexService
import java.util.*


class TexDriveDemoApplication : Application() {
    var tripRecorder: TripRecorder? = null
    private var config: TexConfig? = null
    var service: TexService? = null

    override fun onCreate() {
        super.onCreate()
        // Fabric.with(this, Crashlytics())

        config = TexConfig.Builder(applicationContext, "APP-TEST", "22910000").enableTrackers().platformHost(Platform.PREPROD).build();


        service = TexService.configure(config!!)
        tripRecorder = service?.getTripRecorder();


        val autoModeHandler = service?.automodeHandler()
        autoModeHandler?.state?.subscribe {
            if (it == AutomodeHandler.State.DRIVING) {
                if (!tripRecorder?.isRecording()!!) {
                    startTheService()
                }
            } else {
                if (tripRecorder?.isRecording()!!) {
                    stopTheService()

                }
            }

        }

        if(!autoModeHandler?.running!!) {
            Toast.makeText(applicationContext, "ACTIVATING.....", Toast.LENGTH_SHORT).show()
            autoModeHandler?.activateAutomode(applicationContext)
        }else{
            Toast.makeText(applicationContext, "Already running.....", Toast.LENGTH_SHORT).show()
        }
    }


    private fun startTheService() {


        /*  var notification : Notification? = null
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
              val channelId = applicationContext.createNotificationChannel();
              val notificationBuilder = NotificationCompat.Builder(this, channelId)
              notification  = notificationBuilder.setOngoing(true)
                      .setSmallIcon(R.drawable.white_hare)
                      .setCategory(Notification.CATEGORY_SERVICE)
                      .build()
          }

          tripRecorder?.setCustomNotification(notification)*/

        try {
            val tripId = tripRecorder?.startTracking(Date().time);
            println(tripId)
        } catch (e: PermissionException) {
            e.printStackTrace()
        }

    }

    private fun stopTheService() {
        Thread {
            try {
                tripRecorder?.stopTracking(Date().time)
            } catch (e: PermissionException) {
                e.printStackTrace()
            }
        }.start()

    }
}