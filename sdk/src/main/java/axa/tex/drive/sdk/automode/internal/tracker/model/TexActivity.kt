package axa.tex.drive.sdk.automode.internal.tracker.model

enum class Where{
    IN_VEHICLE,
    ON_FOOT,
    WALKING
}

data class TexActivity(val where: Where, val confidence : Int)