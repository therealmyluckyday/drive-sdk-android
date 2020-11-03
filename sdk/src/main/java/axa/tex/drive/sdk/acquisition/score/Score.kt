package axa.tex.drive.sdk.acquisition.score

import axa.tex.drive.sdk.acquisition.model.TripId
import axa.tex.drive.sdk.acquisition.score.model.ScoreStatus
import axa.tex.drive.sdk.acquisition.score.model.ScoreType
import axa.tex.drive.sdk.acquisition.score.model.ScoresDil
import axa.tex.drive.sdk.acquisition.score.model.TripInfo
import com.fasterxml.jackson.annotation.JsonProperty


open class Score {
    var status: ScoreStatus = ScoreStatus.invalid
}



class ScoreV1: Score {
    var alerts: List<String>? = null
    var end_time: Long = -1
    var flags: List<String>? = null
    var score_type: ScoreType? = null
    var scoregw_version: String? = null
    var scores_dil: ScoresDil? = null
    var scoring_version: String? = null
    var start_time: Long = -1
    var tags: List<String>? = null
    var timezone: String? = null
    @JsonProperty("trip_id")
    var tripId: TripId? = null
    var trip_info: TripInfo? = null
    var uid: String? = null

    constructor(alerts: List<String>?,
                end_time: Long,
                flags: List<String>?,
                score_type: ScoreType?,
                scoregw_version: String?,
                scores_dil: ScoresDil?,
                scoring_version: String?,
                start_time: Long,
                status: ScoreStatus,
                tags: List<String>?,
                timezone: String?,
                trip_id: String,
                trip_info: TripInfo?,
                uid: String?) {
        this.alerts = alerts
        this.end_time = end_time // peut être null
        this.flags = flags // Peut être null / vide
        this.score_type = score_type
        this.scoregw_version = scoregw_version
        this.scores_dil = scores_dil // toujours présents
        this.scoring_version = scoring_version
        this.start_time = start_time  // peut être null
        this.status = status
        this.tags = tags // peut être vide ou null
        this.timezone = timezone
        this.tripId = TripId(trip_id)
        this.trip_info = trip_info // Toujours présents
        this.uid = uid
    }
}























