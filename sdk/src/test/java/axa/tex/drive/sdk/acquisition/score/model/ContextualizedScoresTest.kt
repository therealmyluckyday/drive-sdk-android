package axa.tex.drive.sdk.acquisition.score.model

import org.junit.Assert
import org.junit.Test

class ContextualizedScoresTest {


    @Test
    fun testContextualizedScoresInitialization(){

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

        val contextualizedScores = ContextualizedScores(day,fog, motorway, mountain, night, normal, rain, rural, snow, urban)

        Assert.assertTrue(contextualizedScores.day == day)
        Assert.assertTrue(contextualizedScores.fog == fog)
        Assert.assertTrue(contextualizedScores.motorway == motorway)
        Assert.assertTrue(contextualizedScores.mountain == mountain)
        Assert.assertTrue(contextualizedScores.night == night)
        Assert.assertTrue(contextualizedScores.normal == normal)
        Assert.assertTrue(contextualizedScores.rain == rain)
        Assert.assertTrue(contextualizedScores.rural == rural)
        Assert.assertTrue(contextualizedScores.snow == snow)
        Assert.assertTrue(contextualizedScores.urban == urban)
    }





}