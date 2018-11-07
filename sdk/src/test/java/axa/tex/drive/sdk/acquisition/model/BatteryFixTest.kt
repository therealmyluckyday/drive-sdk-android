package axa.tex.drive.sdk.acquisition.model


import org.junit.Assert
import org.junit.Test
import java.util.*

class BatteryFixTest {

    @Test
    fun testBatteryFixInitialization() {
        val level = 20
        val timestamp = Date().time

        val batteryFix = BatteryFix(level,BatteryState.plugged,timestamp)

        Assert.assertTrue(batteryFix.level==level)
        Assert.assertTrue(batteryFix.timestamp==timestamp)
        Assert.assertTrue(batteryFix.state==BatteryState.plugged)
    }
}