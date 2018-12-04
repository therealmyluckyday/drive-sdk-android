package axa.tex.drive.sdk.acquisition.collection.internal


import axa.tex.drive.sdk.acquisition.internal.tracker.Tracker
import axa.tex.drive.sdk.acquisition.model.Fix
import axa.tex.drive.sdk.acquisition.model.LocationFix
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlin.reflect.KClass
import android.content.Context
import androidx.work.*
import axa.tex.drive.sdk.acquisition.score.internal.ScoreWorker
import axa.tex.drive.sdk.core.internal.utils.Utils
import com.orhanobut.logger.DiskLogAdapter
import com.orhanobut.logger.Logger


internal class Collector {

    private val trackers: Array<Tracker>

    private var trackersMap: Map<KClass<out Tracker>, Tracker>? = null

    private var context : Context;

     internal companion object {
        private val locations : PublishSubject<Fix> = PublishSubject.create()
        fun locationObservable() : Observable<Fix>{
            return locations
        }
    }



    constructor(context : Context,vararg trackers: Tracker) {
        this.trackers = trackers as Array<Tracker>
        this.context = context
        trackersMap = trackers.associateBy({ it::class }, { it })
    }


    fun collect() {
        for (tracker in trackers) {
            if (tracker.isEnabled()) {
                Logger.i("Enabling tracker ${tracker.javaClass.simpleName}")
                tracker.enableTracking()
                Logger.i("${tracker.javaClass.simpleName} enabled = ${tracker.isEnabled()}")
                collect(tracker)
            }
        }
    }



    private fun collect(tracker: Tracker) {


        /*val data : Data = Data.Builder().putBoolean("4260e592-008b-4fcf-877d-fe8d3923b5f5",true).build()
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        val fixUploadWork : OneTimeWorkRequest = OneTimeWorkRequest.Builder(ScoreWorker::class.java).
                setInputData(data).setConstraints(constraints)
                .build()
        WorkManager.getInstance().enqueue(fixUploadWork)*/


        tracker.enableTracking();
        val fixData = tracker.provideFixProducer() as Observable<Any>
        fixData.subscribeOn(io.reactivex.schedulers.Schedulers.computation()).subscribe { fix ->
            if (fix is Fix) {
                if(fix is LocationFix){
                    locations.onNext(fix)
                }
                Thread { FixProcessor.addFixes(context,listOf(fix)) }.start()
            } else {
                Thread { FixProcessor.addFixes(context,fix as List<Fix>) }.start()
            }
        }
    }



    fun enableTrackers(vararg trackersClass: KClass<out Tracker>) {
        for (kClass in trackersClass) {
            val tracker: Tracker? = trackersMap?.get(kClass);
            tracker?.enableTracking()
        }
    }



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
        Utils.removeTripId(context)
    }

    fun numberOfTrackers(): Int {
        return trackers.size
    }

}