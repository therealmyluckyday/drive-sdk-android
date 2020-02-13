package axa.tex.drive.sdk.automode.internal.states


import axa.tex.drive.sdk.automode.AutomodeHandler
import axa.tex.drive.sdk.automode.internal.Automode
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.logger.LoggerFactory
import org.koin.android.ext.android.inject

internal class IdleState : AutomodeState, KoinComponentCallbacks {

    private var automode: Automode
    private var disabled = false
    internal val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger

    constructor(automode: Automode) {
        this.automode = automode
        LOGGER.info("\"Idle state created", "constructor")
    }

    override fun next() {
        if (!disabled) {
            if (!automode.states.containsKey(AutomodeHandler.State.TRACKING_ACTIVITY)) {
                automode.setCurrentState(TrackingState(automode))
            } else {
                val trackingState = automode.states[AutomodeHandler.State.TRACKING_ACTIVITY]
                trackingState?.let { automode.setCurrentState(it) }
            }

            this@IdleState.disable(true)
            LOGGER.info("\"Idle state next", "next")
            automode.getCurrentState().disable(false)
            automode.next()
        }

    }

    override fun state(): AutomodeHandler.State {
        return AutomodeHandler.State.IDLE
    }

    override fun disable(disabled: Boolean) {
        this.disabled = disabled
    }
}