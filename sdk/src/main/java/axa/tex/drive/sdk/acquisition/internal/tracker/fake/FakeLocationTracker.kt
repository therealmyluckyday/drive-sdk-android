package axa.tex.drive.sdk.acquisition.internal.tracker.fake


import axa.tex.drive.sdk.acquisition.collection.internal.FixProcessor
import axa.tex.drive.sdk.acquisition.internal.tracker.Tracker
import axa.tex.drive.sdk.acquisition.model.Fix
import axa.tex.drive.sdk.acquisition.model.LocationFix
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.*


class FakeLocationTracker : Tracker {

    private val fixProducer: PublishSubject<Fix> = PublishSubject.create()
    private var stopTracking: Boolean = true
    private var isEnabled: Boolean


    constructor(isEnabled: Boolean = false) {
        this.isEnabled = isEnabled

    }

    override fun isEnabled(): Boolean {
        return isEnabled
    }

    override fun provideFixProducer(): Any {

        return fixProducer;
    }

    override fun enableTracking() {
        isEnabled = true
        stopTracking = false
        var time : Long = Date().time

        Thread {   while (isEnabled) {
            if (!stopTracking) {

                val fakeFix = LocationFix(12.0, 1.88282, 28.0f, 10.9f, 18.0f, 10.0, time);
                time += 1000L
                //subscriber.onNext(Data(fakeFix.timestamp,location = fakeFix))
                //subscriber.onNext(fakeFix)
                Thread.sleep(1000)
                fixProducer.onNext(fakeFix)
            }
        }}.start()



    }

    override fun disableTracking() {
        isEnabled = false
        stopTracking = true
    }

}