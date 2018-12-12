package axa.tex.drive.sdk.acquisition.score.model

import org.junit.Assert
import org.junit.Test

class RoadCategoriesTest {

    @Test
    fun testRoadCategoiesInitialization(){
        val motorway: Int = 3
        val mountain: Int = 5
        val rural: Double = (7).toDouble()
        val urban: Double = (11).toDouble()

        val roadCategories = RoadCategories(motorway,mountain,rural,urban)

        Assert.assertTrue(roadCategories.motorway == motorway)
        Assert.assertTrue(roadCategories.mountain == mountain)
        Assert.assertTrue(roadCategories.rural == rural)
        Assert.assertTrue(roadCategories.urban == urban)
    }
}