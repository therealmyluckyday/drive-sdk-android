package axa.tex.drive.sdk.acquisition.score.model

import org.junit.Assert
import org.junit.Test

class UrbanTest {
    @Test
    fun testUrbanInitialization() {
        val percentage: Int = 50
        val acceleration: Double = (2).toDouble()
        val braking: Double = (3).toDouble()
        val expert: Double = (20).toDouble()
        val smoothness: Double = (1).toDouble()
        val scores = Scores(acceleration,
                braking,
                expert,
                smoothness)

        val urban = Urban(percentage, scores)

        Assert.assertTrue(urban.scores == scores)
        Assert.assertTrue(urban.percentage == percentage)
    }

}