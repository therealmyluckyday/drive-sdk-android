package axa.tex.drive.sdk.acquisition.collection.internal

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import axa.tex.drive.sdk.acquisition.model.FixPacket
import axa.tex.drive.sdk.acquisition.model.Event
import axa.tex.drive.sdk.acquisition.model.Fix
import java.util.*
import axa.tex.drive.sdk.acquisition.db.PendingTripDao
import axa.tex.drive.sdk.acquisition.model.PendingTrip
import axa.tex.drive.sdk.core.Constants
import axa.tex.drive.sdk.core.internal.utils.*


internal object FixProcessor {
    private const val DEFAULT_PACKET_SIZE = 50
    private val buffer = mutableListOf<Fix>()
    private var packetSize : Int = DEFAULT_PACKET_SIZE;


    fun setPacketSize(packetSize : Int){
        this.packetSize = packetSize
    }

    public fun startTrip(context : Context){
        val start = Event(listOf("start"), Date().time);
        addFixes(context,listOf(start))
    }

    public fun endTrip(context : Context){
        val start = Event(listOf("end"), Date().time);
        addFixes(context,listOf(start))
    }



    @Synchronized fun addFixes(context : Context,fixes : List<Fix>) {

        val model = Utils.getDeviceName()
        val os: String = Utils.getOSVersion()
        val timezone:String = Utils.getFormattedTZ()
        val uid: String = Utils.getUid(context)
        val version: String = Constants.JSON_SCHEMA_VERSION
        val tripId = Utils.tripId(context)

        for (fix in fixes){
            buffer.add(fix)
            if(buffer.size >= packetSize){

                val theFixes = mutableListOf<Fix>();
                theFixes.addAll(buffer)
                val packet = FixPacket(theFixes, model ,os,timezone,uid,version, tripId)
                val json = packet.toJson()

                val id = UUID.randomUUID().toString()
               // val data : Data = Data.Builder().putAll(buffer.associateBy ( {it.timestamp().toString()}, {it.toJson()} )).build()

                val data : Data = Data.Builder().putAll(buffer.associateBy ( {/*it.timestamp().toString()*/id}, {json} )).build()
                buffer.clear()
                val fixUploadWork : OneTimeWorkRequest = OneTimeWorkRequest.Builder(FixWorker::class.java).setInputData(data)
                        .build()


                WorkManager.getInstance().enqueue(fixUploadWork)
                val pendingTrip = PendingTrip(id, tripId)
                PendingTripDao.saveTrip(context,pendingTrip)

            }
        }
    }
}