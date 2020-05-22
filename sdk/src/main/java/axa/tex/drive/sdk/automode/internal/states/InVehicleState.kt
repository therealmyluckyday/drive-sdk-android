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

internal class InVehicleState : AutomodeState, KoinComponentCallbacks {

    private var automode: Automode
    private val filterer: SpeedFilter by inject()
    private var disabled = false
    internal val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger

    constructor(automode: Automode) {
        this.automode = automode
        LOGGER.info("InVehicleState", "Constructor")
    }

    override fun next() {
        val testing = automode.isSimulateDriving
        if (testing) {
            LOGGER.info("isSimulatedDriving", function = "next")
            goNext()
        } else {
            var locationSubscription: Disposable? = null
            locationSubscription = filterer.gpsStream.subscribeOn(automode.rxScheduler).filter { it.speed >= SPEED_MOVEMENT_THRESHOLD && it.accuracy <= LOCATION_ACCURACY_THRESHOLD  }.subscribe( {
                    if (!disabled) {
                        LOGGER.info(Date().toString() + ":Speed of ${it.speed} >= ${SPEED_MOVEMENT_THRESHOLD} reached with ${it.accuracy} <= ${LOCATION_ACCURACY_THRESHOLD} of accuracy", function = "next")
                        if ( it.speed >= SPEED_MOVEMENT_THRESHOLD ) {
                            LOGGER.info(Date().toString() + ":Speed GOOD ${it.speed} >= ${SPEED_MOVEMENT_THRESHOLD} reached with ${it.accuracy} <= ${LOCATION_ACCURACY_THRESHOLD} of accuracy", function = "next")
                        }
                        if ( it.accuracy < LOCATION_ACCURACY_THRESHOLD ) {
                            LOGGER.info(Date().toString() + ":Speed  ${it.speed} >= ${SPEED_MOVEMENT_THRESHOLD} reached with ACCURACY GOOD ${it.accuracy} <= ${LOCATION_ACCURACY_THRESHOLD} of accuracy", function = "next")
                        }

                        LOGGER.info(Date().toString() + ":Speed of ${it.speed} reached with ${it.accuracy} of accuracy", function = "next")
                    locationSubscription?.dispose()
                    goNext()
                }
            }, {throwable ->
                print(throwable)
            })
        }
    }

    fun goNext() {

        if (!automode.states.containsKey(AutomodeHandler.State.DRIVING)) {
            automode.setCurrentState(DrivingState(automode))
        } else {
            val drivingState = automode.states[AutomodeHandler.State.DRIVING]
            drivingState?.let { automode.setCurrentState(it) }
        }
        LOGGER.info("In vehicule state next", "next")
        this@InVehicleState.disable(true)
        automode.getCurrentState().disable(false)
        automode.next()
    }

    override fun state(): AutomodeHandler.State {
        return AutomodeHandler.State.IN_VEHICLE
    }

    override fun disable(disabled: Boolean) {
        this.disabled = disabled
    }
}