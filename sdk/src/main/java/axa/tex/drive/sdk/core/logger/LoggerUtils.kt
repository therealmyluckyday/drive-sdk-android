package axa.tex.drive.sdk.core.logger

import android.os.Environment
import java.io.File
import java.io.FileWriter
import java.io.IOException


internal class LoggerUtils {

    companion object {
        fun logInFile(folder: String, fileName: String, content: String) {

            var fileWriter: FileWriter? = null
            val logFile = getLogFile(folder, fileName)
            if (!logFile.exists()) {
                logFile.createNewFile()
            }
            try {
                fileWriter = FileWriter(logFile, true)

                writeLog(fileWriter, content)

                fileWriter.flush()
                fileWriter.close()
            } catch (e: IOException) {
                if (fileWriter != null) {
                    try {
                        fileWriter.flush()
                        fileWriter.close()
                    } catch (e1: IOException) {
                        print(e1.printStackTrace())
                    }
                }
            }

        }


        @Throws(IOException::class)
        private fun writeLog(fileWriter: FileWriter, content: String) {
            fileWriter.append(content)
        }

        private fun getLogFile(folderName: String, fileName: String): File {

            val storage = Environment.getDownloadCacheDirectory().absolutePath

            val folder = File(storage, folderName)
            if (!folder.exists()) {
                folder.mkdirs()
            }

            return File(folder, fileName)

        }
    }
}