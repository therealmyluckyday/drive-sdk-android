package axa.tex.drive.sdk.automode.internal.tracker

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import axa.tex.drive.sdk.automode.internal.tracker.model.TexLocation
import axa.tex.drive.sdk.core.LocationSensorService
import axa.tex.drive.sdk.core.SensorService
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.logger.LoggerFactory
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import io.reactivex.Scheduler
import io.reactivex.subjects.PublishSubject
import org.koin.android.ext.android.inject



@SuppressLint("MissingPermission")
internal class AutoModeTracker : TexActivityTracker, KoinComponentCallbacks {

    val sensorService: SensorService

    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
    private var context: Context


    constructor(context: Context, sensorService: SensorService, scheduler: Scheduler) {
        this.context = context
        this.sensorService = sensorService
    }

    override fun passivelyScanSpeed() {
        this.sensorService.passivelyScanSpeed()
    }

    override fun activelyScanSpeed() {
        this.sensorService.activelyScanSpeed()
    }

    @SuppressLint("MissingPermission")
    override fun stopSpeedScanning() {
        this.sensorService.stopSpeedScanning()
    }

    override fun checkWhereAmI() {
        this.sensorService.checkWhereAmI()
    }

    override fun stopActivityScanning() {
        this.sensorService.stopActivityScanning()
    }
}