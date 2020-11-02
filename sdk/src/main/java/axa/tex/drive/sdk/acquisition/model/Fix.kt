package axa.tex.drive.sdk.acquisition.model

import axa.tex.drive.sdk.core.logger.LoggerFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ObjectNode


open class Fix {
    private val timestamp: Long

    constructor(timestamp: Long) {
        this.timestamp = timestamp
    }

    fun timestamp(): Long {
        return timestamp
    }

    open fun toJson(isAPIV2: Boolean): String {
       if (isAPIV2) {
           return toJsonAPIV2()
       }
        return toJsonAPIV1()
    }

    open fun toJsonAPIV1(): String {
        return try {
            val mapper = ObjectMapper()
            mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true)
            val jsonStr: String = mapper.writeValueAsString(this)
            val jsonNode = mapper.readTree(jsonStr)
            (jsonNode as ObjectNode).put("timestamp", timestamp)
            return jsonNode.toString()
        } catch (e: Exception) {
            LoggerFactory().getLogger(this::class.java.name).logger.error("Exception : $e", function = "toJsonAPIV1")
            return "{}"
        }
    }

    open fun toJsonAPIV2(): String {
        return try {
            val mapper = ObjectMapper()
            val jsonStr: String = mapper.writeValueAsString(this)
            val jsonNode = mapper.readTree(jsonStr)
            (jsonNode as ObjectNode).put("timestamp", timestamp)
            return jsonNode.toString()
        } catch (e: Exception) {
            LoggerFactory().getLogger(this::class.java.name).logger.error("Exception : $e", function = "toJsonAPIV2")
            return "{}"
        }
    }
}