package axa.tex.drive.sdk.automode.internal.states

import axa.tex.drive.sdk.automode.AutomodeHandler
import axa.tex.drive.sdk.automode.internal.Automode
import axa.tex.drive.sdk.automode.internal.tracker.SpeedFilter
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.logger.LoggerFactory
import io.reactivex.disposables.Disposable
import org.koin.android.ext.android.inject
import java.util.*


internal class TrackingState : AutomodeState, KoinComponentCallbacks {

    private val automodeHandler: AutomodeHandler by inject()
    private val filterer: SpeedFilter by inject()

    internal val logger = LoggerFactory().getLogger(this::class.java.name).logger
    private var automode: Automode
    private var disabled = false

    constructor(automode: Automode) {
        this.automode = automode
        logger.info(Date().toString() + ":Tracking state")
    }

    override fun next() {
        val tracker = automode.activityTracker
        var activitySubscription: Disposable? = null
        activitySubscription = filterer.activityOutput.subscribe {
            if (!disabled) {
                activitySubscription?.dispose()
                tracker.stopActivityScanning()
                var subscription: Disposable? = null
                logger.info(Date().toString() + " : In vehicle according to Activity Recognition Client", function = "fun next()")
                subscription = filterer.locationOutputWhatEverTheAccuracy.subscribe {

                    logger.info(Date().toString() + ":Speed of ${it.speed} reached", function = "fun next()")
                    subscription?.dispose()

                    if (!automode.states.containsKey(AutomodeHandler.State.IN_VEHICLE)) {
                        automode.setCurrentState(InVehicleState(automode))
                    } else {
                        val vehicleState = automode.states[AutomodeHandler.State.IN_VEHICLE]
                        vehicleState?.let { automode.setCurrentState(it) }
                    }

                    this@TrackingState.disable(true)
                    automode.getCurrentState().disable(false)
                    automode.next()
                }
                //scanning speed
                tracker.activelyScanSpeed()
            }
        }
        //Scanning activity
        tracker.checkWhereAmI()
    }

    override fun state(): AutomodeHandler.State {
        return AutomodeHandler.State.TRACKING_ACTIVITY
    }

    override fun disable(disabled: Boolean) {
        this.disabled = disabled
    }
}