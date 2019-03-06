package axa.tex.drive.sdk.automode.internal.states

import axa.tex.drive.sdk.automode.internal.tracker.SpeedFilter
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.automode.internal.tracker.model.Message
import axa.tex.drive.sdk.automode.AutomodeHandler
import axa.tex.drive.sdk.automode.internal.Automode
import io.reactivex.disposables.Disposable
import org.koin.android.ext.android.inject
import java.util.*

internal class InVehicleState : AutomodeState, KoinComponentCallbacks {

    private var automode: Automode
    private val automodeHandler: AutomodeHandler by inject()
    private val filterer: SpeedFilter by inject()
    private var disabled = false

    constructor(automode: Automode) {
        this.automode = automode
        automodeHandler.messages.onNext(Message(Date().toString()+":Really in vehicle"))
    }

    override fun next() {
        //val filterer = automode.activityTracker.filter()
        //val tracker = automode.activityTracker
        var locationSubscription: Disposable? = null
        locationSubscription = filterer.locationOutputWithAccuracy.subscribe {
            if (!disabled) {
                automodeHandler.messages.onNext(Message(Date().toString() + ":Speed of ${it.speed} reached with ${it.accuracy} of accuracy"))
                locationSubscription?.dispose()

                if (!automode.states.containsKey(AutomodeHandler.State.DRIVING)) {
                    automode.setCurrentState(DrivingState(automode))
                } else {
                    val drivingState = automode.states[AutomodeHandler.State.DRIVING]
                    drivingState?.let { automode.setCurrentState(it) }
                }

                //automode.setCurrentState(DrivingState(automode))
                this@InVehicleState.disable(true)
                automode.getCurrentState().disable(false)
                automode.next()
            }
        }
        //tracker.activelyScanSpeed()
    }

    override fun sate(): AutomodeHandler.State {
        return AutomodeHandler.State.IN_VEHICLE
    }

    override fun disable(disabled: Boolean) {
        this.disabled = disabled
    }
}