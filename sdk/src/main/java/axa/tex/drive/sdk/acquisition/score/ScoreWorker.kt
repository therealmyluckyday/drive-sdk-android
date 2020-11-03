package axa.tex.drive.sdk.acquisition.score


import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import axa.tex.drive.sdk.acquisition.score.model.ScoreError
import axa.tex.drive.sdk.acquisition.score.model.ScoreResult
import axa.tex.drive.sdk.acquisition.score.model.ScoreStatus
import axa.tex.drive.sdk.core.CertificateAuthority
import axa.tex.drive.sdk.core.internal.Constants
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.logger.LoggerFactory
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.koin.android.ext.android.inject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection


internal class ScoreWorker(appContext: Context, workerParams: WorkerParameters)
    : Worker(appContext, workerParams), KoinComponentCallbacks {

    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger

    override fun doWork(): Result {
        val appName = inputData.getString(Constants.APP_NAME_KEY) ?: "APP_TEST"
        val serverUrl = inputData.getString(Constants.PLATFORM_URL) ?: "https://gw-preprod.tex.dil.services/v2.0"
        val tripId = inputData.getString(Constants.TRIP_ID_KEY)
        val finalScore = inputData.getBoolean(Constants.FINAL_SCORE_BOOLEAN_KEY, true)
        val isAPIV2 = inputData.getBoolean(Constants.PLATFORM_VERSION, false)
        if (tripId != null) {
            return scoreRequest(tripId, finalScore, serverUrl, appName, isAPIV2)
        }

        return Result.success()
    }

    private fun getLocaleId(): String {
        var theLocale = Locale.getDefault()
        var result = theLocale.language
        val region = theLocale.country
        if (region != null && !region.isEmpty()) {
            result += "_" + region
        }
        return result!!
    }

    @Throws(Exception::class)
    private fun scoreRequest(tripId: String, finalScore: Boolean, serverUrl: String, appName: String, isAPIV2: Boolean): Result {
        if (isAPIV2) {
            return scoreRequestV2(tripId, finalScore, serverUrl, appName)
        }

        return scoreRequestV1(tripId, finalScore, serverUrl, appName)
    }

    private fun scoreRequestV1(tripId: String, finalScore: Boolean, serverUrl: String, appName: String): Result {
        LOGGER.info("scoreRequest "+ tripId, "scoreRequestV1")
        val scoreRetriever: ScoreRetriever by inject()
        val responseString = StringBuffer("")
        val url: URL
        val connection: HttpsURLConnection
        val theLocal = getLocaleId()
        if (!finalScore) {
            url = URL("$serverUrl/score?trip_id=$tripId&lang=$theLocal")
        } else {
            url = URL("$serverUrl/score?trip_id=$tripId&lang=$theLocal&final=true")
        }
        val certificate: CertificateAuthority by inject()
        connection = url.openConnection() as HttpsURLConnection
        certificate.configureSSLSocketFactory(connection)
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
        if(connection.responseCode.toString().startsWith("5")){
            LOGGER.info("scoreRequest RETRY", "scoreRequestV1")
            return Result.retry()
        }
        val mapper = ObjectMapper().registerKotlinModule()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        val node = mapper.readTree(responseString.toString())

        try {
            LOGGER.info("try", "scoreRequestV1")
            //{"flags":[],"score_type":"final","scoregw_version":"2.2.6","status":"trip_too_short","status_details":["not_enough_locations"],"trip_id":"7F05D5C6-D56E-4455-9A11-096CDC94CD75"}
            val fullScore = mapper.readValue(node.toString(), ScoreV1::class.java)
            LOGGER.info("Score http result body "+responseString.toString(), "scoreRequest")
            LOGGER.info(""+fullScore.tripId?.value + " Score Status "+fullScore.status.name, "scoreRequest")
            if (fullScore.status == ScoreStatus.pending) {
                return Result.retry()
            }
            if (fullScore.scores_dil != null && fullScore.status == ScoreStatus.ok) {
                scoreRetriever.getScoreListener().onNext(ScoreResult(fullScore))
            } else {
                val scoreError = mapper.readValue(responseString.toString(), ScoreError::class.java)
                LOGGER.error("RESPONSE CODES ${connection.responseCode}"+" Error ${scoreError}", "scoreRequest")
                scoreRetriever.getScoreListener().onNext(ScoreResult(scoreError = scoreError))
            }
            return Result.success()
        } catch (e: Exception) {
            LOGGER.error("RESPONSE CODES ${connection.responseCode}"+" Exception ${e}", "scoreRequest")
            val scoreError = mapper.readValue(responseString.toString(), ScoreError::class.java)
            scoreRetriever.getScoreListener().onNext(ScoreResult(scoreError = scoreError))
            if (connection.responseCode == 202) {
                return Result.success()
            }
        } catch (err: Error) {
            LOGGER.error("RESPONSE CODES ${connection.responseCode}"+" Error ${err}", "scoreRequest")
            val scoreError = mapper.readValue(responseString.toString(), ScoreError::class.java)
            scoreRetriever.getScoreListener().onNext(ScoreResult(scoreError = scoreError))
        }
        return Result.failure()
    }

    private fun scoreRequestV2(tripId: String, finalScore: Boolean, serverUrl: String, appName: String): Result {
        LOGGER.info("scoreRequest "+ tripId, "scoreRequestV2")
        val scoreRetriever: ScoreRetriever by inject()
        val responseString = StringBuffer("")
        val url = URL("$serverUrl/score/$tripId")
        val connection = url.openConnection() as HttpsURLConnection
        connection.requestMethod = "GET"
        connection.addRequestProperty("X-AppKey", appName)
        val mapper = ObjectMapper().registerKotlinModule()
        try {
            connection.connect()
            val inputStream = connection.inputStream
            val rd = BufferedReader(InputStreamReader(inputStream))
            var line = rd.readLine()
            while (line != null) {
                responseString.append(line)
                line = rd.readLine()
            }
            if(connection.responseCode.toString().startsWith("5")){
                LOGGER.info("scoreRequest RETRY", "scoreRequestV2")
                return Result.retry()
            }
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            val node = mapper.readTree(responseString.toString())
            //{"status": "found", "result": ["ok"]}
            val fullScore = mapper.readValue(node.toString(), Score::class.java)
            LOGGER.info("Score http result body "+responseString.toString(), "scoreRequest")
            if (fullScore.status == ScoreStatus.pending) {
                LOGGER.info("scoreRequest RETRY", "scoreRequestV2")
                return Result.retry()
            }
            if (fullScore.status == ScoreStatus.notFound) {
                LOGGER.info("scoreRequest RETRY", "scoreRequestV2")
                return Result.retry()
            } else if (fullScore.status == ScoreStatus.found) {
                scoreRetriever.getScoreListener().onNext(ScoreResult(fullScore))
            } else {
                val scoreError = mapper.readValue(responseString.toString(), ScoreError::class.java)
                LOGGER.error("RESPONSE CODES ${connection.responseCode}"+" Error ${scoreError}", "scoreRequest")
                scoreRetriever.getScoreListener().onNext(ScoreResult(scoreError = scoreError))
            }
            return Result.success()
        } catch (e: IllegalStateException) {
            LOGGER.error("RESPONSE CODES ${connection.responseCode}"+" Exception ${e}", "scoreRequest")
            if (connection.responseCode == 200) {
                return Result.success()
            }
        } catch (e: Exception) {
            LOGGER.error("RESPONSE CODES ${connection.responseCode}"+" Exception ${e}", "scoreRequest")
            val scoreError = mapper.readValue(responseString.toString(), ScoreError::class.java)
            scoreRetriever.getScoreListener().onNext(ScoreResult(scoreError = scoreError))
            if (connection.responseCode == 200) {
                return Result.success()
            }
        } catch (err: Error) {
            LOGGER.error("RESPONSE CODES ${connection.responseCode}"+" Error ${err}", "scoreRequest")
            val scoreError = mapper.readValue(responseString.toString(), ScoreError::class.java)
            scoreRetriever.getScoreListener().onNext(ScoreResult(scoreError = scoreError))
        }
        return Result.failure()
    }
}