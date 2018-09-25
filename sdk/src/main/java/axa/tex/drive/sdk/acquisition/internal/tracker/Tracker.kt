package axa.tex.drive.sdk.acquisition.internal.tracker

interface Tracker {

    /**
     * @return An observable for listening fixes.
     */
    fun provideFixProducer(): Any

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