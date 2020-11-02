package axa.tex.drive.sdk.acquisition.model

import axa.tex.drive.sdk.core.logger.LoggerFactory
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode


@JsonRootName(value = "")
data class Event(val event: List<String>,
                 val timestamp: Long) : Fix(timestamp) {
    override fun toJsonAPIV1(): String {
        return try {
            val mapper = ObjectMapper()
            val eventStr: String = mapper.writeValueAsString(this)
            return eventStr
        } catch (e: Exception) {
            LoggerFactory().getLogger(this::class.java.name).logger.error("Exception : $e", function = "toJsonAPIV2")
            return "{}"
        }
    }
    override fun toJsonAPIV2(): String {
        return try {
            val mapper = ObjectMapper()
            val eventsNode: ArrayNode = mapper.readTree("[]") as ArrayNode
            for (fix in event) {
                eventsNode.add(mapper.readTree(mapper.writeValueAsString(fix)))
            }
            val jsonNode = mapper.readTree("{}")
            var objectNode: ObjectNode = jsonNode as ObjectNode
            objectNode.set<ObjectNode>("events", eventsNode)
            objectNode.set<ObjectNode>("timestamp", mapper.readTree(mapper.writeValueAsString(timestamp)))
            return jsonNode.toString()
        } catch (e: Exception) {
            LoggerFactory().getLogger(this::class.java.name).logger.error("Exception : $e", function = "toJsonAPIV2")
            return "{}"
        }
    }
}
