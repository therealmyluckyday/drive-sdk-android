package axa.tex.drive.sdk.acquisition.collection.internal

import android.content.ComponentCallbacks
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.work.*
import java.util.*
import axa.tex.drive.sdk.acquisition.collection.internal.db.CollectionDb
import axa.tex.drive.sdk.acquisition.model.*
import axa.tex.drive.sdk.core.internal.Constants
import axa.tex.drive.sdk.core.internal.utils.*
import org.koin.android.ext.android.inject

private const val DEFAULT_PACKET_SIZE = 100
internal class FixProcessor : ComponentCallbacks{

    private var context : Context

    override fun onLowMemory() {
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
    }

    constructor(context : Context){
        this.context = context
    }


    private val buffer = mutableListOf<Fix>()
    private var packetSize : Int = DEFAULT_PACKET_SIZE;
    private var tripEnded  = false;


    fun setPacketSize(packetSize : Int){
        this.packetSize = packetSize
    }

     fun startTrip(/*context : Context*/){
        val start = Event(listOf("start"), Date().time);
        tripEnded = false
        addFixes(/*context,*/listOf(start))
    }

     fun endTrip(/*context : Context*/){
        val end = Event(listOf("stop"), Date().time);
        tripEnded = true
        addFixes(/*context,*/listOf(end))

    }



    @Synchronized fun addFixes(/*context : Context,*/fixes : List<Fix>) {

        val model = DeviceInfo.getDeviceName()
        val os: String = DeviceInfo.getOSVersion()
        val timezone:String = Utils.getFormattedTZ()
        val uid: String = DeviceInfo.getUid(context)
        val version: String = Constants.JSON_SCHEMA_VERSION
        val tripId = TripManager.tripId(context)

        for (fix in fixes){
            buffer.add(fix)
            if(buffer.size >= packetSize || tripEnded){
                val collectorDb: CollectionDb by inject()

                val config  = collectorDb.getConfig()
                val appName = config?.appName

                val clientId : String? = config?.clientId

                val theFixes = mutableListOf<Fix>();
                theFixes.addAll(buffer)
                if(clientId != null && appName != null && !appName.isEmpty()) {
                    val packet = appName?.let {theAppName ->
                        clientId?.let { theClentId ->
                            FixPacket(theFixes, model, os, timezone, uid, version, tripId?.value, theAppName,
                                    theClentId)
                        }
                    }

                    val json = packet?.toJson()


                    val id = UUID.randomUUID().toString()
                    val data: Data = Data.Builder().putString(id, json).build()
                    Log.i("DATA FOR WORKER MANAGER", data.toString())
                    buffer.clear()

                    val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()

                    val fixUploadWork: OneTimeWorkRequest = OneTimeWorkRequest.Builder(FixWorker::class.java).setInputData(data).setConstraints(constraints)
                            .build()

                    val pendingTrip = PendingTrip(id, tripId?.value, tripEnded)
                    //CollectionDb.saveTrip(context,pendingTrip)
                    collectorDb.saveTrip(pendingTrip)
                    WorkManager.getInstance().enqueue(fixUploadWork)
                }
            }
        }
    }
}