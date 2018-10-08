package axa.tex.drive.sdk.acquisition.collection.internal

import axa.tex.drive.sdk.acquisition.internal.tracker.LocationTracker
import axa.tex.drive.sdk.acquisition.internal.tracker.Tracker
import axa.tex.drive.sdk.acquisition.model.Fix
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlin.reflect.KClass


internal class Collector {

    private val trackers: Array<Tracker>

    //Uses simple name
   // private var trackersMap: Map<String, Tracker>? = null

    private var trackersMap: Map<KClass<out Tracker>, Tracker>? = null


     internal companion object {
        private val locations : PublishSubject<Fix> = PublishSubject.create()
        fun locationObservable() : Observable<Fix>{
            return locations
        }
    }



    //Uses simple name
    /*constructor(vararg trackers: Tracker) {
        this.trackers = trackers as Array<Tracker>
        trackersMap = trackers.associateBy({ it.javaClass.simpleName }, { it })
    }*/

    constructor(vararg trackers: Tracker) {
        this.trackers = trackers as Array<Tracker>
        trackersMap = trackers.associateBy({ it::class }, { it })
    }


    fun collect() {
        for (tracker in trackers) {
            if (tracker.isEnabled()) {
                tracker.enableTracking()
                collect(tracker)
            }
        }
    }



    private fun collect(tracker: Tracker) {
        tracker.enableTracking();
        val fixData = tracker.provideFixProducer() as Observable<Any>
        fixData.subscribeOn(io.reactivex.schedulers.Schedulers.computation()).subscribe { fix ->
            if (fix is Fix) {
                if(fix is LocationTracker){
                    locations.onNext(fix)
                }
                Thread { FixProcessor.addFixes(listOf(fix)) }.start()
            } else {
                Thread { FixProcessor.addFixes(fix as List<Fix>) }.start()
            }
        }
    }

    //Uses simple name
    /*fun enableTrackers(vararg trackersClass: KClass<out Tracker>) {
        for (kClass in trackersClass) {
            val tracker: Tracker? = trackersMap?.get(kClass.simpleName);
            tracker?.enableTracking()
        }
    }*/

    fun enableTrackers(vararg trackersClass: KClass<out Tracker>) {
        for (kClass in trackersClass) {
            val tracker: Tracker? = trackersMap?.get(kClass);
            tracker?.enableTracking()
        }
    }



    //Uses simple name
    /*fun stopCollecting(vararg trackersClass: KClass<out Tracker>) {
        for (kClass in trackersClass) {
            val tracker: Tracker? = trackersMap?.get(kClass.simpleName);
            tracker?.disableTracking()
        }
    }*/

    fun stopCollecting(vararg trackersClass: KClass<out Tracker>) {
        for (kClass in trackersClass) {
            val tracker: Tracker? = trackersMap?.get(kClass);
            tracker?.disableTracking()
        }
    }

    fun stopCollecting() {
        for (tracker in trackers) {
            tracker.disableTracking()
        }
    }

    fun numberOfTrackers(): Int {
        return trackers.size
    }

}