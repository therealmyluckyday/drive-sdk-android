package axa.tex.drive.sdk.acquisition

import android.Manifest
import android.app.Notification
import android.content.*
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import axa.tex.drive.sdk.acquisition.collection.internal.Collector
import axa.tex.drive.sdk.acquisition.collection.internal.CollectorService
import axa.tex.drive.sdk.acquisition.collection.internal.FixProcessor
import axa.tex.drive.sdk.acquisition.model.LocationFix
import axa.tex.drive.sdk.acquisition.model.TripId
import axa.tex.drive.sdk.acquisition.score.ScoreRetriever
import axa.tex.drive.sdk.automode.AutomodeHandler
import axa.tex.drive.sdk.automode.internal.tracker.AutoModeTracker
import axa.tex.drive.sdk.automode.internal.tracker.model.Message
import axa.tex.drive.sdk.automode.internal.tracker.model.TexSpeed
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.internal.utils.TripManager
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import org.koin.android.ext.android.inject
import java.util.*
import java.util.concurrent.TimeUnit


internal class TripRecorderImpl : TripRecorder, KoinComponentCallbacks {

    private val context: Context
    private var fixProcessor: FixProcessor? = null
    private var collector: Collector
    private var myCustomNotification : Notification? = null
    private val automodeHandler: AutomodeHandler by inject()

    private var mCurrentLocation : Location? = null
    private var mCurrentDistance: Double = 0.toDouble()
    private var mCurrentSpeed: Int = 0
    private  var start : Long  = 0
    private  var disposable : Disposable

    private val tripProgress = PublishSubject.create<TripProgress>()


    override fun setCustomNotification(customNotification: Notification?) {
        myCustomNotification = customNotification
    }

    override fun getCurrentTripId(): TripId? {
        val tripManager : TripManager by inject()
        return tripManager.tripId(context)
    }



    constructor(context: Context) {
        this.context = context
        val collector: Collector by inject()
        this.collector = collector
        try {
            val fixProcessor: FixProcessor by inject()
            this.fixProcessor = fixProcessor
        } catch (e: Exception) {
            e.printStackTrace()
        }


        disposable = automodeHandler.speedListener.locations.subscribe {
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
        }

     /*   autoModeTracker.locations.subscribe {
            var deltaDistance = 0.0
            if (mCurrentLocation != null) { // this is not the first point GPS received
                deltaDistance = (mCurrentLocation!!.distanceTo(it) / 1000).toDouble() // Km
            }else{
                mCurrentLocation = it
            }
            mCurrentDistance += deltaDistance
            mCurrentSpeed = (it.speed * 3.6).toInt() // km/h
        }*/
    }

    @Throws(PermissionException::class)
    private fun requestForLocationPermission() {

        if (Build.VERSION.SDK_INT >= 23) {

           /* if (context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
               val exception = PermissionException("need permission.WRITE_EXTERNAL_STORAGE")
                throw exception
            }*/

            if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                val exception = PermissionException("need permission.ACCESS_FINE_LOCATION")
                throw exception
            }

            if (context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                val exception = PermissionException("need permission.ACCESS_COARSE_LOCATION")
                throw exception
            }
        }
    }


    override fun startTrip(startTime: Long) : TripId?{
        start = startTime
        automodeHandler.messages.onNext(Message("${Date()} TripRecorder : Start tracking."))
        requestForLocationPermission()
        val tripManager : TripManager by inject()
        tripManager.removeTripId(context)
        val serviceIntent = Intent(context, CollectorService::class.java)
        if(myCustomNotification != null) {
            serviceIntent.putExtra("notif", myCustomNotification)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        }else{
            context.startService(serviceIntent)
        }

        //FixProcessor.startTrip(context)
        fixProcessor?.startTrip(startTime)

        return tripManager.tripId(context)
    }

    override fun stopTrip(endTime: Long) {
        requestForLocationPermission()
        //FixProcessor.endTrip(context)
        fixProcessor?.endTrip(endTime)
        val serviceIntent = Intent(context, CollectorService::class.java)
        context.bindService(serviceIntent, object : ServiceConnection {

            override fun onServiceConnected(className: ComponentName,
                                            service: IBinder) {
                val binder = service as CollectorService.LocalBinder
                binder.service.stopCollectorService();
            }

            override fun onServiceDisconnected(componentName: ComponentName) {
            }
        }, /*Context.BIND_AUTO_CREATE*/0);
        disposable.dispose()
    }

    override fun isRecording(): Boolean {
        //return CollectorService.isRunning()
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