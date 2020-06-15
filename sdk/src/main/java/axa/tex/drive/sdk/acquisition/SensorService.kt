package axa.tex.drive.sdk.acquisition

import axa.tex.drive.sdk.automode.internal.tracker.SpeedFilter
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks

interface SensorService: KoinComponentCallbacks {
    fun speedFilter() : SpeedFilter

    fun checkWhereAmI()

    fun stopActivityScanning()

    fun stopSpeedScanning()

    fun passivelyScanSpeed()

    fun activelyScanSpeed()

    fun requestForLocationPermission()
}