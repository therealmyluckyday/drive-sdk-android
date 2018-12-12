package axa.tex.drive.sdk.acquisition.score.model

import org.junit.Assert
import org.junit.Test

class WeatherCategoriesTest {

    @Test
    fun testWeatherCategoriesInitialization(){
        var cloudy: Int = 2
        var foggy: Int = 3
        var heavy_ice: Int = 5
        var heavy_rain: Int = 7
        var heavy_snow: Int = 11
        var light_ice: Int = 13
        var normal: Int = 17
        var rainy: Int = 19
        var snowy: Int = 23
        var thundery: Int = 29
        var unknown: Int = 31


       val weatherCategory =  WeatherCategories(cloudy, foggy, heavy_ice, heavy_rain, heavy_snow, light_ice, normal, rainy, snowy, thundery, unknown)
        Assert.assertTrue(weatherCategory.cloudy == cloudy)
        Assert.assertTrue(weatherCategory.foggy == foggy)
        Assert.assertTrue(weatherCategory.heavy_ice == heavy_ice)
        Assert.assertTrue(weatherCategory.heavy_rain == heavy_rain)
        Assert.assertTrue(weatherCategory.heavy_snow == heavy_snow)
        Assert.assertTrue(weatherCategory.light_ice == light_ice)
        Assert.assertTrue(weatherCategory.normal == normal)
        Assert.assertTrue(weatherCategory.rainy == rainy)
        Assert.assertTrue(weatherCategory.snowy == snowy)
        Assert.assertTrue(weatherCategory.thundery == thundery)
        Assert.assertTrue(weatherCategory.unknown == unknown)
    }



}