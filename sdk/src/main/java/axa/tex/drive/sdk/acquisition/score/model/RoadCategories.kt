package axa.tex.drive.sdk.acquisition.score.model

class RoadCategories {

    var motorway: Int = -1
    var mountain: Int = -1
    var rural: Double = (-1).toDouble()
    var urban: Double = (-1).toDouble()

    constructor()

    constructor(motorway: Int,
                mountain: Int,
                rural: Double,
                urban: Double) {
        this.motorway = motorway
        this.mountain = mountain
        this.rural = rural
        this.urban = urban
    }

}