package axa.tex.drive.sdk.acquisition.score

import axa.tex.drive.sdk.acquisition.score.model.ScoresDil
import axa.tex.drive.sdk.acquisition.score.model.TripInfo

class  Score{

    var alerts: List<String>? = null
    var end_time: Long = - 1
    var flags: List<String>? = null
    var score_type: String? = null
    var scoregw_version: String? = null
    var scores_dil: ScoresDil? = null
    var scoring_version: String? = null
    var start_time: Long = -1
    var status: String? = null
    var tags: List<String>? = null
    var timezone: String? = null
    var trip_id: String? = null
    var trip_info: TripInfo? = null
    var uid: String? = null

    constructor(){

    }

    constructor(alerts: List<String>,
                end_time: Long,
                flags: List<String>,
                score_type: String,
                scoregw_version: String,
                scores_dil: ScoresDil,
                scoring_version: String,
                start_time: Long,
                status: String,
                tags: List<String>,
                timezone: String,
                trip_id: String,
                trip_info: TripInfo,
                uid: String) {
        this.alerts = alerts
        this.end_time = end_time
        this.flags = flags
        this.score_type = score_type
        this.scoregw_version = scoregw_version
        this.scores_dil = scores_dil
        this.scoring_version = scoring_version
        this.start_time = start_time
        this.status = status
        this.tags = tags
        this.timezone = timezone
        this.trip_id = trip_id
        this.trip_info = trip_info
        this.uid = uid
    }
}
























