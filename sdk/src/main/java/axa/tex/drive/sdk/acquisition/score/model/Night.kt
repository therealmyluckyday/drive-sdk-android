package axa.tex.drive.sdk.acquisition.score.model

class Night {

    var percentage: Int = -1
    var scores: Scores? = null


    constructor(percentage: Int,
                scores: Scores) {
        this.percentage = percentage
        this.scores = scores
    }

}