package axa.tex.drive.sdk.core

import axa.tex.drive.sdk.acquisition.model.TripId
import axa.tex.drive.sdk.core.internal.Constants
import axa.tex.drive.sdk.core.internal.utils.DeviceInfo
import axa.tex.drive.sdk.core.internal.utils.Utils
import java.util.*

class TripInfos(val appName: String, val clientId: String) {
    companion object {
        private fun generateTripId() : TripId {
            val generatedTripId = TripId(UUID.randomUUID().toString().toUpperCase(Locale.US))
            return generatedTripId
        }
    }
    val tripId: TripId = TripInfos.generateTripId()
    val model = DeviceInfo.getDeviceName()
    val os: String = DeviceInfo.getOSVersion()
    val timezone: String = Utils.getFormattedTZ()
    val uid: String = UUID.randomUUID().toString()
    val version: String = Constants.JSON_SCHEMA_VERSION
}