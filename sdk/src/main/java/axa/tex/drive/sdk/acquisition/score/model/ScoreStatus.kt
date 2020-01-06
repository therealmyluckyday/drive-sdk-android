package axa.tex.drive.sdk.acquisition.score.model

import com.fasterxml.jackson.annotation.JsonProperty

enum class ScoreStatus {
    @JsonProperty("ok")
    ok,
    @JsonProperty("pending")
    pending,
    @JsonProperty("trip_not_found")
    tripNotFound,
    @JsonProperty("trip_too_short")
    tooShort,
    @JsonProperty("trip_invalid")
    invalid,
    @JsonProperty("trip_too_long")
    tooLong,
    @JsonProperty("no_external_data")
    noExternalData,
    @JsonProperty("error")
    error
}