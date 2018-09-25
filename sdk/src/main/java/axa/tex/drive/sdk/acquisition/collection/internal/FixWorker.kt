package axa.tex.drive.sdk.acquisition.collection.internal

import android.util.Log
import androidx.work.Data
import androidx.work.Worker


internal class FixWorker() : Worker() {


    companion object {
        private val FIX_SENDER_TAG: String = "COLLECTOR_" + (FixWorker::class.java.simpleName).toUpperCase();
    }

    override fun doWork(): WorkerResult {

        val inputData : Data =  inputData

        sendFixes(inputData);

        return WorkerResult.SUCCESS
    }


    private fun sendFixes(inputData : Data) {

        val data = inputData.keyValueMap
        Log.i("COLLECTOR_WORKER SIZE :", inputData.keyValueMap.size.toString())
        for((key , value) in data) {
            Log.i(FIX_SENDER_TAG, value as String)
        }
    }
}