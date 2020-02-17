package axa.tex.drive.sdk.automode.internal

import axa.tex.drive.sdk.automode.AutomodeHandler
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.automode.internal.tracker.TexActivityTracker
import axa.tex.drive.sdk.automode.internal.states.AutomodeState
import axa.tex.drive.sdk.automode.internal.states.IdleState
import axa.tex.drive.sdk.core.logger.LoggerFactory
import org.koin.android.ext.android.inject
private const val TIME_TO_WAIT_FOR_GPS = 1000 * 60 * 4L
private const val ACCEPTABLE_STOPPED_DURATION = 1000 * 60 * 3L

internal class Automode : KoinComponentCallbacks{
    internal var activityTracker: TexActivityTracker
    internal val autoModeHandler : AutomodeHandler by inject()
    private  var currentState : AutomodeState
    internal var timeToWaitForGps = TIME_TO_WAIT_FOR_GPS
    internal var acceptableStopDuration = ACCEPTABLE_STOPPED_DURATION
    var isSimulateDriving = false
    var isForeground = false

    internal val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
    internal val states  = mutableMapOf<AutomodeHandler.State, AutomodeState>()

    internal constructor(activityTracker: TexActivityTracker){
        this.activityTracker = activityTracker
        this.currentState = IdleState(this)
        if(!states.containsKey(currentState.state())){
            states[currentState.state()] = currentState
        }
    }

    internal fun setCurrentState(currentState : AutomodeState){
        if(!states.containsKey(currentState.state())){
            states[currentState.state()] = currentState
        }
        this.currentState = currentState

    }

    internal fun next(){
        LOGGER.info("\"current state $currentState", "next")
        currentState.next()
    }
    internal fun disable(disabled : Boolean){
        currentState.disable(disabled)
    }

    internal fun getCurrentState() : AutomodeState {
        return currentState
    }
}