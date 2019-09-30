package axa.tex.drive.sdk.automode.internal.states


import axa.tex.drive.sdk.automode.AutomodeHandler
import axa.tex.drive.sdk.automode.internal.Automode
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.logger.LoggerFactory
import org.koin.android.ext.android.inject

internal class IdleState : AutomodeState, KoinComponentCallbacks{

    private var automode: Automode
    private var disabled = false
    internal val logger = LoggerFactory().getLogger(this::class.java.name).logger

    constructor(automode: Automode){
        this.automode = automode
        val automodeHandler : AutomodeHandler by inject()
    }

    override fun next() {
        if(!disabled) {
        if(!automode.states.containsKey(AutomodeHandler.State.TRACKING_ACTIVITY)){
            automode.setCurrentState(TrackingState(automode))
        }else{
            val trackingState = automode.states[AutomodeHandler.State.TRACKING_ACTIVITY]
            trackingState?.let { automode.setCurrentState(it) }
        }

        //automode.setCurrentState(DrivingState(automode))
            this@IdleState.disable(true)
            automode.getCurrentState().disable(false)
            automode.next()
           // disabled = true
        }

    }

    override fun sate(): AutomodeHandler.State {
        return AutomodeHandler.State.IDLE
    }

    override fun disable(disabled: Boolean) {
        this.disabled = disabled
    }
}