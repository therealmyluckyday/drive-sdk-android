package axa.tex.drive.sdk.internal

import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream


internal fun String.compress(): ByteArray {
    val os = ByteArrayOutputStream(this.length)
    val gos = GZIPOutputStream(os)
    gos.write(this.toByteArray())
    gos.close()
    val compressed = os.toByteArray()
    os.close()
    return compressed
}
