package axa.tex.drive.sdk.core.internal.utils

import android.content.Context
import android.os.Build
import java.util.*

private const val UID_KEY = "UID_KEY"

class DeviceInfo {

    companion object {
        internal fun getDeviceName(): String {
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            return if (model.startsWith(manufacturer)) {
                model.capitalize()
            } else {
                manufacturer.capitalize() + " " + model
            }
        }

        internal fun getOSVersion(): String {
            return "Android " + Build.VERSION.RELEASE
        }

        internal fun getUid(context: Context): String {
            val prefs = ObscuredSharedPreferences(context,
                    context.getSharedPreferences(UID_KEY, Context.MODE_PRIVATE))
            if (prefs.contains(UID_KEY)) {
                val uId = prefs.getString(UID_KEY, "")
                prefs.edit().putString(UID_KEY, uId).apply() // put the same value but encrypted
                return uId!!
            } else {
                val uuid = UUID.randomUUID().toString()
                prefs.edit().putString(UID_KEY, uuid).apply()
                return uuid
            }
        }
    }
}