package axa.tex.drive.sdk.acquisition.collection.internal


import androidx.work.Data
import androidx.work.State
import androidx.work.WorkManager
import androidx.work.Worker
import axa.tex.drive.sdk.acquisition.collection.internal.db.CollectionDb
import axa.tex.drive.sdk.acquisition.score.ScoreRetriever
import axa.tex.drive.sdk.automode.AutomodeHandler
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
import java.util.concurrent.ExecutionException


internal class FixWorker() : Worker(), KoinComponentCallbacks{
    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
    private val automodeHandler: AutomodeHandler by inject()


    companion object {
        private val FIX_SENDER_TAG: String = "COLLECTOR_" + (FixWorker::class.java.simpleName).toUpperCase();
    }


    override fun doWork(): WorkerResult {

        val inputData: Data = inputData
        val tag = inputData.getInt(Constants.WORK_TAG_KEY, -1)
       /*if(isRunning(tag.toString())){
           LOGGER.info("Waiting for precedent packet to be sent...", "override fun doWork(): WorkerResult")
          return  WorkerResult.RETRY
       }*/

        val result = sendFixes(inputData)

        return if (result) {
            LOGGER.info("Data sent successfully", "override fun doWork(): WorkerResult")
            WorkerResult.SUCCESS
        } else {
            LOGGER.info("Data were not successfully sent :  Retrying...", "override fun doWork(): WorkerResult")
            WorkerResult.RETRY
        }
    }


    private fun isRunning(tag: String): Boolean {

        try {
            val status = WorkManager.getInstance().getStatusesByTag(tag).value
            if (status != null) {
                for (workStatus in status) {
                    if (workStatus.state == State.RUNNING || workStatus.state == State.ENQUEUED) {
                        return true
                    }
                }
            }
            return false

        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

        return false
    }


    private fun sendFixes(inputData: Data): Boolean {
        LOGGER.info("Sending data to the server", "private fun sendFixes(inputData : Data) : Boolean")
       // val data = inputData.keyValueMap
        val appName = inputData.getString(Constants.APP_NAME_KEY, "")
        val clientId = inputData.getString(Constants.CLIENT_ID_KEY, "")
        val id = inputData.getString(Constants.ID_KEY, "")
        val data = inputData.getString(Constants.DATA_KEY, "")
        LOGGER.info("COLLECTOR_WORKER SIZE :", inputData.keyValueMap.size.toString())
        /*for ((id, value) in data) {
            LOGGER.info(FIX_SENDER_TAG, value as String)
            return sendData(id, value as String, appName, clientId)
        }*/
        return sendData(id, data, appName, clientId)
    }


    @Throws(IOException::class)
    private fun sendData(id: String, data: String, appName: String, clientId: String): Boolean {

        try {
            val collectorDb: CollectionDb by inject()
           val config = collectorDb.getConfig()
            var platform : Platform = Platform.PRODUCTION
            if(config != null){
                platform = config.endPoint!!
            }


            val scoreRetriever: ScoreRetriever by inject()

            val platformToHostConverter = PlatformToHostConverter(platform);
            val url = URL(platformToHostConverter.getHost() + "/data")

            LOGGER.info("SENDING DATA URL = ${url.toURI()}", "Fixworker doWork()")

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
            LOGGER.info("SENDING : SENDING DATA", "Fixworker doWork()")
            urlConnection.outputStream.write(data.compress())
            urlConnection.outputStream.close()
            LOGGER.info("UPLOADING DATA/ RESPONSE CODE", "FixWorker" + urlConnection.responseCode)
            if (urlConnection.responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
                LOGGER.info("SENDING : FAILED CODE = ${urlConnection.responseCode}")
                return when (urlConnection.responseCode) {
                    HttpURLConnection.HTTP_BAD_REQUEST -> {
                        LOGGER.info("UPLOADING DATA ERROR/ RESPONSE CODE", "FixWorker" + urlConnection.responseCode)
                        // throw IOException()
                        true
                    }
                    HttpURLConnection.HTTP_INTERNAL_ERROR ->{
                        LOGGER.info("UPLOADING DATA ERROR/ RESPONSE CODE", "FixWorker" + urlConnection.responseCode)
                        false
                    }
                    else -> {
                        //throw IOException()
                        true
                    }
                }
            } else {
                LOGGER.info("SENDING : SUCCEEDED CODE = ${urlConnection.responseCode}")
                val trip = collectorDb.getPendingTrip(id)

                if(trip != null) {
                    automodeHandler.messages.onNext(Message("${Date()} Packet sent successfully and trip id = ${trip.tripId}"))
                    collectorDb.deletePendingTrip(id)
                    if (trip.containsStop) {

                        automodeHandler.messages.onNext(Message("Packet sent successfully and trip id = ${trip.tripId} stop = true"))
                        val collector: Collector by inject()
                        collector.currentTripId = null
                        val tId = trip.tripId
                        if(tId != null) {
                           scoreRetriever.getAvailableScoreListener().onNext(tId)
                            collectorDb.deleteTripNumberPackets(tId)
                          //  trip.tripId?.let { collectorDb.deleteTripNumberPackets(it) }
                        }
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