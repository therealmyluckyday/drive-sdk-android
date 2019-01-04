package axa.tex.drive.demo

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.app.NotificationCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import androidx.work.WorkManager
import axa.tex.drive.sdk.acquisition.TripRecorder
import axa.tex.drive.sdk.acquisition.model.TexUser
import axa.tex.drive.sdk.automode.AutoModeState
import axa.tex.drive.sdk.core.TexConfig
import axa.tex.drive.sdk.core.TexService
import axa.tex.drive.sdk.core.internal.Constants
import axa.tex.drive.sdk.core.logger.LoggerFactory
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    val TAG: String = MainActivity::class.java.simpleName

    var tripRecorder: TripRecorder? = null
    var config: TexConfig? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestForLocationPermission();

        /*  val activityTracker = ActivityTracker(this);
          val automode = AutoMode(this)

          automode.statePublisher().subscribe{state -> setState(state)}

          automode.setCurrentState(activityTracker)
          activityTracker.scan(automode)*/

        val user = TexUser("appId", "FFFDIHOVA3131IJA1")

        config = TexConfig.Builder(user, applicationContext).enableBatteryTracker().enableLocationTracker()
                .enableMotionTracker().withAppName("BC").withClientId("22910000").build(applicationContext);
        tripRecorder = TexService.configure(config!!)?.getTripRecorder();


        play.setOnClickListener { play.visibility = View.GONE; stop.visibility = View.VISIBLE; startService(); }
        stop.setOnClickListener { stop.visibility = View.GONE; play.visibility = View.VISIBLE; stopService() }

        if (tripRecorder?.isRecording()!!) {
            play.visibility = View.GONE;
            stop.visibility = View.VISIBLE;
        }


        val logger = LoggerFactory.getLogger();
        logger.getLogStream().subscribeOn(Schedulers.computation()).subscribe {
            println(it)
        }

        score.setOnClickListener {
            val intent = Intent(this, Trips::class.java)
            startActivity(intent)
        }
    }

    internal  val CHANNEL_ID = "tex-channel-id"
    internal  val CHANNEL_NAME = "Notification"

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(): String {
        val channelId = CHANNEL_ID
        val channelName = CHANNEL_NAME
        val chan = NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }


    private fun startService() {


            var notification : Notification? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelId = createNotificationChannel();
                val notificationBuilder = NotificationCompat.Builder(this, channelId)
                notification  = notificationBuilder.setOngoing(true)
                        .setSmallIcon(R.drawable.white_hare)
                        .setCategory(Notification.CATEGORY_SERVICE)
                        .build()

            }

            tripRecorder?.setCustomNotification(notification)

            tripRecorder?.startTracking(Date().time);


    }

    private fun stopService() {
        val value = WorkManager.getInstance().getStatusesByTag("9a2dc881-c136-47b6-b3b0-afbc44961055").value;

        tripRecorder?.stopTracking(Date().time);
    }


    private fun requestForLocationPermission() {


        val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (Build.VERSION.SDK_INT >= 23) {

            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, 0)
            }

            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, 0)
            }
        }
    }

    private fun scanningSpeedState() {
        speed_scanning.setCompoundDrawablesWithIntrinsicBounds(R.drawable.blue, 0, 0, 0)
        in_vehicle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.green, 0, 0, 0)
    }

    private fun inVehicleState() {
        speed_scanning.setCompoundDrawablesWithIntrinsicBounds(R.drawable.gray, 0, 0, 0)
        in_vehicle.setCompoundDrawablesWithIntrinsicBounds(R.drawable.blue, 0, 0, 0)
    }

    private fun activityTrackingState() {
        activity_tracking.setCompoundDrawablesWithIntrinsicBounds(R.drawable.blue, 0, 0, 0)
    }


    private fun setState(state: AutoModeState.State) {
        when (state) {
            AutoModeState.State.SCANNING_SPEED -> scanningSpeedState()
            AutoModeState.State.IN_VEHICLE -> inVehicleState()
            AutoModeState.State.TRACKING_ACTIVITY -> activityTrackingState()
        }
    }
}