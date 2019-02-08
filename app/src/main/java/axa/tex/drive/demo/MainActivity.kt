package axa.tex.drive.demo

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.arch.lifecycle.Observer
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
import android.widget.Toast
import androidx.work.WorkManager
import axa.tex.drive.sdk.acquisition.PermissionException
import axa.tex.drive.sdk.acquisition.TripRecorder
import axa.tex.drive.sdk.acquisition.model.TexUser
import axa.tex.drive.sdk.automode.AutoMode
import axa.tex.drive.sdk.automode.AutoModeState
import axa.tex.drive.sdk.automode.internal.ActivityTracker

import axa.tex.drive.sdk.core.Platform
import axa.tex.drive.sdk.core.TexConfig
import axa.tex.drive.sdk.core.TexService
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private var tripRecorder: TripRecorder? = null
    private var config: TexConfig? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestForLocationPermission();





       /* config = TexConfig.Builder(user, applicationContext).enableBatteryTracker().enableLocationTracker()
                .enableMotionTracker().withAppName("BCI").withClientId("22910000").build(applicationContext);
        */
        //config = TexConfig.Builder(user, applicationContext).enableTrackers().withAppName("BCI").withClientId("22910000").platformHost(Platform.PREPROD).build();

        config = TexConfig.Builder(applicationContext,"APP-TEST","22910000").
                enableTrackers().platformHost(Platform.PREPROD).build();


        val service:TexService? = TexService.configure(config!!)
        tripRecorder = service?.getTripRecorder();


        play.setOnClickListener { play.visibility = View.GONE; stop.visibility = View.VISIBLE; startService(); }
        stop.setOnClickListener { stop.visibility = View.GONE; play.visibility = View.VISIBLE; stopService() }

        if (tripRecorder?.isRecording()!!) {
            play.visibility = View.GONE;
            stop.visibility = View.VISIBLE;
        }


        service?.logStream()?.subscribeOn(Schedulers.computation())?.subscribe {
            Thread{
                println(it)
            }.start()

        }

        val scoreRetriever = service?.scoreRetriever()
        scoreRetriever?.getScoreListener()?.subscribe {
            it?.let { score ->
                println(score) }
        }

        tripRecorder?.endedTripListener()?.subscribe {
            print(it)
        }

        //scoreRetriever?.retrieveScore(tripId = "ECDAF109-513A-4D0E-88AF-8F69724B86A1")

        score.setOnClickListener {
            val intent = Intent(this, Trips::class.java)
            startActivity(intent)
        }

        WorkManager.getInstance().getStatusesForUniqueWork("B9FBFF8B-D60C-4DA5-B37D-2B054E64612E").observe(this,Observer { stats ->
            run {
                if (stats != null) {
                    for (s in stats){
                        println("id = ${s.id} state = ${s.state} ")
                    }
                    println("THIS is the size : ${stats.size} ")
                }
            }
        })



        //==========================================================================================
     /*      val activityTracker = ActivityTracker(this);

       val automode = AutoMode(this)

          automode.statePublisher().subscribe{state ->
              when (state){
                 AutoModeState.State.DRIVING -> {
                     runOnUiThread{
                         play.visibility = View.GONE
                         stop.visibility = View.VISIBLE
                         Toast.makeText(this@MainActivity, "Now starting collect", Toast.LENGTH_SHORT).show()

                         startService()
                     }

                 }
                  AutoModeState.State.IDLE-> try {
                      runOnUiThread{
                          play.visibility = View.VISIBLE
                          stop.visibility = View.GONE
                          Toast.makeText(this@MainActivity, "Now stopping collect", Toast.LENGTH_SHORT).show()
                          stopService()
                      }
                  }catch (e: Exception){
                      e.printStackTrace()
                  }
              }
          }

         automode.setCurrentState(activityTracker)*/
        //automode.setCurrentState(activityTracker)
        //activityTracker.scan(automode)

        //==========================================================================================

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

        try {
            val tripId =  tripRecorder?.startTracking(Date().time);
            println(tripId)
        }catch (e: PermissionException){
            e.printStackTrace()
        }

    }

    private fun stopService() {
        Thread{
            try {
                tripRecorder?.stopTracking(Date().time)
            }catch (e: PermissionException){
                e.printStackTrace()
            }
            }.start()

    }


    private fun requestForLocationPermission() {


      /*val permissions = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)*/

        val locationPermission = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)

        val storageLocation = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (Build.VERSION.SDK_INT >= 23) {

            /*if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, storageLocation, 0)
            }*/

            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, locationPermission, 0)
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