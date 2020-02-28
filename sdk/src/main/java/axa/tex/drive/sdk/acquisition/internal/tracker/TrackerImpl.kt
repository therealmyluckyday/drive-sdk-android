package axa.tex.drive.sdk.acquisition.internal.tracker

import axa.tex.drive.sdk.acquisition.internal.sensor.TexSensor
import axa.tex.drive.sdk.acquisition.model.Fix
import io.reactivex.Observable

open class TrackerImpl : Tracker {

    private var sensor: TexSensor

    constructor(sensor: TexSensor) {
        this.sensor = sensor
    }

    override fun provideFixProducer(): Observable<List<Fix>> {
        return sensor.producer()
    }

    override fun enableTracking() {
        sensor.enableSensor()
    }

    override fun disableTracking() {
        sensor.disableSensor()
    }

    override fun isEnabled(): Boolean {
        return sensor.isEnabled()
    }

    override fun canBeEnabled(): Boolean {
        return sensor.canBeEnabled()
    }
}