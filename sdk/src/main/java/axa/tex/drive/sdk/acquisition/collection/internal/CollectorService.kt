package axa.tex.drive.sdk.acquisition.collection.internal

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import axa.tex.drive.sdk.core.Constants
import axa.tex.drive.sdk.core.TexConfig

import org.koin.android.ext.android.inject
import java.util.*

private const val NOTIFICATION_ID = 7071

internal class CollectorService : Service() {

    companion object {

        private var running = false
        private var tripId = ""

        fun isRunning() : Boolean{
            return running
        }

        fun currentTripId() : String{
            return tripId
        }
    }

    private val binder = LocalBinder()
    private var collector : axa.tex.drive.sdk.acquisition.collection.internal.Collector? = null;

    inner class LocalBinder : Binder() {
        internal val service: CollectorService
            get() = this@CollectorService
    }

    override fun onBind(intent: Intent?): IBinder {
       return binder;
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        TexConfig.init(applicationContext)
        val collector: axa.tex.drive.sdk.acquisition.collection.internal.Collector by inject()
        this.collector =  collector
        collector.collect()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val notification: Notification
            if (intent != null && intent.hasExtra("")) {
                notification = intent.getParcelableExtra("")
            } else {
                val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val notificationChannel = NotificationChannel(Constants.CHANNEL_ID, Constants.CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
                notificationManager.createNotificationChannel(notificationChannel)
                notification = Notification.Builder(applicationContext, Constants.CHANNEL_ID).build()
            }
            this.startForeground(NOTIFICATION_ID, notification)
        }

        running = true
        if(tripId.isEmpty()) {
            tripId = UUID.randomUUID().toString().toUpperCase(Locale.US)
        }
        return START_STICKY//super.onStartCommand(intent, flags, startId)
    }


    fun stopCollectorService(){
        collector?.stopCollecting()
       stopSelf()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true)
        }
        running = false

    }

    internal fun numberOfTrackers() : Int{
        return collector?.numberOfTrackers()!!
    }
}