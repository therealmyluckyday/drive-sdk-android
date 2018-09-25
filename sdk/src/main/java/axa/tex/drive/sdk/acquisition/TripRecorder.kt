package axa.tex.drive.sdk.acquisition

import axa.tex.drive.sdk.acquisition.model.Fix
import io.reactivex.Observable

interface TripRecorder  {

    /**
     * TripID of the currently recorded trip.
     * @return the trip id
     */
    fun getCurrentTripId(): String

    /**
     * Initiates tracking
     */
    fun track()

    /**
     * Stops the tracking
     */
    fun stopTracking()

    /**
     * Says if we are currently recording
     * @return true or false
     */
    fun isRecording(): Boolean

    /**
     * @return An observable on which we can register to get location fixes.
     */
    fun locationObservable(): Observable<Fix>



}