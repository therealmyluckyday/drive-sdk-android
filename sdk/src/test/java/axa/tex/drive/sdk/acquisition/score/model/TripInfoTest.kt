package axa.tex.drive.sdk.acquisition.score.model

import org.junit.Assert
import org.junit.Test

class TripInfoTest {

    @Test
    fun testTripInfoTestInitialization(){
        var battery: Int = 2
        var daylight_ratio: Int = 3
        var duration: Int = 5
        var length: Double = (9).toDouble()
        val road_categories: RoadCategories = RoadCategories()
        val temperature: Int = 7
        val weather_categories =  WeatherCategories()

        val tripInfo = TripInfo(battery, daylight_ratio, duration, length, road_categories, temperature, weather_categories)
        Assert.assertTrue(tripInfo.battery == battery)
        Assert.assertTrue(tripInfo.daylight_ratio == daylight_ratio)
        Assert.assertTrue(tripInfo.length == length)
        Assert.assertTrue(tripInfo.road_categories == road_categories)
        Assert.assertTrue(tripInfo.temperature == temperature)
        Assert.assertTrue(tripInfo.weather_categories == weather_categories)
    }

}