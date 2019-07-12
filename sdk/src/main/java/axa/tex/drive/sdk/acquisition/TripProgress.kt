package axa.tex.drive.sdk.acquisition

import android.location.Location
import axa.tex.drive.sdk.acquisition.model.TripId

data class TripProgress(val currentTripId: TripId, val location: Location,
                        val speed: Int,
                        val distance: Double,
                        val duration : Long)