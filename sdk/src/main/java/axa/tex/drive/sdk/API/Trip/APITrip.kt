package axa.tex.drive.sdk.API.Trip


import androidx.work.*
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
        LOGGER.info("SENDING : NEW PACKET DETECTED", function = "sendTrip")
        val collectorDb: CollectionDb by inject()

        if (canUpload(tripChunk)) {
            LOGGER.info("SENDING : SETTING DATA", function = "sendTrip")
            val data: Data = tripChunk.data()
            LOGGER.info("SENDING : DATA SET", function = "sendTrip")
            LOGGER.info("PACKET DATA FOR WORKER MANAGER ${data.toString()}", function = "sendTrip")
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

            var fixUploadWork: OneTimeWorkRequest = OneTimeWorkRequest.Builder(FixWorker::class.java).setInputData(data).addTag((tripChunk.tripId).toString()).setConstraints(constraints)
                    .build()
            if (tripChunk.isEnded()) {
                fixUploadWork = OneTimeWorkRequest.Builder(LastFixWorker::class.java).setInputData(data).addTag((tripChunk.tripId).toString()).setConstraints(constraints)
                        .build()
            }
            val pendingTrip = PendingTrip(tripChunk.tripInfos.uid, tripChunk.tripId?.value, tripChunk.isEnded())
            collectorDb.saveTrip(pendingTrip)
            LOGGER.info("SENDING : ENQUEUING", function = "fun addFixes(fixes: List<Fix>")
            WorkManager.getInstance().enqueue(fixUploadWork)
        }
    }

    fun canUpload(tripChunk: TripChunk): Boolean {
        return tripChunk.tripInfos.clientId != null && tripChunk.tripInfos.appName != null && !tripChunk.tripInfos.appName.isEmpty() && !tripChunk.tripInfos.clientId.isEmpty()
    }
}