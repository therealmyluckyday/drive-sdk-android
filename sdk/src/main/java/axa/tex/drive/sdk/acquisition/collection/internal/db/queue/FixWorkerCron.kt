package axa.tex.drive.sdk.acquisition.collection.internal.db.queue


import androidx.work.*
import axa.tex.drive.sdk.acquisition.collection.internal.Collector
import axa.tex.drive.sdk.acquisition.collection.internal.db.CollectionDb
import axa.tex.drive.sdk.acquisition.score.ScoreRetriever
import axa.tex.drive.sdk.core.Platform
import axa.tex.drive.sdk.core.internal.Constants
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.internal.util.PlatformToHostConverter
import axa.tex.drive.sdk.core.internal.utils.DeviceInfo
import axa.tex.drive.sdk.core.logger.LoggerFactory
import axa.tex.drive.sdk.internal.extension.compress
import org.koin.android.ext.android.inject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


internal class FixWorkerCron() : Worker(), KoinComponentCallbacks{
    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
    var fixUploadWork: OneTimeWorkRequest? = null
    private var done = false
   private  lateinit var collectorDb: CollectionDb
   private  lateinit  var persistentQueue : PersistentQueue

    override fun doWork(): WorkerResult {
    val collectorDb :  CollectionDb by inject()
        this.collectorDb = collectorDb
    persistentQueue = PersistentQueue(applicationContext)
        val inputData: Data = inputData

        val result = sendFixes(inputData)

        return if (result) {
            LOGGER.info("Data sent successfully", "override fun doWork(): WorkerResult")
            WorkerResult.SUCCESS
        } else {
            LOGGER.info("Data were not successfully sent :  Retrying...", "override fun doWork(): WorkerResult")
            WorkerResult.RETRY
        }
    }


    private fun sendFixes(inputData: Data): Boolean {

        LOGGER.info("Sending data to the server", "private fun sendFixes(inputData : Data) : Boolean")
       // val data = inputData.keyValueMap
        val appName = inputData.getString(Constants.APP_NAME_KEY, "")
        val clientId = inputData.getString(Constants.CLIENT_ID_KEY, "")
        val id = inputData.getString(Constants.ID_KEY, "")
        val data = inputData.getString(Constants.DATA_KEY, "")
        val tripId = inputData.getString("tripId", "")


        var packetNumber = collectorDb.getPacketNumber(tripId)

        var packet = persistentQueue.pop(tripId,appName, clientId,packetNumber.toString(), false)
        if(packet == null){
            packet = persistentQueue.pop(tripId,appName, clientId,packetNumber.toString(), true)
        }
        var res = false
        if(packet != null){
            done = packet.end
            res = sendData(id, packet.data!!, appName, clientId)
            if(res){
                persistentQueue.delete(packet)
                if(!done){
                collectorDb.setPacketNumber(tripId, packetNumber+1)
                packetNumber = collectorDb.getPacketNumber(tripId)
                packet = persistentQueue.pop(tripId,appName, clientId,packetNumber.toString(), false)
                if(packet == null){
                    packet = persistentQueue.pop(tripId,appName, clientId,packetNumber.toString(), true)
                }

                if(packet != null) {
                    val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    val fixUploadWork = OneTimeWorkRequest.Builder(FixWorkerCron::class.java).setInputData(inputData).setConstraints(constraints)
                            .build()
                    WorkManager.getInstance().enqueue(fixUploadWork)
                }
                }else if(!done){
                    Thread.sleep(3000)
                    val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    val fixUploadWork = OneTimeWorkRequest.Builder(FixWorkerCron::class.java).setInputData(inputData).setConstraints(constraints)
                            .build()
                    WorkManager.getInstance().enqueue(fixUploadWork)
                }

            }
        }else if(!done){
            Thread.sleep(3000)
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            val fixUploadWork = OneTimeWorkRequest.Builder(FixWorkerCron::class.java).setInputData(inputData).setConstraints(constraints)
                    .build()
            WorkManager.getInstance().enqueue(fixUploadWork)
        }


        return res
    }


    @Throws(IOException::class)
    private fun sendData(id: String, data: String, appName: String, clientId: String): Boolean {

        try {
           // val collectorDb: CollectionDb by inject()
           val config = collectorDb.getConfig()
            var platform : Platform = Platform.PREPROD
            if(config != null){
                platform = config.endPoint!!
            }


            val scoreRetriever: ScoreRetriever by inject()

            val platformToHostConverter = PlatformToHostConverter(platform);
            val url = URL(platformToHostConverter.getHost() + "/data")

            val uid = DeviceInfo.getUid(applicationContext);
            //val appName = config?.appName


            val urlConnection: HttpURLConnection
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.doOutput = true
            urlConnection.requestMethod = "PUT"
            urlConnection.setRequestProperty("Content-Type", "application/json")
            urlConnection.setRequestProperty("Content-Encoding", "gzip")
            if (uid != null) {
                urlConnection.addRequestProperty("X-UserId", uid)
                /*if (authToken != null) {
                    urlConnection.addRequestProperty("X-AuthToken", authToken)
                }*/
            }
            urlConnection.addRequestProperty("X-AppKey", appName)
            urlConnection.connect()
            LOGGER.info("SENDING : SENDING DATA")
            urlConnection.outputStream.write(data.compress())
            urlConnection.outputStream.close()
            LOGGER.info("UPLOADING DATA/ RESPONSE CODE", "FixWorker" + urlConnection.responseCode)
            if (urlConnection.responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
                LOGGER.info("SENDING : FAILED CODE = ${urlConnection.responseCode}")
                when (urlConnection.responseCode) {
                    HttpURLConnection.HTTP_BAD_REQUEST, HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                        LOGGER.info("UPLOADING DATA ERROR/ RESPONSE CODE", "FixWorker" + urlConnection.responseCode)
                        throw IOException()
                    }
                    else -> {
                        throw IOException()
                    }
                }
                return false
            } else {
                LOGGER.info("SENDING : SUCCEEDED CODE = ${urlConnection.responseCode}")
                val trip = collectorDb.getPendingTrip(id)
                if(trip != null) {
                    collectorDb.deletePendingTrip(id)
                    if (trip.containsStop) {
                        val collector: Collector by inject()
                        collector.currentTripId = null
                        trip.tripId?.let { scoreRetriever.getAvailableScoreListener().onNext(it) }
                    }
                }
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}