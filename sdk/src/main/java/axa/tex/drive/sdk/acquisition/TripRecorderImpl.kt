package axa.tex.drive.sdk.acquisition

import android.Manifest
import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import androidx.core.app.ActivityCompat
import axa.tex.drive.sdk.acquisition.collection.internal.Collector
import axa.tex.drive.sdk.acquisition.collection.internal.CollectorService
import axa.tex.drive.sdk.acquisition.collection.internal.FixProcessor
import axa.tex.drive.sdk.acquisition.model.LocationFix
import axa.tex.drive.sdk.acquisition.model.TripId
import axa.tex.drive.sdk.acquisition.score.ScoreRetriever
import axa.tex.drive.sdk.automode.AutomodeHandler
import axa.tex.drive.sdk.core.Config
import axa.tex.drive.sdk.core.Platform
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.logger.LoggerFactory
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import org.koin.android.ext.android.inject
import java.util.*


internal class TripRecorderImpl : TripRecorder, KoinComponentCallbacks {
    private val context: Context
    private val fixProcessor: FixProcessor by inject()
    private val collector: Collector by inject()
    private var myCustomNotification : Notification? = null
    private val automodeHandler: AutomodeHandler by inject()
    private var mCurrentLocation : Location? = null
    private var mCurrentDistance: Double = 0.toDouble()
    private var mCurrentSpeed: Int = 0
    private var start : Long  = 0
    private var disposable : Disposable
    private val tripProgress = PublishSubject.create<TripProgress>()
    internal val logger = LoggerFactory().getLogger(this::class.java.name).logger
    override fun setCustomNotification(notification: Notification?) {
        myCustomNotification = notification
    }

    override fun getCurrentTripId(): TripId? {
        return fixProcessor.currentTripChunk?.tripInfos?.tripId
    }

    constructor(context: Context) {
        this.context = context

        disposable = automodeHandler.speedListener.locations.subscribe( {
            var deltaDistance = 0.0
            if (mCurrentLocation != null) { // this is not the first point GPS received
                deltaDistance = (mCurrentLocation!!.distanceTo(it) / 1000).toDouble() // Km
            }else{
                mCurrentLocation = it
            }
            mCurrentDistance += deltaDistance
            mCurrentSpeed = (it.speed * 3.6).toInt() // km/h
            val duration = System.currentTimeMillis() - start
            val progress = TripProgress(getCurrentTripId()!!,it,mCurrentSpeed,mCurrentDistance,duration)
            tripProgress.onNext(progress)
        }, {throwable ->
            print(throwable)
        })
    }

    @Throws(PermissionException::class)
    private fun requestForLocationPermission() {

        if (Build.VERSION.SDK_INT >= 23) {
            if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                val exception = PermissionException("need permission.ACCESS_FINE_LOCATION")
                throw exception
            }

            if (context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                val exception = PermissionException("need permission.ACCESS_COARSE_LOCATION")
                throw exception
            }
            if (context.checkSelfPermission(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                val exception = PermissionException("need permission.ACCESS_BACKGROUND_LOCATION")
                throw exception
            }
        }
    }


    override fun startTrip(startTime: Long) : TripId?{
        //val config = this.mConfig ?:
        val config = Config(false, true, false, "APP-TEST", "22910000", Platform.PRODUCTION)
        start = startTime

        logger.info("${Date()} TripRecorder : Start tracking.", function = "fun startTrip(startTime: Long) : TripId?")
        requestForLocationPermission()
        val serviceIntent = Intent(context, CollectorService::class.java)
        if(myCustomNotification != null) {
            serviceIntent.putExtra("notif", myCustomNotification)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        }else{
            context.startService(serviceIntent)
        }

        return fixProcessor.startTrip(startTime, config)
    }

    override fun stopTrip(endTime: Long) {
        logger.info("TripRecorder : Stop tracking.", function = "fun stopTrip(startTime: Long) : TripId?")
        requestForLocationPermission()
        fixProcessor.endTrip(endTime)
        val serviceIntent = Intent(context, CollectorService::class.java)
        context.bindService(serviceIntent, object : ServiceConnection {

            override fun onServiceConnected(className: ComponentName,
                                            service: IBinder) {
                val binder = service as CollectorService.LocalBinder
                binder.service.stopCollectorService()
            }

            override fun onServiceDisconnected(componentName: ComponentName) {

                logger.info(" CollectorService : onServiceDisconnected", function = "onServiceDisconnected")
            }
        }, 0)
        disposable.dispose()
    }

    override fun isRecording(): Boolean {
        return collector.recording
    }

    override fun locationObservable(): Observable<LocationFix> {
        return collector.locations
    }

    override fun endedTripListener(): PublishSubject<String?> {
        val scoreRetriever : ScoreRetriever by inject()
        return scoreRetriever.getAvailableScoreListener()
    }

    override fun tripProgress(): PublishSubject<TripProgress> {
        return tripProgress
    }



}