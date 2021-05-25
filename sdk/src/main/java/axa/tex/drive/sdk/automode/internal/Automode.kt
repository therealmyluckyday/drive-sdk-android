package axa.tex.drive.sdk.automode.internal

import android.content.Context
import android.content.Intent
import android.os.Build
import axa.tex.drive.sdk.acquisition.SensorService
import axa.tex.drive.sdk.automode.AutomodeHandler
import axa.tex.drive.sdk.automode.internal.service.AutomodeService
import axa.tex.drive.sdk.automode.internal.states.AutoModeDetectionState
import axa.tex.drive.sdk.automode.internal.states.DrivingState
import axa.tex.drive.sdk.automode.internal.states.IdleState
import axa.tex.drive.sdk.automode.internal.tracker.SpeedFilter
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.logger.LoggerFactory
import io.reactivex.Scheduler
import io.reactivex.subjects.PublishSubject

private const val TIME_TO_WAIT_FOR_GPS = 1000 * 60 * 4L
private const val ACCEPTABLE_STOPPED_DURATION = 1000 * 60 * 3L

internal class Automode : KoinComponentCallbacks, AutomodeHandler {
    // Interface AutomodeHandler
    override val state: PublishSubject<Boolean>
    override var running = false

    //
    val rxState: PublishSubject<AutoModeDetectionState> = PublishSubject.create()
    val rxIsDriving: PublishSubject<Boolean> = PublishSubject.create()

    //val rxDisposeBag = DisposeBag()
    internal var sensorService: SensorService
    private var currentState: AutoModeDetectionState
    internal var timeToWaitForGps = TIME_TO_WAIT_FOR_GPS
    internal var acceptableStopDuration = ACCEPTABLE_STOPPED_DURATION
    var isSimulateDriving = false
    var isForeground = false
    var rxScheduler: Scheduler

    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger

    internal constructor(activityTracker: SensorService, scheduler: Scheduler) {
        this.sensorService = activityTracker
        this.currentState = IdleState(this)
        this.state = this.rxIsDriving
        this.setCurrentState(this.currentState)
        this.rxScheduler = scheduler
    }

    internal fun setCurrentState(currentState: AutoModeDetectionState) {
        if (this.currentState is DrivingState) {
            this.rxIsDriving.onNext(false)
        } else if (currentState is DrivingState) {
            this.rxIsDriving.onNext(true)
        }
        rxState.onNext(currentState)
        this.currentState = currentState
    }

    internal fun disable() {
        currentState.disable()
        running = false
    }

    internal fun getCurrentState(): AutoModeDetectionState {
        return currentState
    }

    internal fun getSpeedFilter(): SpeedFilter {
        return sensorService.speedFilter()
    }

    internal fun goToIdleState() {
        if (!(this.currentState is IdleState)) {
            this.currentState.disable()
            this.setCurrentState(IdleState(this))
        }
    }

    override fun activateAutomode(context: Context, isForeground: Boolean, isSimulatedDriving: Boolean) {
        val serviceIntent = Intent(context, AutomodeService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isForeground) {
            serviceIntent.putExtra("isForeground", isForeground)
            serviceIntent.putExtra("isSimulatedDriving", isSimulatedDriving)
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    override fun disableAutoMode(context: Context) {
        val serviceIntent = Intent(context, AutomodeService::class.java)
        context.stopService(serviceIntent)
    }
}