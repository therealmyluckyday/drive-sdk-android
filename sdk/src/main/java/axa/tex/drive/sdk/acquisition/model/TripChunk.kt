package axa.tex.drive.sdk.acquisition.model

import androidx.work.Data
import axa.tex.drive.sdk.core.TripInfos
import axa.tex.drive.sdk.core.internal.Constants
import axa.tex.drive.sdk.core.logger.LoggerFactory


private const val DEFAULT_PACKET_SIZE = 50
private const val DEFAULT_JSON_SIZE = 9000

class TripChunk(internal val tripInfos: TripInfos, internal var idPacket : Int) {
    private var fixes = mutableListOf<Fix>()
    private var packetSize: Int = DEFAULT_PACKET_SIZE
    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
    internal var isLast: Boolean = false
    fun clear() {
        fixes.clear()
        isLast = false
    }

    fun append(fix: Fix) {
        if (fix is Event) {
            val eventFound = fix
            isLast = eventFound.event.contains("stop")
        }
        fixes.add(fix)
    }

    fun canUpload() : Boolean {
        val funcName = "canUpload"
        val json = this.toJson()
        if ( json == Constants.EMPTY_PACKET) {
            LOGGER.info("this.toJson() == Constants.EMPTY_PACKET TRUE", function = funcName)
            return false
        }

        if ( json.count() > DEFAULT_JSON_SIZE) {
            LOGGER.info("this.toJson().count() > ${DEFAULT_JSON_SIZE} TRUE", function = funcName)
            return true
        }

        if (this.isLast) {
            LOGGER.info("this.isLast"+this.isLast, function = funcName)
            return true
        }
        if (fixes.last() is MotionFix) {
            return false
        }

        LOGGER.info("fixes.count() > packetSize " + fixes.count() +" > " + packetSize, function = funcName)
        return fixes.count() > packetSize
    }

    // Private Method
    // MARK: Serialize
    fun toJson() : String {
        val currentFixes = fixes
        val packet = FixPacket(currentFixes, tripInfos.model, tripInfos.os, tripInfos.timezone, tripInfos.uid, tripInfos.version, tripInfos.tripId.value, tripInfos.appName, tripInfos.clientId)
        return packet.toJson()
    }

    fun data(isRetrievingScoreAutomatically: Boolean) : Data {
        return Data.Builder()
                .putString(Constants.ID_KEY, this.tripInfos.uid)
                .putString(Constants.DATA_KEY, this.toJson())
                .putString(Constants.APP_NAME_KEY, this.tripInfos.appName)
                .putString(Constants.PLATFORM_KEY, this.tripInfos.platform.endPoint)
                .putString(Constants.CLIENT_ID_KEY, this.tripInfos.clientId)
                .putBoolean(Constants.CONFIG_RETRIEVE_SCORE_AUTO_BOOLEAN_KEY, isRetrievingScoreAutomatically)
                .putInt(Constants.WORK_TAG_KEY, idPacket)
                .putString(Constants.TRIP_ID_KEY, this.tripInfos.tripId.value)
                .build()
    }
}