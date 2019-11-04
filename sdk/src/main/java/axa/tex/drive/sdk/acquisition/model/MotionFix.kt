package axa.tex.drive.sdk.acquisition.model

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonRootName

@JsonRootName(value = "motion")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class MotionFix(val rotationRate: Motion? = null,
                     val acceleration: Motion? = null,
                     val rawAcceleration: Motion? = null,
                     val gravity: Motion? = null,
                     val magnetometer: Motion? = null,
                     @JsonIgnore val timestamp: Long) : Fix(timestamp){

    fun norm(): Double {
        if(acceleration != null){
            val value = acceleration.x*acceleration.x + acceleration.y*acceleration.y + acceleration.z*acceleration.z
            return Math.sqrt(value.toDouble())
        }
        return -1.0
    }
}