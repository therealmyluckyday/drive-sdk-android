package axa.tex.drive.sdk.automode

import android.content.Context
import io.reactivex.subjects.PublishSubject

interface AutomodeHandler {
    val state: PublishSubject<Boolean>
    var running: Boolean
    fun activateAutomode(context: Context, isForeground: Boolean = true, isSimulatedDriving: Boolean = false)
    fun disableAutoMode(context: Context)
}