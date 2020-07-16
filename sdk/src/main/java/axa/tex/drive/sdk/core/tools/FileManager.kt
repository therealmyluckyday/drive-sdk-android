package axa.tex.drive.sdk.core.tools

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import axa.tex.drive.sdk.core.logger.LoggerFactory
import io.reactivex.subjects.PublishSubject
import java.io.*

final class FileManager {

    companion object {
        internal val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger

        fun loadFileMessage(messages: PublishSubject<String>, fileName:String, context: Context) {
            println("loadTrip")
            val logFile: File? = getLogFile(context, fileName)
            if (logFile !== null && logFile.exists()) {
                Thread {
                    var newTime = System.currentTimeMillis() - 86400000
                    try {
                        val bufferedReader = BufferedReader(FileReader(logFile))
                        var lineIndex = 0
                        var line = bufferedReader.readLine()
                        while (line != null) {
                            messages.onNext(line)
                            line = bufferedReader.readLine()
                            lineIndex++
                        }
                        bufferedReader.close()
                    } catch (e: IOException) {
                        LOGGER.warn("Exception : "+e, function = "enable")
                    }
                }.start()
            }
        }
        fun log(data: String, fileName:String, context: Context) {
            try {
                val rootPath = context.getExternalFilesDir("AUTOMODE")
                val root = File(rootPath!!.toURI())
                if (!root.exists()) {
                    root.mkdirs()
                }
                val f = File(rootPath.path + "/" + fileName)
                if (!f.exists()) {
                    try {
                        f.createNewFile()
                    } catch (e: IOException) {
                        LOGGER.warn("Exception : "+e, function = "enable")
                    }
                }
                try {
                    val out = FileOutputStream(f, true)
                    out.write(data.toByteArray())
                    out.flush()
                    out.close()
                } catch (e: FileNotFoundException) {
                    LOGGER.warn("Exception : "+e, function = "enable")
                } catch (e: IOException) {
                    LOGGER.warn("Exception : "+e, function = "enable")
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        fun getLogFile(context: Context, fileName: String): File? {
            var logDirectory = if (isExternalStorageWritable()) {
                context.getExternalFilesDir(null)
            } else {
                context.filesDir
            }

            return File(logDirectory!!.absolutePath + "/AUTOMODE", fileName)
        }

        fun getListFiles(parentDir: File): List<File> {
            var inFiles = ArrayList<File>()
            var files = parentDir.listFiles()
            for (file in files) {
                if (file.isDirectory()) {
                    inFiles.addAll(getListFiles(file));
                } else {
                    if(file.getName().endsWith(".csv")){
                        inFiles.add(file);
                    }
                }
            }
            return inFiles;
        }

        fun isExternalStorageWritable(): Boolean {
            val state = Environment.getExternalStorageState()
            return if (Environment.MEDIA_MOUNTED == state) {
                true
            } else false
        }

        fun getUriFromFilename(context: Context, packageName: String, filename: String): Uri {
            val rootPath = context.getExternalFilesDir("AUTOMODE")
            val root = File(rootPath!!.toURI())
            if (!root.exists()) {
                root.mkdirs()
            }
            val f = File(rootPath.path + "/" + filename)

            return FileProvider.getUriForFile(context, packageName, f)
        }

        private fun deleteLogsFile(context: Context, fileName:String) {
            val file: File = getLogFile(context, fileName)!!
            if (file.exists()) {
                file.delete()
            }
        }
    }
}