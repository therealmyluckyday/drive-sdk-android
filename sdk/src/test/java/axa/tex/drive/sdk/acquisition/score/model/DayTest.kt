package axa.tex.drive.sdk.acquisition.score.model

import org.junit.Assert
import org.junit.Test

class DayTest {

    @Test
    fun testDayInitialization() {
        val percentage: Int = 50
        val acceleration: Double = (2).toDouble()
        val braking: Double = (3).toDouble()
        val expert: Double = (20).toDouble()
        val smoothness: Double = (1).toDouble()
        val scores = Scores(acceleration,
                braking,
                expert,
                smoothness)

        val day = Day(percentage, scores)

        Assert.assertTrue(day.scores == scores)
        Assert.assertTrue(day.percentage == percentage)
    }
}