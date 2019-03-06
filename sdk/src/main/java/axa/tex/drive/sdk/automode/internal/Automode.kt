package integration.tex.com.automode.internal

import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.newautomode.automode.internal.tracker.TexActivityTracker
import integration.tex.com.automode.AutomodeHandler
import integration.tex.com.automode.internal.states.AutomodeState
import integration.tex.com.automode.internal.states.IdleState
import org.koin.android.ext.android.inject

class Automode : KoinComponentCallbacks{
    internal var activityTracker: TexActivityTracker
    internal val autoModeHandler : AutomodeHandler by inject()
    private  var currentState : AutomodeState

    internal constructor(activityTracker: TexActivityTracker){
        this.activityTracker = activityTracker
        this.currentState = IdleState(this)
    }

    internal fun setCurrentState(currentState : AutomodeState){
        this.currentState = currentState

    }

    internal fun next(){
        currentState.next()
    }

    internal fun getCurrentState() : AutomodeState{
        return currentState
    }
}