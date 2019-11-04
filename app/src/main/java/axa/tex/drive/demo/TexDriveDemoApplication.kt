package axa.tex.drive.demo

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import axa.tex.drive.sdk.acquisition.PermissionException
import axa.tex.drive.sdk.acquisition.TripRecorder
import axa.tex.drive.sdk.acquisition.model.TripId
import axa.tex.drive.sdk.core.Platform
import axa.tex.drive.sdk.core.TexConfig
import axa.tex.drive.sdk.core.TexService
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream
import java.util.*


class TexDriveDemoApplication : Application() {

    private val ID = 1234

    private var myTripId : TripId? = null
    internal  val CHANNEL_ID = "tex-channel-id"
    internal  val CHANNEL_NAME = "Notification"
    val tripRecorder: TripRecorder? by lazy {
        service?.getTripRecorder()
    }
    var service: TexService? = null
    lateinit var notificationManager: NotificationManager
    private var config: TexConfig? = null

    fun driving() {
        val tripRecorder = tripRecorder ?: return
        if (!tripRecorder.isRecording()) {
            myTripId = startTheService()
            notifyStart(if (myTripId != null) {
                "Start trip : ${myTripId!!.value}"
            } else {
                ""
            }, 7)
            saveTripForScore(myTripId!!.value)
            log(applicationContext, "=====================================================================\n")
            log(applicationContext, "New trip at ${Date().toString()}\n")
            log(applicationContext, "${Date().toString()}Trip Id =  at $myTripId\n")
        }
    }

    fun stopDriving() {
        val tripRecorder = tripRecorder ?: return
        if (tripRecorder.isRecording()) {
            notifyStart(if(myTripId != null){
                "End of trip : ${myTripId!!.value}"
            }else{
                ""
            }, 8)

            stopTheService()
            log(applicationContext, "Enf of trip at ${Date().toString()}\n")
            if(myTripId != null) {
                log(applicationContext, "${Date().toString()}Trip Id =  at ${myTripId?.value}\n")
            }
            log(applicationContext, "=====================================================================\n")

        }
    }

    override fun onCreate() {
        super.onCreate()
        // Fabric.with(this, Crashlytics())

        notifyStart("Tex drive next is running", 0)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val newConfig = TexConfig.Builder(applicationContext, "APP-TEST", "22910000").enableTrackers().platformHost(Platform.PREPROD).build()
        config = newConfig
        val newService = TexService.configure(newConfig)
        service = newService


        val autoModeHandler = newService.automodeHandler()

        newService.logStream()?.subscribeOn(Schedulers.io())?.subscribe({ it ->
            log(applicationContext, "[" + java.util.Calendar.getInstance() + "][" + it.file + "][" + it.function + "]" + it.description + "\n")
        })

        autoModeHandler.state?.subscribe ( {driving ->
            if (driving) {
                this.driving()
            } else {
                this.stopDriving()
            }
        }, {throwable ->
            log(applicationContext, "error on automodeHandler subscription ${throwable}\n")
        })

        if(!autoModeHandler.running!!) {
            Toast.makeText(applicationContext, "ACTIVATING.....", Toast.LENGTH_SHORT).show()
            autoModeHandler.activateAutomode(applicationContext)
        }else{
           Toast.makeText(applicationContext, "Already running.....", Toast.LENGTH_SHORT).show()
        }
    }

    fun log(context : Context?, data : String){
        try {
            val rootPath = context?.getExternalFilesDir("AUTOMODE")
            val root = File(rootPath?.toURI())
            if (!root.exists()) {
                root.mkdirs()
            }
            val f = File(rootPath?.path + "/log.txt")
            if (!f.exists()) {
                f.createNewFile()
            }
            val out = FileOutputStream(f, true)
            out.write(data.toByteArray())
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun  saveTripForScore(tripId : String){
        try {
            val rootPath = applicationContext?.getExternalFilesDir("AUTOMODE")
            val root = File(rootPath?.toURI())
            if (!root.exists()) {
                root.mkdirs()
            }
            val f = File(rootPath?.path + "/trips.txt")
            if (!f.exists()) {
                f.createNewFile()
            }
            val out = FileOutputStream(f, true)
            out.write("$tripId\n".toByteArray())
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }



    private fun startTheService() : TripId?{
        try {
           return  tripRecorder?.startTrip(Date().time)


        } catch (e: PermissionException) {
            e.printStackTrace()
        }
    return null
    }

    private fun stopTheService() {
        Thread {
            try {
                tripRecorder?.stopTrip(Date().time)
            } catch (e: PermissionException) {
                e.printStackTrace()
            }
        }.start()

    }

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



    @SuppressLint("NewApi")
    private fun notifyStart(message : String, notifId : Int){
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val currentApiVersion = android.os.Build.VERSION.SDK_INT
        val notification : Notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = createNotificationChannel();

                val notificationBuilder = NotificationCompat.Builder(this, channelId)
                notification = notificationBuilder.setOngoing(true).setContentText(message)
                        .setSmallIcon(axa.tex.drive.sdk.R.drawable.ic_logo)
                        .setCategory(Notification.CATEGORY_SERVICE)
                        .build()
           nm.notify(7, notification)
        }else if (currentApiVersion >= 16) {

            val notificationIntent = Intent(applicationContext, MainActivity::class.java)
            val contentIntent = PendingIntent.getService(applicationContext, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT)



            val res = applicationContext.getResources()
            val builder = Notification.Builder(applicationContext)

            builder.setContentIntent(contentIntent)
                    .setSmallIcon(R.drawable.ic_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.ic_logo))
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(false).setOngoing(true)
                    .setContentTitle(res.getString(R.string.app_name))
                    .setContentText(message)
            val n = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                builder.build()
            } else {
                TODO("VERSION.SDK_INT < JELLY_BEAN")
            }


            nm.notify(notifId, n)
        }
    }
}