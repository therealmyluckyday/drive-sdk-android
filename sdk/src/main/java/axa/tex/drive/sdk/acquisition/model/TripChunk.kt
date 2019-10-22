package axa.tex.drive.sdk.acquisition.model

import androidx.work.Data
import axa.tex.drive.sdk.core.TripInfos
import axa.tex.drive.sdk.core.internal.Constants
import axa.tex.drive.sdk.core.logger.LoggerFactory


private const val DEFAULT_PACKET_SIZE = 50

class TripChunk(internal val tripId: TripId, internal val tripInfos: TripInfos, internal var idPacket : Int) {

    private var fixes = mutableListOf<Fix>()
    private var packetSize: Int = DEFAULT_PACKET_SIZE
    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
    private var isLast: Boolean = false

    fun isEnded() : Boolean {
        return isLast
    }

    fun append(fix: Fix) {
        if (fix is Event) {
            val eventFound = fix as Event
            isLast = eventFound.event.contains("stop")
        }
        fixes.add(fix)
    }

    fun canUpload() : Boolean {
        if (fixes.last() is MotionFix) {
            return false
        }
        if ( this.toJson() == Constants.EMPTY_PACKET) {
            return false
        }
        if ( this.toJson().count() > 9000) {
            return false
        }

        if (this.isEnded()) {
            return true
        }
        return fixes.count() > packetSize
    }

    // Private Method
    private fun generateTripId() : TripId {
        return TripId()
    }

    // MARK: Serialize
    fun toJson() : String {
        val packet = FixPacket(fixes, tripInfos.model, tripInfos.os, tripInfos.timezone, tripInfos.uid, tripInfos.version, tripId?.value, tripInfos.appName,
                tripInfos.clientId)
        LOGGER.info("CONVERTING TO JSON", function = "fun serialize() : String")
        return packet?.toJson()
    }

    fun data() : Data {
        LOGGER.info("Size ${this.fixes.count()}", function = "fun data()")

        LOGGER.info("Size ${this.toJson().count()}", function = "fun data()")
        return Data.Builder().putString(Constants.ID_KEY, this.tripInfos.uid).putString(Constants.DATA_KEY, this.toJson()).putString(Constants.APP_NAME_KEY, this.tripInfos.appName).putString(Constants.CLIENT_ID_KEY,
                this.tripInfos.clientId).putInt(Constants.WORK_TAG_KEY, idPacket).putString(Constants.TRIP_ID_KEY, this.tripId?.value).build()
    }
}