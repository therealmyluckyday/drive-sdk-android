package axa.tex.drive.sdk.acquisition.score.model

import axa.tex.drive.sdk.acquisition.model.TripId
import axa.tex.drive.sdk.core.logger.LoggerFactory
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature

enum class ScoreType {
    @JsonProperty("temporary")
    temporary,
    @JsonProperty("final")
    final
}



// Mapping between status and status detail
//LowPrecisionTrip = ScoreStatus.trip_too_short
//TripTooShort = ScoreStatus.trip_too_short
//TripTooLong = ScoreStatus.trip_too_long
//LowSpeedTrip = ScoreStatus.trip_invalid
//HighSpeedTrip = ScoreStatus.trip_invalid
//NoMappingData = ScoreStatus.no_external_data
//OutOfRoadTrip = ScoreStatus.trip_invalid
//NoLocations = ScoreStatus.trip_invalid
//NotEnoughLocations = ScoreStatus.trip_invalid
//DataQualityIssue = ScoreStatus.trip_invalid

public enum class ExceptionScoreStatus {
    @JsonProperty("not_enough_locations")
    lowPrecisionTrip,
    @JsonProperty("trip_too_short")
    tripTooShort,
    @JsonProperty("trip_too_long")
    tripTooLong,
    @JsonProperty("low_speed_trip")
    lowSpeedTrip,
    @JsonProperty("high_speed_trip")
    highSpeedTrip,
    @JsonProperty("no_mapping_data")
    noMappingData,
    @JsonProperty("out_of_road_trip")
    outOfRoadTrip,
    @JsonProperty("no_locations")
    noLocations
}

class ScoreError {
    @JsonProperty("status")
    var status: ScoreStatus = ScoreStatus.error
    @JsonProperty("trip_id")
    var tripId: TripId? = null
    @JsonProperty("status_details")
    var details: List<ExceptionScoreStatus>? = null

    internal val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger

    constructor(
                status: ScoreStatus,
                trip_id: String,
                details: List<ExceptionScoreStatus>?) {
        this.details = details
        this.status = status
        this.tripId = TripId(trip_id)
    }

    internal fun toJson(): String {
        return try {
            val mapper = ObjectMapper()
            mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true)
            mapper.writeValueAsString(this)
        } catch (e: Exception) {
            LOGGER.warn("Exception : "+e, function = "toJson")
            "{}"
        }

    }

    override fun toString(): String {
        return "Error on score request tripId: "+ (tripId?.value ?: "no trip id") +", status: "+status+" message: "+details
    }

}