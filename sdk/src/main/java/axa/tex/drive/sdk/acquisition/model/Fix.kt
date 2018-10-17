package axa.tex.drive.sdk.acquisition.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode


open class Fix {
    private val timestamp: Long

    constructor(timestamp: Long) {
        this.timestamp = timestamp
    }

    fun timestamp() : Long{
        return timestamp
    }



    open fun toJson() : String{
        return try {
            val mapper = ObjectMapper();
            mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
            val jsonStr : String = mapper.writeValueAsString(this);
            val jsonNode = mapper.readTree(jsonStr)
            (jsonNode as ObjectNode).put("timestamp",timestamp)
            jsonNode.toString()
        }catch (e : Exception){
            e.printStackTrace()
            "{}";
        }
    }
}