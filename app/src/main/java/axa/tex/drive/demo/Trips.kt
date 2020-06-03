package axa.tex.drive.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.recyclerview.TripAdapter
import java.io.File

class Trips : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trips)


        viewManager = LinearLayoutManager(this)
        val trips = getTripsForScore()
        var dataSet: List<String>  = trips.split("\n")
        val data = mutableListOf<String>()
        for(trip in dataSet){
            if(!trip.isEmpty())
            data.add(trip)
        }
        viewAdapter = TripAdapter(this, data)

        recyclerView = findViewById<RecyclerView>(R.id.trips).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(false)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = viewAdapter

        }
    }


    fun  getTripsForScore(): String{
        try {
            val rootPath = applicationContext!!.getExternalFilesDir("AUTOMODE")!!
            val root = File(rootPath.toURI())
            if (!root.exists()) {
                root.mkdirs()
            }
            val f = File(rootPath.path + "/trips.txt")
            return  f.readText()

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
}
