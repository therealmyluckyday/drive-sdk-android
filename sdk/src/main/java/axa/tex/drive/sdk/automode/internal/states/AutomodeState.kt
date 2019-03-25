package axa.tex.drive.sdk.automode.internal.states


import axa.tex.drive.sdk.automode.AutomodeHandler

internal interface AutomodeState {



    fun next()

    fun disable(disabled : Boolean)

    fun sate() : AutomodeHandler.State

}