package axa.tex.drive.sdk.acquisition.score.internal


import android.content.res.Configuration
import androidx.work.Data
import androidx.work.Worker
import axa.tex.drive.sdk.acquisition.collection.internal.db.CollectionDb
import axa.tex.drive.sdk.acquisition.score.ScoreRetriever
import axa.tex.drive.sdk.acquisition.score.model.ScoreError
import axa.tex.drive.sdk.acquisition.score.model.ScoreResult
import axa.tex.drive.sdk.acquisition.score.model.ScoresDil
import axa.tex.drive.sdk.core.Platform
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.internal.util.PlatformToHostConverter
import axa.tex.drive.sdk.core.logger.LoggerFactory
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import org.koin.android.ext.android.inject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


private const val TIME_TO_WAIT = 5000;
private const val MAX_ATTEMPT = 5;

internal class ScoreWorker() : Worker(), KoinComponentCallbacks {


    private var nbAttempt = 0
    private val LOGGER = LoggerFactory.getLogger(this::class.java.name).logger

    override fun doWork(): WorkerResult {

        val inputData: Data = inputData
        for ((tripId, state) in inputData.keyValueMap) {
            scoreRequest(tripId, state as Boolean)
        }


        return WorkerResult.SUCCESS

    }

    private fun getLocaleId(locale: Locale?): String {
        var theLocale = locale
        if (locale == null) {
            theLocale = Locale.getDefault()
        }
        var result = theLocale?.language
        val region = theLocale?.country
        if (region != null && !region!!.isEmpty()) {
            result += "_" + region!!
        }
        return result!!
    }

    @Throws(Exception::class)
    private fun scoreRequest(tripId: String, finalScore: Boolean) {
        //val config  = CollectionDb.getConfig(applicationContext)
        val collectorDb: CollectionDb by inject()
        val scoreRetriever: ScoreRetriever by inject()
        val config = collectorDb.getConfig()
        val responseString = StringBuffer("")
        val locale = null
        val serverUrl = PlatformToHostConverter(Platform.PREPROD).getHost()
        val url: URL
        val theLocal = getLocaleId(locale)
        if (!finalScore) {
            url = URL("$serverUrl/score?trip_id=$tripId&lang=$theLocal")
        } else {
            url = URL("$serverUrl/score?trip_id=$tripId&lang=$theLocal&final=true")
        }
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        /* if (!mTexUser.isAnonymous()) {
             connection.addRequestProperty("X-UserId", getUserID())
             if (mTexUser.getAuthToken() != null) {
                 connection.addRequestProperty("X-AuthToken", getAuthToken())
             }
         }*/
        connection.addRequestProperty("X-AppKey", config?.appName)
        connection.connect()
        //mLogger.debug("Scoring server response code: " + connection.responseCode)
        val inputStream = connection.inputStream
        val rd = BufferedReader(InputStreamReader(inputStream))
        var line = rd.readLine()
        while (line != null) {
            responseString.append(line)
            line = rd.readLine()
        }

        val mapper = ObjectMapper()
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        val node = mapper.readTree(responseString.toString());

        try {
            val score = mapper.readValue(node.get("scores_dil").toString(), ScoresDil::class.java)
            scoreRetriever.getScoreListener().onNext(ScoreResult(score))
        } catch (e: Exception) {
            LOGGER.info("RESPONSE CODES ${connection.responseCode}", "scoreRequest")
            if(connection.responseCode.toString().startsWith("2")) {
                val scoreError = mapper.readValue(responseString.toString(), ScoreError::class.java)
                scoreRetriever.getScoreListener().onNext(ScoreResult(scoreError = scoreError, response = responseString.toString()))
            }else if(connection.responseCode.toString().startsWith("5")){
                 retry(scoreRetriever,tripId, finalScore, responseString.toString())
            }

        } catch (err: Error) {
            if(connection.responseCode.toString().startsWith("5")){
                 retry(scoreRetriever,tripId, finalScore, responseString.toString())
            }
        }

        nbAttempt = 0
         responseString.toString()
    }



    private fun retry(scoreRetriever: ScoreRetriever,tripId: String, finalScore: Boolean, recievedPayload: String) {
        if (nbAttempt < MAX_ATTEMPT) {
            nbAttempt++
            Thread.sleep(TIME_TO_WAIT.toLong())
             scoreRequest(tripId, finalScore)

        } else {
            nbAttempt = 0
            scoreRetriever.getScoreListener().onNext(ScoreResult(response = recievedPayload))
            // return recievedPayload
        }
    }
}