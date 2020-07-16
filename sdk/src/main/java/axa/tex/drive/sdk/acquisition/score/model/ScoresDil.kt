package axa.tex.drive.sdk.acquisition.score.model

import axa.tex.drive.sdk.core.logger.LoggerFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature

class ScoresDil {

    internal val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
    internal fun toJson(): String {
        return try {
            val mapper = ObjectMapper()
            mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true)
            mapper.writeValueAsString(this)
        } catch (e: Exception) {
            LOGGER.warn("Exception : "+e, function = "enable")
            "{}"
        }

    }

    var acceleration: Double = (-1).toDouble()
    var braking: Double = (-1).toDouble()
    var expert: Double = (-1).toDouble()
    var smoothness: Double = (-1).toDouble()
    var contextualized_scores: ContextualizedScores? = null
    var eco_ness: Double = (-1).toDouble()

    var jam: Double = (-1).toDouble()
    var mean: Mean? = null
    var poi_dil: List<PoiDil>? = null

    var speed: Double = (-1).toDouble()
    var weather_penalization: Int = -1


    constructor(acceleration: Double,
                braking: Double,
                expert: Double,
                smoothness: Double,
                eco_ness: Double,
                jam: Double,
                mean: Mean?,
                poi_dil: List<PoiDil>?,
                speed: Double,
                weather_penalization: Int,
                contextualized_scores: ContextualizedScores?) {
        this.acceleration = acceleration
        this.braking = braking
        this.expert = expert
        this.smoothness = smoothness
        this.contextualized_scores = contextualized_scores
        this.eco_ness = eco_ness
        this.jam = jam
        this.mean = mean
        this.poi_dil = poi_dil
        this.speed = speed
        this.weather_penalization = weather_penalization
    }

}