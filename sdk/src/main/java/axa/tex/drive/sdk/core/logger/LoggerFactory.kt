package axa.tex.drive.sdk.core.logger

import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks

internal class LoggerFactory {

   // companion object : KoinComponentCallbacks{
        internal val logger =  Logger()
        fun getLogger(file: String): Logger {
           // val logger =  Logger()
            logger.setFile(file)
            return logger
        }

        fun getLogger(): Logger {
            return logger
        }
   // }


}