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
import axa.tex.drive.sdk.R
import axa.tex.drive.sdk.acquisition.TripRecorder
import axa.tex.drive.sdk.acquisition.collection.internal.db.CollectionDb
import axa.tex.drive.sdk.acquisition.score.ScoreRetriever
import axa.tex.drive.sdk.core.TexConfig
import axa.tex.drive.sdk.core.internal.Constants
import axa.tex.drive.sdk.core.logger.LoggerFactory
import org.koin.android.ext.android.inject
import org.koin.standalone.StandAloneContext


private const val NOTIFICATION_ID = 7071

internal class CollectorService : Service() {
    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger

    private val binder = LocalBinder()
    private var collector: Collector? = null;

    inner class LocalBinder : Binder() {
        internal val service: CollectorService
            get() = this@CollectorService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder;
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String {
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

        //val collectorDb = CollectionDb(applicationContext)
        val collectorDb: CollectionDb by inject()


        try {
            if(collectorDb == null) {
                try {
                    StandAloneContext.stopKoin()
                    TexConfig.setupKoin(applicationContext)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }catch (e : IllegalStateException){
            try {
                StandAloneContext.stopKoin()
                TexConfig.setupKoin(applicationContext)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        //val config = collectorDb.getConfig()

       // TexConfig.init(applicationContext, config)
        val collector: Collector by inject()
        this.collector = collector
        collector.startCollecting()
        collector.recording = true

        val scoreRetriever: ScoreRetriever by inject()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = createNotificationChannel();
            val notification: Notification
            if (intent != null && intent.hasExtra("notif")) {
                notification = intent.getParcelableExtra("notif")
            } else {
                val notificationBuilder = NotificationCompat.Builder(this, channelId)
                notification = notificationBuilder.setOngoing(true)
                        .setSmallIcon(R.drawable.ic_logo)
                        .setCategory(Notification.CATEGORY_SERVICE)
                        .build()
            }
            this.startForeground(NOTIFICATION_ID, notification)
        }

        /*running = true
        if (tripId == null) {
            tripId = Utils.tripId(applicationContext)//UUID.randomUUID().toString().toUpperCase(Locale.US)
        }*/



        try {
            scoreRetriever.getAvailableScoreListener().subscribe { tripId ->
                scoreRetriever.getScoreListener().subscribe { scoreResult ->
                    if(scoreResult.scoreDil == null){
                        LOGGER.info("The retrieved score error: ${scoreResult.scoreError?.toJson()}", "onStartCommand")
                    }else{
                        LOGGER.info("The retrieved score : ${scoreResult.scoreDil.toJson()}", "onStartCommand")
                    }

                }
                Thread {
                    Thread.sleep(10000)
                    tripId?.let { scoreRetriever.retrieveScore(it) } }.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } catch (err: Error) {
            err.printStackTrace()
        }


        return START_STICKY
    }


    fun stopCollectorService() {
        collector?.stopCollecting()
        stopSelf()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true)
        }
        //running = false
        collector?.recording = false
    }

}