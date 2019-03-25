package axa.tex.drive.sdk.acquisition.collection.internal


import android.content.Context
import axa.tex.drive.sdk.acquisition.internal.tracker.Tracker
import axa.tex.drive.sdk.acquisition.model.Fix
import axa.tex.drive.sdk.acquisition.model.LocationFix
import axa.tex.drive.sdk.acquisition.model.TripId
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.internal.utils.TripManager
import axa.tex.drive.sdk.core.logger.LoggerFactory

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.koin.android.ext.android.inject


internal class Collector : KoinComponentCallbacks{

    private val trackers: MutableList<Tracker>?
    val logger = LoggerFactory().getLogger(this::class.java.name)

    private var fixProcessor: FixProcessor

    private var context: Context;

    internal var currentTripId: TripId? = null

    internal var recording: Boolean = false

    var fixData: Observable<List<Fix>>? = null

    // internal companion object {
    val locations: PublishSubject<LocationFix> = PublishSubject.create()
    /* fun locationObservable(): Observable<LocationFix> {
         return locations
     }*/
    //}

    constructor(context: Context, trackers: MutableList<Tracker>?) {
        this.trackers = trackers
        this.context = context
        val fixProcessor: FixProcessor by inject()
        this.fixProcessor = fixProcessor
    }


    fun startCollecting() {

        if (trackers != null) {
            for (tracker in trackers) {
                if (tracker.isEnabled()) {
                    logger.logger.info("Enabling tracker ${tracker.javaClass.simpleName}", "startCollecting")

                    tracker.enableTracking()
                    logger.logger.info("${tracker.javaClass.simpleName} enabled = ${tracker.isEnabled()}", "startCollecting")

                    collect(tracker)
                }
            }
        }
    }


    private fun collect(tracker: Tracker) {
        tracker.enableTracking();
        fixData = tracker.provideFixProducer() as Observable<List<Fix>>

        fixData?.subscribeOn( Schedulers.single())?.subscribe {fixes->
            fixProcessor.addFixes(fixes)
            if (fixes.size == 1 && fixes[0] is LocationFix) {
                locations.onNext(fixes[0] as LocationFix)
            }
        }

       /* fixData?.subscribeOn(io.reactivex.schedulers.Schedulers.computation())?.subscribe { fixes ->

            Thread {
                fixProcessor.addFixes(fixes)
                if (fixes.size == 1 && fixes[0] is LocationFix) {
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
        }*/
    }

    fun stopCollecting() {
        fixData?.unsubscribeOn(Schedulers.computation())
        if (trackers != null) {
            for (tracker in trackers) {
                tracker.disableTracking()
            }
        }
        recording = false
        //val tripManager : TripManager by inject()
        //tripManager.removeTripId(context)
    }

    fun numberOfTrackers(): Int {
        if (trackers != null) {
            return trackers.size
        }
        return 0
    }
}