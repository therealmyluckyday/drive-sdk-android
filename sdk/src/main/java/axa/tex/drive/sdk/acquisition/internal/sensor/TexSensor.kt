package axa.tex.drive.sdk.acquisition.internal.sensor

import android.os.Bundle
import axa.tex.drive.sdk.acquisition.model.Fix
import axa.tex.drive.sdk.acquisition.model.LocationFix
import io.reactivex.Observable

interface TexSensor {
    fun producer() : Observable<List<Fix>>

    fun enableSensor()

    fun disableSensor()

    fun isEnabled() : Boolean
}