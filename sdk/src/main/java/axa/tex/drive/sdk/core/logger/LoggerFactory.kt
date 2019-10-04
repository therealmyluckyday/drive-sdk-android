package axa.tex.drive.sdk.core.logger

internal class LoggerFactory {
    internal val logger = Logger()
    fun getLogger(file: String): Logger {
        logger.setFile(file)
        return logger
    }

    fun getLogger(): Logger {
        return logger
    }
}