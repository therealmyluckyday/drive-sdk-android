package axa.tex.drive.sdk.core.internal.utils

import java.text.SimpleDateFormat
import java.util.*


class Utils {
    companion object {

        fun getFormattedTZ(): String {
            val sdf = SimpleDateFormat("Z")
            sdf.timeZone = TimeZone.getDefault()
            return sdf.format(Date())
        }
    }
}