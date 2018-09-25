package axa.tex.drive.sdk.core

class TexService{



    companion object {
        private var instance: TexService? = null
        private var recorder: axa.tex.drive.sdk.acquisition.TripRecorder? = null
        private var config : TexConfig? = null

        fun configure(conf: TexConfig): TexService? {
            config = conf
            if (instance == null) {
                instance = TexService()
            }
            return instance
        }
    }


    fun getTripRecorder() : axa.tex.drive.sdk.acquisition.TripRecorder?{
        if (recorder == null) {
            recorder = config?.context?.let { axa.tex.drive.sdk.acquisition.TripRecorderImpl(it) }
        }
        return recorder
    }
}