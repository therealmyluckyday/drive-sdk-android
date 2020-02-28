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
import com.google.android.gms.location.DetectedActivity
import io.reactivex.disposables.Disposable
import org.koin.android.ext.android.inject
import java.util.*


internal class TrackingState : AutomodeState, KoinComponentCallbacks {

    private val filterer: SpeedFilter by inject()

    internal val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
    private var automode: Automode
    private var disabled = false


    constructor(automode: Automode) {
        this.automode = automode
        LOGGER.info( ":Tracking state", "Constructor")
    }

    override fun next() {
        LOGGER.info("\"Tracking Activity state ACTIVATE", "next")
        val tracker = automode.activityTracker
        //automode.activityTracker.passivelyScanSpeed()
        val testing = automode.isSimulateDriving
        if (testing) {
            try {
                val mainHandler = Handler(Looper.getMainLooper())
                val myRunnable = Runnable() {
                    tracker.activelyScanSpeed()
                    goNext()
                }
                mainHandler.post(myRunnable);
            }catch (e : Exception){
                e.printStackTrace()
            }
        } else {

            var activitySubscription: Disposable? = null
            activitySubscription = filterer.activityStream.subscribeOn(automode.rxScheduler).filter {it.type == DetectedActivity.IN_VEHICLE }.subscribe( {
                if (!disabled) {
                    activitySubscription?.dispose()
                    tracker.stopActivityScanning()
                    var subscription: Disposable? = null
                    LOGGER.info(Date().toString() + " : In vehicle according to Activity Recognition Client", function = "fun next()")
                    subscription = filterer.gpsStream.subscribeOn(automode.rxScheduler).filter { it.speed >= SPEED_MOVEMENT_THRESHOLD }.subscribe {
                            LOGGER.info(Date().toString() + ":Speed of ${it.speed} reached", function = "fun next()")
                        subscription?.dispose()

                        goNext()
                    }
                    //scanning speed
                    tracker.activelyScanSpeed()
                }
            }, {throwable ->
                print(throwable)
                LOGGER.error("\"Tracking Activity exception $throwable", "next")
            })
            //Scanning activity
            LOGGER.info("\"Tracking Activity start checking activity", "next")
            tracker.checkWhereAmI()
        }
    }

    fun goNext() {
        if (!automode.states.containsKey(AutomodeHandler.State.IN_VEHICLE)) {
            automode.setCurrentState(InVehicleState(automode))
        } else {
            val vehicleState = automode.states[AutomodeHandler.State.IN_VEHICLE]
            vehicleState?.let { automode.setCurrentState(it) }
        }
        LOGGER.info("\"Tracking Activity state next", "next")
        this@TrackingState.disable(true)
        automode.getCurrentState().disable(false)
        automode.next()
    }

    override fun state(): AutomodeHandler.State {
        return AutomodeHandler.State.TRACKING_ACTIVITY
    }

    override fun disable(disabled: Boolean) {
        this.disabled = disabled
    }
}