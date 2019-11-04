package axa.tex.drive.sdk.acquisition.collection.internal.db

import android.content.Context
import axa.tex.drive.sdk.acquisition.model.PendingTrip
import axa.tex.drive.sdk.core.Config
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper


const val PENDING_TRIP: String = "pendin-gtrips"
const val CONFIG: String = "config"


internal class CollectionDb {

    private var context: Context?

    constructor(context: Context?) {
        this.context = context
    }

    internal fun setConfig(config: Config) {
        val prefs =
                context?.getSharedPreferences(PENDING_TRIP, Context.MODE_PRIVATE)
        prefs?.edit()?.putString(CONFIG, config.toJson())?.apply()
    }

    internal fun getConfig(): Config? {
        try {
            val prefs =
                    context?.getSharedPreferences(PENDING_TRIP, Context.MODE_PRIVATE)
            val json = prefs?.getString(CONFIG, "")
            val mapper = ObjectMapper()
            val node = mapper.readTree(json);
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            val conf = mapper.readValue(node.get(Config::class.java.simpleName).toString(), Config::class.java)
            return conf

        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

    }
}