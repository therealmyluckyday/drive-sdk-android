package axa.tex.drive.sdk.acquisition.collection.internal


import android.content.Context
import androidx.work.*
import axa.tex.drive.sdk.acquisition.score.ScoreRetriever
import axa.tex.drive.sdk.core.internal.Constants
import axa.tex.drive.sdk.core.logger.LoggerFactory
import org.koin.android.ext.android.inject

internal class LastFixWorker(): FixWorker() {

    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger

    override fun doWork(): WorkerResult {

        val inputData: Data = inputData
        val tripId = inputData.getString(Constants.TRIP_ID_KEY, "")
        val list = WorkManager.getInstance().getStatusesByTag(tripId).value
        val tripChunkInQueue = list?.filter { it.state in setOf(State.ENQUEUED, State.RUNNING) }
        if (tripChunkInQueue != null && tripChunkInQueue.count() > 0) {
            return WorkerResult.RETRY
        }

        val result = sendFixes(inputData)
        if (result == WorkerResult.SUCCESS) {
            val scoreRetriever: ScoreRetriever by inject()
            val collectorDb: CollectionDb by inject()
            LOGGER.info("Packet sent successfully and trip id = ${tripId} stop = true", "fun sendData(id: String, data: String, appName: String, clientId: String): Boolean")
            val collector: Collector by inject()
            if (collector.currentTripId?.value == tripId) {
                collector.currentTripId = null
            }

            scoreRetriever.getAvailableScoreListener().onNext(tripId)
            collectorDb.deleteTripNumberPackets(tripId)
        }
        return  result
    }

}