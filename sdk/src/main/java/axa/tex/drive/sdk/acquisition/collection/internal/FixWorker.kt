package axa.tex.drive.sdk.acquisition.collection.internal

import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import axa.tex.drive.sdk.acquisition.db.PendingTripDao
import axa.tex.drive.sdk.core.Platform
import axa.tex.drive.sdk.core.internal.util.PlatformToHostConverter
import axa.tex.drive.sdk.core.internal.utils.Utils
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL



internal class FixWorker() : Worker() {

    companion object {
        private val FIX_SENDER_TAG: String = "COLLECTOR_" + (FixWorker::class.java.simpleName).toUpperCase();
    }

    override fun doWork(): WorkerResult {

        val inputData : Data =  inputData

        sendFixes(inputData);

        return WorkerResult.SUCCESS
    }


    private fun sendFixes(inputData : Data) {

        val data = inputData.keyValueMap
        Log.i("COLLECTOR_WORKER SIZE :", inputData.keyValueMap.size.toString())
        for((id , value) in data) {
            sendData(id,value as String)
            Log.i(FIX_SENDER_TAG, value as String)
        }
    }


    @Throws(IOException::class)
    private fun sendData(id: String, data: String) {
        val platformToHostConverter = PlatformToHostConverter(Platform.PREPROD);
        val url = URL(platformToHostConverter.getHost()+"/data")

        val uid = Utils.getUid(applicationContext);
        val authToken = "aaa"
        var appName = "APP-TEST"



        val urlConnection: HttpURLConnection
        urlConnection = url.openConnection() as HttpURLConnection
        urlConnection.doOutput = true
        urlConnection.requestMethod = "PUT"
        urlConnection.setRequestProperty("Content-Type", "application/json")
        urlConnection.setRequestProperty("Content-Encoding", "gzip")
        if (uid != null) {
            urlConnection.addRequestProperty("X-UserId", uid)
            if (authToken != null) {
                urlConnection.addRequestProperty("X-AuthToken", authToken)
            }
        }
        urlConnection.addRequestProperty("X-AppKey", appName)
        urlConnection.connect()
        urlConnection.outputStream.write(Utils.compress(data))
        urlConnection.outputStream.close()
        Log.i("TEST UPLOAD", "Response code = "+urlConnection.responseCode)
        if (urlConnection.responseCode != HttpURLConnection.HTTP_NO_CONTENT) {
            when (urlConnection.responseCode) {
                HttpURLConnection.HTTP_BAD_REQUEST, HttpURLConnection.HTTP_INTERNAL_ERROR -> {
                    //mLogger.warn("Error: {}", urlConnection.responseCode)
                    throw IOException()
                }
                else -> {
                    throw IOException()
                }
            }
        } else {
           val pendingTrips = PendingTripDao.deletePendingTrip(applicationContext,id)
        }
    }

}