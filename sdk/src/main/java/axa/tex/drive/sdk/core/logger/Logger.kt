package axa.tex.drive.sdk.core.logger

import io.reactivex.subjects.PublishSubject

class Logger {

    internal val logger = LogImpl()

    fun getLogStream() : PublishSubject<LogDetail>{
        return logger.subjects()
    }

    fun stopLogging(){
        logger.subjects().onComplete()
    }
}