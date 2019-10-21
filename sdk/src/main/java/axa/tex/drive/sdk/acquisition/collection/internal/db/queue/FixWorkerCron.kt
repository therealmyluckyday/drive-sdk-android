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


internal class FixWorkerCron() : Worker(), KoinComponentCallbacks {
    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
    var fixUploadWork: OneTimeWorkRequest? = null
    private var done = false
    private lateinit var collectorDb: CollectionDb
    private lateinit var persistentQueue: PersistentQueue

    override fun doWork(): WorkerResult {
        val collectorDb: CollectionDb by inject()
        this.collectorDb = collectorDb
        persistentQueue = PersistentQueue(applicationContext)
        val inputData: Data = inputData

        return sendFixes(inputData)
    }


    private fun sendFixes(inputData: Data): WorkerResult {

        LOGGER.info("Sending data to the server", "private fun sendFixes(inputData : Data) : Boolean")
        val appName = inputData.getString(Constants.APP_NAME_KEY, "")
        val clientId = inputData.getString(Constants.CLIENT_ID_KEY, "")
        val id = inputData.getString(Constants.ID_KEY, "")
        val data = inputData.getString(Constants.DATA_KEY, "")
        val tripId = inputData.getString("tripId", "")


        var packetNumber = collectorDb.getPacketNumber(tripId)

        val packet = persistentQueue.pop(tripId, appName, clientId, packetNumber.toString())

        var response = WorkerResult.FAILURE
//        if (packet != null) {
//            done = packet.end
//            if (sendData(id, packet.data!!, appName, clientId)) { //
//                persistentQueue.delete(packet) //
//                if (!packet.end) {
//                    response = WorkerResult.SUCCESS //
//                    collectorDb.setPacketNumber(tripId, packetNumber + 1)
//                    packetNumber = collectorDb.getPacketNumber(tripId)
//                    packet = persistentQueue.pop(tripId, appName, clientId, packetNumber.toString())
//
//                    if (packet != null) {
//                        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
//                                .build()
//                        val fixUploadWork = OneTimeWorkRequest.Builder(FixWorkerCron::class.java).setInputData(inputData).setConstraints(constraints)
//                                .build()
//                        WorkManager.getInstance().enqueue(fixUploadWork)
//                    }
//                } else {
//                    response = WorkerResult.RETRY
//                    val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
//                            .build()
//                    val fixUploadWork = OneTimeWorkRequest.Builder(FixWorkerCron::class.java).setInputData(inputData).setConstraints(constraints)
//                            .build()
//                    WorkManager.getInstance().enqueue(fixUploadWork)
//                }
//
//            }
//        } else if (!done) {
//            response = WorkerResult.FAILURE
//            Thread.sleep(3000)
//            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
//                    .build()
//            val fixUploadWork = OneTimeWorkRequest.Builder(FixWorkerCron::class.java).setInputData(inputData).setConstraints(constraints)
//                    .build()
//            WorkManager.getInstance().enqueue(fixUploadWork)
//        }


        return response
    }


    @Throws(IOException::class)
    private fun sendData(id: String, data: String, appName: String, clientId: String): Boolean {

        try {
            val config = collectorDb.getConfig()
            var platform: Platform = Platform.PREPROD
            if (config != null) {
                platform = config.endPoint!!
            }


            val scoreRetriever: ScoreRetriever by inject()

            val platformToHostConverter = PlatformToHostConverter(platform);
            val url = URL(platformToHostConverter.getHost() + "/data")

            val uid = DeviceInfo.getUid(applicationContext);


            val urlConnection: HttpURLConnection
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.doOutput = true
            urlConnection.requestMethod = "PUT"
            urlConnection.setRequestProperty("Content-Type", "application/json")
            urlConnection.setRequestProperty("Content-Encoding", "gzip")
            if (uid != null) {
                urlConnection.addRequestProperty("X-UserId", uid)
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
                if (trip != null) {
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