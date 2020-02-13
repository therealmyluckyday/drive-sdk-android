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
    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
    private val disposables = mutableListOf<Disposable>()
    private var speedWatcher: TimerTask? = null
    private var gpsWatcher: TimerTask? = null
    private val automodeHandler: AutomodeHandler by inject()

    constructor(automode: Automode) {
        this.automode = automode
        LOGGER.info("${Date()} DrivingState : ${Date()} Now driving...")
        lastMvtTime = System.currentTimeMillis() - 180000
        lastGpsTime = System.currentTimeMillis()
    }

    override fun next() {
        LOGGER.info("\"Driving state ACTIVATE", "next")
        automodeHandler.state.onNext(true)
        lastMvtTime = System.currentTimeMillis()
        lastGpsTime = lastMvtTime
        watchSpeed()
        watchGPS()
        disposables.add(filterer.locationOutputOverOrEqualsToMovementSpeedWhatEverTheAccuracy.subscribe {
            notMoving = false
            lastMvtTime = System.currentTimeMillis()
            LOGGER.info("\"location speed ${it.speed} activate watchspeed", "watchspeed")
            val speedWatcher = speedWatcher
            if (speedWatcher != null) {
                speedWatcher.cancel()
            }
        })


        disposables.add(filterer.gpsStream.subscribe {
            lastGpsTime = System.currentTimeMillis()
            val gpsWatcher = gpsWatcher
            LOGGER.info("\"$it", "activate gpsWatcher")
            if (gpsWatcher != null) {
                gpsWatcher.cancel()
            }
        })
    }


    private fun watchSpeed() {

        LOGGER.info("\"Timer for speed  ", "new")
        speedWatcher = Timer("Timer for speed").schedule(automode.acceptableStopDuration, automode.acceptableStopDuration) {
            val timeInterval = (System.currentTimeMillis() - lastMvtTime)
            LOGGER.info("\"Timer for speed $timeInterval ", "called")
            if (timeInterval >= automode.acceptableStopDuration) {
                LOGGER.info("\"Timer for speed", "stop")
                stop("Speed = 0, We need to stop. from speedWatcher")
            }
        }

    }

    private fun watchGPS() {
        gpsWatcher = Timer("Timer for gps").schedule(automode.timeToWaitForGps, automode.timeToWaitForGps) {
            val timeInterval = (System.currentTimeMillis() - lastGpsTime)
            LOGGER.info("\"Timer for GPS $timeInterval $automode.timeToWaitForGps", "called")
            if (timeInterval >= automode.timeToWaitForGps) {
                LOGGER.info("\"Timer for GPS $timeInterval $automode.timeToWaitForGps", "stop")
                stop("No gps:Stop driving. from watchGPS")
            }
        }
    }

    private fun stop(message: String) {
        dispose()
        stopAllTimers()

        if (!automode.states.containsKey(AutomodeHandler.State.IDLE)) {
            automode.setCurrentState(IdleState(automode))
        } else {
            val idleState = automode.states[AutomodeHandler.State.IDLE]
            idleState?.let { automode.setCurrentState(it) }
        }
        LOGGER.info(" $message", function = "stop")
        automodeHandler.state.onNext(false)
        automode.getCurrentState().disable(false)
        automode.activityTracker.stopSpeedScanning()


        LOGGER.info("\"Driving state End", "stop")
        automode.next()

    }

    private fun stopAllTimers() {
        val funcName = "stopAllTimers"
        try {
            gpsWatcher?.cancel()
        } catch (e: Exception) {
            LOGGER.error("${e.printStackTrace()}", funcName)
        }
        try {
            speedWatcher?.cancel()
        } catch (e: Exception) {
            LOGGER.error("${e.printStackTrace()}", funcName)
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