package axa.tex.drive.sdk.acquisition.score.model

class Mountain{

     var percentage: Int = -1
     var scores: Scores? = null


    constructor()  {

    }


    constructor(percentage: Int,
                scores: Scores) {
        this.percentage = percentage
        this.scores = scores
    }

}