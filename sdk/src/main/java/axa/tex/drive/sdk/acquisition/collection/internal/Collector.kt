package axa.tex.drive.sdk.acquisition.collection.internal

import axa.tex.drive.sdk.acquisition.internal.tracker.Tracker
import axa.tex.drive.sdk.acquisition.model.Data
import axa.tex.drive.sdk.acquisition.model.Fix
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlin.reflect.KClass


internal class Collector {

    private val trackers: Array<Tracker>
    private var trackersMap: Map<String, Tracker>? = null

     internal companion object {
        private val locations : PublishSubject<Fix> = PublishSubject.create()
        fun locationObservable() : Observable<Fix>{
            return locations
        }
    }



    constructor(vararg trackers: Tracker) {
        this.trackers = trackers as Array<Tracker>
        trackersMap = trackers.associateBy({ it.javaClass.simpleName }, { it })
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
        fixData.subscribeOn(io.reactivex.schedulers.Schedulers.computation()).subscribe { data ->
            if (data is Data) {
                if(data.location != null){
                    locations.onNext(data.location)
                }
                Thread { FixProcessor.addFixes(listOf(data)) }.start()
            } else {
                Thread { FixProcessor.addFixes(data as List<Data>) }.start()
            }
        }
    }


    fun enableTrackers(vararg trackersClass: KClass<out Tracker>) {
        for (kClass in trackersClass) {
            val tracker: Tracker? = trackersMap?.get(kClass.simpleName);
            tracker?.enableTracking()
        }
    }


    fun stopCollecting(vararg trackersClass: KClass<out Tracker>) {
        for (kClass in trackersClass) {
            val tracker: Tracker? = trackersMap?.get(kClass.simpleName);
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