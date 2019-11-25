package axa.tex.drive.sdk.acquisition.score.model

class Scores {

    var acceleration: Double = (-1).toDouble()
    var braking: Double = (-1).toDouble()
    var expert: Double = (-1).toDouble()
    var smoothness: Double = (-1).toDouble()


    constructor()


    constructor(acceleration: Double,
                braking: Double,
                expert: Double,
                smoothness: Double) {
        this.acceleration = acceleration
        this.braking = braking
        this.expert = expert
        this.smoothness = smoothness
    }

}