package integration.tex.com.automode.internal.states

import android.content.Context
import java.io.File
import java.io.FileOutputStream

internal interface AutomodeState {



    fun next()

    fun log(context : Context?, data  : String){
        try {
            val rootPath = context?.getExternalFilesDir("AUTOMODE")
            val root = File(rootPath?.toURI())
            if (!root.exists()) {
                root.mkdirs()
            }
            val f = File(rootPath?.path + "/log.txt")
            if (!f.exists()) {
                f.createNewFile()
            }
            val out = FileOutputStream(f, true)
            out.write(data.toByteArray())
            out.flush()
            out.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}