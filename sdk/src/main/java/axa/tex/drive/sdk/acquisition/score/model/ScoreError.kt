package axa.tex.drive.sdk.acquisition.score.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature


class ScoreError{

    var flags : List< String>? = null
    var score_type: String? = null
    var scoregw_version : String? = null
    var status : String? = null
    var trip_id : String? = null
    var status_details: List<String>? = null

    constructor()

    constructor(flags : List< String>,
                score_type: String,
                scoregw_version: String,
                status:String,
                trip_id : String,
                status_details : List<String>){
        this.flags = flags
        this.score_type = score_type
        this.scoregw_version  = scoregw_version
        this.status = status
        this.trip_id = trip_id
        this.status_details = status_details
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