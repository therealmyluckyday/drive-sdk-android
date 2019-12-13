package axa.tex.drive.sdk.acquisition.score


import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import axa.tex.drive.sdk.acquisition.score.model.ScoreError
import axa.tex.drive.sdk.acquisition.score.model.ScoreResult
import axa.tex.drive.sdk.acquisition.score.model.ScoresDil
import axa.tex.drive.sdk.core.Platform
import axa.tex.drive.sdk.core.internal.Constants
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.internal.utils.PlatformToHostConverter
import axa.tex.drive.sdk.core.logger.LoggerFactory
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.koin.android.ext.android.inject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


private const val TIME_TO_WAIT = 5000
private const val MAX_ATTEMPT = 5

internal class ScoreWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams), KoinComponentCallbacks {


    private var nbAttempt = 0
    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger

    override fun doWork(): Result {
        val appName = inputData.getString(Constants.APP_NAME_KEY) ?: "APP_TEST"
        val platform : Platform
        when (inputData.getString(Constants.PLATFORM_KEY)) {
            Platform.PRODUCTION.endPoint -> platform = Platform.PRODUCTION
            Platform.TESTING.endPoint -> platform = Platform.TESTING
            Platform.PREPROD.endPoint -> platform = Platform.PREPROD
            else -> platform = Platform.PRODUCTION
        }

        val tripId = inputData.getString(Constants.TRIP_ID_KEY)
        val finalScore = inputData.getBoolean(Constants.FINAL_SCORE_BOOLEAN_KEY, true)

        if (tripId != null) {
            scoreRequest(tripId, finalScore, platform, appName)
        }

        return Result.success()
    }

    private fun getLocaleId(locale: Locale?): String {
        var theLocale = locale
        if (locale == null) {
            theLocale = Locale.getDefault()
        }
        var result = theLocale?.language
        val region = theLocale?.country
        if (region != null && !region.isEmpty()) {
            result += "_" + region
        }
        return result!!
    }

    @Throws(Exception::class)
    private fun scoreRequest(tripId: String, finalScore: Boolean, platform: Platform, appName: String) {
        try {
            Thread.sleep(5)
        }catch (e : Exception){
            e.printStackTrace()
        }
        val scoreRetriever: ScoreRetriever by inject()
        val responseString = StringBuffer("")
        val locale = null
        val serverUrl = PlatformToHostConverter(platform).getHost()
        val url: URL
        val theLocal = getLocaleId(locale)
        if (!finalScore) {
            url = URL("$serverUrl/score?trip_id=$tripId&lang=$theLocal")
        } else {
            url = URL("$serverUrl/score?trip_id=$tripId&lang=$theLocal&final=true")
        }
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        connection.addRequestProperty("X-AppKey", appName)
        connection.connect()
        val inputStream = connection.inputStream
        val rd = BufferedReader(InputStreamReader(inputStream))
        var line = rd.readLine()
        while (line != null) {
            responseString.append(line)
            line = rd.readLine()
        }
        val mapper = ObjectMapper().registerKotlinModule()
        mapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        val node = mapper.readTree(responseString.toString())

        try {
            val score = mapper.readValue(node.get("scores_dil").toString(), ScoresDil::class.java)
            scoreRetriever.getScoreListener().onNext(ScoreResult(score))
        } catch (e: Exception) {
            LOGGER.error(" Exception ${e}", "scoreRequest")
            LOGGER.error("RESPONSE CODES ${connection.responseCode}", "scoreRequest")
            if(connection.responseCode.toString().startsWith("2")) {
                val scoreError = mapper.readValue(responseString.toString(), ScoreError::class.java)
                scoreRetriever.getScoreListener().onNext(ScoreResult(scoreError = scoreError, response = responseString.toString()))
            }else if(connection.responseCode.toString().startsWith("5")){
                 retry(scoreRetriever,tripId, finalScore, platform, appName, responseString.toString())
            }

        } catch (err: Error) {
            if(connection.responseCode.toString().startsWith("5")){
                 retry(scoreRetriever,tripId, finalScore, platform, appName, responseString.toString())
            }
        }

        nbAttempt = 0
    }



    private fun retry(scoreRetriever: ScoreRetriever,tripId: String, finalScore: Boolean, platform: Platform, appName: String, recievedPayload: String) {
        if (nbAttempt < MAX_ATTEMPT) {
            nbAttempt++
            Thread.sleep(TIME_TO_WAIT.toLong())
            scoreRequest(tripId, finalScore, platform, appName)

        } else {
            nbAttempt = 0
            scoreRetriever.getScoreListener().onNext(ScoreResult(response = recievedPayload))
        }
    }
}