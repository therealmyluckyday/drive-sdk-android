package axa.tex.drive.sdk.automode

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationProvider
import android.os.Bundle
import android.os.IBinder
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.ActivityRecognitionClient



private enum class AutoStartState {
    idle,
    activityScan,
    speedScan,
    driving,
    drivingAndStill
}

class AutoModeService : Service(),  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    override fun onBind(intent: Intent?): IBinder? {
        return null;
    }

    override fun onConnected(p0: Bundle?) {

    }

    override fun onConnectionSuspended(p0: Int) {

    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    private var locationManager: LocationManager? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mActivityPendingIntent: PendingIntent? = null
    private var autoStartState = AutoStartState.idle

    private var activityRecognitionClient : ActivityRecognitionClient = ActivityRecognition.getClient(this)
    private val mDrivingLocationListener = DrivingLocationListener()


    private val ACTIVITY_INTENT_ACTION = "axa.tex.drive.sdk.automode.ACTIVITY_DETECTED"
    private val PENDING_INTENT_REQUEST_CODE = 484
    private val NOTIFICATION_ID = 7071

    private val LOCATION_ACCURACY_THRESHOLD = 20
    private val ACTIVITY_DETECTION_INTERVAL = (10 * 1000).toLong() // in ms
    private val ACTIVITY_CONFIDENCE = 75 // Between 0 and 100
    private val SPEED_START_THRESHOLD = 20 * 0.28f // in m/s
    private val SPEED_SCAN_TIME = 60 * 1000 // in ms
    private val SPEED_MOVEMENT_THRESHOLD = 10 * 0.28f // in m/s
    private val STOPPED_DURATION = (3 * 60 * 1000).toLong() // in ms
    private val NO_GPS_DURATION = (4 * 60 * 1000).toLong() // in ms
    private var sStopTimer = STOPPED_DURATION // in ms

    override fun onCreate() {
        super.onCreate()


        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val activityIntent = Intent(ACTIVITY_INTENT_ACTION)
        mActivityPendingIntent = PendingIntent.getBroadcast(
                this,
                PENDING_INTENT_REQUEST_CODE,
                activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
        mGoogleApiClient?.connect()
    }


    internal var mProvidersChangedReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //mLogger.debug("Providers changed receiver broadcast was received")
            if (locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)!!) {
                if (AutoStartState.idle == autoStartState) {
                    goStateActivityScan()
                }
            } else {
                if (AutoStartState.activityScan == autoStartState) {
                    forceStopAll()
                }
            }
        }
    }

    private val mActivityReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (AutoStartState.activityScan != autoStartState) {
                return
            }
            val result = ActivityRecognitionResult.extractResult(intent)
            val activity = result.mostProbableActivity
            if (activity.type != DetectedActivity.IN_VEHICLE) {
                return
            }
            if (activity.confidence < ACTIVITY_CONFIDENCE) {
                //mLogger.trace("Not enough confidence for car activity: {}", activity.confidence)
                return
            }
           // mLogger.debug("Activity Receiver: going to speed scan mode")
            autoStartState = AutoStartState.speedScan
            //postStatusEventSticky(AutoModeStatusEvent(AutoModeStatus.scanning_activity))
            stopActivityScan()
            startSpeedScan()
        }
    }

    private val mPassiveLocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (AutoStartState.activityScan != autoStartState) {
                return
            }
            if (location.speed < SPEED_START_THRESHOLD || location.accuracy > LOCATION_ACCURACY_THRESHOLD) {
                return
            }
            //mLogger.debug("Passive Listener: going to speed scan mode")
            autoStartState = AutoStartState.speedScan
            //postStatusEventSticky(AutoModeStatusEvent(AutoModeStatus.scanning_activity))
            stopActivityScan()
            startSpeedScan()
        }






        override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
        override fun onProviderEnabled(s: String) {}
        override fun onProviderDisabled(s: String) {}
    }


    private val mSpeedLocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (AutoStartState.speedScan != autoStartState) {
                //mLogger.warn("mSpeedLocationListener - this state should not happen: {}", mAutoStartState)
                return
            }
            if (location.accuracy > LOCATION_ACCURACY_THRESHOLD) {
                return
            }
            if (location.speed > SPEED_START_THRESHOLD) {
                autoStartState = AutoStartState.driving
                //postStatusEventSticky(AutoModeStatusEvent(AutoModeStatus.driving))
                stopSpeedScan()
                startDrivingMode()
                // mLogger.debug("Triggering auto start")

                //  postDrivingEventSticky(AutoModeDrivingEvent(java.lang.Boolean.TRUE))
            }
        }

        override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
        override fun onProviderEnabled(s: String) {}
        override fun onProviderDisabled(s: String) {}
    }

    private inner class DrivingLocationListener : LocationListener {
        private var _stopTimeBeginMs: Long = 0
        private val _gpsWatchdog = Runnable {
            if (AutoStartState.driving != autoStartState && AutoStartState.drivingAndStill != autoStartState) {
                //mLogger.debug("gpsWatchdog - leaving without auto stop")
                return@Runnable
            }
            // If not GPS update in 4 minutes, stopping
            //mLogger.debug("No more GPS data, stopping")
            triggerAutoStop()
        }

        fun resetGpsWatchdog() {
            stopGpsWatchdog()
           // mHandler.postDelayed(_gpsWatchdog, sNoGpsTimer)
        }

        fun stopGpsWatchdog() {
           // mHandler.removeCallbacks(_gpsWatchdog)
        }

        private fun triggerAutoStop() {
            autoStartState = AutoStartState.activityScan
            //postStatusEventSticky(AutoModeStatusEvent(AutoModeStatus.stopped))
            stopDrivingMode()
            startActivityScan()
          //  mLogger.debug("Triggering auto stop")

           // postDrivingEventSticky(AutoModeDrivingEvent(java.lang.Boolean.FALSE))
        }

        override fun onLocationChanged(location: Location) {
            resetGpsWatchdog()
            when (autoStartState) {
                AutoStartState.driving -> {
                    if (location.speed < SPEED_MOVEMENT_THRESHOLD) {
                       // mLogger.debug("Car enter still mode")
                        autoStartState = AutoStartState.drivingAndStill
                        //postStatusEventSticky(AutoModeStatusEvent(AutoModeStatus.stopped))
                        _stopTimeBeginMs = System.currentTimeMillis()
                    }
                }
                AutoStartState.drivingAndStill -> {
                    if (location.speed >= SPEED_MOVEMENT_THRESHOLD) {
                        //mLogger.debug("Car enter moving mode")
                        autoStartState = AutoStartState.driving
                        //postStatusEventSticky(AutoModeStatusEvent(AutoModeStatus.driving))
                    } else if (System.currentTimeMillis() - _stopTimeBeginMs > sStopTimer) {
                       // mLogger.debug("Car stopped value timeout")
                        triggerAutoStop()
                    }
                }
                else -> {
                    //mLogger.warn("mLocationLister() - this state should not happen: {}", mAutoStartState)
                }
            }
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            val stringStatus: String
            when (status) {
                LocationProvider.OUT_OF_SERVICE -> stringStatus = "OUT_OF_SERVICE"
                LocationProvider.AVAILABLE -> stringStatus = "AVAILABLE"
                LocationProvider.TEMPORARILY_UNAVAILABLE -> stringStatus = "TEMPORARILY_UNAVAILABLE"
                else -> stringStatus = "UNKNOWN"
            }
           // mLogger.debug("LocationListener.onStatusChanged() - Status: {}", stringStatus)
        }

        override fun onProviderEnabled(s: String) {
           //mLogger.debug("LocationListener.onProviderEnabled() - GPS enabled by user")
        }

        override fun onProviderDisabled(s: String) {
            //mLogger.debug("LocationListener.onProviderDisabled() - GPS disabled by user")
        }
    };


    private fun forceStopAll() {
        autoStartState = AutoStartState.idle
      //  postStatusEventSticky(AutoModeStatusEvent(AutoModeStatus.service_not_started))
        try {
            stopDrivingMode()
            stopSpeedScan()
            stopActivityScan()
            stopPassiveScan()
        } catch (e: IllegalArgumentException) { // Because of possible unregister of broadcast receiver
           // mLogger.debug("Not serious, but to check: ", e)
        }

    }

    private fun goStateActivityScan() {
        //mLogger.trace("goStateActivityScan()")
        forceStopAll()
        autoStartState = AutoStartState.activityScan
        //postStatusEventSticky(AutoModeStatusEvent(AutoModeStatus.waiting_scan_trigger))
        startPassiveScan()
        startActivityScan()
    }

    private fun startPassiveScan() {
      //  mLogger.trace("startPassiveScan()")
        try {
            locationManager?.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 0L, 0.0f, mPassiveLocationListener)
        } catch (e: SecurityException) {
        //    mLogger.info("Cannot fully execute startPassiveScan() due to user restrictions", e)
        }

    }

    private fun stopPassiveScan() {
        //mLogger.trace("stopPassiveScan()")
        try {
            locationManager?.removeUpdates(mPassiveLocationListener)
        } catch (e: SecurityException) {
           // mLogger.info("Cannot fully execute stopPasiveScan() due to user restrictions", e)
        }

    }

    private fun startActivityScan() {
        //mLogger.trace("startActivityScan()")
        if (locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER)!!) {
            //mLogger.info("GPS is enabled, authorizing activity scan")
        } else {
           // mLogger.info("GPS is not enabled, waiting for it to activate activity scan")
            forceStopAll()
            return
        }
        registerReceiver(mActivityReceiver, IntentFilter(ACTIVITY_INTENT_ACTION))

       // val activityRecognitionClient = ActivityRecognition.getClient(this)


        activityRecognitionClient.requestActivityUpdates(ACTIVITY_DETECTION_INTERVAL, mActivityPendingIntent)

        /*ActivityRecognition
                .ActivityRecognitionApi
                .requestActivityUpdates(mGoogleApiClient, ACTIVITY_DETECTION_INTERVAL, mActivityPendingIntent)*/
    }

    private fun stopActivityScan() {
        //mLogger.trace("stopActivityScan()")
        try {
            unregisterReceiver(mActivityReceiver)
        } catch (e: Exception) {
          //  mLogger.debug("stopActivityScan() - Not serious, but to check: ", e)
        }


        activityRecognitionClient
                .removeActivityUpdates(mActivityPendingIntent)

        /*ActivityRecognition
                .ActivityRecognitionApi
                .removeActivityUpdates(mGoogleApiClient, mActivityPendingIntent)*/
    }

    private fun startSpeedScan() {
       // mLogger.trace("startSpeedScan()")
        try {
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0.0f, mSpeedLocationListener)
        } catch (e: SecurityException) {
            //mLogger.info("Cannot fully execute startSpeedScan() due to user restrictions", e)
        }

        //mHandler.postDelayed(mSpeedScanEnd, SPEED_SCAN_TIME.toLong())
    }


    private fun startDrivingMode() {
       // mLogger.trace("startDrivingMode()")
        mDrivingLocationListener.resetGpsWatchdog()
        try {
            locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0.0f, mDrivingLocationListener)
        } catch (e: SecurityException) {
           // mLogger.info("Cannot fully execute startDrivingMode() due to user restrictions", e)
        }

    }

    private fun stopDrivingMode() {
        //mLogger.trace("stopDrivingMode()")
        mDrivingLocationListener.stopGpsWatchdog()
        try {
            locationManager?.removeUpdates(mDrivingLocationListener)
        } catch (e: SecurityException) {
            //mLogger.info("Cannot fully execute stopDrivingMode() due to user restrictions", e)
        }

    }

    private fun stopSpeedScan() {
      //  mLogger.trace("stopSpeedScan()")
        try {
            locationManager?.removeUpdates(mSpeedLocationListener)
        } catch (e: SecurityException) {
            //mLogger.info("Cannot fully execute stopSpeedScan() due to user restrictions", e)
        }

       // mHandler.removeCallbacks(mSpeedScanEnd)
    }


}