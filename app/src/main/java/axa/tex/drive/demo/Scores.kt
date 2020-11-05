package axa.tex.drive.demo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import axa.tex.drive.sdk.acquisition.score.ScoreRetriever
import axa.tex.drive.sdk.acquisition.score.ScoreV1
import axa.tex.drive.sdk.core.Platform
import kotlinx.android.synthetic.main.activity_scores.*

class Scores : AppCompatActivity() {

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scores)
        title = "SCORES"


        val tripId = intent.getStringExtra("trip")
        val service = (application as TexDriveDemoApplication).service
        val scoreRetriever: ScoreRetriever = service?.scoreRetriever()!!

        scoreRetriever.getScoreListener().subscribe {
            this.runOnUiThread {
                val scoreV1: ScoreV1 = it.score as ScoreV1
                if (scoreV1.scores_dil != null) {
                    val scoreDil = scoreV1.scores_dil!!
                    speed.visibility = View.VISIBLE
                    speed.text = "${scoreDil.acceleration}"

                    breaking.visibility = View.VISIBLE
                    breaking.text = "${scoreDil.braking}"

                    smoothness.visibility = View.VISIBLE
                    smoothness.text = "${scoreDil.smoothness}"
                    println("SCORES ${scoreDil.acceleration}")
                }else{

                    error.text = it.scoreError.toString()
                    error.visibility = android.view.View.VISIBLE
                }

            }
        }
        val appName = "APP-TEST"
        val platform = Platform.PRODUCTION

        Thread {
            scoreRetriever.retrieveScore(tripId!!, appName, platform.generateUrl(false), true, isAPIV2 = false)
        }.start()

    }
}
