package axa.tex.drive.sdk.acquisition.model


import axa.tex.drive.sdk.core.internal.Constants
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode


class FixPacket(@JsonIgnore val fixes: List<Fix>,
                val model: String,
                val os: String,
                val timezone: String,
                val uid: String,
                val version: String, val trip_id: String?, val app_name: String) {

    fun toJson(): String {
        return try {
            val mapper = ObjectMapper()
            mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false)
            val jsonNode = mapper.readTree(mapper.writeValueAsString(this))
            val fixesNode: ArrayNode = mapper.readTree("[]") as ArrayNode
            for (fix in fixes) {
                fixesNode.add(mapper.readTree(fix.toJson()))
            }

            var objectNode: ObjectNode = jsonNode as ObjectNode
            objectNode.set<ObjectNode>("fixes", fixesNode)
            jsonNode.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            Constants.EMPTY_PACKET
        }
    }
}