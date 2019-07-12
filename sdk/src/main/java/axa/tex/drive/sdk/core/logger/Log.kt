package axa.tex.drive.sdk.core.logger

import axa.tex.drive.sdk.core.logger.LogType.*


internal interface Log {
    fun print(description: String, type: LogType = INFO, function: String = "")

    fun warn(description: String, function: String = "") {
        print(description, WARNING, function)
    }

    fun error(description: String, function: String = "") {
        print(description, ERROR, function)
    }

    fun info(description: String, function: String = "") {
        print(description, INFO, function)
    }
}