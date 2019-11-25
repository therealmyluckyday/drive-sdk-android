package axa.tex.drive.sdk.automode.internal.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import axa.tex.drive.sdk.R
import axa.tex.drive.sdk.acquisition.TripRecorder
import axa.tex.drive.sdk.automode.AutomodeHandler
import axa.tex.drive.sdk.automode.internal.Automode
import axa.tex.drive.sdk.automode.internal.states.DrivingState
import axa.tex.drive.sdk.core.TexConfig
import axa.tex.drive.sdk.core.logger.LoggerFactory
import org.koin.android.ext.android.inject


private const val NOTIFICATION_ID = 7071

internal class AutomodeService : Service() {
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String {
        val channelId = "10001"
        val channelName = "Tracker"
        val chan = NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        activateAutomode()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = createNotificationChannel();
            val notification: Notification
            if (intent != null && intent.hasExtra("notif") && intent.getParcelableExtra("notif") as Notification != null) {
                notification = intent.getParcelableExtra("notif")
            } else {
                val notificationBuilder = NotificationCompat.Builder(this, channelId)
                notification = notificationBuilder.setOngoing(true).setSmallIcon(R.drawable.notification_icon_background)
                        .setCategory(Notification.CATEGORY_SERVICE)
                        .build()
            }
            this.startForeground(NOTIFICATION_ID, notification)
        }

        val automodeHandler: AutomodeHandler by inject()
        automodeHandler.running = true

        return START_STICKY
    }


    fun stopCollectorService() {
        stopSelf()
    }

    private fun activateAutomode() {
        try {
            TexConfig.setupKoin(applicationContext)
        } catch (e: Exception) {
            //LOGGER.error("${e.printStackTrace().toString()}", "activateAutomode")
        }

        val automode: Automode by inject()
        val tripRecorder: TripRecorder by inject()
        if (tripRecorder.isRecording()) {
            automode.setCurrentState(DrivingState(automode))
        } else {
            automode.next()
        }
    }

}
