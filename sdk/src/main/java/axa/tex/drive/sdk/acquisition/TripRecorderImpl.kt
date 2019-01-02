package axa.tex.drive.sdk.acquisition

import android.content.ComponentCallbacks
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import axa.tex.drive.sdk.acquisition.collection.internal.CollectorService
import android.content.ComponentName
import android.os.IBinder
import android.content.ServiceConnection
import axa.tex.drive.sdk.acquisition.collection.internal.Collector
import axa.tex.drive.sdk.acquisition.collection.internal.FixProcessor
import axa.tex.drive.sdk.acquisition.model.Fix
import axa.tex.drive.sdk.acquisition.model.LocationFix
import axa.tex.drive.sdk.acquisition.model.TripId
import axa.tex.drive.sdk.core.internal.utils.TripManager
import axa.tex.drive.sdk.core.internal.utils.Utils
import io.reactivex.Observable
import org.koin.android.ext.android.inject


internal class TripRecorderImpl : TripRecorder, ComponentCallbacks {


    private val context: Context
    private var fixProcessor: FixProcessor? = null
    private var collector : Collector



    override fun getCurrentTripId(): TripId? {
        //return CollectorService.currentTripId()
        return TripManager.tripId(context)
    }

    override fun onLowMemory() {
    }

    override fun onConfigurationChanged(configuration: Configuration?) {

    }

    constructor(context: Context){
        this.context = context
        val collector: Collector by inject()
        this.collector = collector
        try {
            val fixProcessor: FixProcessor by inject()
            this.fixProcessor = fixProcessor
        }catch (e : Exception){
            e.printStackTrace()
        }
    }


    override fun startTracking(startTime : Long) {
        val serviceIntent = Intent(context, CollectorService::class.java)
        context.startService(serviceIntent)
        //FixProcessor.startTrip(context)
        fixProcessor?.startTrip(startTime)
    }

    override fun stopTracking(endTime : Long) {
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

    override fun isRecording(): Boolean{
        //return CollectorService.isRunning()
        return collector.recording
    }

    override fun locationObservable(): Observable<LocationFix> {
       return collector.locations
    }

    override fun tripIdListener(): Observable<TripId> {
    return  null!!
    }
}