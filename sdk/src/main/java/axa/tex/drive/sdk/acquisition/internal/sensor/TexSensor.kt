package axa.tex.drive.sdk.acquisition.internal.sensor

import axa.tex.drive.sdk.acquisition.model.Fix
import io.reactivex.Observable

 interface TexSensor {
     fun producer(): Observable<List<Fix>>
     fun enableSensor()
     fun disableSensor()
     fun isEnabled(): Boolean
     fun canBeEnabled(): Boolean
}