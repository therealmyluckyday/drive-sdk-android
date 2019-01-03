package axa.tex.drive.sdk.acquisition.score.model

import org.junit.Assert
import org.junit.Test

class ScoresDilTest {


    @Test
    fun testScoreDilInitilization() {
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
        val fog = Fog(percentage, scores)

        val motorway = Motorway(percentage, scores)
        val mountain = Mountain(percentage, scores)
        val night = Night(percentage, scores)
        val normal = Normal(percentage, scores)
        val rain = Rain(percentage, scores)
        val rural = Rural(percentage, scores)
        val snow = Snow(percentage, scores)
        val urban = Urban(percentage, scores)
        val eco_ness: Double = (2).toDouble()

        val jam: Double = (3).toDouble()

        val poi_dil: List<PoiDil> = listOf()

        val speed: Double = (5).toDouble()
        val weather_penalization: Int = 3


        val mean = Mean(acceleration, braking, expert, smoothness, speed, weight = 12)
        val contextualizedScores = ContextualizedScores(day, fog, motorway, mountain, night, normal, rain, rural, snow, urban)


        val scoresDil = ScoresDil(acceleration, braking, expert, smoothness, eco_ness, jam, mean, poi_dil, speed, weather_penalization, contextualizedScores)

        Assert.assertTrue(scoresDil.acceleration == acceleration)
        Assert.assertTrue(scoresDil.braking == braking)
        Assert.assertTrue(scoresDil.expert == expert)
        Assert.assertTrue(scoresDil.smoothness == smoothness)
        Assert.assertTrue(scoresDil.eco_ness == eco_ness)
        Assert.assertTrue(scoresDil.jam == jam)
        Assert.assertTrue(scoresDil.mean == mean)
        Assert.assertTrue(scoresDil.poi_dil == poi_dil)
        Assert.assertTrue(scoresDil.speed == speed)
        Assert.assertTrue(scoresDil.weather_penalization == weather_penalization)
        Assert.assertTrue(scoresDil.contextualized_scores == contextualizedScores)
    }
}