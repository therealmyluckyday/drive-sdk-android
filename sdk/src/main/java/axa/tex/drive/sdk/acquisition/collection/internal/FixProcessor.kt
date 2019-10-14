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
import org.koin.android.ext.android.inject

internal class FixProcessor : KoinComponentCallbacks {

    private val buffer = mutableListOf<Fix>()
    private var context: Context
    private val tripManager: TripManager by inject()
    private val apiTrip: APITrip = APITrip()
    private var currentTripChunk: TripChunk? = null


    constructor(context: Context) {
        this.context = context
    }

    fun startTrip(startTime: Long) {
        val tripId = tripManager.tripId(context)
        val collectorDb: CollectionDb by inject()
        val config = collectorDb.getConfig()
        val appName = config?.appName
        val clientId: String? = config?.clientId
        val start = Event(listOf("start"), startTime)
        val tripInfos = TripInfos(tripId!!, appName!!, clientId!!)
        currentTripChunk = TripChunk(tripId, tripInfos)
        addFix(start)
    }

    fun endTrip(endTime: Long) {
        val end = Event(listOf("stop"), endTime);
        addFix(end)
        currentTripChunk = null
    }

    fun push(buffer: MutableList<Fix>) {
        if (currentTripChunk != null) {
            apiTrip.sendTrip(this!!.currentTripChunk!!)
        }
    }

    fun addFix(fix: Fix) {
        currentTripChunk?.append(fix)
        if ((currentTripChunk != null) && (currentTripChunk!!.canUpload())) {
            push(this.buffer)
            this.buffer.clear()
        }
    }

    @Synchronized
    fun addFixes(fixes: List<Fix>) {
        for (fix in fixes) {
            addFix(fix)
        }
    }
}