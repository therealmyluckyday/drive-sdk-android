package axa.tex.drive.sdk.automode.internal.states

import android.os.Handler
import android.os.Looper
import axa.tex.drive.sdk.automode.AutomodeHandler
import axa.tex.drive.sdk.automode.internal.Automode
import axa.tex.drive.sdk.automode.internal.tracker.SPEED_MOVEMENT_THRESHOLD
import axa.tex.drive.sdk.automode.internal.tracker.SpeedFilter
import axa.tex.drive.sdk.automode.internal.tracker.model.TexLocation
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.logger.LoggerFactory
import io.reactivex.disposables.Disposable
import org.koin.android.ext.android.inject
import java.util.*
import kotlin.concurrent.schedule


internal class DrivingState : AutoModeDetectionState {

    private var automode: Automode
    private val filterer: SpeedFilter
    private var disabled = false
    private var lastMvtTime: Long
    private var lastGpsTime: Long
    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
    private val disposables = mutableListOf<Disposable>()
    private var timerSpeedWatcher: Timer? = null
    private var speedWatcher: TimerTask? = null
    private var gpsWatcher: TimerTask? = null
    private var timerGPSWatcher: Timer? = null


    constructor(automode: Automode) {
        this.automode = automode
        this.filterer = automode.getSpeedFilter()
        LOGGER.info("DrivingState", "Constructor")
        lastMvtTime = System.currentTimeMillis() - 180000
        lastGpsTime = System.currentTimeMillis()
    }

    override fun next() {
        LOGGER.info("\"Driving state ACTIVATE", "next")
        lastMvtTime = System.currentTimeMillis()
        lastGpsTime = lastMvtTime
        watchSpeed()
        watchGPS()

        disposables.add(filterer.gpsStream.subscribeOn(automode.rxScheduler).filter { t: TexLocation ->  t.speed >= SPEED_MOVEMENT_THRESHOLD }.subscribe {
            LOGGER.info("\"location speed ${it.speed} activate watchspeed", "watchspeed")
            lastMvtTime = it.time
        })

        disposables.add(filterer.gpsStream.subscribeOn(automode.rxScheduler).subscribe {
            LOGGER.info("\"$it", "activate gpsWatcher")
            lastGpsTime = it.time
        })
    }


    private fun watchSpeed() {
        timerSpeedWatcher = Timer("Timer for speed")
        LOGGER.info("\"Timer for speed  ", "new")
        val delayTimerSpeedWatcher: Long = (if (automode.isSimulateDriving) 80000 else 55000)
        speedWatcher = timerSpeedWatcher!!.schedule(delayTimerSpeedWatcher, automode.acceptableStopDuration) {
            val timeInterval = (System.currentTimeMillis() - lastMvtTime)
            LOGGER.info("\"Timer for speed $timeInterval ", "called")
            if (timeInterval >= 50000) {
                LOGGER.info("\"Timer for speed", "stop")
                stop("Speed = 0, We need to stop. from speedWatcher")
            }
        }

    }

    private fun watchGPS() {
        timerGPSWatcher = Timer("Timer for gps")
        gpsWatcher = timerGPSWatcher!!.schedule(automode.timeToWaitForGps, automode.timeToWaitForGps) {
            val timeInterval = (System.currentTimeMillis() - lastGpsTime)
            LOGGER.info("\"Timer for GPS $timeInterval $automode.timeToWaitForGps", "called")
            if (timeInterval >= automode.timeToWaitForGps) {
                LOGGER.info("\"Timer for GPS $timeInterval $automode.timeToWaitForGps", "stop")
                stop("No gps:Stop driving. from watchGPS")
            }
        }
    }

    private fun stop(message: String) {
        LOGGER.info(" $message", function = "stop")
        goNext()

    }

    override fun goNext() {
        // Get a handler that can be used to post to the main thread
        val mainHandler = Handler(Looper.getMainLooper())

        val myRunnable = Runnable() {
            this.disable(true)
            automode.setCurrentState(IdleState(automode))
            automode.getCurrentState().disable(false)
            LOGGER.info("\"Driving state End", "goNext")
            automode.next()
        }
        mainHandler.post(myRunnable);
    }

    private fun stopAllTimers() {
        val funcName = "stopAllTimers"
        try {
            gpsWatcher?.cancel()
            timerGPSWatcher?.cancel()
        } catch (e: Exception) {
            LOGGER.error("${e.printStackTrace()}", funcName)
        }
        try {
            speedWatcher?.cancel()
            timerSpeedWatcher?.cancel()
        } catch (e: Exception) {
            LOGGER.error("${e.printStackTrace()}", funcName)
        }
        disposables.clear()
        gpsWatcher = null
        speedWatcher = null
        timerSpeedWatcher = null
        timerGPSWatcher = null
    }

    private fun dispose() {
        for (disposable in disposables) {
            disposable.dispose()
        }
        disposables.clear()
    }

    override fun disable(disabled: Boolean) {
        this.disabled = disabled
        if (disabled) {
            automode.sensorService.stopSpeedScanning()
            dispose()
            stopAllTimers()
        }
    }
}