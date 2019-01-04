package axa.tex.drive.sdk.acquisition.collection.internal

import android.content.ComponentCallbacks
import android.content.Context
import android.content.res.Configuration
import androidx.work.*
import axa.tex.drive.sdk.acquisition.collection.internal.db.CollectionDb
import axa.tex.drive.sdk.acquisition.model.Event
import axa.tex.drive.sdk.acquisition.model.Fix
import axa.tex.drive.sdk.acquisition.model.FixPacket
import axa.tex.drive.sdk.acquisition.model.PendingTrip
import axa.tex.drive.sdk.core.internal.Constants
import axa.tex.drive.sdk.core.internal.utils.DeviceInfo
import axa.tex.drive.sdk.core.internal.utils.TripManager
import axa.tex.drive.sdk.core.internal.utils.Utils
import axa.tex.drive.sdk.core.logger.LoggerFactory
import org.koin.android.ext.android.inject
import java.util.*

private const val DEFAULT_PACKET_SIZE = 100

internal class FixProcessor : ComponentCallbacks {

    private var context: Context
    private val LOGGER = LoggerFactory.getLogger(this::class.java.name).logger

    override fun onLowMemory() {
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
    }

    constructor(context: Context) {
        this.context = context
    }


    private val buffer = mutableListOf<Fix>()
    private var packetSize: Int = DEFAULT_PACKET_SIZE;
    private var tripEnded = false;


    fun setPacketSize(packetSize: Int) {
        this.packetSize = packetSize
    }

    fun startTrip(startTime: Long) {
        val start = Event(listOf("start"), startTime);
        tripEnded = false
        addFixes(listOf(start))
    }

    fun endTrip(endTime: Long) {
        val end = Event(listOf("stop"), endTime);
        tripEnded = true
        addFixes(listOf(end))

    }


    @Synchronized
    fun addFixes(/*context : Context,*/fixes: List<Fix>) {

        val model = DeviceInfo.getDeviceName()
        val os: String = DeviceInfo.getOSVersion()
        val timezone: String = Utils.getFormattedTZ()
        val uid: String = DeviceInfo.getUid(context)
        val version: String = Constants.JSON_SCHEMA_VERSION
        val tripId = TripManager.tripId(context)

        for (fix in fixes) {
            buffer.add(fix)
            if (buffer.size >= packetSize || tripEnded) {
                val collectorDb: CollectionDb by inject()

                val config = collectorDb.getConfig()
                val appName = config?.appName

                val clientId: String? = config?.clientId

                val theFixes = mutableListOf<Fix>();
                theFixes.addAll(buffer)
                if (clientId != null && appName != null && !appName.isEmpty() && !clientId.isEmpty()) {
                    val packet = FixPacket(theFixes, model, os, timezone, uid, version, tripId?.value, appName,
                            clientId)
                    val json = packet?.toJson()


                    val id = UUID.randomUUID().toString()
                    val data: Data = Data.Builder().putString(id, json).build()
                    LOGGER.info("DATA FOR WORKER MANAGER", data.toString())
                    buffer.clear()

                    val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()

                    val fixUploadWork: OneTimeWorkRequest = OneTimeWorkRequest.Builder(FixWorker::class.java).setInputData(data).setConstraints(constraints)
                            .build()

                    val pendingTrip = PendingTrip(id, tripId?.value, tripEnded)
                    collectorDb.saveTrip(pendingTrip)
                    WorkManager.getInstance().enqueue(fixUploadWork)
                }
            }
        }
    }
}