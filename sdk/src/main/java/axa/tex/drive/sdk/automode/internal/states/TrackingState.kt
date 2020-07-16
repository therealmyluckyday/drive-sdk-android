package axa.tex.drive.sdk.automode.internal.states

import android.os.Handler
import android.os.Looper
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
        LOGGER.info( ":Tracking state created", "Constructor")
    }

    override fun enable() {
        LOGGER.info("\"Tracking Activity state ACTIVATE", "enable")
        val sensorService = automode.sensorService
        //automode.activityTracker.passivelyScanSpeed()
        val testing = automode.isSimulateDriving
        if (testing) {
            LOGGER.info("isSimulatedDriving", function = "enable")
            try {
                val mainHandler = Handler(Looper.getMainLooper())
                val myRunnable = Runnable() {
                    sensorService.activelyScanSpeed()
                    goNext()
                }
                mainHandler.post(myRunnable);
            }catch (e : Exception){
                LOGGER.warn("Exception : "+e, function = "enable")
            }
        } else {
            var activitySubscription: Disposable? = null
            activitySubscription = filterer.activityStream.subscribeOn(automode.rxScheduler).filter {it.type == DetectedActivity.IN_VEHICLE }.subscribe( {
                        if (!disabled) {
                            LOGGER.info(Date().toString() + " : In "+ it.type +" according to Activity Recognition Client", function = "fun activityStream.subscribe")
                            activitySubscription?.dispose()
                            sensorService.stopActivityScanning()
                            var subscription: Disposable? = null
                            subscription = filterer.gpsStream.subscribeOn(automode.rxScheduler).filter { it.speed >= SPEED_MOVEMENT_THRESHOLD }.subscribe {
                                LOGGER.info(Date().toString() + ":Speed of ${it.speed} reached", function = "fun gpsStream.subscribeOn()")
                                println(  ":Speed of ${it.speed} reached"+Thread.currentThread().getName())
                                subscription?.dispose()
                                goNext()
                            }
                            //scanning speed
                            sensorService.activelyScanSpeed()
                        }
            }, {throwable ->
                LOGGER.error("\"Tracking Activity exception $throwable", "next")
            })
            //Scanning activity
            LOGGER.info("\"Tracking Activity start checking activity", "next")
            sensorService.checkWhereAmI()
        }
    }

    override fun goNext() {
        this.disable()
        val nextState = InVehicleState(automode)
        automode.setCurrentState(nextState)
        LOGGER.info("\"Tracking Activity state next", "next")
        nextState.enable()
    }


    override fun disable() {
        this.disabled = true
    }
}