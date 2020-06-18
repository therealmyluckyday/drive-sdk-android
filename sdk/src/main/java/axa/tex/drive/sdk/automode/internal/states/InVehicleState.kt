    package axa.tex.drive.sdk.automode.internal.states

import axa.tex.drive.sdk.automode.AutomodeHandler
import axa.tex.drive.sdk.automode.internal.Automode
import axa.tex.drive.sdk.automode.internal.tracker.LOCATION_ACCURACY_THRESHOLD
import axa.tex.drive.sdk.automode.internal.tracker.SPEED_MOVEMENT_THRESHOLD
import axa.tex.drive.sdk.automode.internal.tracker.SpeedFilter
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.logger.LoggerFactory
import io.reactivex.disposables.Disposable
import org.koin.android.ext.android.inject
import java.util.*

internal class InVehicleState : AutoModeDetectionState {

    private var automode: Automode
    private val filterer: SpeedFilter
    private var disabled = false
    internal val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger

    constructor(automode: Automode) {
        this.automode = automode
        this.filterer = automode.getSpeedFilter()
        LOGGER.info("InVehicleState", "Constructor")
    }

    override fun enable() {
        val testing = automode.isSimulateDriving
        if (testing) {
            LOGGER.info("isSimulatedDriving", function = "enable")
            goNext()
        } else {
            var locationSubscription: Disposable? = null
            locationSubscription = filterer.gpsStream.subscribeOn(automode.rxScheduler).filter { it.speed >= SPEED_MOVEMENT_THRESHOLD && it.accuracy <= LOCATION_ACCURACY_THRESHOLD  }.subscribe( {
                    if (!disabled) {
                        LOGGER.info(Date().toString() + ":Speed of ${it.speed} >= ${SPEED_MOVEMENT_THRESHOLD} reached with ${it.accuracy} <= ${LOCATION_ACCURACY_THRESHOLD} of accuracy", function = "enable")
                        locationSubscription?.dispose()
                        goNext()
                }
            }, {throwable ->
                print(throwable)

                LOGGER.warn(""+throwable, function = "enable")
            })
        }
    }

    override fun goNext() {
        this.disable()
        val nextState = DrivingState(automode)
        automode.setCurrentState(nextState)
        LOGGER.info("In vehicule state next", "next")
        nextState.enable()
    }

    override fun disable() {
        this.disabled = disabled
    }
}