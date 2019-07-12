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
        val service = (application as TexDriveDemoApplication).service
        val scoreRetriever: ScoreRetriever = service?.scoreRetriever()!!

        scoreRetriever.getScoreListener().subscribe {
            this.runOnUiThread {
                if (it.scoreDil != null) {
                    speed.visibility = View.VISIBLE
                    speed.text = "${it.scoreDil?.acceleration}"

                    breaking.visibility = View.VISIBLE
                    breaking.text = "${it.scoreDil?.braking}"

                    smoothness.visibility = View.VISIBLE
                    smoothness.text = "${it.scoreDil?.smoothness}"
                    println("SCORES ${it.scoreDil?.acceleration}")
                }else{

                    error.text = it.response
                    error.visibility = android.view.View.VISIBLE


                }

            }
        }

        //Thread { scoreRetriever.retrieveScore("4260e592-008b-4fcf-877d-fe8d3923b5f5") }.start()
        Thread { scoreRetriever.retrieveScore(tripId) }.start()

    }
}
