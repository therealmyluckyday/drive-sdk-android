package axa.tex.drive.sdk.acquisition.internal.tracker

import axa.tex.drive.sdk.acquisition.model.Fix
import io.reactivex.Observable

interface Tracker {

    /**
     * @return An observable for listening fixes.
     */
    fun provideFixProducer(): Observable<List<Fix>>

    /**
     * Enables tracking of a given tracker
     */
    fun enableTracking()

    /**
     * Disables tracking of a given tracker
     */
    fun disableTracking()

    /**
     * States if a tracker can be enable
     */
    fun isEnabled(): Boolean
}