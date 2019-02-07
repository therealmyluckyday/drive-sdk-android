package axa.tex.drive.sdk.core

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature

internal class Config {
    var batteryTrackerEnabled: Boolean = false
    var locationTrackerEnabled: Boolean = false
    var motionTrackerEnabled: Boolean = false
    var appName: String? = null//Constants.DEFAULT_APP_NAME
    var clientId: String? = null
    var endPoint : Platform? = Platform.PRODUCTION

    constructor() {

    }

    constructor(batteryTrackerEnabled: Boolean,
                locationTrackerEnabled: Boolean,
                motionTrackerEnabled: Boolean,
                appName: String?, clientId: String?,endPoint : Platform?) {
        this.batteryTrackerEnabled = batteryTrackerEnabled
        this.locationTrackerEnabled = locationTrackerEnabled
        this.motionTrackerEnabled = motionTrackerEnabled
        this.appName = appName
        this.clientId = clientId
        this.endPoint = endPoint
    }

    internal fun toJson(): String {
        return try {
            val mapper = ObjectMapper();
            mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
            mapper.writeValueAsString(this);
        } catch (e: Exception) {
            e.printStackTrace()
            "{}";
        }

    }
}