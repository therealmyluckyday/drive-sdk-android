package axa.tex.drive.sdk.core.logger

class LoggerFactory {

    companion object {
        private val logger = Logger()
        fun getLogger(file: String): Logger {
            logger.setFile(file)
            return logger
        }

        fun getLogger(): Logger {
            return logger
        }
    }
}