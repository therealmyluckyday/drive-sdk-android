package axa.tex.drive.sdk.acquisition.score.model


class PoiDil {

    var alerts: List<String>? = null
    var base_speed: Int = -1
    var distance: Double = (-1).toDouble()
    var intensity: String? = null
    var intensity_value: Int = -1
    var latitude: Double = (-1).toDouble()
    var longitude: Double = (-1).toDouble()
    var physical_value: Double = (-1).toDouble()
    var physical_value_unit: String? = null
    var road_flags: List<String>? = null
    var timestamp: Long = -1
    var type: String? = null
    var weather_code: String? = null

    constructor() {

    }

    constructor(alerts: List<String>, base_speed: Int,
                distance: Double,
                intensity: String,
                intensity_value: Int,
                latitude: Double,
                longitude: Double,
                physical_value: Double,
                physical_value_unit: String,
                road_flags: List<String>,
                timestamp: Long,
                type: String,
                weather_code: String) {
        this.alerts = alerts
        this.base_speed = base_speed
        this.distance = distance
        this.intensity = intensity
        this.intensity_value = intensity_value
        this.latitude = latitude
        this.longitude = longitude
        this.physical_value = physical_value
        this.physical_value_unit = physical_value_unit
        this.road_flags = road_flags
        this.timestamp = timestamp
        this.type = type
        this.weather_code = weather_code
    }
}