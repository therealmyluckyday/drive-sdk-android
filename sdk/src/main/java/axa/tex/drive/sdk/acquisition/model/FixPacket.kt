package axa.tex.drive.sdk.acquisition.model


import axa.tex.drive.sdk.core.internal.Constants
import axa.tex.drive.sdk.core.logger.LoggerFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode

class FixPacket(val fixes: List<Fix>,
                val model: String,
                val os: String,
                val timezone: String,
                val uid: String,
                val version: String,
                val trip_id: String?,
                val app_name: String,
                val client_id: String,
               ) {

    fun toJson(isAPIV2: Boolean): String {
        if (isAPIV2) {
            return toJsonAPIV2()
        }
        return  toJsonAPIV1()
    }
    fun toJsonAPIV1(): String {
        return try {
            val currentFixes = fixes.toList()
            val mapper = ObjectMapper()

            val jsonNode = mapper.readTree(mapper.writeValueAsString(this))
            val fixesNode: ArrayNode = mapper.readTree("[]") as ArrayNode
            for (fix in currentFixes) {
                fixesNode.add(mapper.readTree(fix.toJsonAPIV1()))
            }
            var objectNode: ObjectNode = jsonNode as ObjectNode
            objectNode.set<ObjectNode>("fixes", fixesNode)
            jsonNode.toString()
        } catch (e: Exception) {
            LoggerFactory().getLogger(this::class.java.name).logger.error("Exception : $e", function = "toJsonAPIV1")
            Constants.EMPTY_PACKET
        }
    }
    fun toJsonAPIV2(): String {
        return try {
            val currentFixes = fixes.toList()
            val mapper = ObjectMapper()
            mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false)
            val jsonNode = mapper.readTree("{}")
            val fixesNode: ArrayNode = mapper.readTree("[]") as ArrayNode
            for (fix in currentFixes) {
                fixesNode.add(mapper.readTree(fix.toJsonAPIV2()))
            }
            mapper.writerFor(this.javaClass).withAttribute("toto", "tata")
            var objectNode: ObjectNode = jsonNode as ObjectNode
            objectNode.set<ObjectNode>("fixes", fixesNode)
            objectNode.set<ObjectNode>("os", mapper.readTree(mapper.writeValueAsString(os)))
            objectNode.set<ObjectNode>("device_id", mapper.readTree(mapper.writeValueAsString(uid)))
            objectNode.set<ObjectNode>("trip_id", mapper.readTree(mapper.writeValueAsString(trip_id)))
            objectNode.set<ObjectNode>("client_id", mapper.readTree(mapper.writeValueAsString(client_id)))
            objectNode.set<ObjectNode>("device_info", mapper.readTree(mapper.writeValueAsString("$model/$os/$version")))
            jsonNode.toString()
        } catch (e: Exception) {
            LoggerFactory().getLogger(this::class.java.name).logger.error("Exception : $e", function = "toJsonAPIV2")
            Constants.EMPTY_PACKET
        }
    }
}