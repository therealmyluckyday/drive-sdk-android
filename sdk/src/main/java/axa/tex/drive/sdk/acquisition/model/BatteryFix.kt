package axa.tex.drive.sdk.acquisition.model

data class BatteryFix (val level : Int , val state: BatteryState, val timestamp: Long): Fix(timestamp)