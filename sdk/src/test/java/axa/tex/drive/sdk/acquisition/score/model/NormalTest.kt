package axa.tex.drive.sdk.acquisition.score.model

import org.junit.Assert
import org.junit.Test

class NormalTest {
    @Test
    fun testNightInitialization() {
        val percentage: Int = 50
        val acceleration: Double = (2).toDouble()
        val braking: Double = (3).toDouble()
        val expert: Double = (20).toDouble()
        val smoothness: Double = (1).toDouble()
        val scores = Scores(acceleration,
                braking,
                expert,
                smoothness)

        val normal = Normal(percentage, scores)

        Assert.assertTrue(normal.scores == scores)
        Assert.assertTrue(normal.percentage == percentage)
    }
}