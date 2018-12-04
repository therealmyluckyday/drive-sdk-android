package axa.tex.drive.sdk.acquisition.score.internal

import androidx.work.Data
import androidx.work.Worker
import axa.tex.drive.sdk.acquisition.collection.internal.db.CollectionDb
import axa.tex.drive.sdk.acquisition.score.Score
import axa.tex.drive.sdk.acquisition.score.ScoreRetriever
import axa.tex.drive.sdk.acquisition.score.Test
import axa.tex.drive.sdk.acquisition.score.model.ScoresDil
import axa.tex.drive.sdk.core.Config
import axa.tex.drive.sdk.core.Platform
import axa.tex.drive.sdk.core.internal.util.PlatformToHostConverter
import axa.tex.drive.sdk.core.logger.LoggerFactory
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

private val LOGGER = LoggerFactory.getLogger().logger

internal class ScoreWorker() : Worker() {



    override fun doWork(): WorkerResult {

        val inputData : Data =  inputData
        for((tripId , state) in inputData.keyValueMap) {
            scoreRequest(tripId,state as Boolean)
        }


        return WorkerResult.SUCCESS

    }

    private fun getLocaleId(locale : Locale?): String {
        var theLocale = locale
        if (locale == null){
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
    private fun scoreRequest(tripId: String, finalScore: Boolean): String {
        val config  = CollectionDb.getConfig(applicationContext)
        val responseString = StringBuffer("")
        val locale = null
        val serverUrl = PlatformToHostConverter(Platform.PREPROD).getHost()
        val url: URL
        val theLocal = getLocaleId(locale)
        if (!finalScore) {
            url = URL( "$serverUrl/score?trip_id=$tripId&lang=$theLocal")
        } else {
            url = URL( "$serverUrl/score?trip_id=$tripId&lang=$theLocal&final=true")
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
        var line =  rd.readLine()
        while (line  != null) {
            responseString.append(line)
            line =  rd.readLine()
        }

        val mapper = ObjectMapper()
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        val node = mapper.readTree(responseString.toString());
        val score = mapper.readValue(node.get("scores_dil").toString(), ScoresDil::class.java)
        //val score = mapper.readValue(responseString.toString(), Score::class.java)
        ScoreRetriever.getScoreListener().onNext(score)
        /*try {
            val map = mapper.readValue(responseString.toString(), Test::class.java)
        }catch (e :Exception){
            e.printStackTrace()
        }*/
        return responseString.toString()
    }



}