package axa.tex.drive.sdk.acquisition.internal.tracker.fake

import axa.tex.drive.sdk.acquisition.internal.sensor.TexSensor
import axa.tex.drive.sdk.acquisition.model.BatteryFix
import axa.tex.drive.sdk.acquisition.model.BatteryState
import axa.tex.drive.sdk.acquisition.model.Fix
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class FakeBatterySensor(private val level: Int = 0, private val state: BatteryState = BatteryState.plugged, private val timestamp: Long = 1000000000) : TexSensor {


    private val fixProducer: PublishSubject<List<Fix>> = PublishSubject.create()

    private var enabled: Boolean = false


    override fun producer(): Observable<List<Fix>> {
        return fixProducer
    }

    override fun enableSensor() {
        enabled = true
        fixProducer.onNext(listOf(BatteryFix(level, state, timestamp)))
    }

    override fun disableSensor() {
        enabled = false
    }

    override fun isEnabled(): Boolean {
        return enabled
    }

}