package axa.tex.drive.demo

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import androidx.work.WorkManager
import axa.tex.drive.sdk.acquisition.PermissionException
import axa.tex.drive.sdk.acquisition.TripRecorder
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {
    private var tripRecorder: TripRecorder? = null
    val REQUEST_CODE_BACKGROUND = 1545
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestForLocationPermission();

        val texApplication = application as TexDriveDemoApplication
        texApplication.configure()
        val service = texApplication.service
        tripRecorder = texApplication.tripRecorder

        play.setOnClickListener {
            play.visibility = View.GONE
            stop.visibility = View.VISIBLE
            startService()
        }
        stop.setOnClickListener {
            speedView.speedTo(0f)
            speedView.isWithTremble = false
            speedView.stop()
            stop.visibility = View.GONE
            play.visibility = View.VISIBLE
            stopService()
        }

        if (tripRecorder?.isRecording()!!) {
            play.visibility = View.GONE
            stop.visibility = View.VISIBLE
        }




        val scoreRetriever = service?.scoreRetriever()
        scoreRetriever?.getScoreListener()?.subscribe ( {
            it?.let { score ->
                println("ScoreWorker result" + score) }
        }, {throwable ->
            print(throwable)
        })

        (application as TexDriveDemoApplication).tripRecorder?.endedTripListener()?.subscribe ( {
            print(it)
        }, {throwable ->
            print(throwable)
        })

        trips.setOnLongClickListener {
            val intent = Intent(this, Trips::class.java)
            startActivity(intent)
            true
        }

        val autoModeHandler = service?.automodeHandler()
        autoModeHandler?.state?.subscribe( {driving ->
            if(driving){
                runOnUiThread {
                   startService()
                    play.visibility = View.GONE
                    stop.visibility = View.VISIBLE
                }

            }else{
                runOnUiThread {
                   stopService()
                    play.visibility = View.VISIBLE
                    stop.visibility = View.GONE
                    speedView.speedTo(0f)
                    speedView.stop()
                    speedView.isWithTremble = false
                }
            }

        }, {throwable ->
            print(throwable)
        })

        autoModeHandler?.speedListener?.gpsStream?.subscribe({
            speedView.speedTo(it.speed*3.6f, 50)
        }, {throwable ->
            print(throwable)
        })
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
            val tripId =  tripRecorder?.startTrip(Date().time);

            if (tripId!= null) {
                val texApplication = application as TexDriveDemoApplication
                texApplication.driving(tripId)
            }
            println(tripId)
        }catch (e: PermissionException){
            e.printStackTrace()
        }
    }

    private fun stopService() {
        Thread{
            try {
                tripRecorder?.stopTrip(Date().time)

                val texApplication = application as TexDriveDemoApplication
                texApplication.stopDriving()

            }catch (e: PermissionException){
                e.printStackTrace()
            }
            }.start()

    }

    private fun requestForLocationPermission() {
        ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_BACKGROUND_LOCATION), 123)
        if (Build.VERSION.SDK_INT >= 23 ) {
            val hasForegroundLocationPermission = ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (Build.VERSION.SDK_INT >= 29 ) {
                if (hasForegroundLocationPermission) {
                    val hasBackgroundLocationPermission = ActivityCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                    if (!hasBackgroundLocationPermission) {
                        ActivityCompat.requestPermissions(this,
                                arrayOf(android.Manifest.permission.ACCESS_BACKGROUND_LOCATION), REQUEST_CODE_BACKGROUND)
                    }
                } else {
                    ActivityCompat.requestPermissions(this,
                            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION), REQUEST_CODE_BACKGROUND)
                }
            } else {
                if (!hasForegroundLocationPermission) {
                    ActivityCompat.requestPermissions(this,
                            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_BACKGROUND_LOCATION), REQUEST_CODE_BACKGROUND)
                }
            }
        }
    }
}