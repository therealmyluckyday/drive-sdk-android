package axa.tex.drive.demo

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import axa.tex.drive.sdk.acquisition.SensorService
import axa.tex.drive.sdk.acquisition.SensorServiceFake
import axa.tex.drive.sdk.acquisition.SensorServiceImpl
import axa.tex.drive.sdk.acquisition.TripRecorder
import axa.tex.drive.sdk.acquisition.model.TripId
import axa.tex.drive.sdk.core.*
import axa.tex.drive.sdk.core.tools.FileManager
import axa.tex.drive.sdk.core.tools.FileManager.Companion.getLogFile
import io.reactivex.schedulers.Schedulers
import java.io.*
import java.util.*
import kotlin.concurrent.schedule


class TexDriveDemoApplication : Application() {

    private val ID = 1234
    // Trip recorder
    private var oldLocation: Location? = null
    private val tripLogFileName = "trip_location_test.csv"
    var sensorService: SensorServiceFake? = null

    val logFileName = "axa-dil-tex.txt"

    val rxScheduler = Schedulers.single()
    private var myTripId : TripId? = null
    internal  val CHANNEL_ID = "tex-channel-id"
    internal  val CHANNEL_NAME = "Notification"
    val tripRecorder: TripRecorder? by lazy {
        service?.getTripRecorder()
    }
    var service: TexService? = null
    lateinit var notificationManager: NotificationManager
    private var config: TexConfig? = null

    fun driving(myTripId: TripId) {
        val tripRecorder = tripRecorder ?: return
        if (!tripRecorder.isRecording()) {
            notifyStart("Start trip : ${myTripId.value}", 7)
            saveTripForScore(myTripId.value)
            FileManager.log( "=====================================================================\n", logFileName, applicationContext)
            FileManager.log( "New trip at ${Date().toString()}\n", logFileName, applicationContext)
            FileManager.log( "${Date().toString()}Trip Id =  at $myTripId\n", logFileName, applicationContext)
        }
    }

    fun stopDriving() {
        val tripRecorder = tripRecorder ?: return
        if (tripRecorder.isRecording()) {
            notifyStart("End of trip :", 8)
            FileManager.log( "Enf of trip at ${Date().toString()}\n", logFileName, applicationContext)
            FileManager.log( "=====================================================================\n", logFileName, applicationContext)

        }
    }

    fun configure() {
        val sensorServiceFake = SensorServiceFake(applicationContext, rxScheduler)
        sensorService = sensorServiceFake
        val newConfig = TexConfig.Builder(applicationContext, "APP-TEST", "22910000", sensorServiceFake, rxScheduler ).enableTrackers().platformHost(Platform.PRODUCTION).build()
        config = newConfig
        val newService = TexService.configure(newConfig)
        service = newService
        val autoModeHandler = newService.automodeHandler()

        newService.logStream().subscribeOn(Schedulers.io()).subscribe({ it ->
            FileManager.log( "[" + it.file + "][" + it.function + "]" + it.description + "\n", logFileName, applicationContext)
            Thread{
                println("["+it.file +"]["+ it.function + "]"+ it.description )
            }.start()
        })

        newService.getTripRecorder().tripProgress().subscribeOn(Schedulers.io())?.subscribe({ it ->
            var timeDelay: Long = 0
            if (oldLocation == null) {
            } else {
                // time  UTC time of this fix, in milliseconds since January 1, 1970.
                timeDelay = it.location.time - oldLocation!!.time
            }

            oldLocation = it.location
            // Save trip for reuse
            //saveLocation(it.location, timeDelay)
            FileManager.log("[TripProgress][" + it.duration + "][" + it.distance + "]["+it.speed+"]\n", logFileName, applicationContext)
            Thread{
                println("[TripProgress][" + it.duration + "][" + it.distance + "]["+it.speed+"]")
            }.start()
        })
        if(!autoModeHandler.running) {
            Toast.makeText(applicationContext, "ACTIVATING.....", Toast.LENGTH_SHORT).show()
            autoModeHandler.activateAutomode(applicationContext,true, isSimulatedDriving = false)
        }else{
            Toast.makeText(applicationContext, "Already running.....", Toast.LENGTH_SHORT).show()
        }
        // Show all log text
        //updateLogsText()

        // Delete log file
        //FileManager.getLogFile(applicationContext, logFileName)

        // Delete Trip Log file
        //FileManager.getLogFile(applicationContext, tripLogFileName)

        // Launch recorded trip
        Timer("SettingUp", false).schedule(1000) {
            sensorServiceFake.loadTrip(applicationContext, 1000L)
        }

    }
    fun saveLocation(location: Location, delay:Long) {
        var message = "${location.latitude},${location.longitude},${location.accuracy},${location.speed},${location.bearing},${location.altitude},${delay}\n"
        //println(message)
        FileManager.log(message, tripLogFileName, applicationContext)
    }

    override fun onCreate() {
        super.onCreate()

        notifyStart("Tex drive next is running", 0)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun updateLogsText() {
        val logFile: File? = getLogFile(applicationContext, logFileName)
        if (logFile !== null && logFile.exists()) {
            val queue: LinkedHashMap<Int, String?> = LinkedHashMap(500)
            try {
                val bufferedReader = BufferedReader(FileReader(logFile))
                var lineIndex = 0
                var line = bufferedReader.readLine()
                while (line != null) {
                    queue[lineIndex] = line
                    line = bufferedReader.readLine()
                    lineIndex++
                }
                bufferedReader.close()
            } catch (e: IOException) {
                //You'll need to add proper error handling here
            }
            val list: List<String?> = ArrayList(queue.values)
            Collections.reverse(list)
            Thread {
                for (line in list) {
                        println(line)
                }
            }.start()
        }
    }


    fun saveTripForScore(tripId : String){
        val tripFileName = "trips.txt"
        FileManager.log("$tripId\n", tripFileName, applicationContext)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannel(): String {
        val channelId = CHANNEL_ID
        val channelName = CHANNEL_NAME
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(channelId,
                        channelName, NotificationManager.IMPORTANCE_NONE)


            chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            service.createNotificationChannel(chan)
            return channelId
        }
        return "ERROR"
    }



    @SuppressLint("NewApi")
    fun notifyStart(message : String, notifId : Int){
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