package axa.tex.drive.sdk.automode.internal

import android.content.Context
import axa.tex.drive.sdk.automode.AutoMode
import axa.tex.drive.sdk.automode.AutoModeState

 internal class IdleState : AutoModeState{

    private var context: Context

    constructor(context: Context){
        this.context = context
    }

    override fun scan(autoMode: AutoMode) {
        val activityTracker = ActivityTracker(context)
        autoMode.setCurrentState(activityTracker)
    }

    override fun stopScan() {
    }

    override fun state(): AutoModeState.State {
        return AutoModeState.State.IDLE
    }
}