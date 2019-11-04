package axa.tex.drive.sdk.acquisition.collection.internal


import android.content.Context
import androidx.work.*
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
            val scoreRetriever: ScoreRetriever by inject()
            LOGGER.info("Packet sent successfully and trip id = ${tripId} stop = true", "fun sendData(id: String, data: String, appName: String, clientId: String): Boolean")
            val collector: Collector by inject()
            if (collector.currentTripId?.value == tripId) {
                collector.currentTripId = null
            }

            scoreRetriever.getAvailableScoreListener().onNext(tripId)
        }
        return  result
    }

}