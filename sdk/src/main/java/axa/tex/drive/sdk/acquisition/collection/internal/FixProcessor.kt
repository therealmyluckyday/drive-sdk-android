package axa.tex.drive.sdk.acquisition.collection.internal

import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import axa.tex.drive.sdk.internal.extension.toJson


internal object FixProcessor {
    private const val DEFAULT_PACKET_SIZE = 50
    private val buffer = mutableListOf<axa.tex.drive.sdk.acquisition.model.Data>()
    private var packetSize : Int = DEFAULT_PACKET_SIZE;


    fun setPacketSize(packetSize : Int){
        this.packetSize = packetSize
    }


    @Synchronized fun addFixes(fixes : List<axa.tex.drive.sdk.acquisition.model.Data>) {

        for (fix in fixes){
            buffer.add(fix)
            if(buffer.size >= packetSize){
                val data : Data = Data.Builder().putAll(buffer.associateBy ( {it.timestamp.toString()}, {it.toJson()} )).build()
                buffer.clear()
                val fixUploadWork = OneTimeWorkRequest.Builder(FixWorker::class.java).setInputData(data)
                        .build()
                WorkManager.getInstance().enqueue(fixUploadWork)

            }
        }
    }
}