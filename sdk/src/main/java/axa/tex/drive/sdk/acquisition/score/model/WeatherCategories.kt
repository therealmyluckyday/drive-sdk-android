package axa.tex.drive.sdk.acquisition.score.model

class  WeatherCategories{

     var cloudy: Int = -1
    var foggy: Int = -1
    var heavy_ice: Int = -1
    var heavy_rain: Int = -1
    var heavy_snow: Int = -1
    var light_ice: Int = -1
    var normal: Int = -1
    var rainy: Int = -1
    var snowy: Int = -1
    var thundery: Int = -1
    var unknown: Int = -1

    constructor(){

    }

    constructor(cloudy: Int,
                foggy: Int,
                heavy_ice: Int,
                heavy_rain: Int,
                heavy_snow: Int,
                light_ice: Int,
                normal: Int,
                rainy: Int,
                snowy: Int,
                thundery: Int,
                unknown: Int) {
        this.cloudy = cloudy
        this.foggy = foggy
        this.heavy_ice = heavy_ice
        this.heavy_rain = heavy_rain
        this.heavy_snow = heavy_snow
        this.light_ice = light_ice
        this.normal = normal
        this.rainy = rainy
        this.snowy = snowy
        this.thundery = thundery
        this.unknown = unknown
    }
}