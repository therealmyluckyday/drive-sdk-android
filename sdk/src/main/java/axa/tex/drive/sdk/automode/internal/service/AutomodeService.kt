package axa.tex.drive.sdk.automode.internal.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
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
    private var automodeHandler: AutomodeHandler? = null
    private val binder = LocalBinder()
    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger

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
            val channelId = createNotificationChannel()
            val notification: Notification
            if (intent != null && intent.hasExtra("notif")) {
                notification = intent.getParcelableExtra("notif")!!
            } else {
                val notificationBuilder = NotificationCompat.Builder(this, channelId)
                notification = notificationBuilder.setOngoing(true).setSmallIcon(R.drawable.notification_icon_background)
                        .setCategory(Notification.CATEGORY_SERVICE)
                        .build()
            }
            this.startForeground(NOTIFICATION_ID, notification)
        }
        val automodeHandler : AutomodeHandler by inject()
        this.automodeHandler = automodeHandler
        this.automodeHandler!!.running = true

        return START_STICKY
    }


    private fun activateAutomode() {

        // Get a handler that can be used to post to the main thread
        val mainHandler = Handler(Looper.getMainLooper())
        val myRunnable = Runnable() {

            try {
                TexConfig.setupKoin(applicationContext)
            } catch (e: Exception) {
                LOGGER.error("${e.printStackTrace().toString()}", "activateAutomode")
            }

            val automode: Automode by inject()
            val tripRecorder: TripRecorder by inject()
            if (tripRecorder.isRecording()) {
                automode.setCurrentState(DrivingState(automode))
            } else {
                automode.next()
            }
        }
        mainHandler.post(myRunnable);
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }
}
