package axa.tex.drive.sdk.acquisition.model

import axa.tex.drive.sdk.core.TripInfos
import axa.tex.drive.sdk.core.logger.LoggerFactory


private const val DEFAULT_PACKET_SIZE = 50

class TripChunk {

    // MARK: Property
    private val tripId: TripId
    private var fixes = mutableListOf<Fix>()
    private val tripInfos: TripInfos
    private var packetSize: Int = DEFAULT_PACKET_SIZE
    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger

    fun append(fix: Fix) {
        fixes.add(fix)
    }

    fun canUpload() : Boolean {
        if fixes.last is MotionFix {
            return false
        }
        if let event = self.event, event.eventType == EventType.stop {
            return true
        }
        return fixes.count > packetSize
    }

    // Private Method
    private fun generateTripId() : TripId {
        return TripId()
    }

    // MARK: Serialize
    fun serialize() : String {
        val packet = FixPacket(fixes, tripInfos.model, tripInfos.os, tripInfos.timezone, tripInfos.uid, tripInfos.version, tripId?.value, tripInfos.appName,
                tripInfos.clientId)
        LOGGER.info("CONVERTING TO JSON", function = "fun serialize() : String")
        return packet?.toJson()
    }


}