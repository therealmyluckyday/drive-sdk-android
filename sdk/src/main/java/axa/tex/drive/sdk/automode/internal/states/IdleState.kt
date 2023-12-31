package axa.tex.drive.sdk.automode.internal.states


import android.os.Handler
import android.os.Looper
import axa.tex.drive.sdk.automode.internal.Automode
import axa.tex.drive.sdk.core.logger.LoggerFactory
import java.util.*
import kotlin.concurrent.schedule

internal class IdleState : AutoModeDetectionState {

    private var automode: Automode
    private var disabled = false
    internal val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger

    constructor(automode: Automode) {
        this.automode = automode
        LOGGER.info("\"Idle state created", "constructor")
    }

    override fun enable() {
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
        } else {
            goNext()
        }

    }

    override fun disable() {
    }

    override fun goNext() {
        this.disable()
        val nextState = TrackingState(automode)
        automode.setCurrentState(nextState)
        LOGGER.info("\"Idle state next", "next")
        nextState.enable()
    }
}