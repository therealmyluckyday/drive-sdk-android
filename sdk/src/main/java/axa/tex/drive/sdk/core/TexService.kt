package axa.tex.drive.sdk.core

import android.content.ComponentCallbacks
import android.content.res.Configuration
import axa.tex.drive.sdk.acquisition.TripRecorder
import axa.tex.drive.sdk.acquisition.TripRecorderImpl
import org.koin.android.ext.android.inject

class TexService : ComponentCallbacks {
    override fun onLowMemory() {

    }

    override fun onConfigurationChanged(newConfig: Configuration?) {

    }


    companion object : ComponentCallbacks {
        override fun onLowMemory() {

        }

        override fun onConfigurationChanged(newConfig: Configuration?) {
        }

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