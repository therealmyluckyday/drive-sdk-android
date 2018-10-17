package axa.tex.drive.sdk.acquisition.db

import android.content.Context
import axa.tex.drive.sdk.acquisition.model.PendingTrip
import axa.tex.drive.sdk.internal.extension.toJson


const val  PENDING_TRIP: String = "pendin-gtrips"

class PendingTripDao {

    companion object {
        internal fun saveTrip(context: Context,pendingTrip : PendingTrip) {
            val prefs =
                    context.getSharedPreferences(PENDING_TRIP, Context.MODE_PRIVATE)
            prefs.edit().putString(pendingTrip.id, pendingTrip.toJson()).apply()
        }

        internal fun getPendingTrip(context: Context,id : String) {
            val prefs =
                    context.getSharedPreferences(PENDING_TRIP, Context.MODE_PRIVATE)
            prefs.getString(id, "");
        }

        internal fun deletePendingTrip(context: Context,id : String) {
            val prefs =
                    context.getSharedPreferences(PENDING_TRIP, Context.MODE_PRIVATE)
            prefs.edit().remove(id).apply()

        }


        internal fun getPendingTrips(context: Context) : Map<String, *>{
            val prefs = context.getSharedPreferences(PENDING_TRIP, Context.MODE_PRIVATE)
            return  prefs.all
        }
    }
}