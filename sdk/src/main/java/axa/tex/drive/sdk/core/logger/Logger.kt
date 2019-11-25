package axa.tex.drive.sdk.core.logger

import io.reactivex.subjects.PublishSubject


class Logger {

    internal val logger = LogImpl()

    internal fun getLogStream(): PublishSubject<LogMessage> {
        return logger.subjects()
    }

    fun setFile(file: String) {
        logger.file = file
    }

}