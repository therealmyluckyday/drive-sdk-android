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
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.view.View
import axa.tex.drive.sdk.R
import axa.tex.drive.sdk.acquisition.collection.internal.db.CollectionDb
import axa.tex.drive.sdk.acquisition.score.ScoreRetriever
import axa.tex.drive.sdk.core.internal.Constants
import axa.tex.drive.sdk.core.TexConfig
import axa.tex.drive.sdk.core.logger.LoggerFactory
import axa.tex.drive.sdk.internal.extension.toJson
import io.reactivex.subjects.PublishSubject

import org.koin.android.ext.android.inject
import org.koin.standalone.StandAloneContext
import java.util.*

private const val NOTIFICATION_ID = 7071

internal class CollectorService : Service() {
    internal val LOGGER = LoggerFactory.getLogger().logger
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


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String{
        val channelId = Constants.CHANNEL_ID
        val channelName = Constants.CHANNEL_NAME
        val chan = NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val config  = CollectionDb.getConfig(applicationContext)
        try {
            StandAloneContext.closeKoin()
        }catch (e : Exception){

        }

        TexConfig.init(applicationContext, config)
        val collector: axa.tex.drive.sdk.acquisition.collection.internal.Collector by inject()
        this.collector =  collector
        collector.collect()


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = createNotificationChannel();
            val notification: Notification
            if (intent != null && intent.hasExtra("")) {
                notification = intent.getParcelableExtra("")
            } else {
                val notificationBuilder = NotificationCompat.Builder(this, channelId )
                notification = notificationBuilder.setOngoing(true)
                        .setSmallIcon(R.drawable.notification_icon_background)
                        .setCategory(Notification.CATEGORY_SERVICE)
                        .build()
            }
            this.startForeground(NOTIFICATION_ID, notification)
        }

        running = true
        if(tripId.isEmpty()) {
            tripId = UUID.randomUUID().toString().toUpperCase(Locale.US)
        }


        try {
            ScoreRetriever.getAvailableScoreListener().subscribe{tripId->
                ScoreRetriever.getScoreListener().subscribe{score ->
                    LOGGER.info("The retrieved score : ${score.toJson()}","Collector Service","onStartCommand")
                }
                Thread{ ScoreRetriever.retrieveScore(tripId)}.start()
            }
        }catch (e : Exception ){
            e.printStackTrace()
        }catch (err : Error){
           err.printStackTrace()
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