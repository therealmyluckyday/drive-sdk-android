package axa.tex.drive.sdk.acquisition.collection.internal.db.queue


import androidx.work.Data
import androidx.work.Worker
import axa.tex.drive.sdk.acquisition.TripRecorder
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


internal class FixWorkerWithQueue() : Worker(), KoinComponentCallbacks {
    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
    val persistentQueue: PersistentQueue by inject()
    val tripRecoder: TripRecorder by inject()

    companion object {
        private val FIX_SENDER_TAG: String = "COLLECTOR_" + (FixWorkerWithQueue::class.java.simpleName).toUpperCase();
    }


    override fun doWork(): WorkerResult {

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
        val appName = inputData.getString(Constants.APP_NAME_KEY, "")
        val clientId = inputData.getString(Constants.CLIENT_ID_KEY, "")
        val id = inputData.getString(Constants.ID_KEY, "")
        val tripId = inputData.getString("tripId", "")
        var packet = persistentQueue.next(tripId)

        LOGGER.info("COLLECTOR_WORKER SIZE :", inputData.keyValueMap.size.toString())
        if (packet != null) {
            var stop = false
            while (!stop) {
                if (packet != null) {
                    val res = sendData(id, tripId, packet?.data!!, appName, clientId)
                    if (res) {
                        persistentQueue.delete(packet)
                        val rest = persistentQueue.numberOfPendingPacket(tripId)
                    }
                    if (packet != null) {
                        stop = packet.end
                    }
                }

                packet = persistentQueue.next(tripId)
            }
            try {
                persistentQueue.deleteTrip(tripId)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return true
        }
        return false
    }


    @Throws(IOException::class)
    private fun sendData(id: String, tripId: String, data: String, appName: String, clientId: String): Boolean {
        try {
            val collectorDb: CollectionDb by inject()
            val scoreRetriever: ScoreRetriever by inject()
            val platformToHostConverter = PlatformToHostConverter(Platform.PREPROD);
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
            try {
                urlConnection.connect()
            } catch (e: Exception) {
                return false
            } catch (t: Throwable) {
                return false
            }

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