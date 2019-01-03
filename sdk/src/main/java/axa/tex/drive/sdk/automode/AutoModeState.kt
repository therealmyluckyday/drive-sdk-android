package axa.tex.drive.sdk.automode

interface AutoModeState {


    /**
     * State machine of AutoStart service
     * This state machine is independent of a possible start or stop tracking triggered by the user.
     *
     *                               No GPS Watchdog (4min)
     *               +-----------------------------------------------------+           >10km/h
     *               |          <30km/h after 1min                         |   +--------------------+
     *               |    +------------------------------+                 |   |                    |
     *               |    |                              |                 |   |                    |
     * +----+  +-----v----v-+  IN_VEHICLE activity  +----+----+ >30km/h  +-+---v-+  <10km/h  +------+--------+
     * |idle+-->activityScan+----------------------->speedScan+---------->driving+----------->drivingAndStill|
     * +----+  +-----+------+    passive location:  +---------+          +-------+           +------+--------+
     *               ^            speed >10km/h                                                     |
     *               |                                                                              |
     *               |            Stopped Car Timeout (3min) / No GPS Watchdog (4min)               |
     *               +------------------------------------------------------------------------------+
     *
     * From any state, an activityScan state can be enforced if the user force a stop tracking
     */


    fun scan(autoMode: AutoMode)

    fun stopScan()

    fun state(): State

    enum class State {
        TRACKING_ACTIVITY,
        SCANNING_SPEED,
        IN_VEHICLE,
        DRIVING
    }

}