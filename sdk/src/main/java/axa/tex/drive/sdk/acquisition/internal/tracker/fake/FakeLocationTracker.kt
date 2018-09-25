package axa.tex.drive.sdk.acquisition.internal.tracker.fake


import android.content.Context
import axa.tex.drive.sdk.acquisition.internal.tracker.Tracker
import axa.tex.drive.sdk.acquisition.internal.tracker.fake.model.FakeLocation
import axa.tex.drive.sdk.acquisition.model.Data
import axa.tex.drive.sdk.acquisition.model.Fix
import axa.tex.drive.sdk.acquisition.model.LocationFix
import io.reactivex.Observable


class FakeLocationTracker : Tracker {

    private val context: Context
    private var stopTracking: Boolean = true
    private var isEnabled: Boolean


    constructor(context: Context, isEnabled: Boolean = false) {
        this.isEnabled = isEnabled
        this.context = context
    }

    override fun isEnabled(): Boolean {
        return isEnabled
    }

    override fun provideFixProducer(): Any {
        val fixProducer: Observable<Data> = Observable.create { subscriber ->
            for (i in 0..10) {
                if (!stopTracking) {
                    val fakeFix = LocationFix(12.0, 1.88282, 28.0f, 10.9f, 18.0f, 10.0, 1881);
                    subscriber.onNext(Data(fakeFix.timestamp,location = fakeFix))
                }
            }

        }



        return fixProducer;
    }

    override fun enableTracking() {
        stopTracking = false
    }

    override fun disableTracking() {
        stopTracking = true
    }

}