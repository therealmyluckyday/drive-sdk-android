package axa.tex.drive.demo

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Build
import android.os.Environment
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import axa.tex.drive.sdk.acquisition.TripRecorder
import axa.tex.drive.sdk.acquisition.model.TripId
import axa.tex.drive.sdk.automode.internal.tracker.SpeedFilter
import axa.tex.drive.sdk.automode.internal.tracker.model.TexLocation
import axa.tex.drive.sdk.core.Platform
import axa.tex.drive.sdk.core.SensorService
import axa.tex.drive.sdk.core.TexConfig
import axa.tex.drive.sdk.core.TexService
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.io.*
import java.util.*
import kotlin.concurrent.schedule


class TexDriveDemoApplication : Application() {

    private val ID = 1234
    // Trip recorder
    private var oldLocation: Location? = null
    private val tripLogFileName = "axa-dil-tex-trip-1.csv"
    var sensorService: SensorService? = null

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
            log( "=====================================================================\n")
            log( "New trip at ${Date().toString()}\n")
            log( "${Date().toString()}Trip Id =  at $myTripId\n")
        }
    }

    fun stopDriving() {
        val tripRecorder = tripRecorder ?: return
        if (tripRecorder.isRecording()) {
            notifyStart("End of trip :", 8)
            log( "Enf of trip at ${Date().toString()}\n")
            log( "=====================================================================\n")

        }
    }

    fun configure() {
        sensorService = SensorService(applicationContext, rxScheduler)
        val newConfig = TexConfig.Builder(applicationContext, "APP-TEST", "22910000",sensorService!!, rxScheduler ).enableTrackers().platformHost(Platform.PRODUCTION).build()
        config = newConfig
        val newService = TexService.configure(newConfig)
        service = newService
        val autoModeHandler = newService.automodeHandler()

        newService.logStream().subscribeOn(Schedulers.io()).subscribe({ it ->
            log( "[" + it.file + "][" + it.function + "]" + it.description + "\n")
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
            //log("[TripProgress][" + it.duration + "][" + it.distance + "]["+it.speed+"]\n")
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
        //deleteLogsFile()

        // Delete Trip Log file
        //deleteLogsFile(tripLogFileName)

        // Launch recorded trip
        Timer("SettingUp", false).schedule(10000) {
            //loadTrip(sensorService!!.speedFilter())
        }
    }

    fun saveLocation(location: Location, delay:Long) {
        var message = "${location.latitude},${location.longitude},${location.accuracy},${location.speed},${location.bearing},${location.altitude},${delay}\n"
        println(message)
        log(message, tripLogFileName)
    }



    fun loadTrip() {
        println("loadTrip")
        val logFile: File? = this.getLogFile(applicationContext, tripLogFileName)
        if (logFile !== null && logFile.exists()) {
            Thread {
                var newTime = System.currentTimeMillis() - 86400000
                try {
                    val bufferedReader = BufferedReader(FileReader(logFile))
                    var lineIndex = 0
                    var line = bufferedReader.readLine()
                    while (line != null) {
                        newTime = sendLocationLineStringToSpeedFilter(line, newTime)
                        line = bufferedReader.readLine()
                        lineIndex++
                    }
                    bufferedReader.close()
                } catch (e: IOException) {
                    //You'll need to add proper error handling here
                }
            }.start()
        }
    }

    private fun sendLocationLineStringToSpeedFilter(line: String, time: Long) : Long {
        val locationDetails = line.split(",")
        var newLocation = Location("")
        val latitude = locationDetails[0].toDouble()
        newLocation.latitude = latitude
        val longitude = locationDetails[1].toDouble()
        newLocation.longitude = longitude
        val accuracy = locationDetails[2].toFloat()
        newLocation.accuracy = accuracy
        val speed = locationDetails[3].toFloat()
        newLocation.speed = speed // 20.F
        val bearing = locationDetails[4].toFloat()
        newLocation.bearing = bearing
        val altitude = locationDetails[5].toDouble()
        newLocation.altitude =altitude
        val delay = locationDetails[6].toLong()
        newLocation.time = time + delay
        //if (speed > 0) {
            //println("sendLocationLineStringToSpeedFilter"+newLocation.latitude+" "+newLocation.longitude+" "+newLocation.accuracy+" "+newLocation.speed+" "+newLocation.bearing+ " "+newLocation.altitude+" "+newLocation.time)
            this.sensorService!!.forceLocationChanged(newLocation)
            Thread.sleep(100L)
        //}
        return newLocation.time
    }

    override fun onCreate() {
        super.onCreate()
        // Fabric.with(this, Crashlytics())

        notifyStart("Tex drive next is running", 0)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    private fun log(data: String, fileName:String = "axa-dil-tex.txt") {
        try {
            val rootPath = applicationContext.getExternalFilesDir("AUTOMODE")
            val root = File(rootPath!!.toURI())
            if (!root.exists()) {
                root.mkdirs()
            }
            val f = File(rootPath.path + "/" + fileName)
            if (!f.exists()) {
                try {
                    f.createNewFile()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            try {
                val out = FileOutputStream(f, true)
                out.write(data.toByteArray())
                out.flush()
                out.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
    private fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return if (Environment.MEDIA_MOUNTED == state) {
            true
        } else false
    }

    private fun deleteLogsFile(fileName:String = "axa-dil-tex.txt") {
        val file: File = getLogFile(applicationContext!!, fileName)!!
        if (file.exists()) {
            file.delete()
        }
    }

    private fun getLogFile(context: Context, fileName: String = "axa-dil-tex.txt"): File? {
        var logDirectory = if (isExternalStorageWritable()) {
            context.getExternalFilesDir(null)
        } else {
            context.filesDir
        }
        return File(logDirectory!!.absolutePath + "/AUTOMODE", fileName)
    }

    private fun updateLogsText() {
        val logFile: File? = this.getLogFile(applicationContext)
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
        try {
            val rootPath = applicationContext!!.getExternalFilesDir("AUTOMODE")!!
            val root = File(rootPath.toURI())
            if (!root.exists()) {
                root.mkdirs()
            }
            val f = File(rootPath.path + "/trips.txt")
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