package axa.tex.drive.sdk.acquisition.collection.internal


import android.content.Context
import androidx.work.*
import axa.tex.drive.sdk.acquisition.score.ScoreRetriever
import axa.tex.drive.sdk.core.Platform
import axa.tex.drive.sdk.core.internal.Constants
import axa.tex.drive.sdk.core.logger.LoggerFactory
import org.koin.android.ext.android.inject

internal class LastFixWorker(appContext: Context, workerParams: WorkerParameters)
    : FixWorker(appContext, workerParams) {

    private val isRetrievingScoreAutomatically = false
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
        val platform : Platform
        when (inputData.getString(Constants.PLATFORM_KEY)) {
            Platform.PRODUCTION.endPoint -> platform = Platform.PRODUCTION
            Platform.TESTING.endPoint -> platform = Platform.TESTING
            Platform.PREPROD.endPoint -> platform = Platform.PREPROD
            else -> platform = Platform.PRODUCTION
        }
        val scoreRetriever: ScoreRetriever by inject()
        if (isRetrievingScoreAutomatically) {
            scoreRetriever.retrieveScore(tripId, appName, platform, true)
        }
        else {
            scoreRetriever.getAvailableScoreListener().onNext(tripId)
        }
    }
}