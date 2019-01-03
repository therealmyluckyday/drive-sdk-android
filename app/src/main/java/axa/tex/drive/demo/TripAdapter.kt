package com.example.android.recyclerview

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import axa.tex.drive.demo.R
import axa.tex.drive.demo.Scores


/**
 * Provide views to RecyclerView with data from dataSet.
 *
 * Initialize the dataset of the Adapter.
 *
 * @param dataSet String[] containing the data to populate views to be used by RecyclerView.
 */

class TripAdapter(private val context: Context, private val dataSet: List<String>?) :
        RecyclerView.Adapter<TripAdapter.ViewHolder>() {


    /**
     * Provide a reference to the type of views that you are using (custom ViewHolder)
     */
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val applicationName: TextView? = v.findViewById(R.id.trip_id);

        init {
            // Define click listener for the ViewHolder's View.
            v.setOnClickListener { }

        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view.
        val v = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.trip_item, viewGroup, false)




        return ViewHolder(v)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {


        // Get element from your dataset at this position and replace the contents of the view
        // with that element
        viewHolder.applicationName?.text = dataSet?.get(position)
        viewHolder.itemView.setOnClickListener {
            val intent = Intent(context, Scores::class.java)
            intent.putExtra("trip", dataSet?.get(position))
            context.startActivity(intent)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = dataSet?.size as Int

    companion object {
        private val TAG = "CustomAdapter"
    }
}