package axa.tex.drive.sdk.acquisition.collection.internal


import android.content.Context
import axa.tex.drive.sdk.API.Trip.APITrip
import axa.tex.drive.sdk.acquisition.collection.internal.db.CollectionDb
import axa.tex.drive.sdk.acquisition.model.Event
import axa.tex.drive.sdk.acquisition.model.Fix
import axa.tex.drive.sdk.acquisition.model.TripChunk
import axa.tex.drive.sdk.acquisition.model.TripId
import axa.tex.drive.sdk.core.Config
import axa.tex.drive.sdk.core.TripInfos
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.internal.utils.TripManager
import axa.tex.drive.sdk.core.logger.LoggerFactory
import org.koin.android.ext.android.inject

internal class FixProcessor : KoinComponentCallbacks {
    private var context: Context
    private val apiTrip: APITrip
    internal var currentTripChunk: TripChunk? = null
    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger

    constructor(context: Context) {
        this.context = context
        this.apiTrip = APITrip(context)
    }

    fun startTrip(startTime: Long, config: Config) : TripId {
        val appName = config.appName
        val clientId = config.clientId
        val start = Event(listOf("start"), startTime)
        val tripInfos = TripInfos(appName, clientId)
        currentTripChunk = TripChunk(tripInfos, 0)
        LOGGER.info("Start trip ", "startTrip")
        addFix(start)
        return tripInfos.tripId
    }
    
    fun endTrip(endTime: Long) {
        LOGGER.info("stop trip Begin", "endTrip")
        val end = Event(listOf("stop"), endTime)
        addFix(end)
        currentTripChunk = null
        LOGGER.info("stop trip End", "endTrip")
    }


    fun push() {
        LOGGER.info("push tripchunk ", "push")
        apiTrip.sendTrip(this.currentTripChunk!!)
        this.currentTripChunk?.clear()
    }


    fun addFix(fix: Fix) {
        currentTripChunk?.append(fix)
        if ((currentTripChunk != null) && (currentTripChunk!!.canUpload())) {
            push()
        }
    }


    fun addFixes(fixes: List<Fix>) {
        if (currentTripChunk != null) {
            for (fix in fixes) {
                addFix(fix)
            }
        }
    }
}