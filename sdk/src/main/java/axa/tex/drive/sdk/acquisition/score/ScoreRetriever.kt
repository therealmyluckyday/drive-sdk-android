package axa.tex.drive.sdk.acquisition.score

import androidx.work.*
import axa.tex.drive.sdk.acquisition.score.internal.ScoreWorker
import axa.tex.drive.sdk.acquisition.score.model.Scores
import axa.tex.drive.sdk.acquisition.score.model.ScoresDil
import io.reactivex.subjects.PublishSubject

class ScoreRetriever {

    companion object {

        private val listener : PublishSubject<ScoresDil> = PublishSubject.create()

        fun getScoreListener() : PublishSubject<ScoresDil> {
            return listener;
        }

        fun retrieveScore(tripId : String){
            val data : Data = Data.Builder().putBoolean(tripId,true).build()
            val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            val fixUploadWork : OneTimeWorkRequest = OneTimeWorkRequest.Builder(ScoreWorker::class.java).
                    setInputData(data).setConstraints(constraints)
                    .build()
            WorkManager.getInstance().enqueue(fixUploadWork)
        }
    }

}