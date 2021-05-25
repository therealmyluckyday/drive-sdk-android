package axa.tex.drive.sdk.acquisition


@Deprecated("Deprecated SDKV2 interface", ReplaceWith("setTripUploadedListener() from TripRecorder"))
interface RecordedTripListener {
    fun onTripEnded(tripId: String?)
}