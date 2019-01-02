package axa.tex.drive.sdk.core.logger

import io.reactivex.subjects.PublishSubject
import axa.tex.drive.sdk.core.logger.LogType.WARNING
import axa.tex.drive.sdk.core.logger.LogType.ERROR

internal class LogImpl : Log{

    private val logSubject : PublishSubject<LogMessage> = PublishSubject.create()

    override fun print(description: String, type: LogType, file: String, function: String) {
        val logDetail = LogMessage(description,type,file,function)
        logDetail.reportInStandardOutput()
        logDetail.reportInFile()
        logSubject.onNext(logDetail)
    }

   /* override fun warn(description: String, file: String, function: String) {
        print(description, WARNING, file, function)
    }

    override fun error(description: String, file: String, function: String) {
        print(description, ERROR, file, function)
    }

    override fun info(description: String, file: String, function: String) {
        print(description=description, file=file, function=function)
    }*/

    internal fun subjects() : PublishSubject<LogMessage>{
        return logSubject
    }
}