package integration.tex.com.automode.internal.states

import axa.tex.drive.sdk.automode.internal.new.SpeedFilter
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.newautomode.automode.Message
import integration.tex.com.automode.AutomodeHandler
import integration.tex.com.automode.internal.Automode
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import org.koin.android.ext.android.inject
import java.util.*
import kotlin.concurrent.schedule

private const val TIME_TO_WAIT_FOR_GPS = 1000 * 60 * 4L
private const val ACCEPTABLE_STOPPED_DURATION = 1000 * 60 * 3L

class DrivingState : AutomodeState, KoinComponentCallbacks {

    private var automode: Automode
    private val filterer: SpeedFilter by inject()

    private var notMoving: Boolean = false
    private var noGPS: Boolean = false
    //private val timerMessages: PublishSubject<Message> = PublishSubject.create()
   // private val eventMessage: PublishSubject<AutomodeHandler.State> = PublishSubject.create()

    private val disposables = mutableListOf<Disposable>()

    private var speedWatcher: TimerTask? = null
    private var gpsWatcher: TimerTask? = null
    private val automodeHandler: AutomodeHandler by inject()

    constructor(automode: Automode) {
        this.automode = automode
        automodeHandler.messages.onNext(Message("Now driving..."))
        automodeHandler.state.onNext(AutomodeHandler.State.DRIVING)

        watchGPS()
    }

    override fun next() {

        //val filterer = automode.activityTracker.filter()

        disposables.add(filterer.locationOutputUnderMovementSpeedWhatEverTheAccuracy.subscribe {
            automodeHandler.messages.onNext(Message("Speed equals to  ${it.speed} : May be the the vehicle is not moving"))
            notMoving = true
            if (speedWatcher == null) {
                watchSpeed()
            }
        })

        disposables.add(filterer.locationOutputOverOrEqualsToMovementSpeedWhatEverTheAccuracy.subscribe {
            notMoving = false
            automodeHandler.messages.onNext(Message("Speed equals to  ${it.speed} : We restart moving"))
            if (speedWatcher != null) {
                speedWatcher?.cancel()
                speedWatcher = null
            }
        })


        disposables.add(filterer.gpsStream.subscribe {
            noGPS = false
            gpsWatcher?.cancel()
            gpsWatcher = null
            watchGPS()
        })



        /*eventMessage.observeOn(AndroidSchedulers.mainThread()).subscribe {
            automodeHandler.state.onNext(it)
            println("EVENT : $it")
        }*/


    }

    private fun watchSpeed() {
        speedWatcher = Timer("Timer for speed", false).schedule(ACCEPTABLE_STOPPED_DURATION) {
            if (notMoving) {
                stopAllTimers()
                automodeHandler.messages.onNext(Message("Speed = 0, We need to stop"))
                automodeHandler.state.onNext(AutomodeHandler.State.IDLE)
                automode.setCurrentState(IdleState(automode))
                automode.activityTracker.stopSpeedScanning()
                automode.next()
                dispose()
            }
        }

    }


    private fun watchGPS() {
        noGPS = true
        gpsWatcher = Timer("Timer for gps", false).schedule(TIME_TO_WAIT_FOR_GPS) {
            if (noGPS) {
                stopAllTimers()
                automodeHandler.messages.onNext(Message("No gps:Stop driving"))
                automodeHandler.state.onNext(AutomodeHandler.State.IDLE)
                automode.setCurrentState(IdleState(automode))
                automode.activityTracker.stopSpeedScanning()
                automode.next()
                dispose()
            }
        }

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
    }

    private fun dispose(){
        for ( disposable in disposables){
            disposable.dispose()
        }
        disposables.clear()
    }
}