package axa.tex.drive.sdk.core

import axa.tex.drive.sdk.acquisition.TripRecorder
import axa.tex.drive.sdk.acquisition.TripRecorderImpl
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import org.koin.android.ext.android.inject

class TexService : KoinComponentCallbacks{

    companion object : KoinComponentCallbacks {

        private var instance: TexService? = null
        private var recorder: TripRecorder? = null
        private var config: TexConfig? = null

        fun configure(conf: TexConfig): TexService? {
            config = conf
            if (instance == null) {
                instance = TexService()
            }
            return instance
        }


    }


    fun getTripRecorder(): TripRecorder? {
        val tripRecorder: TripRecorder by inject()

        return tripRecorder
    }
}