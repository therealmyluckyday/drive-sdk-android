package axa.tex.drive.sdk.acquisition

import android.content.Context
import android.location.Location
import axa.tex.drive.sdk.R
import axa.tex.drive.sdk.automode.internal.tracker.SpeedFilter
import axa.tex.drive.sdk.automode.internal.tracker.model.TexLocation
import io.reactivex.Scheduler
import axa.tex.drive.sdk.core.logger.LoggerFactory
import io.reactivex.subjects.PublishSubject
import java.io.IOException

open class SensorServiceFake : SensorService {
    private var speedFilter = SpeedFilter()
    internal val logger = LoggerFactory().getLogger(this::class.java.name).logger
    val infosMethodCalled: PublishSubject<String> = PublishSubject.create()

    constructor(context: Context, scheduler: Scheduler) {
    }

    constructor() {

    }

    fun forceLocationChanged(location: Location) {
        val texLocation = TexLocation(location.latitude.toFloat(), location.longitude.toFloat(), location.accuracy, location.speed, location.bearing, location.altitude.toFloat(), location.time)
        speedFilter.gpsStream.onNext(texLocation)
        speedFilter.locations.onNext(location)
    }

    override fun speedFilter() : SpeedFilter {
        return this.speedFilter
    }

    override fun passivelyScanSpeed() {
    }

    override fun activelyScanSpeed() {
        logger.info("sensorFake activelyScanSpeed", function = "activelyScanSpeed")
        infosMethodCalled.onNext("activelyScanSpeed")
    }

    override fun stopSpeedScanning() {
    }
    override fun checkWhereAmI() {
        logger.info("sensorFake checkWhereAmI", function = "checkWhereAmI")
        infosMethodCalled.onNext("checkWhereAmI")
    }

    override fun stopActivityScanning() {
    }

    override fun requestForLocationPermission() {

    }

    /**
     * Add method to simulate trip
     */

    fun loadTrip(appContext: Context, intervalBetweenGPSPointInMilliSecond: Long) {
        val inputStream = appContext.resources?.openRawResource(R.raw.trip_location_simulation)
        if (inputStream == null) {
            logger.error("ERROR File NOT FOUND", function = "loadTrip")
        }else {
            Thread {
                var newTime = System.currentTimeMillis() - 86400000
                try {
                    logger.info("bufferedReader read", function = "loadTrip")
                    val bufferedReader = inputStream.bufferedReader()
                    var lineIndex = 0
                    var line = bufferedReader.readLine()
                    while (line != null) {
                        newTime = sendLocationLineStringToSpeedFilter(line, newTime, intervalBetweenGPSPointInMilliSecond)
                        line = bufferedReader.readLine()
                        lineIndex++
                    }
                    logger.info("bufferedReader close", function = "loadTrip")
                    bufferedReader.close()
                } catch (e: IOException) {
                    logger.error("IOException read"+e.localizedMessage, function = "loadTrip")
                }
            }.start()
        }
    }

    private fun sendLocationLineStringToSpeedFilter(line: String, time: Long, intervalBetweenGPSPointInMilliSecond: Long) : Long {
        val locationDetails = line.split(",")
        var newLocation = Location("")
        val latitude = locationDetails[0].toDouble()
        newLocation.latitude = latitude
        val longitude = locationDetails[1].toDouble()
        newLocation.longitude = longitude
        val accuracy = locationDetails[2].toFloat()
        newLocation.accuracy = accuracy
        val speed = locationDetails[3].toFloat()
        newLocation.speed = speed // 20.F
        val bearing = locationDetails[4].toFloat()
        newLocation.bearing = bearing
        val altitude = locationDetails[5].toDouble()
        newLocation.altitude =altitude
        val delay = locationDetails[6].toLong()
        newLocation.time = time + delay
        this.forceLocationChanged(newLocation)
        Thread.sleep(intervalBetweenGPSPointInMilliSecond)
        return newLocation.time
    }

}