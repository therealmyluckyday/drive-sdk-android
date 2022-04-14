package axa.tex.drive.demo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import axa.tex.drive.demo.databinding.ActivityMainBinding
import axa.tex.drive.demo.databinding.ActivityScoresBinding
import axa.tex.drive.sdk.acquisition.score.ScoreRetriever
import axa.tex.drive.sdk.acquisition.score.ScoreV1
import axa.tex.drive.sdk.core.Platform

class Scores : AppCompatActivity() {
    private lateinit var binding: ActivityScoresBinding
    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScoresBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        title = "SCORES"


        val tripId = intent.getStringExtra("trip")
        val service = (application as TexDriveDemoApplication).service
        val scoreRetriever: ScoreRetriever = service?.scoreRetriever()!!

        scoreRetriever.getScoreListener().subscribe {
            this.runOnUiThread {
                val scoreV1: ScoreV1 = it.score as ScoreV1
                if (scoreV1.scores_dil != null) {
                    val scoreDil = scoreV1.scores_dil!!
                    binding.speed.visibility = View.VISIBLE
                    binding.speed.text = "${scoreDil.acceleration}"

                    binding.breaking.visibility = View.VISIBLE
                    binding.breaking.text = "${scoreDil.braking}"

                    binding.smoothness.visibility = View.VISIBLE
                    binding.smoothness.text = "${scoreDil.smoothness}"
                    println("SCORES ${scoreDil.acceleration}")
                }else{

                    binding.error.text = it.scoreError.toString()
                    binding.error.visibility = android.view.View.VISIBLE
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
