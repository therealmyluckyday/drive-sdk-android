package axa.tex.drive.sdk.acquisition.score.model

import org.junit.Assert
import org.junit.Test

class MeanTest {


    @Test
    fun testMeanInitialization() {
        val acceleration: Double = (2).toDouble()
        val braking: Double = (3).toDouble()
        val expert: Double = (20).toDouble()
        val smoothness: Double = (1).toDouble()
        var speed: Double = (20).toDouble()
        var weight = 23


        val mean = Mean(acceleration, braking, expert, smoothness, speed, weight)

        Assert.assertTrue(mean.acceleration == acceleration)
        Assert.assertTrue(mean.braking == braking)
        Assert.assertTrue(mean.expert == expert)
        Assert.assertTrue(mean.smoothness == smoothness)
        Assert.assertTrue(mean.speed == speed)
        Assert.assertTrue(mean.weight == weight)
    }
}