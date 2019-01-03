package axa.tex.drive.sdk.acquisition.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature


class PendingTrip {


    var id: String = ""
    var tripId: String? = null
    var containsStop: Boolean = false

    constructor() {

    }


    constructor(id: String, tripId: String?, containsStop: Boolean) {
        this.tripId = tripId
        this.id = id;
        this.containsStop = containsStop
    }

    internal fun toJson(): String {
        return try {
            val mapper = ObjectMapper();
            mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
            mapper.writeValueAsString(this);
        } catch (e: Exception) {
            e.printStackTrace()
            "{}";
        }

    }
}