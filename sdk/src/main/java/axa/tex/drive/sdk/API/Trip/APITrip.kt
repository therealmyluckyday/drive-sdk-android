package axa.tex.drive.sdk.API.Trip


import androidx.work.*
import axa.tex.drive.sdk.acquisition.collection.internal.FixWorker
import axa.tex.drive.sdk.acquisition.collection.internal.db.CollectionDb
import axa.tex.drive.sdk.acquisition.model.PendingTrip
import axa.tex.drive.sdk.acquisition.model.TripChunk
import axa.tex.drive.sdk.core.internal.Constants
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.logger.LoggerFactory
import org.koin.android.ext.android.inject
import java.util.*

internal class APITrip : KoinComponentCallbacks {
    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
    fun sendTrip(tripChunk: TripChunk) {
        LOGGER.info("SENDING : NEW PACKET DETECTED", function = "sendTrip")
        val collectorDb: CollectionDb by inject()

        if (canUpload(tripChunk)) {
            LOGGER.info("SENDING : CONVERTING TO JSON", function = "sendTrip")
            val json = tripChunk.toJson()
            if (Constants.EMPTY_PACKET == json) {
                return
            }
            val tripEnded = tripChunk.isEnded()
            LOGGER.info("SENDING : $json", function = "sendTrip")
            val uid = UUID.randomUUID().toString()
            val nbPackets = collectorDb.getNumberPackets(tripChunk.tripId?.value!!)
            collectorDb.setNumberPackets(tripChunk.tripId.value, nbPackets + 1)
            LOGGER.info("SENDING : SETTING DATA", function = "sendTrip")
            val data: Data = Data.Builder().putString(Constants.ID_KEY, uid).putString(Constants.DATA_KEY, json).putString(Constants.APP_NAME_KEY, tripChunk.tripInfos.appName).putString(Constants.CLIENT_ID_KEY,
                    tripChunk.tripInfos.clientId).putInt(Constants.WORK_TAG_KEY, nbPackets).putString(Constants.TRIP_ID_KEY, tripChunk.tripId?.value).build()
            LOGGER.info("SENDING : DATA SET", function = "sendTrip")
            LOGGER.info("PACKET DATA FOR WORKER MANAGER ${data.toString()}", function = "sendTrip")
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            val fixUploadWork: OneTimeWorkRequest = OneTimeWorkRequest.Builder(FixWorker::class.java).setInputData(data).addTag((nbPackets - 1).toString()).setConstraints(constraints)
                    .build()
            val pendingTrip = PendingTrip(uid, tripChunk.tripId?.value, tripEnded)
            collectorDb.saveTrip(pendingTrip)
            LOGGER.info("SENDING : ENQUEUING", function = "fun addFixes(fixes: List<Fix>")
            WorkManager.getInstance().enqueue(fixUploadWork)
        }
    }

    fun canUpload(tripChunk: TripChunk): Boolean {
        return tripChunk.tripInfos.clientId != null && tripChunk.tripInfos.appName != null && tripChunk.tripInfos.appName.isEmpty() && tripChunk.tripInfos.clientId.isEmpty()
    }
}