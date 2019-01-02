package axa.tex.drive.sdk.core.logger

import axa.tex.drive.sdk.core.logger.LogType.*


internal interface Log {
    fun print(description: String, type : LogType = INFO, file: String="", function: String = "")

    fun warn(description: String, file: String = "", function: String = ""){
        print(description, WARNING, file, function)
    }

    fun error(description: String, file: String = "", function: String = ""){
        print(description, ERROR, file, function)
    }

    fun info(description: String, file: String = "", function: String = ""){
        print(description, INFO, file, function)
    }


}