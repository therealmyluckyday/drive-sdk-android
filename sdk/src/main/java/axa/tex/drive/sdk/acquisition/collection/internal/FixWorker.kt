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
        val data = inputData.getString(Constants.DATA_KEY, "")
        LOGGER.info("COLLECTOR_WORKER SIZE :$inputData.keyValueMap.size.toString()", "private fun sendFixes(inputData : Data) : Boolean")
        return sendData(data, appName)
    }


    @Throws(IOException::class)
    private fun sendData(data: String, appName: String): WorkerResult {
        val funcName = "enableTracking"
        try {
            val collectorDb: CollectionDb by inject()
            val config = collectorDb.getConfig()
            var platform: Platform = Platform.PRODUCTION
            if (config != null) {
                platform = config.endPoint!!
            }



            val platformToHostConverter = PlatformToHostConverter(platform);
            val url = URL(platformToHostConverter.getHost() + "/data")

            LOGGER.info("SENDING DATA URL = ${url.toURI()}, DATA = ${data}", funcName)


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
            urlConnection.outputStream.write(data.compress())
            urlConnection.outputStream.close()
            if (urlConnection.responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
                LOGGER.error("SENDING : error CODE = ${urlConnection.responseCode}", funcName)
                return when (urlConnection.responseCode) {
                    HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                        WorkerResult.RETRY
                    }
                    else -> {
                        WorkerResult.FAILURE
                    }
                }
            } else {
                LOGGER.info("SENDING worker: success ", funcName)
                return WorkerResult.SUCCESS
            }
        } catch (e: Exception) {
            LOGGER.error("SENDING : error = ${e.printStackTrace()}", funcName)
            return WorkerResult.RETRY
        }
    }
}