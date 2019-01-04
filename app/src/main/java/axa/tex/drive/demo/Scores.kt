package axa.tex.drive.demo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import axa.tex.drive.sdk.acquisition.score.ScoreRetriever
import kotlinx.android.synthetic.main.activity_scores.*

class Scores : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scores)
        title = "SCORES"


        val tripId = intent.getStringExtra("trip")

        val scoreRetriever: ScoreRetriever = ScoreRetriever()

        scoreRetriever.getScoreListener().subscribe {
            this.runOnUiThread {
                speed.visibility = View.VISIBLE
                speed.text = "${it.acceleration}"

                breaking.visibility = View.VISIBLE
                breaking.text = "${it.braking}"

                smoothness.visibility = View.VISIBLE
                smoothness.text = "${it.smoothness}"
            }
            println("SCORES ${it.acceleration}")
        }

        Thread { scoreRetriever.retrieveScore("4260e592-008b-4fcf-877d-fe8d3923b5f5") }.start()

    }
}
