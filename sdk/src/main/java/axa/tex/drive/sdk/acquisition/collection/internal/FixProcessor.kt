package axa.tex.drive.sdk.acquisition.collection.internal


import android.content.Context
import axa.tex.drive.sdk.API.Trip.APITrip
import axa.tex.drive.sdk.acquisition.model.Event
import axa.tex.drive.sdk.acquisition.model.Fix
import axa.tex.drive.sdk.acquisition.model.TripChunk
import axa.tex.drive.sdk.acquisition.model.TripId
import axa.tex.drive.sdk.core.Config
import axa.tex.drive.sdk.core.TripInfos
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.logger.LoggerFactory
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

internal class FixProcessor : KoinComponentCallbacks {
    private var context: Context
    private val apiTrip: APITrip
    internal var currentTripChunk: TripChunk? = null
    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
    internal var fixToSend: PublishSubject<Fix> = PublishSubject.create()


    constructor(context: Context) {
        this.context = context
        this.apiTrip = APITrip(context)
        this.fixToSend.observeOn(Schedulers.single()).subscribe( { fix ->
            this.addFix(fix)
        }, {throwable ->
            LOGGER.warn("Exception : "+throwable, function = "enable")
        })
    }

    fun startTrip(startTime: Long, config: Config) : TripId {
        val appName = config.appName
        val clientId = config.clientId
        this.apiTrip.isRetrievingScoreAutomatically = config.isRetrievingScoreAutomatically
        val start = Event(listOf("start"), startTime)
        val tripInfos = TripInfos(appName, clientId, config.endPoint, isAPIV2 = config.isAPIV2)
        currentTripChunk = TripChunk(tripInfos, 0)
        LOGGER.info("Start trip, tripId : "+tripInfos.tripId.value, "startTrip")
        this.fixToSend.onNext(start)
        return tripInfos.tripId
    }
    
    fun endTrip(endTime: Long) {
        LOGGER.info("stop trip", "endTrip")
        val end = Event(listOf("stop"), endTime)
        this.fixToSend.onNext(end)
    }


    fun push(tripChunk: TripChunk) {
        LOGGER.info("push tripchunk ", "push")
        apiTrip.sendTrip(tripChunk)
        tripChunk.clear()
    }


    fun addFix(fix: Fix) {
        currentTripChunk?.append(fix)
        val tripChunk = this.currentTripChunk
        if ((fix is Event)) {
            val eventFound = fix
            if (eventFound.event.contains("stop")) {
                currentTripChunk = null
            }
        }
        if ((tripChunk != null) && (tripChunk.canUpload())) {
            LOGGER.info("WillPush", "addFix")
            push(tripChunk)
        }
    }


    fun addFixes(fixes: List<Fix>) {
        if (currentTripChunk != null) {
            for (fix in fixes) {
                this.fixToSend.onNext(fix)
            }
        }
    }
}