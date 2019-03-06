package integration.tex.com.automode.internal.states

import android.widget.Toast
import axa.tex.drive.sdk.automode.internal.new.SpeedFilter
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.newautomode.automode.Message
import integration.tex.com.automode.AutomodeHandler
import integration.tex.com.automode.internal.Automode
import io.reactivex.disposables.Disposable
import org.koin.android.ext.android.inject

class InVehicleState : AutomodeState, KoinComponentCallbacks {

    private var automode: Automode
    private val automodeHandler: AutomodeHandler by inject()
    private val filterer: SpeedFilter by inject()

    constructor(automode: Automode) {
        this.automode = automode
        automodeHandler.messages.onNext(Message("Really in vehicle"))
    }

    override fun next() {
        //val filterer = automode.activityTracker.filter()
        val tracker = automode.activityTracker
        var locationSubscription: Disposable? = null
        locationSubscription = filterer.locationOutputWithAccuracy.subscribe {
            automodeHandler.messages.onNext(Message("Speed of ${it.speed} reached with ${it.accuracy} of accuracy"))
            locationSubscription?.dispose()
            automode.setCurrentState(DrivingState(automode))
            automode.next()
        }
        tracker.activelyScanSpeed()
    }
}