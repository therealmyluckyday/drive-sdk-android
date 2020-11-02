package axa.tex.drive.sdk.acquisition.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonRootName
@JsonRootName(value = "location")
data class LocationFix(val latitude: Double,
                       val longitude: Double,
                       val precision: Float,
                       val speed: Float,
                       val bearing: Float,
                       val altitude: Double,
                       @JsonIgnore val timestamp: Long) : Fix(timestamp) {
    override fun toJsonAPIV2(): String {
        return "{\"gps\":{\"latitude\":$latitude, \"longitude\":$longitude, \"precision_hdop\":$precision, \"speed\":$speed, \"bearing\":$bearing, \"altitude\":$altitude}, \"timestamp\":$timestamp}"
    }
}