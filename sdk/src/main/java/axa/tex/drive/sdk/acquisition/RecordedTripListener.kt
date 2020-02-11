package axa.tex.drive.sdk.acquisition

import android.location.Location
import java.util.*


@Deprecated("Deprecated SDKV2 interface", ReplaceWith("setTripUploadedListener() from TripRecorder"))
interface RecordedTripListener {
    fun onTripEnded(tripId: String?)
}