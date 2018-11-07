package axa.tex.drive.sdk.acquisition.model


import org.junit.Assert
import org.junit.Test
import java.util.*

class LoctionFixTest {

    @Test
    fun testLocationFixInitialization() {
        val latitude = 12.0
        val longitude = 2.0
        val precision = 14f
        val speed = 53f
        val bearing = 12f
        val altitude = 15.0
        val timestamp = Date().time

        val locationFix = LocationFix(12.0,2.0,14f,53f,12f,15.0,timestamp)


        Assert.assertTrue(locationFix.timestamp==timestamp)
        Assert.assertTrue(locationFix.latitude==latitude)
        Assert.assertTrue(locationFix.longitude==longitude)
        Assert.assertTrue(locationFix.precision==precision)
        Assert.assertTrue(locationFix.speed == speed)
        Assert.assertTrue(locationFix.bearing == bearing)
        Assert.assertTrue(locationFix.altitude == altitude)
    }
}