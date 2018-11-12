package axa.tex.drive.sdk.core.logger

class LoggerFactory {

    companion object {
        private val logger =  Logger()
        fun getLogger() : Logger {
          return logger
        }
    }
}