package axa.tex.drive.sdk.automode.internal.states

import axa.tex.drive.sdk.automode.internal.tracker.SpeedFilter
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.automode.internal.tracker.model.Message
import axa.tex.drive.sdk.automode.AutomodeHandler
import axa.tex.drive.sdk.automode.internal.Automode
import io.reactivex.disposables.Disposable
import org.koin.android.ext.android.inject
import java.util.*
import kotlin.concurrent.schedule




internal class DrivingState : AutomodeState, KoinComponentCallbacks {


    //private var timeToWaitForGps = TIME_TO_WAIT_FOR_GPS
    private var automode: Automode
    private val filterer: SpeedFilter by inject()
    private var disabled = false
    private  var lastMvtTime : Long
    private  var lastGpsTime : Long

    private var notMoving: Boolean = true
    private var noGPS: Boolean = true
    //private val timerMessages: PublishSubject<Message> = PublishSubject.create()
   // private val eventMessage: PublishSubject<AutomodeHandler.State> = PublishSubject.create()

    private val disposables = mutableListOf<Disposable>()

    private var speedWatcher: TimerTask? = null
    private var gpsWatcher: TimerTask? = null
    private val automodeHandler: AutomodeHandler by inject()

    constructor(automode: Automode) {
        this.automode = automode
        automodeHandler.messages.onNext(Message(Date().toString()+":Now driving..."))
        //automodeHandler.state.onNext(AutomodeHandler.State.DRIVING)
            lastMvtTime = System.currentTimeMillis()
            lastGpsTime = System.currentTimeMillis()
        watchGPS()
    }

    override fun next() {
        automodeHandler.state.onNext(AutomodeHandler.State.DRIVING)
        //val filterer = automode.activityTracker.filter()
        lastMvtTime = System.currentTimeMillis()
        lastGpsTime = System.currentTimeMillis()
        /*disposables.add(filterer.locationOutputUnderMovementSpeedWhatEverTheAccuracy.subscribe {
            automodeHandler.messages.onNext(Message(Date().toString()+":Speed equals to  ${it.speed} : May be the the vehicle is not moving"))
            notMoving = true
            if (speedWatcher == null) {
                watchSpeed()
            }
        })*/

        disposables.add(filterer.locationOutputOverOrEqualsToMovementSpeedWhatEverTheAccuracy.subscribe {
            notMoving = false
           // automodeHandler.messages.onNext(Message(Date().toString()+":Speed equals to  ${it.speed} : We restart moving"))
            lastMvtTime = System.currentTimeMillis()
            /*if (speedWatcher != null) {
                speedWatcher?.cancel()
                speedWatcher = null
            }*/
            if(speedWatcher == null){
                watchSpeed()
            }

        })


        disposables.add(filterer.gpsStream.subscribe {
            lastGpsTime = System.currentTimeMillis()
            //noGPS = false
           // gpsWatcher?.cancel()
          //  gpsWatcher = null
            if(gpsWatcher == null){
                watchGPS()
            }

        })



        /*eventMessage.observeOn(AndroidSchedulers.mainThread()).subscribe {
            automodeHandler.state.onNext(it)
            println("EVENT : $it")
        }*/


    }


    private fun watchSpeed() {
        //if(speedWatcher == null){
        speedWatcher = Timer("Timer for speed").schedule(1000*60, automode.acceptableStopDuration) {

           if((System.currentTimeMillis() - lastMvtTime) >= automode.acceptableStopDuration){
               /* automodeHandler.messages.onNext(Message(Date().toString()+": Speed = 0, We need to stop. from speedWatcher"))
                automodeHandler.state.onNext(AutomodeHandler.State.IDLE)

                if(!automode.states.containsKey(AutomodeHandler.State.IDLE)){
                    automode.setCurrentState(IdleState(automode))
                }else{
                    val idleState = automode.states[AutomodeHandler.State.IDLE]
                    idleState?.let { automode.setCurrentState(it) }
                }

                //automode.setCurrentState(IdleState(automode))
                automode.activityTracker.stopSpeedScanning()
                automode.getCurrentState().disable(false)
                automode.next()
                dispose()
                stopAllTimers()*/
               stop("Speed = 0, We need to stop. from speedWatcher")
            }

            //}


        }

    }



    /*private fun watchSpeed() {
       //if(speedWatcher == null){
        speedWatcher = Timer("Timer for speed").schedule(automode.acceptableStopDuration,automode.acceptableStopDuration) {
            if (notMoving) {

                automodeHandler.messages.onNext(Message(Date().toString()+": Speed = 0, We need to stop. from speedWatcher"))
                automodeHandler.state.onNext(AutomodeHandler.State.IDLE)

                if(!automode.states.containsKey(AutomodeHandler.State.IDLE)){
                    automode.setCurrentState(IdleState(automode))
                }else{
                    val idleState = automode.states[AutomodeHandler.State.IDLE]
                    idleState?.let { automode.setCurrentState(it) }
                }

                //automode.setCurrentState(IdleState(automode))
                automode.activityTracker.stopSpeedScanning()
                automode.getCurrentState().disable(false)
                automode.next()
                dispose()
                stopAllTimers()
            }
            notMoving = true
        //}


        }

    }*/

    private fun watchGPS() {

        //if (gpsWatcher == null){
            gpsWatcher = Timer("Timer for gps").schedule(automode.timeToWaitForGps, automode.timeToWaitForGps) {
                //if (noGPS) {
                if((System.currentTimeMillis() - lastGpsTime) >= automode.timeToWaitForGps){
                   // cancel()

                    //automode.setCurrentState(IdleState(automode))

                   /* if(!automode.states.containsKey(AutomodeHandler.State.IDLE)){
                        automode.setCurrentState(IdleState(automode))
                    }else{
                        val idleState = automode.states[AutomodeHandler.State.IDLE]
                        idleState?.let { automode.setCurrentState(it) }
                    }
                    automodeHandler.state.onNext(AutomodeHandler.State.IDLE)
                    automode.getCurrentState().disable(false)
                    automodeHandler.messages.onNext(Message(Date().toString()+":No gps:Stop driving. from watchGPS"))


                    automode.activityTracker.stopSpeedScanning()
                    automode.next()

                    dispose()
                    stopAllTimers()*/

                    stop("No gps:Stop driving. from watchGPS")

                }
                //noGPS = true
            }
    //}

    }

    private fun stop(message : String){
            //automode.setCurrentState(IdleState(automode))

            if(!automode.states.containsKey(AutomodeHandler.State.IDLE)){
                automode.setCurrentState(IdleState(automode))
            }else{
                val idleState = automode.states[AutomodeHandler.State.IDLE]
                idleState?.let { automode.setCurrentState(it) }
            }
            automodeHandler.messages.onNext(Message(Date().toString()+": $message"))
            automodeHandler.state.onNext(AutomodeHandler.State.IDLE)
            automode.getCurrentState().disable(false)



            automode.activityTracker.stopSpeedScanning()

            //Wait for thirty seconds before going back to scan state. This allows not clear the current trip id while the
            // the stop not already sent.
            Thread.sleep(1000*30)

            automode.next()

            dispose()
            stopAllTimers()
    }

    /*private fun watchGPS() {
        noGPS = true
        gpsWatcher = Timer("Timer for gps", false).schedule(automode.timeToWaitForGps) {
            if (noGPS) {
                stopAllTimers()
                automode.setCurrentState(IdleState(automode))
                automodeHandler.messages.onNext(Message("No gps:Stop driving"))
                automodeHandler.state.onNext(AutomodeHandler.State.IDLE)
                automode.activityTracker.stopSpeedScanning()
                automode.next()
                dispose()
            }
        }

    }
*/
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

    private fun dispose(){
        for ( disposable in disposables){
            disposable.dispose()
        }
        disposables.clear()
    }

    override fun disable(disabled: Boolean) {
        this.disabled = disabled
    }
    override fun sate(): AutomodeHandler.State {
        return AutomodeHandler.State.DRIVING
    }
}