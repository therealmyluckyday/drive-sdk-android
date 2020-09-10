package axa.tex.drive.sdk.automode.internal.tracker


import android.location.Location
import axa.tex.drive.sdk.automode.internal.tracker.model.TexLocation

import axa.tex.drive.sdk.core.logger.LoggerFactory
import com.google.android.gms.location.DetectedActivity
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject

// 2.8 m/s 10km/h
internal const val SPEED_MOVEMENT_THRESHOLD = 10 * 0.28f

// 5.6 m/s 20km/h
private const val SPEED_START_THRESHOLD = 20 * 0.28f

//Accuracy for speed
internal const val LOCATION_ACCURACY_THRESHOLD = 20

class SpeedFilter {
    var rxScheduler: Scheduler = Schedulers.io()
    internal val activityStream: PublishSubject<DetectedActivity> = PublishSubject.create()
    public val gpsStream: PublishSubject<TexLocation> = PublishSubject.create()
    internal val locations: PublishSubject<Location> = PublishSubject.create()
    internal var collectionEnabled = false
    private val LOGGER = LoggerFactory().getLogger(this::class.java.name).logger

}