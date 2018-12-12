package axa.tex.drive.sdk.acquisition.score.model

import org.junit.Assert
import org.junit.Test

class RainTest {

    @Test
    fun testRainInitialization() {
        val percentage: Int = 50
        val acceleration: Double = (2).toDouble()
        val braking: Double = (3).toDouble()
        val expert: Double = (20).toDouble()
        val smoothness: Double = (1).toDouble()
        val scores = Scores(acceleration,
                braking,
                expert,
                smoothness)

        val rain = Rain(percentage, scores)

        Assert.assertTrue(rain.scores == scores)
        Assert.assertTrue(rain.percentage == percentage)
    }
}