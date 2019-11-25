package axa.tex.drive.sdk.core.internal.utils

import android.content.Context
import axa.tex.drive.sdk.acquisition.model.TripId
import axa.tex.drive.sdk.automode.AutomodeHandler
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.logger.LoggerFactory
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import org.koin.android.ext.android.inject
import java.util.*

private const val TRIP_ID = "TRIP_ID"

class TripManager : KoinComponentCallbacks {
    internal val logger = LoggerFactory().getLogger(this::class.java.name).logger
    internal fun tripId(context: Context): TripId? {
        val prefs = context.getSharedPreferences(TRIP_ID, Context.MODE_PRIVATE)
        if (prefs.contains(TRIP_ID)) {
            val tripId = prefs.getString(TRIP_ID, "")
            val mapper = ObjectMapper()
            val node = mapper.readTree(tripId)
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            return mapper.readValue(node.get(TripId::class.java.simpleName).toString(), TripId::class.java)

        } else {
            val uuid = TripId(UUID.randomUUID().toString().toUpperCase(Locale.US))
            logger.info("${Date()} Generating new trip : ${uuid.value}", function = "fun tripId(context: Context): TripId?")

            prefs.edit().putString(TRIP_ID, uuid.toJson()).apply()
            return uuid
        }
    }

}