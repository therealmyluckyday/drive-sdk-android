package axa.tex.drive.sdk.acquisition.collection.internal


import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import axa.tex.drive.sdk.core.CertificateAuthority
import axa.tex.drive.sdk.core.Platform
import axa.tex.drive.sdk.core.internal.Constants
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.logger.LoggerFactory
import axa.tex.drive.sdk.internal.compress
import org.koin.android.ext.android.inject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

internal open class FixWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams), KoinComponentCallbacks {

    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger




    override fun doWork(): Result {
        val inputData: Data = inputData
        return sendFixes(inputData)
    }

    fun sendFixes(inputData: Data): Result {
        LOGGER.info("Sending data to the server", "private fun sendFixes(inputData : Data) : Boolean")
        val appName = inputData.getString(Constants.APP_NAME_KEY) ?: "APP_TEST"
        val serverUrl = inputData.getString(Constants.PLATFORM_URL) ?: "https://gw-preprod.tex.dil.services/v2.0"
        val data = inputData.getString(Constants.DATA_KEY) ?: ""
        LOGGER.info("TRIPCHUNK SIZE :$inputData.keyValueMap.size", "private fun sendFixes(inputData : Data) : Boolean")
        val uid = inputData.getString((Constants.UID_KEY)) ?: ""
        return sendData(data, appName, serverUrl, uid)
    }


    @Throws(IOException::class)
    private fun sendData(data: String, appName: String, serverUrl: String, uid: String): Result {
        val funcName = "enableTracking"
        try {

            val url = URL(serverUrl + "/data")
            LOGGER.info("SENDING DATA URL = ${url.toURI()}, DATA = $data", funcName)
            val urlConnection: HttpURLConnection
            val certificate: CertificateAuthority by inject()
            urlConnection = url.openConnection() as HttpsURLConnection
            certificate.configureSSLSocketFactory(urlConnection)
            urlConnection.doOutput = true
            urlConnection.requestMethod = "PUT"
            urlConnection.setRequestProperty("Content-Type", "application/json")
            urlConnection.setRequestProperty("Content-Encoding", "gzip")
            urlConnection.addRequestProperty("X-UserId", uid)
            urlConnection.addRequestProperty("X-AppKey", appName)
            urlConnection.connect()
            urlConnection.outputStream.write(data.compress())
            urlConnection.outputStream.close()
            if (urlConnection.responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
                LOGGER.error("SENDING : error CODE = ${urlConnection.responseCode}", funcName)
                return when (urlConnection.responseCode) {
                    HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                        LOGGER.error("SENDING : error RETRY = ${urlConnection.responseCode}", funcName)
                        Result.retry()
                    }
                    else -> {
                        LOGGER.error("SENDING : error FAILURE = ${urlConnection.responseCode}", funcName)
                        Result.failure()
                    }
                }
            } else {
                LOGGER.info("SENDING worker: success ", funcName)
                return Result.success()
            }
        } catch (e: Exception) {
            LOGGER.error("SENDING : error = ${e.printStackTrace()}", funcName)
            return Result.retry()
        }
    }
}