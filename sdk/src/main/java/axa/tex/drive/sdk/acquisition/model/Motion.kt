package axa.tex.drive.sdk.acquisition.model

import com.fasterxml.jackson.annotation.JsonIgnore

data class Motion(val x: Float, val y: Float, val z: Float, @JsonIgnore val timestamp: Long)

