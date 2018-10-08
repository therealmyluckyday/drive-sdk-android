package axa.tex.drive.sdk.acquisition.internal.tracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import axa.tex.drive.sdk.acquisition.model.*
import io.reactivex.subjects.PublishSubject
import java.lang.IllegalArgumentException
import java.util.*


class BatteryTracker : Tracker, BroadcastReceiver {

    private val fixProducer: PublishSubject<Fix> = PublishSubject.create()

    private val mContext: Context;
    private var isEnabled : Boolean


    constructor(context: Context,isEnabled : Boolean = false) {
        this.isEnabled = isEnabled
        this.mContext = context;
    }

    override fun provideFixProducer(): Any {
        return fixProducer
    }

    override fun enableTracking() {
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        mContext.registerReceiver(this, intentFilter)
        isEnabled = true
    }

    override fun disableTracking() {
        try {
            mContext.unregisterReceiver(this);
        }catch (e : IllegalArgumentException){
            e.printStackTrace()
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        // Battery level
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val batteryLevel = (100 * (level!! / scale!!.toFloat())).toInt()
        // Battery status
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isStateUnknown = status == BatteryManager.BATTERY_STATUS_UNKNOWN
        val isStateCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        var batteryState =  BatteryState.unknown
        if (!isStateUnknown) {
             batteryState = if (isStateCharging) {
                BatteryState.plugged
            } else {
                BatteryState.unplugged
            }
        }
        val batteryFix = BatteryFix(batteryLevel,batteryState,Date().time)
        fixProducer.onNext(batteryFix)
    }


  override fun isEnabled() : Boolean{
      return isEnabled
  }
}