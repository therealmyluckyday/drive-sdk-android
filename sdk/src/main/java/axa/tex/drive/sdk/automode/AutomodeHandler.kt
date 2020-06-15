package axa.tex.drive.sdk.automode

import android.content.Context
import android.content.Intent
import android.os.Build
import axa.tex.drive.sdk.automode.internal.service.AutomodeService
import axa.tex.drive.sdk.automode.internal.tracker.SpeedFilter
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import io.reactivex.subjects.PublishSubject
import org.koin.android.ext.android.inject

interface AutomodeHandler {
    val state: PublishSubject<Boolean>
    var running: Boolean
    fun activateAutomode(context: Context, isForeground: Boolean = true, isSimulatedDriving: Boolean = false)
    fun disableAutoMode(context: Context)
}