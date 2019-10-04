package axa.tex.drive.sdk.automode.internal.states

import axa.tex.drive.sdk.automode.AutomodeHandler
import axa.tex.drive.sdk.automode.internal.Automode
import axa.tex.drive.sdk.automode.internal.tracker.SpeedFilter
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.logger.LoggerFactory
import io.reactivex.disposables.Disposable
import org.koin.android.ext.android.inject
import java.util.*
import kotlin.concurrent.schedule


internal class DrivingState : AutomodeState, KoinComponentCallbacks {

    private var automode: Automode
    private val filterer: SpeedFilter by inject()
    private var disabled = false
    private var lastMvtTime: Long
    private var lastGpsTime: Long
    private var notMoving: Boolean = true
    private var noGPS: Boolean = true
    internal val logger = LoggerFactory().getLogger(this::class.java.name).logger
    private val disposables = mutableListOf<Disposable>()
    private var speedWatcher: TimerTask? = null
    private var gpsWatcher: TimerTask? = null
    private val automodeHandler: AutomodeHandler by inject()

    constructor(automode: Automode) {
        this.automode = automode
        logger.info("${Date()} DrivingState : ${Date().toString()} Now driving...")
        lastMvtTime = System.currentTimeMillis()
        lastGpsTime = System.currentTimeMillis()
        watchGPS()
    }

    override fun next() {

        automodeHandler.state.onNext(true)
        lastMvtTime = System.currentTimeMillis()
        lastGpsTime = lastMvtTime

        disposables.add(filterer.locationOutputOverOrEqualsToMovementSpeedWhatEverTheAccuracy.subscribe {
            notMoving = false
            lastMvtTime = System.currentTimeMillis()
            if (speedWatcher == null) {
                watchSpeed()
            }
        })


        disposables.add(filterer.gpsStream.subscribe {
            lastGpsTime = System.currentTimeMillis()
            if (gpsWatcher == null) {
                watchGPS()
            }
        })
    }


    private fun watchSpeed() {
        speedWatcher = Timer("Timer for speed").schedule(1000 * 60, automode.acceptableStopDuration) {
            if ((System.currentTimeMillis() - lastMvtTime) >= automode.acceptableStopDuration) {
                stop("Speed = 0, We need to stop. from speedWatcher")
            }
        }

    }

    private fun watchGPS() {
        gpsWatcher = Timer("Timer for gps").schedule(automode.timeToWaitForGps, automode.timeToWaitForGps) {
            if ((System.currentTimeMillis() - lastGpsTime) >= automode.timeToWaitForGps) {
                stop("No gps:Stop driving. from watchGPS")
            }
        }
    }

    private fun stop(message: String) {
        if (!automode.states.containsKey(AutomodeHandler.State.IDLE)) {
            automode.setCurrentState(IdleState(automode))
        } else {
            val idleState = automode.states[AutomodeHandler.State.IDLE]
            idleState?.let { automode.setCurrentState(it) }
        }
        logger.info(Date().toString() + ": $message", function = "stop(message : String)")
        automodeHandler.state.onNext(false)
        automode.getCurrentState().disable(false)
        automode.activityTracker.stopSpeedScanning()

        // @ERWAN WTF???
        //Wait for thirty seconds before going back to scan state. This allows not clear the current trip id while the
        // the stop not already sent.
        Thread.sleep(1000 * 30)

        automode.next()

        dispose()
        stopAllTimers()
    }

    private fun stopAllTimers() {
        try {
            gpsWatcher?.cancel()
        } catch (e: Exception) {
        }
        try {
            speedWatcher?.cancel()
        } catch (e: Exception) {
        }
        disposables.clear()
        gpsWatcher = null
        speedWatcher = null
    }

    private fun dispose() {
        for (disposable in disposables) {
            disposable.dispose()
        }
        disposables.clear()
    }

    override fun disable(disabled: Boolean) {
        this.disabled = disabled
    }

    override fun state(): AutomodeHandler.State {
        return AutomodeHandler.State.DRIVING
    }
}