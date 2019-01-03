package axa.tex.drive.sdk.acquisition.score.model

class TripInfo {

    var battery: Int = -1
    var daylight_ratio: Int = -1
    var duration: Int = -1
    var length: Double = (-1).toDouble()
    var road_categories: RoadCategories? = null
    var temperature: Int = -1
    var weather_categories: WeatherCategories? = null


    constructor() {

    }


    constructor(battery: Int,
                daylight_ratio: Int,
                duration: Int,
                length: Double,
                road_categories: RoadCategories,
                temperature: Int,
                weather_categories: WeatherCategories) {
        this.battery = battery
        this.daylight_ratio = daylight_ratio
        this.duration = duration
        this.length = length
        this.road_categories = road_categories
        this.temperature = temperature
        this.weather_categories = weather_categories
    }
}