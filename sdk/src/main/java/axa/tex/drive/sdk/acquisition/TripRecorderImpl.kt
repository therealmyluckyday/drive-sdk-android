package axa.tex.drive.sdk.acquisition

import android.content.ComponentCallbacks
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import axa.tex.drive.sdk.acquisition.collection.internal.CollectorService
import android.content.ComponentName
import android.os.IBinder
import android.content.ServiceConnection
import axa.tex.drive.sdk.acquisition.collection.internal.FixProcessor
import axa.tex.drive.sdk.acquisition.model.Fix
import io.reactivex.Observable


internal class TripRecorderImpl(private val context: Context) : ComponentCallbacks, axa.tex.drive.sdk.acquisition.TripRecorder {

    override fun getCurrentTripId(): String {
        return CollectorService.currentTripId()
    }

    override fun onLowMemory() {
    }

    override fun onConfigurationChanged(configuration: Configuration?) {

    }

    override fun track() {
        val serviceIntent = Intent(context, CollectorService::class.java)
        context.startService(serviceIntent)
        FixProcessor.startTrip(context)
    }

    override fun stopTracking() {
        FixProcessor.endTrip(context)
        val serviceIntent = Intent(context, CollectorService::class.java);
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
        return CollectorService.isRunning()
    }

    override fun locationObservable(): Observable<Fix> {
       return axa.tex.drive.sdk.acquisition.collection.internal.Collector.locationObservable()
    }
}