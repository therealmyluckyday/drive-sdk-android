package axa.tex.drive.sdk.acquisition.collection.internal.db

import android.content.Context
import axa.tex.drive.sdk.acquisition.model.PendingTrip
import axa.tex.drive.sdk.core.Config
import axa.tex.drive.sdk.internal.extension.toJson
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper


const val  PENDING_TRIP: String = "pendin-gtrips"
const val  CONFIG: String = "config"

internal class CollectionDb {

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

        internal fun setConfig(context: Context?,config : Config){
            val prefs =
                    context?.getSharedPreferences(PENDING_TRIP, Context.MODE_PRIVATE)
            prefs?.edit()?.putString(CONFIG, config.toJson())?.apply()
        }



        internal fun getConfig(context: Context?):Config?{
            try {
                val prefs =
                        context?.getSharedPreferences(PENDING_TRIP, Context.MODE_PRIVATE)
                val json = prefs?.getString(CONFIG,"")
                val mapper = ObjectMapper()
                val node = mapper.readTree(json);
                mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                val conf = mapper.readValue(node.get(Config::class.java.simpleName).toString(), Config::class.java)
                return conf

            }catch (e : Exception){
                return null
            }

        }
    }
}