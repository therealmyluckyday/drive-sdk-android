package axa.tex.drive.sdk.acquisition.score.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature


class ScoreError {

    var flags: List<String>? = null
    var score_type: String? = null
    var scoregw_version: String? = null
    var status: String? = null
    var trip_id: String? = null
    var status_details: List<String>? = null


    internal fun toJson(): String {
        return try {
            val mapper = ObjectMapper()
            mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true)
            mapper.writeValueAsString(this)
        } catch (e: Exception) {
            e.printStackTrace()
            "{}"
        }

    }

}