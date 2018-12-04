package axa.tex.drive.sdk.acquisition.score
import com.fasterxml.jackson.annotation.JsonProperty


data class Test(
    @JsonProperty("alerts")
    val alerts: List<Any>,
    @JsonProperty("end_time")
    val endTime: Long,
    @JsonProperty("flags")
    val flags: List<Any>,
    @JsonProperty("score_type")
    val scoreType: String,
    @JsonProperty("scoregw_version")
    val scoregwVersion: String,
    @JsonProperty("scores_dil")
    val scoresDil: ScoresDil,
    @JsonProperty("scoring_version")
    val scoringVersion: String,
    @JsonProperty("start_time")
    val startTime: Long,
    @JsonProperty("status")
    val status: String,
    @JsonProperty("tags")
    val tags: List<String>,
    @JsonProperty("timezone")
    val timezone: String,
    @JsonProperty("trip_id")
    val tripId: String,
    @JsonProperty("trip_info")
    val tripInfo: TripInfo,
    @JsonProperty("uid")
    val uid: String
)

data class ScoresDil(
    @JsonProperty("acceleration")
    val acceleration: Double,
    @JsonProperty("braking")
    val braking: Double,
    @JsonProperty("contextualized_scores")
    val contextualizedScores: ContextualizedScores,
    @JsonProperty("eco_ness")
    val ecoNess: Double,
    @JsonProperty("expert")
    val expert: Double,
    @JsonProperty("jam")
    val jam: Double,
    @JsonProperty("mean")
    val mean: Mean,
    @JsonProperty("poi_dil")
    val poiDil: List<PoiDil>,
    @JsonProperty("smoothness")
    val smoothness: Double,
    @JsonProperty("speed")
    val speed: Double,
    @JsonProperty("weather_penalization")
    val weatherPenalization: Int
)

data class Mean(
    @JsonProperty("acceleration")
    val acceleration: Double,
    @JsonProperty("braking")
    val braking: Double,
    @JsonProperty("expert")
    val expert: Double,
    @JsonProperty("smoothness")
    val smoothness: Double,
    @JsonProperty("speed")
    val speed: Double,
    @JsonProperty("weight")
    val weight: Int
)

data class PoiDil(
    @JsonProperty("alerts")
    val alerts: List<String>,
    @JsonProperty("base_speed")
    val baseSpeed: Int,
    @JsonProperty("distance")
    val distance: Double,
    @JsonProperty("intensity")
    val intensity: String,
    @JsonProperty("intensity_value")
    val intensityValue: Int,
    @JsonProperty("latitude")
    val latitude: Double,
    @JsonProperty("longitude")
    val longitude: Double,
    @JsonProperty("physical_value")
    val physicalValue: Double,
    @JsonProperty("physical_value_unit")
    val physicalValueUnit: String,
    @JsonProperty("road_flags")
    val roadFlags: List<String>,
    @JsonProperty("timestamp")
    val timestamp: Long,
    @JsonProperty("type")
    val type: String,
    @JsonProperty("weather_code")
    val weatherCode: String
)

data class ContextualizedScores(
    @JsonProperty("day")
    val day: Day,
    @JsonProperty("fog")
    val fog: Fog,
    @JsonProperty("motorway")
    val motorway: Motorway,
    @JsonProperty("mountain")
    val mountain: Mountain,
    @JsonProperty("night")
    val night: Night,
    @JsonProperty("normal")
    val normal: Normal,
    @JsonProperty("rain")
    val rain: Rain,
    @JsonProperty("rural")
    val rural: Rural,
    @JsonProperty("snow")
    val snow: Snow,
    @JsonProperty("urban")
    val urban: Urban
)

data class Normal(
    @JsonProperty("percentage")
    val percentage: Int,
    @JsonProperty("scores")
    val scores: Scores
)

data class Scores(
    @JsonProperty("acceleration")
    val acceleration: Double,
    @JsonProperty("braking")
    val braking: Double,
    @JsonProperty("expert")
    val expert: Double,
    @JsonProperty("smoothness")
    val smoothness: Double,
    @JsonProperty("speed")
    val speed: Double
)

data class Night(
    @JsonProperty("percentage")
    val percentage: Int,
    @JsonProperty("scores")
    val scores: Scores
)



data class Day(
    @JsonProperty("percentage")
    val percentage: Int,
    @JsonProperty("scores")
    val scores: Scores
)

data class Rural(
    @JsonProperty("percentage")
    val percentage: Double,
    @JsonProperty("scores")
    val scores: Scores
)


data class Mountain(
    @JsonProperty("percentage")
    val percentage: Int,
    @JsonProperty("scores")
    val scores: Scores
)

data class Motorway(
    @JsonProperty("percentage")
    val percentage: Int,
    @JsonProperty("scores")
    val scores: Scores
)

data class Urban(
    @JsonProperty("percentage")
    val percentage: Double,
    @JsonProperty("scores")
    val scores: Scores
)

data class Snow(
    @JsonProperty("percentage")
    val percentage: Int,
    @JsonProperty("scores")
    val scores: Scores
)

data class Fog(
    @JsonProperty("percentage")
    val percentage: Int,
    @JsonProperty("scores")
    val scores: Scores
)

data class Rain(
    @JsonProperty("percentage")
    val percentage: Int,
    @JsonProperty("scores")
    val scores: Scores
)

data class TripInfo(
    @JsonProperty("battery")
    val battery: Int,
    @JsonProperty("daylight_ratio")
    val daylightRatio: Int,
    @JsonProperty("duration")
    val duration: Int,
    @JsonProperty("length")
    val length: Double,
    @JsonProperty("road_categories")
    val roadCategories: RoadCategories,
    @JsonProperty("temperature")
    val temperature: Int,
    @JsonProperty("weather_categories")
    val weatherCategories: WeatherCategories
)

data class WeatherCategories(
    @JsonProperty("cloudy")
    val cloudy: Int,
    @JsonProperty("foggy")
    val foggy: Int,
    @JsonProperty("heavy_ice")
    val heavyIce: Int,
    @JsonProperty("heavy_rain")
    val heavyRain: Int,
    @JsonProperty("heavy_snow")
    val heavySnow: Int,
    @JsonProperty("light_ice")
    val lightIce: Int,
    @JsonProperty("normal")
    val normal: Int,
    @JsonProperty("rainy")
    val rainy: Int,
    @JsonProperty("snowy")
    val snowy: Int,
    @JsonProperty("thundery")
    val thundery: Int,
    @JsonProperty("unknown")
    val unknown: Int
)

data class RoadCategories(
    @JsonProperty("motorway")
    val motorway: Int,
    @JsonProperty("mountain")
    val mountain: Int,
    @JsonProperty("rural")
    val rural: Double,
    @JsonProperty("urban")
    val urban: Double
)