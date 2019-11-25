package axa.tex.drive.sdk.acquisition.score.model


class ContextualizedScores {

    var day: Day? = null
    var fog: Fog? = null
    var motorway: Motorway? = null
    var mountain: Mountain? = null
    var night: Night? = null
    var normal: Normal? = null
    var rain: Rain? = null
    var rural: Rural? = null
    var snow: Snow? = null
    var urban: Urban? = null

    constructor(day: Day,
                fog: Fog,
                motorway: Motorway,
                mountain: Mountain,
                night: Night,
                normal: Normal,
                rain: Rain,
                rural: Rural,
                snow: Snow,
                urban: Urban) {
        this.day = day
        this.fog = fog
        this.motorway = motorway
        this.night = night
        this.mountain = mountain
        this.normal = normal
        this.rain = rain
        this.rural = rural
        this.snow = snow
        this.urban = urban
    }

}