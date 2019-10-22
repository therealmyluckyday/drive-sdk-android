package axa.tex.drive.sdk.acquisition.collection.internal


import androidx.work.Data
import androidx.work.Worker
import axa.tex.drive.sdk.acquisition.collection.internal.db.CollectionDb
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


internal open class FixWorker() : Worker(), KoinComponentCallbacks {
    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger

    override fun doWork(): WorkerResult {
        val inputData: Data = inputData
        return sendFixes(inputData)
    }

    fun sendFixes(inputData: Data): WorkerResult {
        LOGGER.info("Sending data to the server", "private fun sendFixes(inputData : Data) : Boolean")
        val appName = inputData.getString(Constants.APP_NAME_KEY, "")
        val clientId = inputData.getString(Constants.CLIENT_ID_KEY, "")
        val id = inputData.getString(Constants.ID_KEY, "")
        val data = inputData.getString(Constants.DATA_KEY, "")
        LOGGER.info("COLLECTOR_WORKER SIZE :$inputData.keyValueMap.size.toString()", "private fun sendFixes(inputData : Data) : Boolean")
        return sendData(id, data, appName, clientId)
    }


    @Throws(IOException::class)
    private fun sendData(id: String, data: String, appName: String, clientId: String): WorkerResult {

        try {
            val collectorDb: CollectionDb by inject()
            val config = collectorDb.getConfig()
            var platform: Platform = Platform.PRODUCTION
            if (config != null) {
                platform = config.endPoint!!
            }



            val platformToHostConverter = PlatformToHostConverter(platform);
            val url = URL(platformToHostConverter.getHost() + "/data")

            LOGGER.info("SENDING DATA URL = ${url.toURI()}", "fun sendData(id: String, data: String, appName: String, clientId: String): Boolean")
            LOGGER.info("SENDING DATA = ${data}", "fun sendData(id: String, data: String, appName: String, clientId: String): Boolean")

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
            LOGGER.info("SENDING : SENDING DATA", "Fixworkfun sendData(id: String, data: String, appName: String, clientId: String): Booleaner doWork()")
            urlConnection.outputStream.write(data.compress())
            urlConnection.outputStream.close()
            LOGGER.info("UPLOADING DATA/ RESPONSE CODE $urlConnection.responseCode", "fun sendData(id: String, data: String, appName: String, clientId: String): Boolean")
            if (urlConnection.responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
                LOGGER.error("SENDING : FAILED CODE = ${urlConnection.responseCode}", "fun sendData(id: String, data: String, appName: String, clientId: String): Boolean")
                return when (urlConnection.responseCode) {
                    HttpURLConnection.HTTP_BAD_REQUEST -> {
                        LOGGER.error("UPLOADING DATA ERROR/ RESPONSE CODE $urlConnection.responseCode", "fun sendData(id: String, data: String, appName: String, clientId: String): Boolean")
                        // throw IOException()
                        WorkerResult.FAILURE
                    }
                    HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                        LOGGER.error("UPLOADING DATA ERROR/ RESPONSE CODE $urlConnection.responseCode", "fun sendData(id: String, data: String, appName: String, clientId: String): Boolean")

                        WorkerResult.RETRY
                    }
                    else -> {
                        LOGGER.error("Exception", "fun sendData(id: String, data: String, appName: String, clientId: String): Boolean")
                        WorkerResult.FAILURE
                    }
                }
            } else {
                LOGGER.info("SENDING : SUCCEEDED CODE = ${urlConnection.responseCode}", "fun sendData(id: String, data: String, appName: String, clientId: String): Boolean")

                return WorkerResult.SUCCESS
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return WorkerResult.RETRY
        }
    }
}