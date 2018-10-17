package axa.tex.drive.demo

import android.content.pm.PackageManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.View
import androidx.work.WorkManager

import axa.tex.drive.sdk.acquisition.TripRecorder
import axa.tex.drive.sdk.acquisition.model.TexUser
import axa.tex.drive.sdk.automode.AutoMode
import axa.tex.drive.sdk.automode.AutoModeState
import axa.tex.drive.sdk.automode.internal.ActivityTracker
import axa.tex.drive.sdk.core.Platform
import axa.tex.drive.sdk.core.TexConfig
import axa.tex.drive.sdk.core.TexService

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val TAG: String = MainActivity::class.java.simpleName

    var tripRecorder : TripRecorder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestForLocationPermission();

        val activityTracker = ActivityTracker(this);
        val automode = AutoMode(this)

        automode.statePublisher().subscribe{state -> setState(state)}

        automode.setCurrentState(activityTracker)
        activityTracker.scan(automode)

        val user = TexUser("appId", "FFFDIHOVA3131IJA1")
        val config : TexConfig = TexConfig.Builder(user,applicationContext).enableBatteryTracker().enableLocationTracker().enableMotionTracker().build();
        tripRecorder = TexService.configure(config)?.getTripRecorder();
       // tripRecorder = TripRecorder(applicationContext);

        play.setOnClickListener { play.visibility = View.GONE; stop.visibility = View.VISIBLE; startService();}
        stop.setOnClickListener { stop.visibility = View.GONE; play.visibility = View.VISIBLE; stopService()}

        if(tripRecorder?.isRecording()!!){
            play.visibility = View.GONE;
            stop.visibility = View.VISIBLE;
        }



        //var locationFix = LocationFix(1.000000, 1.614514, 1.6552f, 12.4442f, 13.8f, 16.0, 14414, "124");

        //Log.i(TAG, locationFix.toJson())
       // Log.i(TAG, locationFix.altitude.toString())

       // val collector = Collector(LocationTracker(this), BatteryTracker(this));
        //collector.collect();
       // TexConfig("").build(this.applicationContext);
        //TexConfig.Builder("AppId","user",applicationContext).platformHost(Platform.PREPROD).build()

       // val motionTracker = MotionTracker (this);
       // motionTracker.enableTracking(true);


    }

    private fun startService(){

        tripRecorder?.track();
    }

    private fun stopService(){
        val value = WorkManager.getInstance().getStatusesByTag("9a2dc881-c136-47b6-b3b0-afbc44961055").value;
        Log.i("WORKS",value.toString())
        tripRecorder?.stopTracking();
    }



    private fun requestForLocationPermission() {
        val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, 0)
            }
        }
    }

    private fun scanningSpeedState(){
        speed_scanning.setCompoundDrawablesWithIntrinsicBounds(R.drawable.blue,0,0,0)
        in_vehicle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.green,0,0,0)
    }

    private fun inVehicleState(){
        speed_scanning.setCompoundDrawablesWithIntrinsicBounds(R.drawable.gray,0,0,0)
        in_vehicle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.blue,0,0,0)
    }

    private fun activityTrackingState(){
        activity_tracking.setCompoundDrawablesWithIntrinsicBounds(R.drawable.blue,0,0,0)
    }


    private fun setState(state: AutoModeState.State){
        when(state){
            AutoModeState.State.SCANNING_SPEED -> scanningSpeedState()
            AutoModeState.State.IN_VEHICLE -> inVehicleState()
            AutoModeState.State.TRACKING_ACTIVITY -> activityTrackingState()
        }
    }
}


