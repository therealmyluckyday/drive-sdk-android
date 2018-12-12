package axa.tex.drive.sdk.acquisition.score.model

import org.junit.Assert
import org.junit.Test

class PoiDilTest {

    @Test
    fun testPoiDilInitialization() {
        val alerts: List<String> = listOf()
        val base_speed: Int = 10
        val distance: Double = (5).toDouble()
        val intensity: String = "intensity test"
        val intensity_value: Int = 0
        val latitude: Double = (7).toDouble()
        val longitude: Double = (2).toDouble()
        val physical_value: Double = (15).toDouble()
        val physical_value_unit: String = "physical value unit test"
        val road_flags: List<String> = listOf()
        val timestamp: Long = -1
        val type: String = "type test"
        val weather_code: String = "weather code test"

    val poiDil = PoiDil(alerts,base_speed,
                distance,
                intensity,
                intensity_value,
                latitude,
                longitude,
                physical_value,
                physical_value_unit,
                road_flags,
                timestamp,
                type,
                weather_code)

        Assert.assertTrue(poiDil.alerts == alerts)
        Assert.assertTrue(poiDil.base_speed == base_speed)
        Assert.assertTrue(poiDil.intensity_value == intensity_value)
        Assert.assertTrue(poiDil.latitude == latitude)
        Assert.assertTrue(poiDil.longitude == longitude)
        Assert.assertTrue(poiDil.physical_value == physical_value)
        Assert.assertTrue(poiDil.physical_value_unit == physical_value_unit)
        Assert.assertTrue(poiDil.road_flags == road_flags)
        Assert.assertTrue(poiDil.timestamp == timestamp)
        Assert.assertTrue(poiDil.type == type)
        Assert.assertTrue(poiDil.weather_code == weather_code)

}
}