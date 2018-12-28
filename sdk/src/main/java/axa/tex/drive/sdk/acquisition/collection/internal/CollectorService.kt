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
import axa.tex.drive.sdk.acquisition.model.TripId
import axa.tex.drive.sdk.acquisition.score.ScoreRetriever
import axa.tex.drive.sdk.core.internal.Constants
import axa.tex.drive.sdk.core.TexConfig
import axa.tex.drive.sdk.core.internal.utils.Utils
import axa.tex.drive.sdk.core.logger.LoggerFactory
import io.reactivex.subjects.PublishSubject

import org.koin.android.ext.android.inject
import org.koin.standalone.StandAloneContext
import java.util.*

private const val NOTIFICATION_ID = 7071

internal class CollectorService : Service() {
    private val LOGGER = LoggerFactory.getLogger().logger

    /*companion object {

        private var running = false
        private var tripId : TripId? = null

        fun isRunning(): Boolean {
            return running
        }

        fun currentTripId(): TripId? {
            return tripId
        }
    }*/

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

        val collectorDb = CollectionDb(applicationContext)
        //val collectorDb: CollectionDb by inject()
        val config = collectorDb.getConfig()
        try {
            StandAloneContext.stopKoin()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        TexConfig.init(applicationContext, config)
        val collector: Collector by inject()
        this.collector = collector
        collector.collect()
        collector.recording = true

        val scoreRetriever : ScoreRetriever by inject()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = createNotificationChannel();
            val notification: Notification
            if (intent != null && intent.hasExtra("")) {
                notification = intent.getParcelableExtra("")
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
                scoreRetriever.getScoreListener().subscribe { score ->
                    LOGGER.info("The retrieved score : ${score.toJson()}", "Collector Service", "onStartCommand")
                }
                Thread { tripId?.let { scoreRetriever.retrieveScore(it) } }.start()
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
        collector?.recording = true
    }

    /*internal fun numberOfTrackers(): Int {
        return collector?.numberOfTrackers()!!
    }*/
}