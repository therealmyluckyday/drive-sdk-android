package axa.tex.drive.sdk.acquisition.model
import com.fasterxml.jackson.annotation.JsonRootName

@JsonRootName(value = "location")
data class LocationFix(val latitude : Double,
                       val longitude: Double,
                       val precision: Float,
                       val seed : Float,
                       val bearing : Float,
                       val altitude: Double,
                       val timestamp: Long) : Fix(timestamp)
