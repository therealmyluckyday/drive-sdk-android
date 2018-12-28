package axa.tex.drive.sdk.acquisition.collection.internal


import android.content.ComponentCallbacks
import axa.tex.drive.sdk.acquisition.internal.tracker.Tracker
import axa.tex.drive.sdk.acquisition.model.Fix
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import android.content.Context
import android.content.res.Configuration
import axa.tex.drive.sdk.acquisition.model.LocationFix
import axa.tex.drive.sdk.acquisition.model.TripId
import axa.tex.drive.sdk.core.internal.utils.TripManager
import axa.tex.drive.sdk.core.internal.utils.Utils
import com.orhanobut.logger.Logger
import org.koin.android.ext.android.inject


internal class Collector : ComponentCallbacks {
    override fun onLowMemory() {
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
    }

    private val trackers: MutableList<Tracker>?

    private var fixProcessor: FixProcessor

    private var context: Context;

    internal var currentTripId : TripId? = null

    internal var recording : Boolean = false

   // internal companion object {
        private val locations: PublishSubject<LocationFix> = PublishSubject.create()
        fun locationObservable(): Observable<LocationFix> {
            return locations
        }
    //}

    constructor(context: Context, trackers: MutableList<Tracker>?) {
        this.trackers = trackers
        this.context = context
        val fixProcessor: FixProcessor by inject()
        this.fixProcessor = fixProcessor
    }


    fun collect() {
        if (trackers != null) {
            for (tracker in trackers) {
                if (tracker.isEnabled()) {
                    Logger.i("Enabling tracker ${tracker.javaClass.simpleName}")
                    tracker.enableTracking()
                    Logger.i("${tracker.javaClass.simpleName} enabled = ${tracker.isEnabled()}")
                    collect(tracker)
                }
            }
        }
    }



    private fun collect(tracker: Tracker) {
        tracker.enableTracking();
        val fixData = tracker.provideFixProducer() as Observable<List<Fix>>
        fixData.subscribeOn(io.reactivex.schedulers.Schedulers.computation()).subscribe { fixes ->

            Thread { fixProcessor.addFixes(fixes)
                if(fixes.size == 1 && fixes[0] is LocationFix){
                    locations.onNext(fixes[0] as LocationFix)
                }
            }.start()
          /*  if (fix is Fix) {
                if (fix is LocationFix) {
                    locations.onNext(fix)
                }
                //Thread { FixProcessor.addFixes(context,listOf(fix)) }.start()
                Thread { fixProcessor.addFixes(context, listOf(fix)) }.start()
            }

            else {
                Thread { fixProcessor.addFixes(context, fix as List<Fix>) }.start()
                //Thread { FixProcessor.addFixes(context,fix as List<Fix>) }.start()
            }*/
        }
    }

    fun stopCollecting() {
        if (trackers != null) {
            for (tracker in trackers) {
                tracker.disableTracking()
            }
        }
        TripManager.removeTripId(context)
    }

    fun numberOfTrackers(): Int {
        if (trackers != null) {
            return trackers.size
        }
        return 0
    }
}