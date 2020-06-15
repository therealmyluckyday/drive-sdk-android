package axa.tex.drive.sdk.acquisition.collection.internal


import android.content.Context
import axa.tex.drive.sdk.acquisition.internal.tracker.Tracker
import axa.tex.drive.sdk.acquisition.model.Fix
import axa.tex.drive.sdk.acquisition.model.LocationFix
import axa.tex.drive.sdk.acquisition.model.TripId
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.logger.LoggerFactory
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.koin.android.ext.android.inject


internal class Collector : KoinComponentCallbacks {

    private val trackers: MutableList<Tracker>?
    val logger = LoggerFactory().getLogger(this::class.java.name)

    internal var fixProcessor: FixProcessor

    private var context: Context

    internal var currentTripId: TripId? = null

    internal var recording: Boolean = false

    var fixData: Observable<List<Fix>>? = null

    val locations: PublishSubject<LocationFix> = PublishSubject.create()
    val rxScheduler: Scheduler

    constructor(context: Context, trackers: MutableList<Tracker>?, scheduler: Scheduler) {
        this.trackers = trackers
        this.context = context
        val fixProcessor: FixProcessor by inject()
        this.fixProcessor = fixProcessor
        this.rxScheduler = scheduler
    }


    fun startCollecting() {

        if (trackers != null) {
            for (tracker in trackers) {
                logger.logger.info("Tracker ${tracker.javaClass.simpleName}", "startCollecting")
                if (tracker.canBeEnabled()) {
                    logger.logger.info("Enabling tracker ${tracker.javaClass.simpleName}", "startCollecting")

                    tracker.enableTracking()
                    logger.logger.info("${tracker.javaClass.simpleName} enabled = ${tracker.isEnabled()}", "startCollecting")

                    collect(tracker)
                }
            }
        }
    }


    private fun collect(tracker: Tracker) {
        tracker.enableTracking()
        fixData = tracker.provideFixProducer()

        fixData?.subscribeOn(rxScheduler)?.subscribe( { fixes ->
                fixProcessor.addFixes(fixes)
            if (fixes.size == 1 && fixes[0] is LocationFix) {
                locations.onNext(fixes[0] as LocationFix)
            }
        }, {throwable ->
            print(throwable)
        })
    }

    fun stopCollecting() {
        fixData?.unsubscribeOn(rxScheduler)
        if (trackers != null) {
            for (tracker in trackers) {
                tracker.disableTracking()
            }
        }
        recording = false
    }
}