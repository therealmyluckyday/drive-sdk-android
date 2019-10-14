package axa.tex.drive.sdk.core

import axa.tex.drive.sdk.acquisition.model.TripId
import axa.tex.drive.sdk.core.internal.Constants
import axa.tex.drive.sdk.core.internal.utils.DeviceInfo
import axa.tex.drive.sdk.core.internal.utils.Utils
import java.util.*

class TripInfos(val tripId: TripId, val appName: String, val clientId: String) {
    val model = DeviceInfo.getDeviceName()
    val os: String = DeviceInfo.getOSVersion()
    val timezone: String = Utils.getFormattedTZ()
    val uid: String = UUID.randomUUID().toString()
    val version: String = Constants.JSON_SCHEMA_VERSION
}