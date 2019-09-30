package axa.tex.drive.sdk.acquisition.collection.internal.db.queue


import android.content.Context
import androidx.work.*
import axa.tex.drive.sdk.acquisition.collection.internal.db.CollectionDb
import axa.tex.drive.sdk.acquisition.collection.internal.db.queue.FixWorkerWithQueue
import axa.tex.drive.sdk.acquisition.model.Event
import axa.tex.drive.sdk.acquisition.model.Fix
import axa.tex.drive.sdk.acquisition.model.FixPacket
import axa.tex.drive.sdk.acquisition.model.PendingTrip
import axa.tex.drive.sdk.core.internal.Constants
import axa.tex.drive.sdk.core.internal.KoinComponentCallbacks
import axa.tex.drive.sdk.core.internal.utils.DeviceInfo
import axa.tex.drive.sdk.core.internal.utils.TripManager
import axa.tex.drive.sdk.core.internal.utils.Utils
import axa.tex.drive.sdk.core.logger.LoggerFactory
import org.koin.android.ext.android.inject
import java.util.*

private const val DEFAULT_PACKET_SIZE = 100

internal class FixProcessor : KoinComponentCallbacks {

    private var context: Context
    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger
    private var tripStart: Boolean = false
    val tripManager: TripManager by inject()

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
        tripStart = true
        addFixes(listOf(start))
    }

    fun endTrip(endTime: Long) {
        val end = Event(listOf("stop"), endTime);
        tripEnded = true
        addFixes(listOf(end))

    }


    @Synchronized
    fun addFixes(fixes: List<Fix>) {

        val model = DeviceInfo.getDeviceName()
        val os: String = DeviceInfo.getOSVersion()
        val timezone: String = Utils.getFormattedTZ()
        val uid: String = DeviceInfo.getUid(context)
        val version: String = Constants.JSON_SCHEMA_VERSION
        val tripId = tripManager.tripId(context)

        for (fix in fixes) {
            buffer.add(fix)
            if (buffer.size >= packetSize || tripEnded || tripStart) {
                tripStart = false
                LOGGER.info("SENDING : NEW PACKET DETECTED", function = "fun addFixes(fixes: List<Fix>")
                val collectorDb: CollectionDb by inject()

                val config = collectorDb.getConfig()
                val appName = config?.appName

                val clientId: String? = config?.clientId

                val theFixes = mutableListOf<Fix>();
                theFixes.addAll(buffer)
                if (clientId != null && appName != null && !appName.isEmpty() && !clientId.isEmpty()) {
                    val packet = FixPacket(theFixes, model, os, timezone, uid, version, tripId?.value, appName,
                            clientId)
                    LOGGER.info("SENDING : CONVERTING TO JSON", function = "fun addFixes(fixes: List<Fix>")
                    val json = packet?.toJson()
                    if (Constants.EMPTY_PACKET == json) {
                        buffer.clear()
                        return
                    }

                    LOGGER.info("SENDING : $json", function = "fun addFixes(fixes: List<Fix>")
                    val id = UUID.randomUUID().toString()

                    LOGGER.info("SENDING : SETTING DATA", function = "fun addFixes(fixes: List<Fix>")
                    val data: Data = Data.Builder().putString(Constants.ID_KEY, id).putString(Constants.DATA_KEY, json).putString(Constants.APP_NAME_KEY, appName).putString(Constants.CLIENT_ID_KEY, clientId).putString("tripId", tripId?.value).build()
                    LOGGER.info("SENDING : DATA SET")
                    LOGGER.info("PACKET DATA FOR WORKER MANAGER ${data.toString()}", function = "fun addFixes(fixes: List<Fix>")
                    buffer.clear()

                    val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()


                    val persistentQueue: PersistentQueue by inject()



                    if (!persistentQueue.tripExists(tripId!!.value)) {
                        val fixUploadWork: OneTimeWorkRequest = OneTimeWorkRequest.Builder(FixWorkerWithQueue::class.java).setInputData(data).setConstraints(constraints)
                                .build()
                        WorkManager.getInstance().enqueue(fixUploadWork)
                    }
0
                    val pendingTrip = PendingTrip(id, tripId?.value, tripEnded)
                    collectorDb.saveTrip(pendingTrip)

                    LOGGER.info("SENDING : ENQUEUING", function = "fun addFixes(fixes: List<Fix>")

                    LOGGER.info("SENDING : ENQUEUED", function = "fun addFixes(fixes: List<Fix>")
                }
            }
        }
    }

    fun processInOrder(fixes: List<Fix>) {

    }
}