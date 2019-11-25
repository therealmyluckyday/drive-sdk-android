package axa.tex.drive.sdk.acquisition.score.model


class Mean {

    var acceleration: Double = (-1).toDouble()
    var braking: Double = (-1).toDouble()
    var expert: Double = (-1).toDouble()
    var smoothness: Double = (-1).toDouble()
    var speed: Double = (-1).toDouble()
    var weight: Int = -1


    constructor(acceleration: Double,
                braking: Double,
                expert: Double,
                smoothness: Double,
                speed: Double,
                weight: Int) {
        this.acceleration = acceleration
        this.braking = braking
        this.expert = expert
        this.smoothness = smoothness
        this.speed = speed
        this.weight = weight
    }

}