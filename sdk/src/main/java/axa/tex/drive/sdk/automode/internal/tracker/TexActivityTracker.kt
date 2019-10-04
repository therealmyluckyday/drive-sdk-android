package axa.tex.drive.sdk.automode.internal.tracker

interface TexActivityTracker {
    fun checkWhereAmI()

    fun stopActivityScanning()

    fun stopSpeedScanning()

    fun passivelyScanSpeed()

    fun activelyScanSpeed()
}