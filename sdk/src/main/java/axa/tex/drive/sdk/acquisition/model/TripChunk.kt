package axa.tex.drive.sdk.acquisition.model

import androidx.work.Data
import axa.tex.drive.sdk.core.TripInfos
import axa.tex.drive.sdk.core.internal.Constants
import axa.tex.drive.sdk.core.logger.LoggerFactory


private const val DEFAULT_PACKET_SIZE = 50
private const val DEFAULT_JSON_SIZE = 10000

class TripChunk(internal val tripInfos: TripInfos, internal var idPacket : Int) {
    private var fixes = mutableListOf<Fix>()
    private var packetSize: Int = DEFAULT_PACKET_SIZE
    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
    private var isLast: Boolean = false
    fun clear() {
        fixes.clear()
        isLast = false
    }

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

        if ( this.toJson() == Constants.EMPTY_PACKET) {
            LOGGER.info("this.toJson() == Constants.EMPTY_PACKET TRUE", function = "canUpload()")
            return false
        }

        if ( this.toJson().count() > DEFAULT_JSON_SIZE) {
            LOGGER.info("this.toJson().count() > 10000 TRUE", function = "canUpload()")
            return true
        }

        if (this.isEnded()) {
            LOGGER.info("this.isEnded()"+this.isEnded(), function = "canUpload()")
            return true
        }
        if (fixes.last() is MotionFix) {
            return false
        }

        //LOGGER.info("fixes.count() > packetSize " + fixes.count() +" > " + packetSize, function = "canUpload()")
        return fixes.count() > packetSize
    }

    // Private Method

    // MARK: Serialize
    fun toJson() : String {
        val packet = FixPacket(fixes, tripInfos.model, tripInfos.os, tripInfos.timezone, tripInfos.uid, tripInfos.version, tripInfos.tripId.value, tripInfos.appName,
                tripInfos.clientId)
        LOGGER.info("CONVERTING TO JSON", function = "fun serialize() : String")
        return packet?.toJson()
    }

    fun data() : Data {
        LOGGER.info("Size ${this.fixes.count()}", function = "fun data()")

        LOGGER.info("Size ${this.toJson().count()}", function = "fun data()")
        return Data.Builder().putString(Constants.ID_KEY, this.tripInfos.uid).putString(Constants.DATA_KEY, this.toJson()).putString(Constants.APP_NAME_KEY, this.tripInfos.appName).putString(Constants.CLIENT_ID_KEY,
                this.tripInfos.clientId).putInt(Constants.WORK_TAG_KEY, idPacket).putString(Constants.TRIP_ID_KEY, this.tripInfos.tripId.value).build()
    }
}