package integration.tex.com.automode.internal.states

import axa.tex.drive.sdk.automode.internal.new.SpeedFilter
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.newautomode.automode.Message
import integration.tex.com.automode.AutomodeHandler
import integration.tex.com.automode.internal.Automode
import io.reactivex.disposables.Disposable
import org.koin.android.ext.android.inject



class TrackingState : AutomodeState, KoinComponentCallbacks {

    private val automodeHandler: AutomodeHandler by inject()
    private val filterer: SpeedFilter by inject()

    private var automode: Automode

    constructor(automode: Automode) {
        this.automode = automode

        //log(automode.autoModeTracker.context, "Tracking state")
        automodeHandler.messages.onNext(Message("Tracking state"))
    }

    override fun next() {
      //val filterer = automode.activityTracker.filter()
        val tracker = automode.activityTracker
        var activitySubscription: Disposable? = null
        activitySubscription = filterer.activityOutput.subscribe {
            activitySubscription?.dispose()
            tracker.stopActivityScanning()
            var subscription: Disposable? = null
            automodeHandler.messages.onNext(Message("In vehicle according to Activity Recognition Client"))
            subscription = filterer.locationOutputWhatEverTheAccuracy.subscribe {
                automodeHandler.messages.onNext(Message("Speed of ${it.speed} reached"))
                subscription?.dispose()
                automode.setCurrentState(InVehicleState(automode))
                automode.next()
            }
            //scanning speed
            tracker.passivelyScanSpeed()

        }
        //Scanning activity
        tracker.checkWhereAmI()
    }
}