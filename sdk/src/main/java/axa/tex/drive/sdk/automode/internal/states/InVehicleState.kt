package axa.tex.drive.sdk.automode.internal.states

import axa.tex.drive.sdk.automode.AutomodeHandler
import axa.tex.drive.sdk.automode.internal.Automode
import axa.tex.drive.sdk.automode.internal.tracker.SpeedFilter
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.logger.LoggerFactory
import io.reactivex.disposables.Disposable
import org.koin.android.ext.android.inject
import java.util.*

internal class InVehicleState : AutomodeState, KoinComponentCallbacks {

    private var automode: Automode
    private val filterer: SpeedFilter by inject()
    private var disabled = false
    internal val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger

    constructor(automode: Automode) {
        this.automode = automode
        LOGGER.info("InVehicleState", "Constructor")
    }

    override fun next() {
        var locationSubscription: Disposable? = null
        locationSubscription = filterer.locationOutputWithAccuracy.subscribe( {
            if (!disabled) {
                LOGGER.info(Date().toString() + ":Speed of ${it.speed} reached with ${it.accuracy} of accuracy", function = "next")
                locationSubscription?.dispose()

                if (!automode.states.containsKey(AutomodeHandler.State.DRIVING)) {
                    automode.setCurrentState(DrivingState(automode))
                } else {
                    val drivingState = automode.states[AutomodeHandler.State.DRIVING]
                    drivingState?.let { automode.setCurrentState(it) }
                }
                LOGGER.info("In vehicule state next", "next")
                this@InVehicleState.disable(true)
                automode.getCurrentState().disable(false)
                automode.next()
            }
        }, {throwable ->
            print(throwable)
        })
    }

    override fun state(): AutomodeHandler.State {
        return AutomodeHandler.State.IN_VEHICLE
    }

    override fun disable(disabled: Boolean) {
        this.disabled = disabled
    }
}