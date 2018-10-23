package axa.tex.drive.sdk.acquisition.internal.tracker.fake.model

import android.location.Location
import com.fasterxml.jackson.annotation.JsonIgnore


data class FakeLocation(val latitude : Double,
                        val longitude: Double,
                        val precision: Float,
                        val bearing : Float,
                        val altitude: Double,
                        @JsonIgnore val timestamp: Long,
                        val accuracy : Float,
                        val speed : Float,
                        val time : Long)

