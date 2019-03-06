package axa.tex.drive.sdk.automode.internal.states

import android.content.Context
import axa.tex.drive.sdk.automode.AutomodeHandler
import java.io.File
import java.io.FileOutputStream

internal interface AutomodeState {



    fun next()

    fun disable(disabled : Boolean)

    fun sate() : AutomodeHandler.State

}