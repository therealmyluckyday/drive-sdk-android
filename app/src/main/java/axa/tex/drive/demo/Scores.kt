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
                if (it.scoreDil != null) {
                    speed.visibility = View.VISIBLE
                    speed.text = "${it.scoreDil?.acceleration}"

                    breaking.visibility = View.VISIBLE
                    breaking.text = "${it.scoreDil?.braking}"

                    smoothness.visibility = View.VISIBLE
                    smoothness.text = "${it.scoreDil?.smoothness}"
                }
                println("SCORES ${it.scoreDil?.acceleration}")
            }
        }

        //Thread { scoreRetriever.retrieveScore("4260e592-008b-4fcf-877d-fe8d3923b5f5") }.start()
        Thread { scoreRetriever.retrieveScore("65747F5D-8F8B-495E-BFA7-1E12B70997C7") }.start()

    }
}
