package axa.tex.drive.sdk.acquisition.score.model


class  Day{

    internal var percentage: Int = -1
    internal var scores: Scores? = null

    constructor(){

    }


    constructor(percentage: Int,
                scores: Scores) {
       this.percentage = percentage
        this.scores = scores
    }

}