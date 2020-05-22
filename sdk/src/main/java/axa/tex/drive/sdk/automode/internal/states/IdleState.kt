package axa.tex.drive.sdk.automode.internal.states


import android.os.Handler
import android.os.Looper
import axa.tex.drive.sdk.acquisition.TripRecorder
import axa.tex.drive.sdk.automode.AutomodeHandler
import axa.tex.drive.sdk.automode.internal.Automode
import axa.tex.drive.sdk.core.TexConfig
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.logger.LoggerFactory
import org.koin.android.ext.android.inject
import java.util.*
import kotlin.concurrent.schedule

internal class IdleState : AutomodeState, KoinComponentCallbacks {

    private var automode: Automode
    private var disabled = false
    internal val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger

    constructor(automode: Automode) {
        this.automode = automode
        LOGGER.info("\"Idle state created", "constructor")
    }

    override fun next() {
        if (automode.isSimulateDriving) {
            LOGGER.info("isSimulatedDriving", function = "next")
             Timer("IdleState Timer").schedule(15000) {
                 // Get a handler that can be used to post to the main thread
                 val mainHandler = Handler(Looper.getMainLooper())
                 val myRunnable = Runnable() {
                    goNext()
                 }
                 mainHandler.post(myRunnable);
            }
        } else if (!disabled) {
            goNext()
        }

    }

    override fun state(): AutomodeHandler.State {
        return AutomodeHandler.State.IDLE
    }

    override fun disable(disabled: Boolean) {
        this.disabled = disabled
    }

    fun goNext() {
        if (!automode.states.containsKey(AutomodeHandler.State.TRACKING_ACTIVITY)) {
            automode.setCurrentState(TrackingState(automode))
        } else {
            val trackingState = automode.states[AutomodeHandler.State.TRACKING_ACTIVITY]
            trackingState?.let { automode.setCurrentState(it) }
        }

        this@IdleState.disable(true)
        LOGGER.info("\"Idle state next", "next")
        automode.getCurrentState().disable(false)
        automode.next()
    }
}