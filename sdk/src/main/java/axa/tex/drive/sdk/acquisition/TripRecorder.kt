package axa.tex.drive.sdk.acquisition

import axa.tex.drive.sdk.acquisition.model.LocationFix
import axa.tex.drive.sdk.acquisition.model.TripId
import io.reactivex.Observable

interface TripRecorder {

    /**
     * TripID of the currently recorded trip.
     * @return the trip id
     */
    fun getCurrentTripId(): TripId?

    /**
     * Initiates tracking
     */
    fun startTracking(startTime: Long)

    /**
     * Stops the tracking
     */
    fun stopTracking(endTime: Long)

    /**
     * Says if we are currently recording
     * @return true or false
     */
    fun isRecording(): Boolean

    /**
     * @return An observable on which we can register to get location fixes.
     */
    fun locationObservable(): Observable<LocationFix>


    fun tripIdListener(): Observable<TripId>
}