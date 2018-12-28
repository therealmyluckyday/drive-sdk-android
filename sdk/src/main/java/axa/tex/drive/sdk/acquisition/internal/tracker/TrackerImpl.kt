package axa.tex.drive.sdk.acquisition.internal.tracker

import axa.tex.drive.sdk.acquisition.internal.sensor.TexSensor

open class TrackerImpl : Tracker{

    private var sensor : TexSensor

    constructor(sensor: TexSensor){
        this.sensor = sensor
    }

    override fun provideFixProducer(): Any {
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
}