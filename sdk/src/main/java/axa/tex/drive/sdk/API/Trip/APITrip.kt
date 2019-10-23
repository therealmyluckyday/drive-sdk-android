package axa.tex.drive.sdk.API.Trip


import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import axa.tex.drive.sdk.acquisition.collection.internal.FixWorker
import axa.tex.drive.sdk.acquisition.collection.internal.LastFixWorker
import axa.tex.drive.sdk.acquisition.collection.internal.db.CollectionDb
import axa.tex.drive.sdk.acquisition.model.PendingTrip
import axa.tex.drive.sdk.acquisition.model.TripChunk
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.logger.LoggerFactory
import org.koin.android.ext.android.inject

internal class APITrip : KoinComponentCallbacks {
    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
    fun sendTrip(tripChunk: TripChunk) {
        val collectorDb: CollectionDb by inject()
        LOGGER.info("SENDING PACKET DATA FOR WORKER MANAGER ${tripChunk.data()}", function = "sendTrip")
        val fixUploadWork: OneTimeWorkRequest = OneTimeWorkRequest
                .Builder(if (tripChunk.isEnded()) LastFixWorker::class.java else FixWorker::class.java)
                .setInputData(tripChunk.data())
                .addTag((tripChunk.tripInfos.tripId).toString())
                .setConstraints(Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .build()
        val pendingTrip = PendingTrip(tripChunk.tripInfos.uid, tripChunk.tripInfos.tripId.value, tripChunk.isEnded())
        collectorDb.saveTrip(pendingTrip)
        WorkManager.getInstance().enqueue(fixUploadWork)
    }
}