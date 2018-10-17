package axa.tex.drive.sdk.acquisition.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonRootName

@JsonRootName(value = "battery")
data class BatteryFix (val level : Int ,
                       val state: BatteryState,
                       @JsonIgnore val timestamp: Long): Fix(timestamp)