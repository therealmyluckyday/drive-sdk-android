package axa.tex.drive.sdk.acquisition.internal.tracker.fake

import axa.tex.drive.sdk.acquisition.internal.sensor.TexSensor
import axa.tex.drive.sdk.acquisition.model.Fix
import axa.tex.drive.sdk.acquisition.model.LocationFix
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.*

class FakeLocationSensor : TexSensor {

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

    private fun produceFixes() {
        var time: Long = Date().time

        Thread {
            while (enabled) {
                val fakeFix = LocationFix(12.0, 1.88282, 28.0f, 10.9f, 18.0f, 10.0, time)
                time += 1000L
                Thread.sleep(1000)
                fixProducer.onNext(listOf(fakeFix))

            }
        }.start()
    }
}