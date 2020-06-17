package axa.tex.drive.sdk.automode.internal.states


import axa.tex.drive.sdk.automode.AutomodeHandler

internal interface AutoModeDetectionState {
    fun next()
    fun goNext()
    fun disable(disabled : Boolean)
}