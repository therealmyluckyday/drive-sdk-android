package axa.tex.drive.sdk.core.logger

import io.reactivex.subjects.PublishSubject

internal class LogImpl : Log {

    companion object {
        private val logSubject: PublishSubject<LogMessage> = PublishSubject.create()
    }

    internal var file: String? = null

    override fun print(description: String, type: LogType, function: String) {
        val className = if (file != null) file!!.splitToSequence(".").last() else "Unknown"
        val logDetail = LogMessage(description, type, className, function)
        logSubject.onNext(logDetail)
    }


    internal fun subjects(): PublishSubject<LogMessage> {
        return logSubject
    }
}