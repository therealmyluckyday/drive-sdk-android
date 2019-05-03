package axa.tex.drive.sdk.acquisition

import android.app.Notification
import axa.tex.drive.sdk.acquisition.model.LocationFix
import axa.tex.drive.sdk.acquisition.model.TripId
import axa.tex.drive.sdk.acquisition.score.ScoreRetriever
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

interface TripRecorder {

    /**
     * TripID of the currently recorded trip.
     * @return the trip id
     */
    fun getCurrentTripId(): TripId?

    /**
     * Initiates tracking
     */
    fun startTrip(startTime: Long) : TripId?

    /**
     * Stops the tracking
     */
    fun stopTrip(endTime: Long)

    /**
     * Says if we are currently recording
     * @return true or false
     */
    fun isRecording(): Boolean

    /**
     * @return An observable on which we can register to get location fixes.
     */
    fun locationObservable(): Observable<LocationFix>


    fun setCustomNotification(notification: Notification?)


    fun endedTripListener() : PublishSubject<String?>

    fun tripProgress() : PublishSubject<TripProgress>
}