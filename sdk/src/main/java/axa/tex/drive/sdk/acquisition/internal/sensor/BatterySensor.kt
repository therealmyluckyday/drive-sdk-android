package axa.tex.drive.sdk.acquisition.internal.sensor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_BATTERY_CHANGED
import android.content.IntentFilter
import android.os.BatteryManager
import axa.tex.drive.sdk.acquisition.model.BatteryFix
import axa.tex.drive.sdk.acquisition.model.BatteryState
import axa.tex.drive.sdk.acquisition.model.Fix
import axa.tex.drive.sdk.core.logger.LoggerFactory
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.*

internal class BatterySensor : TexSensor, BroadcastReceiver {

    private val context: Context?
    private val fixProducer: PublishSubject<List<Fix>> = PublishSubject.create()
    private var enabled = true

    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
    var canBeEnabled: Boolean


    constructor(context: Context?, canBeEnabled: Boolean = true) {
        this.context = context
        this.canBeEnabled = canBeEnabled
    }


    override fun producer(): Observable<List<Fix>> {
        return fixProducer
    }

    override fun enableSensor() {
        if (canBeEnabled) {
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            try {
                context?.registerReceiver(this, intentFilter)
            } catch (e: IllegalArgumentException) {
                LOGGER.warn("Unable to register : The receiver may already be registered", "enableSensor")
            }
            enabled = true
        }
    }

    override fun disableSensor() {
        try {
            context?.unregisterReceiver(this)
        } catch (e: IllegalArgumentException) {
            //The receiver may not be registered yet.
            LOGGER.warn("Unable to unregister: The receiver may not be registered yet.", "disableSensor")
        }
        enabled = false
    }

    override fun isEnabled(): Boolean {
        return enabled
    }

    override fun canBeEnabled(): Boolean {
        return canBeEnabled
    }


    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || intent?.action != Intent.ACTION_BATTERY_CHANGED) {
            return
        }
        // Battery level
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryLevel = (100 * (level!! / scale!!.toFloat())).toInt()
        // Battery status
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isStateUnknown = status == BatteryManager.BATTERY_STATUS_UNKNOWN
        val isStateCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        var batteryState = BatteryState.unknown
        if (!isStateUnknown) {
            batteryState = if (isStateCharging) {
                BatteryState.plugged
            } else {
                BatteryState.unplugged
            }
        }
        val batteryFix = BatteryFix(batteryLevel, batteryState, Date().time)
        fixProducer.onNext(listOf(batteryFix))
    }
}