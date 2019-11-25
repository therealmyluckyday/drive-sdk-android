package axa.tex.drive.sdk.acquisition.score

import android.content.Context
import androidx.work.*
import axa.tex.drive.sdk.acquisition.score.model.ScoreResult
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import io.reactivex.subjects.PublishSubject

class ScoreRetriever: KoinComponentCallbacks {

    private val workManager: WorkManager
    private val scoreListener: PublishSubject<ScoreResult> = PublishSubject.create()
    private val availableScoreListener: PublishSubject<String?> = PublishSubject.create()

    constructor(appContext: Context) {
        this.workManager = WorkManager.getInstance(appContext)
    }

    fun getScoreListener(): PublishSubject<ScoreResult> {
        return scoreListener
    }

    internal fun getAvailableScoreListener(): PublishSubject<String?> {
        return availableScoreListener
    }

    fun retrieveScore(tripId: String) {
        val data: Data = Data.Builder().putBoolean(tripId, true).build()
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        val fixUploadWork: OneTimeWorkRequest = OneTimeWorkRequest.Builder(ScoreWorker::class.java).setInputData(data).setConstraints(constraints)
                .build()
        this.workManager.enqueue(fixUploadWork)
    }
}