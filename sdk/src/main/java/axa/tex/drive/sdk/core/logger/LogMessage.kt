package axa.tex.drive.sdk.core.logger

data class LogMessage(val description: String, val type: LogType, val file: String?, val function: String, val threadName: String)

