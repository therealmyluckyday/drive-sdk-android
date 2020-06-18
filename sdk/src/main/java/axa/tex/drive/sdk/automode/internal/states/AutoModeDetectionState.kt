package axa.tex.drive.sdk.automode.internal.states

internal interface AutoModeDetectionState {
    fun enable()
    fun goNext()
    fun disable()
}