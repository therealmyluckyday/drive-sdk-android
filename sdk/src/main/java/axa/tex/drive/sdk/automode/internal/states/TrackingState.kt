package axa.tex.drive.sdk.automode.internal.states

import android.os.Handler
import android.os.Looper
import axa.tex.drive.sdk.automode.AutomodeHandler
import axa.tex.drive.sdk.automode.internal.Automode
import axa.tex.drive.sdk.automode.internal.tracker.SPEED_MOVEMENT_THRESHOLD
import axa.tex.drive.sdk.automode.internal.tracker.SpeedFilter
import axa.tex.drive.sdk.core.logger.LoggerFactory
import com.google.android.gms.location.DetectedActivity
import io.reactivex.disposables.Disposable
import java.util.*


internal class TrackingState : AutoModeDetectionState {

    private val filterer: SpeedFilter
    internal val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
    private var automode: Automode
    private var disabled = false


    constructor(automode: Automode) {
        this.automode = automode
        this.filterer = automode.getSpeedFilter()
        LOGGER.info( ":Tracking state", "Constructor")
    }

    override fun next() {
        LOGGER.info("\"Tracking Activity state ACTIVATE", "next")
        val sensorService = automode.sensorService
        //automode.activityTracker.passivelyScanSpeed()
        val testing = automode.isSimulateDriving
        if (testing) {
            LOGGER.info("isSimulatedDriving", function = "next")
            try {
                val mainHandler = Handler(Looper.getMainLooper())
                val myRunnable = Runnable() {
                    sensorService.activelyScanSpeed()
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
                    LOGGER.info(Date().toString() + " : In "+ it.type +" according to Activity Recognition Client", function = "fun next()")
                    activitySubscription?.dispose()
                    sensorService.stopActivityScanning()
                    var subscription: Disposable? = null
                    subscription = filterer.gpsStream.subscribeOn(automode.rxScheduler).filter { it.speed >= SPEED_MOVEMENT_THRESHOLD }.subscribe {
                            LOGGER.info(Date().toString() + ":Speed of ${it.speed} reached", function = "fun next()")
                        subscription?.dispose()

                        goNext()
                    }
                    //scanning speed
                    sensorService.activelyScanSpeed()
                }
            }, {throwable ->
                print(throwable)
                LOGGER.error("\"Tracking Activity exception $throwable", "next")
            })
            //Scanning activity
            LOGGER.info("\"Tracking Activity start checking activity", "next")
            sensorService.checkWhereAmI()
        }
    }

    fun goNext() {
        this.disable(true)
        automode.setCurrentState(InVehicleState(automode))
        LOGGER.info("\"Tracking Activity state next", "next")
        automode.getCurrentState().disable(false)
        automode.next()
    }


    override fun disable(disabled: Boolean) {
        this.disabled = disabled
    }
}