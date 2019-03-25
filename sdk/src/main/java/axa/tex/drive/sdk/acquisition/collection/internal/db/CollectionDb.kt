package axa.tex.drive.sdk.acquisition.collection.internal.db

import android.content.Context
import axa.tex.drive.sdk.acquisition.model.PendingTrip
import axa.tex.drive.sdk.core.Config
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper


const val PENDING_TRIP: String = "pendin-gtrips"
const val CONFIG: String = "config"
const val LAST_TRIP: String = "last-trip"
const val LAST_PACKET: String = "last-packet"

const val NUMBER_PACKETS: String = "number-packets"



internal class CollectionDb {

    private var context: Context?

    constructor(context: Context?) {
        this.context = context
    }


    internal fun saveTrip(pendingTrip: PendingTrip) {
        val prefs =
                context?.getSharedPreferences(PENDING_TRIP, Context.MODE_PRIVATE)
        prefs?.edit()?.putString(pendingTrip.id, pendingTrip.toJson())?.apply()
    }

    internal fun getPendingTrip(id: String): PendingTrip? {
        val prefs =
                context?.getSharedPreferences(PENDING_TRIP, Context.MODE_PRIVATE)

        val json = prefs?.getString(id, "")
        val mapper = ObjectMapper()
        if(json == null || json.isEmpty()){
            return null
        }
        val node = mapper.readTree(json);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper.readValue(node.get(PendingTrip::class.java.simpleName).toString(), PendingTrip::class.java)
    }

    internal fun deletePendingTrip(id: String) {
        val prefs =
                context?.getSharedPreferences(PENDING_TRIP, Context.MODE_PRIVATE)
        prefs?.edit()?.remove(id)?.apply()

    }


    internal fun getPendingTrips(): MutableMap<String, *>? {
        val prefs = context?.getSharedPreferences(PENDING_TRIP, Context.MODE_PRIVATE)
        return prefs?.all
    }

    internal fun setConfig(config: Config) {
        val prefs =
                context?.getSharedPreferences(PENDING_TRIP, Context.MODE_PRIVATE)
        prefs?.edit()?.putString(CONFIG, config.toJson())?.apply()
    }


    internal fun setLastTrip(trip: String) {
        val prefs =
                context?.getSharedPreferences(LAST_TRIP, Context.MODE_PRIVATE)
        prefs?.edit()?.putString(LAST_TRIP, trip)?.apply()
    }

    internal fun getLastTrip(): String {
        val prefs = context?.getSharedPreferences(LAST_TRIP, Context.MODE_PRIVATE)
        return prefs?.getString(LAST_TRIP, "")!!
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

    @Synchronized
    internal fun setPacketNumber(trip : String, packetNumber : Int){
        val prefs =
                context?.getSharedPreferences(LAST_PACKET, Context.MODE_PRIVATE)
        prefs?.edit()?.putInt(trip, packetNumber)?.apply()

    }

    @Synchronized
    internal fun getPacketNumber(trip : String) : Int{
        val prefs =
                context?.getSharedPreferences(LAST_PACKET, Context.MODE_PRIVATE)
        return prefs?.getInt(trip, -1)!!
    }


    @Synchronized
    internal fun setNumberPackets(trip : String, packetNumber : Int){
        val prefs =
                context?.getSharedPreferences(NUMBER_PACKETS, Context.MODE_PRIVATE)
        prefs?.edit()?.putInt(trip, packetNumber)?.apply()

    }

    @Synchronized
    internal fun getNumberPackets(trip : String) : Int{
        val prefs =
                context?.getSharedPreferences(NUMBER_PACKETS, Context.MODE_PRIVATE)
        return prefs?.getInt(trip, -1)!!
    }

    @Synchronized
    internal fun deleteTripNumberPackets(trip : String){
        val prefs =
                context?.getSharedPreferences(NUMBER_PACKETS, Context.MODE_PRIVATE)
        prefs?.edit()?.clear()?.apply()

    }
}