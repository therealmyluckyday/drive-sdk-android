package axa.tex.drive.sdk.acquisition.score.model

import axa.tex.drive.sdk.acquisition.model.BatteryFix
import axa.tex.drive.sdk.acquisition.model.BatteryState
import org.junit.Assert
import org.junit.Test
import java.util.*

class ScoresTest {

    @Test
    fun testScoresInitialization() {
        val acceleration: Double = (2).toDouble()
        val braking: Double = (3).toDouble()
        val expert: Double = (20).toDouble()
        val smoothness: Double = (1).toDouble()




        val score = Scores(acceleration,
                braking,
                expert,
                smoothness)

        Assert.assertTrue(score.acceleration==acceleration)
        Assert.assertTrue(score.braking==braking)
        Assert.assertTrue(score.expert==expert)
        Assert.assertTrue(score.smoothness==smoothness)
    }
}