package axa.tex.drive.sdk.core

import axa.tex.drive.sdk.acquisition.TripRecorder
import axa.tex.drive.sdk.acquisition.TripRecorderImpl
import axa.tex.drive.sdk.acquisition.score.ScoreRetriever
import axa.tex.drive.sdk.automode.AutomodeHandler
import axa.tex.drive.sdk.automode.internal.tracker.AutoModeTracker
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.logger.LogMessage
import axa.tex.drive.sdk.core.logger.LoggerFactory
import io.reactivex.subjects.PublishSubject
import org.koin.android.ext.android.inject

class TexService : KoinComponentCallbacks {
    companion object : KoinComponentCallbacks {
        internal var instance: TexService? = null
        private var config: TexConfig? = null

        fun configure(conf: TexConfig): TexService {
            config = conf
            val instanceToReturn = instance ?: TexService()
            instance = instanceToReturn
            if (instanceToReturn.getTripRecorder() is TripRecorderImpl) {
                val triprecorderImpl = instanceToReturn.getTripRecorder() as TripRecorderImpl
                triprecorderImpl.setConfig(TexConfig.config!!)
            }
            return instanceToReturn
        }
    }

    fun getTripRecorder(): TripRecorder {
        val tripRecorder: TripRecorder by inject()
        return tripRecorder
    }

    fun scoreRetriever(): ScoreRetriever {
        val scoreRetriever: ScoreRetriever by inject()
        return scoreRetriever
    }


    fun logStream(): PublishSubject<LogMessage> {
        val logger = LoggerFactory().logger
        return logger.getLogStream()
    }


    fun automodeHandler(): AutomodeHandler {
        val automodeHandler: AutomodeHandler by inject()
        return automodeHandler
    }

}