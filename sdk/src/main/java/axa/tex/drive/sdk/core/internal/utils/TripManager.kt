package axa.tex.drive.sdk.core.internal.utils

import android.content.Context
import axa.tex.drive.sdk.acquisition.model.TripId
import axa.tex.drive.sdk.automode.AutomodeHandler
import axa.tex.drive.sdk.automode.internal.tracker.model.Message
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import org.koin.android.ext.android.inject
import java.util.*

private const val TRIP_ID = "TRIP_ID"

class TripManager : KoinComponentCallbacks{

    private val automodeHandler: AutomodeHandler by inject()
   // companion object {
        internal fun tripId(context: Context): TripId? {
            val prefs = context.getSharedPreferences(TRIP_ID, Context.MODE_PRIVATE)
            if (prefs.contains(TRIP_ID)) {
                val tripId = prefs.getString(TRIP_ID, "")
                val mapper = ObjectMapper()
                val node = mapper.readTree(tripId);
                mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                return mapper.readValue(node.get(TripId::class.java.simpleName).toString(), TripId::class.java)

            } else {
                val uuid = TripId(UUID.randomUUID().toString().toUpperCase(Locale.US))
                automodeHandler.messages.onNext(Message("${Date()} Generating new trip : ${uuid.value}"))
                prefs.edit().putString(TRIP_ID, uuid.toJson()).apply()
                return uuid
            }
        }

        internal fun removeTripId(context: Context) {
            val prefs = context.getSharedPreferences(TRIP_ID, Context.MODE_PRIVATE)
            if (prefs.contains(TRIP_ID)) {
                automodeHandler.messages.onNext(Message("${Date()} Removing trip id"))
                prefs.edit().clear().apply()
            }
        }
    //}
}