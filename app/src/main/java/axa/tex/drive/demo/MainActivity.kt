package axa.tex.drive.demo

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.viewbinding.ViewBinding
import axa.tex.drive.demo.databinding.ActivityMainBinding
import axa.tex.drive.sdk.acquisition.PermissionException
import axa.tex.drive.sdk.acquisition.TripRecorder
import axa.tex.drive.sdk.core.tools.FileManager
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var tripRecorder: TripRecorder? = null
    val REQUEST_CODE_BACKGROUND = 1545
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        requestForLocationPermission();

        println("["+Thread.currentThread().getName()+"][Configure]")
        val texApplication = application as TexDriveDemoApplication
        texApplication.configure()
        val service = texApplication.service
        tripRecorder = texApplication.tripRecorder

        binding.play.setOnClickListener {

            runOnUiThread {

                binding.play.visibility = View.GONE
                binding.stop.visibility = View.VISIBLE

                startService()
            }
        }
        binding.stop.setOnClickListener {
            binding.speedView.speedTo(0f)
            binding.speedView.stop()
            binding.stop.visibility = View.GONE
            binding.play.visibility = View.VISIBLE
            stopService()
        }

        if (tripRecorder?.isRecording()!!) {
            binding.play.visibility = View.GONE
            binding.stop.visibility = View.VISIBLE
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

        binding.trips.setOnLongClickListener {
            val intent = Intent(this, Trips::class.java)
            startActivity(intent)
            true
        }

        val autoModeHandler = service?.automodeHandler()
        autoModeHandler?.state?.subscribe( {driving ->
            if(driving){
                runOnUiThread {
                    binding.play.visibility = View.GONE
                    binding.stop.visibility = View.VISIBLE
                    startService()

                }

            }else{
                runOnUiThread {
                    stopService()
                    binding.play.visibility = View.VISIBLE
                    binding.stop.visibility = View.GONE
                    binding.speedView.speedTo(0f)
                    binding.speedView.stop()
                }
            }

        }, {throwable ->
            print(throwable)
        })

        service?.getSensorService()!!.speedFilter()?.gpsStream?.subscribe({
            binding.speedView.speedTo(it.speed*1f, 50)
        }, {throwable ->
            print(throwable)
        })
        service?.getTripRecorder()?.tripProgress()?.subscribeOn(Schedulers.io())?.subscribe({ it ->
            binding.distanceTextView.text = "Distance : "+it.distance+"Km\nSpeed : "+it.speed+"Km/h\nDuration : "+it.duration/1000+"s"
            Thread{
                //distanceTextView.text = "Distance["+it.distance+"]\n["+it.speed+"]\n["+it.duration+"]"
                print("Distance["+it.distance+"]\n["+it.speed+"]\n["+it.duration+"]")
            }.start()
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


    private fun shareLogsFile() {
        val tripLogFileName = "trip_location_test.csv"
        val uri = FileManager.getUriFromFilename(this, this.packageName, tripLogFileName)
        val logFile: File? = FileManager.getLogFile(applicationContext, tripLogFileName)

        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "*/*"
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        //shareIntent.putExtra(Intent.EXTRA_TEXT, message)
        shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Choose a destination"))
    }

    private fun startService() {


        try {
            val mainHandler = Handler(Looper.getMainLooper())
            val myRunnable = Runnable() {
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
                val tripId =  tripRecorder?.startTrip(Date().time);

                if (tripId!= null) {
                    val texApplication = application as TexDriveDemoApplication
                    texApplication.driving(tripId)
                }
                println(tripId)
            }
            mainHandler.post(myRunnable);
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
                        android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACTIVITY_RECOGNITION), 123)
        if (Build.VERSION.SDK_INT >= 23 ) {
            val hasForegroundLocationPermission = ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (Build.VERSION.SDK_INT >= 29 ) {
                if (hasForegroundLocationPermission) {

                } else {
                    ActivityCompat.requestPermissions(this,
                            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_CODE_BACKGROUND)
                }
            } else {
                if (!hasForegroundLocationPermission) {
                    ActivityCompat.requestPermissions(this,
                            arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_CODE_BACKGROUND)
                }
            }
        }
    }
}