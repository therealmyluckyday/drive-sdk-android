package axa.tex.drive.sdk.automode

import android.content.Context
import android.content.Intent
import android.os.Build
import axa.tex.drive.sdk.automode.internal.service.AutomodeService
import axa.tex.drive.sdk.automode.internal.tracker.SpeedFilter
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import io.reactivex.subjects.PublishSubject
import org.koin.android.ext.android.inject


class AutomodeHandler : KoinComponentCallbacks {
    val state: PublishSubject<Boolean> = PublishSubject.create()
    var running = false
    val speedListener: SpeedFilter by inject()

    internal constructor()

    internal enum class State {
        IDLE,
        TRACKING_ACTIVITY,
        SCANNING_SPEED,
        IN_VEHICLE,
        DRIVING
    }


    fun activateAutomode(context: Context) {
        val serviceIntent = Intent(context, AutomodeService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }


}