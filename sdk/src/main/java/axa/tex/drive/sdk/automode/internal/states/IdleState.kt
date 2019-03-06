package integration.tex.com.automode.internal.states


import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.newautomode.automode.Message
import integration.tex.com.automode.AutomodeHandler
import integration.tex.com.automode.internal.Automode
import org.koin.android.ext.android.inject

class IdleState : AutomodeState, KoinComponentCallbacks{

    private var automode: Automode
    constructor(automode: Automode){
        this.automode = automode
        //log(automode.autoModeTracker.context,"Idle state")
        val automodeHandler : AutomodeHandler by inject()
        automodeHandler.messages.onNext(Message("Idle state"))
    }

    override fun next() {
        automode.setCurrentState(TrackingState(automode))
        //automode.setCurrentState(DrivingState(automode))
        automode.next()

    }

}