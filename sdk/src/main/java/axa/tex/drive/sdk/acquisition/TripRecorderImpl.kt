package axa.tex.drive.sdk.acquisition

import android.app.Notification
import android.content.*
import android.content.res.Configuration
import android.os.IBinder
import axa.tex.drive.sdk.acquisition.collection.internal.Collector
import axa.tex.drive.sdk.acquisition.collection.internal.CollectorService
import axa.tex.drive.sdk.acquisition.collection.internal.FixProcessor
import axa.tex.drive.sdk.acquisition.model.LocationFix
import axa.tex.drive.sdk.acquisition.model.TripId
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.internal.utils.TripManager
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.koin.android.ext.android.inject


internal class TripRecorderImpl : TripRecorder, KoinComponentCallbacks {



    private val context: Context
    private var fixProcessor: FixProcessor? = null
    private var collector: Collector
    var myCustomNotification : Notification? = null

    var currentTripListener : Observable<TripId> = PublishSubject.create()



    override fun setCustomNotification(customNotification: Notification?) {
        myCustomNotification = customNotification
    }

    override fun getCurrentTripId(): TripId? {
        return TripManager.tripId(context)
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
    }


    override fun startTracking(startTime: Long) {
        val serviceIntent = Intent(context, CollectorService::class.java)
        if(myCustomNotification != null) {
            serviceIntent.putExtra("notif", myCustomNotification)
        }
        context.startService(serviceIntent)
        //FixProcessor.startTrip(context)
        fixProcessor?.startTrip(startTime)
    }

    override fun stopTracking(endTime: Long) {
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
    }

    override fun isRecording(): Boolean {
        //return CollectorService.isRunning()
        return collector.recording
    }

    override fun locationObservable(): Observable<LocationFix> {
        return collector.locations
    }
}