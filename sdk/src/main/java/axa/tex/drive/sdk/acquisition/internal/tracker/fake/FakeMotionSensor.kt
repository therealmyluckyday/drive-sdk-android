package axa.tex.drive.sdk.acquisition.internal.tracker.fake


class FakeMotionSensor {

    var trackingEnabled: Boolean = false
    var trackingDisable: Boolean = true

    internal fun enableTracking() {
        disableOrEnableTracking(true)
    }

    internal fun disableTracking() {
        disableOrEnableTracking(false);
    }

    private fun disableOrEnableTracking(enable: Boolean) {
        trackingDisable = !enable
        trackingEnabled = enable
    }
}