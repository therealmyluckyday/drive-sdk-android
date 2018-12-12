package axa.tex.drive.sdk.acquisition.internal.tracker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import axa.tex.drive.sdk.acquisition.internal.tracker.fake.FakeBatterySensor
import axa.tex.drive.sdk.acquisition.internal.tracker.fake.FakeMotionSensor
import axa.tex.drive.sdk.acquisition.model.*
import io.reactivex.subjects.PublishSubject
import java.lang.IllegalArgumentException
import java.util.*


class BatteryTracker : Tracker, BroadcastReceiver {

    private val fixProducer: PublishSubject<Fix> = PublishSubject.create()

    private val mContext: Context?;
    private var isEnabled : Boolean
    private var fakeBatterySensor: FakeBatterySensor? = null


    constructor(context: Context,isEnabled : Boolean = false) {
        this.isEnabled = isEnabled
        this.mContext = context;
    }


    constructor(fakeBatterySensor: FakeBatterySensor? = null){
        this.fakeBatterySensor = fakeBatterySensor
        this.mContext = null
        this.isEnabled = false
    }


    override fun provideFixProducer(): Any {
        return fixProducer
    }

    override fun enableTracking() {
        if(fakeBatterySensor != null){
            fakeBatterySensor?.enableTracking()
            fakeBatterySensor?.provideFix()?.let { fixProducer.onNext(it) }
        }else {

            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            mContext?.registerReceiver(this, intentFilter)
        }
        isEnabled = true
    }

    override fun disableTracking() {
        if(fakeBatterySensor != null){
            fakeBatterySensor?.disableTracking()
        }else {
            try {
                mContext?.unregisterReceiver(this);
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
        }
        isEnabled = false
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