package axa.tex.drive.sdk.acquisition.collection.internal


import android.content.Context
import androidx.work.Data
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import axa.tex.drive.sdk.acquisition.score.ScoreRetriever
import axa.tex.drive.sdk.core.internal.Constants
import axa.tex.drive.sdk.core.logger.LoggerFactory
import org.koin.android.ext.android.inject

internal class LastFixWorker(appContext: Context, workerParams: WorkerParameters)
    : FixWorker(appContext, workerParams) {
    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
    private val workManager: androidx.work.WorkManager by lazy {
        WorkManager.getInstance(appContext)
    }

    override fun doWork(): Result {

        val inputData: Data = inputData
        val tripId = inputData.getString(Constants.TRIP_ID_KEY) ?: ""
        val list = workManager.getWorkInfosByTagLiveData(tripId).value
        val tripChunkInQueue = list?.filter { it.state in setOf(WorkInfo.State.ENQUEUED , WorkInfo.State.RUNNING) }
        if (tripChunkInQueue != null && tripChunkInQueue.count() > 0) {
            return Result.retry()
        }

        val result = sendFixes(inputData)
        if (result == Result.success()) {
            LOGGER.info("Packet sent successfully and trip id = $tripId stop = true", "fun sendData(id: String, data: String, appName: String, clientId: String): Boolean")
            val collector: Collector by inject()
            if (collector.currentTripId?.value == tripId) {
                collector.currentTripId = null
            }
            retrieveScore(tripId)
        }
        return  result
    }

    fun retrieveScore(tripId: String) {
        val appName = inputData.getString(Constants.APP_NAME_KEY) ?: "APP_TEST"
        val isRetrievingScoreAutomatically = inputData.getBoolean(Constants.CONFIG_RETRIEVE_SCORE_AUTO_BOOLEAN_KEY, true)
        val serverUrl = inputData.getString(Constants.PLATFORM_URL) ?: "https://gw-preprod.tex.dil.services/v2.0"
        val isAPIV2 = inputData.getBoolean(Constants.PLATFORM_VERSION, false)
        val scoreRetriever: ScoreRetriever by inject()
        if (isRetrievingScoreAutomatically) {
            LOGGER.info("isRetrievingScoreAutomatically serverUrl $serverUrl", "retrieveScore")
            scoreRetriever.retrieveScore(tripId, appName, serverUrl, true, isAPIV2, delay = 60)
        }
        else {
            LOGGER.info("scoreRetriever.getAvailableScoreListener trip id = $tripId ", "retrieveScore")
            scoreRetriever.getAvailableScoreListener().onNext(tripId)
        }
    }
}