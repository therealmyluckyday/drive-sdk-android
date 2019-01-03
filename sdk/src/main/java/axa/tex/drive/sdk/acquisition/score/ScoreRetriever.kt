package axa.tex.drive.sdk.acquisition.score

import androidx.work.*
import axa.tex.drive.sdk.acquisition.score.internal.ScoreWorker
import axa.tex.drive.sdk.acquisition.score.model.ScoresDil
import io.reactivex.subjects.PublishSubject

class ScoreRetriever {

    private val scoreListener: PublishSubject<ScoresDil> = PublishSubject.create()

    private val availableScoreListener: PublishSubject<String?> = PublishSubject.create()

    fun getScoreListener(): PublishSubject<ScoresDil> {
        return scoreListener;
    }

    fun getAvailableScoreListener(): PublishSubject<String?> {
        return availableScoreListener;
    }

    fun retrieveScore(tripId: String) {
        val data: Data = Data.Builder().putBoolean(tripId, true).build()
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        val fixUploadWork: OneTimeWorkRequest = OneTimeWorkRequest.Builder(ScoreWorker::class.java).setInputData(data).setConstraints(constraints)
                .build()
        WorkManager.getInstance().enqueue(fixUploadWork)
    }
}