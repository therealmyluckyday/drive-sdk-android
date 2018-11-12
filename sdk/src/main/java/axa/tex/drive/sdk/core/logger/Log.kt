package axa.tex.drive.sdk.core.logger

import axa.tex.drive.sdk.core.logger.LogType.INFO


internal interface Log {
    fun print(description: String, type : LogType = INFO, file: String="", function: String = "")

    fun warn(description: String, file: String = "", function: String = "")

    fun error(description: String, file: String = "", function: String = "")

    fun info(description: String, file: String = "", function: String = "")


}