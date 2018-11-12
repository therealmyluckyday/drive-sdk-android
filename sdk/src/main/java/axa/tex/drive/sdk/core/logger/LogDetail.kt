package axa.tex.drive.sdk.core.logger
import android.util.Log
import axa.tex.drive.sdk.core.logger.LogType.WARNING
import axa.tex.drive.sdk.core.logger.LogType.ERROR
import axa.tex.drive.sdk.core.logger.LogType.INFO
import java.util.*

data class LogDetail(val description : String, val type: LogType, val file : String, val function:String){



    internal fun reportInStandardOutput(){
        val tag = "File $file  Function = $function"

        when(type){
            ERROR -> Log.e(tag, description)
            WARNING -> Log.w(tag, description)
            INFO -> Log.i(tag, description)
        }
    }

    internal fun reportInFile(){
        val tag = "File $file  Function = $function"
        val log = " $type ${Date().toString()} @ $tag  $description\n"
        LoggerUtils.logInFile("LOGS", "logs.txt", log)
    }
}

