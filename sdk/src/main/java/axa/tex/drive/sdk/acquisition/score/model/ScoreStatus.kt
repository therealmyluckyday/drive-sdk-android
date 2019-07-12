package axa.tex.drive.sdk.acquisition.score.model

enum class ScoreStatus {
    OK,
    pending,
    trip_too_short,
    trip_invalid,
    trip_too_long,
    no_external_data,
    error
}