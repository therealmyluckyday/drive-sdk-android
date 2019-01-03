package axa.tex.drive.sdk.core.logger

import io.reactivex.subjects.PublishSubject

class Logger {

    internal val logger = LogImpl()

    fun getLogStream(): PublishSubject<LogMessage> {
        return logger.subjects()
    }

    fun stopLogging() {
        logger.subjects().onComplete()
    }

    fun setFile(file: String) {
        logger.file = file
    }

}