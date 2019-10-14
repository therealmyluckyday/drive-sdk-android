package axa.tex.drive.sdk.acquisition.model

import axa.tex.drive.sdk.core.TripInfos
import axa.tex.drive.sdk.core.logger.LoggerFactory


private const val DEFAULT_PACKET_SIZE = 50

class TripChunk(internal val tripId: TripId, internal val tripInfos: TripInfos) {

    private var fixes = mutableListOf<Fix>()
    private var packetSize: Int = DEFAULT_PACKET_SIZE
    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
    private var event: Event? = null

    fun isEnded() : Boolean {
        return ((this.event != null) && (this.event!!.event.contains("stop")))
    }

    fun append(fix: Fix) {
        fixes.add(fix)
    }

    fun canUpload() : Boolean {
        if (fixes.last() is MotionFix) {
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
}