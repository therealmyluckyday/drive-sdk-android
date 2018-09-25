package axa.tex.drive.sdk.automode.internal

import axa.tex.drive.sdk.automode.AutoMode
import axa.tex.drive.sdk.automode.AutoModeState

class Driving : AutoModeState {
    override fun stopScan() {

    }


    override fun scan(autoMode: AutoMode) {

    }

    override fun state() :AutoModeState.State{
        return AutoModeState.State.DRIVING
    }

}
