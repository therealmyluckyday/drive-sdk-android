package axa.tex.drive.sdk.acquisition.collection.internal

import android.annotation.TargetApi
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
import axa.tex.drive.sdk.acquisition.score.ScoreRetriever
import axa.tex.drive.sdk.core.Platform
import axa.tex.drive.sdk.core.internal.Constants
import axa.tex.drive.sdk.core.logger.LoggerFactory
import org.koin.android.ext.android.inject


private const val NOTIFICATION_ID = 7071

internal class CollectorService : Service() {
    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
    private val binder = LocalBinder()
    private var collector: Collector? = null

    inner class LocalBinder : Binder() {
        internal val service: CollectorService
            get() = this@CollectorService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
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

    //To avoid executing that block of code on devices older than Android X.0
    fun isNewerPhone(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }

    @TargetApi(26)
    fun startNotification(intent: Intent?) {
        val channelId = createNotificationChannel()
        val notification: Notification?

        if (intent != null) {
            if (intent.hasExtra("notif")) {
                notification = intent.getParcelableExtra("notif")
            } else {
                val notificationBuilder = NotificationCompat.Builder(this, channelId)
                notification = notificationBuilder.setOngoing(true)
                        .setSmallIcon(R.drawable.ic_logo)
                        .setCategory(Notification.CATEGORY_SERVICE)
                        .build()
            }
            if (notification != null) {
                this.startForeground(NOTIFICATION_ID, notification)
            }
        }
    }

    fun subscribeScoreAvailability(appName: String, platform: Platform) {
        try {
            val scoreRetriever: ScoreRetriever by inject()
            scoreRetriever.getAvailableScoreListener().subscribe ( { tripId ->
                scoreRetriever.getScoreListener().subscribe { scoreResult ->
                    if (scoreResult.scoreDil == null) {
                        LOGGER.info("The retrieved score error: ${scoreResult.scoreError?.toJson()}", "subscribeScoreAvailability")
                    } else {
                        LOGGER.info("The retrieved score : ${scoreResult.scoreDil.toJson()}", "subscribeScoreAvailability")
                    }

                }
                Thread {
                    Thread.sleep(10000)
                    tripId?.let { scoreRetriever.retrieveScore(it, appName, platform, true) }
                }.start()
            }, {throwable ->
                LOGGER.error("The retrieved rx score exception: ${throwable.printStackTrace()}", "subscribeScoreAvailability")
            })
        } catch (e: Exception) {
            LOGGER.error("The retrieved score exception: ${e.printStackTrace()}", "subscribeScoreAvailability")
        } catch (err: Error) {
            LOGGER.error("The retrieved score error: ${err.printStackTrace()}", "subscribeScoreAvailability")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val collector: Collector by inject()
        this.collector = collector
        collector.startCollecting()
        collector.recording = true

        if (isNewerPhone()) {
            startNotification(intent)
        }



        if ((intent != null) && (intent.hasExtra(Constants.APP_NAME_KEY))&& (intent.hasExtra(Constants.PLATFORM_KEY))) {
            val appName: String = intent.getStringExtra(Constants.APP_NAME_KEY)
            val platformValue: String = intent.getStringExtra(Constants.PLATFORM_KEY)
            val platform : Platform
            when (platformValue) {
                Platform.PRODUCTION.endPoint -> platform = Platform.PRODUCTION
                Platform.TESTING.endPoint -> platform = Platform.TESTING
                Platform.PREPROD.endPoint -> platform = Platform.PREPROD
                else -> platform = Platform.PRODUCTION
            }

            subscribeScoreAvailability(appName, platform)
        }
        
        return START_STICKY
    }


    fun stopCollectorService() {
        collector?.stopCollecting()
        stopSelf()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true)
        }
        collector?.recording = false
    }
}