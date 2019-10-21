package axa.tex.drive.sdk.acquisition.collection.internal


import android.content.Context
import axa.tex.drive.sdk.API.Trip.APITrip
import axa.tex.drive.sdk.acquisition.collection.internal.db.CollectionDb
import axa.tex.drive.sdk.acquisition.model.Event
import axa.tex.drive.sdk.acquisition.model.Fix
import axa.tex.drive.sdk.acquisition.model.TripChunk
import axa.tex.drive.sdk.core.TripInfos
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.internal.utils.TripManager
import axa.tex.drive.sdk.core.logger.LoggerFactory
import org.koin.android.ext.android.inject

internal class FixProcessor : KoinComponentCallbacks {

    private val buffer = mutableListOf<Fix>()
    private var context: Context
    private val tripManager: TripManager by inject()
    private val apiTrip: APITrip = APITrip()
    private var currentTripChunk: TripChunk? = null
    private val collectorDb: CollectionDb by inject()

    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
    constructor(context: Context) {
        this.context = context
    }

    fun startTrip(startTime: Long) {
        val tripId = tripManager.tripId(context)
        val config = collectorDb.getConfig()
        val appName = config?.appName
        val clientId: String? = config?.clientId
        val start = Event(listOf("start"), startTime)
        val tripInfos = TripInfos(tripId!!, appName!!, clientId!!)
        currentTripChunk = TripChunk(tripId, tripInfos, 0)
        LOGGER.info("Start trip ", "startTrip")
        addFix(start)
    }
    
    fun endTrip(endTime: Long) {
        val end = Event(listOf("stop"), endTime);
        addFix(end)
        LOGGER.info("stop trip ", "endTrip")
        currentTripChunk = null
    }


    fun push() {

        LOGGER.info("push  ", "push")
        if (currentTripChunk != null) {
            LOGGER.info("push tripchunk ", "push")
            apiTrip.sendTrip(this!!.currentTripChunk!!)
            this!!.currentTripChunk!!.idPacket += 1
            collectorDb.setNumberPackets(this!!.currentTripChunk!!.tripId.value, this!!.currentTripChunk!!.idPacket)
        }
    }


    fun addFix(fix: Fix) {
        currentTripChunk?.append(fix)
        if ((currentTripChunk != null) && (currentTripChunk!!.canUpload())) {
            push()
            this.buffer.clear()
        }
    }


    fun addFixes(fixes: List<Fix>) {
        for (fix in fixes) {
            addFix(fix)
        }
    }
}