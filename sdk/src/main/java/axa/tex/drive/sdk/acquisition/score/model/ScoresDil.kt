package axa.tex.drive.sdk.acquisition.score.model

class  ScoresDil{

     var acceleration: Double = (- 1).toDouble()
     var braking: Double = (- 1).toDouble()
     var expert: Double = (- 1).toDouble()
     var smoothness: Double = (- 1).toDouble()
     var contextualized_scores: ContextualizedScores? = null
     var eco_ness: Double = (- 1).toDouble()

     var jam: Double = (- 1).toDouble()
     var mean: Mean? = null
     var poi_dil: List<PoiDil>? = null

     var speed: Double = (- 1).toDouble()
     var weather_penalization: Int = -1


    constructor(){

    }

    constructor(acceleration: Double,
                braking: Double,
                expert: Double,
                smoothness: Double,
                eco_ness: Double,
                jam: Double,
                mean: Mean,
                poi_dil: List<PoiDil>,
                speed: Double,
                 weather_penalization: Int,
                contextualized_scores: ContextualizedScores) {
        this.acceleration = acceleration
        this.braking = braking
        this.expert = expert
        this.smoothness = smoothness
        this.contextualized_scores = contextualized_scores
        this.eco_ness = eco_ness
        this.jam = jam
        this.mean = mean
        this.poi_dil = poi_dil
        this.speed = speed
        this.weather_penalization = weather_penalization
    }

}