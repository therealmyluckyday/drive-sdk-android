package axa.tex.drive.sdk.internal.extension

import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream


internal fun String.capitalize(): String {
    if (this.isNullOrEmpty()) {
        return ""
    }
    val first = this[0]
    return if (Character.isUpperCase(first)) {
        this
    } else {
        Character.toUpperCase(first) + this.substring(1)
    }
}


internal fun String.compress(): ByteArray {
    val os = ByteArrayOutputStream(this.length)
    val gos = GZIPOutputStream(os)
    gos.write(this.toByteArray())
    gos.close()
    val compressed = os.toByteArray()
    os.close()
    return compressed
}
