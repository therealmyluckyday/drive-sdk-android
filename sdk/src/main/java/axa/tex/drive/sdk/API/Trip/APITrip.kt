package axa.tex.drive.sdk.API.Trip

import android.content.Context
import androidx.work.*
import axa.tex.drive.sdk.acquisition.collection.internal.FixWorker
import axa.tex.drive.sdk.acquisition.collection.internal.LastFixWorker
import axa.tex.drive.sdk.acquisition.model.TripChunk
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.logger.LoggerFactory


internal class APITrip : KoinComponentCallbacks {
    private val workerManager: WorkManager
    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
    constructor(context: Context) {
        this.workerManager = WorkManager.getInstance(context)
    }
    var isRetrievingScoreAutomatically: Boolean = false

    fun sendTrip(tripChunk: TripChunk) {
        LOGGER.info("SENDING PACKET DATA FOR WORKER MANAGER ${tripChunk.data(isRetrievingScoreAutomatically)}", function = "sendTrip")
        val fixUploadWork: OneTimeWorkRequest = OneTimeWorkRequest
                .Builder(if (tripChunk.isLast) LastFixWorker::class.java else FixWorker::class.java)
                .setInputData(tripChunk.data(isRetrievingScoreAutomatically))
                .addTag((tripChunk.tripInfos.tripId).toString())
                .setConstraints(Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .build()
        workerManager.enqueueUniqueWork(tripChunk.tripInfos.tripId.value, ExistingWorkPolicy.APPEND, fixUploadWork)
    }
}