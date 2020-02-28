package axa.tex.drive.sdk.acquisition.internal.tracker.fake

import axa.tex.drive.sdk.acquisition.internal.sensor.TexSensor
import axa.tex.drive.sdk.acquisition.model.Fix
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class FakeMotionSensor : TexSensor {
    private val fixProducer: PublishSubject<List<Fix>> = PublishSubject.create()

    private var enabled: Boolean = false
    override fun producer(): Observable<List<Fix>> {
        return fixProducer
    }

    override fun enableSensor() {
        enabled = true
        produceFixes()
    }


    override fun disableSensor() {
        enabled = false
    }

    override fun isEnabled(): Boolean {
        return enabled
    }

    override fun canBeEnabled(): Boolean {
        return true
    }

    private fun produceFixes() {
    /*
    Fake Classe
    // this is a Fake method
    */
    }

}