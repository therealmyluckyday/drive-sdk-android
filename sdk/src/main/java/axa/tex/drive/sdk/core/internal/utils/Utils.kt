package axa.tex.drive.sdk.core.internal.utils

import android.content.Context
import android.location.Location
import android.os.Build
import axa.tex.drive.sdk.acquisition.internal.tracker.fake.model.FakeLocation
import axa.tex.drive.sdk.acquisition.model.LocationFix
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.GZIPOutputStream


private const val UID_KEY = "UID_KEY"
private const val TRIP_ID = "TRIP_ID"

class Utils {
    companion object {


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

        internal fun tripId(context: Context): String {
            val prefs = context.getSharedPreferences(TRIP_ID, Context.MODE_PRIVATE)
            if (prefs.contains(TRIP_ID)) {
                val uId = prefs.getString(TRIP_ID, "")
                return uId!!
            } else {
                val uuid = UUID.randomUUID().toString()
                prefs.edit().putString(TRIP_ID, uuid).apply()
                return uuid
            }
        }

        internal fun removeTripId(context: Context) {
            val prefs = context.getSharedPreferences(TRIP_ID, Context.MODE_PRIVATE)
            if (prefs.contains(TRIP_ID)) {
                prefs.edit().clear().apply()
            }
        }

        @Throws(IOException::class)
        internal fun compress(string: String): ByteArray {
            val os = ByteArrayOutputStream(string.length)
            val gos = GZIPOutputStream(os)
            gos.write(string.toByteArray())
            gos.close()
            val compressed = os.toByteArray()
            os.close()
            return compressed
        }


        fun getFormattedTZ(): String {
            val sdf = SimpleDateFormat("Z")
            sdf.timeZone = TimeZone.getDefault()
            return sdf.format(Date())
        }

        fun getOSVersion(): String {
            return "Android " + Build.VERSION.RELEASE
        }

        fun capitalize(s: String?): String {
            if (s == null || s.length == 0) {
                return ""
            }
            val first = s[0]
            return if (Character.isUpperCase(first)) {
                s
            } else {
                Character.toUpperCase(first) + s.substring(1)
            }
        }

        fun getDeviceName(): String {
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            return if (model.startsWith(manufacturer)) {
                capitalize(model)
            } else {
                capitalize(manufacturer) + " " + model
            }
        }
    }
}