package axa.tex.drive.sdk.core

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message

import java.io.File
import java.io.FileWriter
import java.io.IOException

private fun getDefaultLooper(): Looper {
    val ht = HandlerThread("AndroidFileLogger")
    ht.start()
    return ht.looper
}

class DiskLogHandler(looper: Looper?, private val folder: String, private val fileName: String, private val maxFileSize: Int) : Handler(looper) {


    constructor(folder: String, fileName: String, maxFileSize: Int) : this(getDefaultLooper(), folder, fileName, maxFileSize) {}

    override fun handleMessage(msg: Message) {
        val content = msg.obj as String

        var fileWriter: FileWriter? = null
        val logFile = getLogFile(folder, fileName)

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
                } catch (e1: IOException) { /* fail silently */
                }

            }
        }

    }

    /**
     * This is always called on a single background thread.
     * Implementing classes must ONLY write to the fileWriter and nothing more.
     * The abstract class takes care of everything else including close the stream and catching IOException
     *
     * @param fileWriter an instance of FileWriter already initialised to the correct file
     */
    @Throws(IOException::class)
    private fun writeLog(fileWriter: FileWriter, content: String) {
        fileWriter.append(content)
    }

    private fun getLogFile(folderName: String, fileName: String): File {
        val folder = File(folderName)
        if (!folder.exists()) {
            //TODO: What if folder is not created, what happens then?
            folder.mkdirs()
        }

        var newFileCount = 0
        var newFile: File
        var existingFile: File? = null

        newFile = File(folder, String.format("%s_%s.csv", fileName, newFileCount))
        while (newFile.exists()) {
            existingFile = newFile
            newFileCount++
            newFile = File(folder, String.format("%s_%s.csv", fileName, newFileCount))
        }

        return if (existingFile != null) {
            if (existingFile.length() >= maxFileSize) {
                newFile
            } else existingFile
        } else newFile

    }
}