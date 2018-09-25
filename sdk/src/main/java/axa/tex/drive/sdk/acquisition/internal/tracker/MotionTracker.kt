package axa.tex.drive.sdk.acquisition.internal.tracker

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.util.SparseArray
import android.util.SparseIntArray
import axa.tex.drive.sdk.acquisition.model.Data
import axa.tex.drive.sdk.acquisition.model.Fix
import axa.tex.drive.sdk.acquisition.model.Motion
import axa.tex.drive.sdk.acquisition.model.MotionFix
import io.reactivex.subjects.PublishSubject
import java.util.*


internal class MotionTracker : SensorEventListener, Tracker{


    private val MOTION_TAG : String = "MOTION_" + (axa.tex.drive.sdk.acquisition.collection.internal.Collector::class.java.simpleName).toUpperCase();
    private val SENSORS_TYPES = intArrayOf(Sensor.TYPE_GRAVITY, Sensor.TYPE_LINEAR_ACCELERATION, Sensor.TYPE_MAGNETIC_FIELD, Sensor.TYPE_ACCELEROMETER)
    private val GRAVITY_FORCE = 9.81f
    private val DEFAULT_ACCELERATION_THRESHOLD = 2.5f // [G]
    private val DEFAULT_STRESSED_CAPTURE_RATE = 10 * 1000 // 10 ms = 100 Hz
    private val DEFAULT_BUFFER_LIMIT = 10 * 1000 // record 10 sec before and 5 sec after the acceleration event

    private var motionBufferLimit = DEFAULT_BUFFER_LIMIT

    private val sensors: SparseArray<Sensor>
    private val sensorManager: SensorManager
    private val accuracies: SparseIntArray
    private val SENSOR_KEYS = arrayOf("GRAVITY", "LINEAR_ACCELERATION", "MAGNETIC_FIELD", "ACCELERATION")
    private var stressedCaptureRate = DEFAULT_STRESSED_CAPTURE_RATE
    private var accelerationThreshold = DEFAULT_ACCELERATION_THRESHOLD // [G]
    private var isOverAccelerationThreshold = false
    private var accelerationEventTimestamp: Long = 0
    private val motionBufferQueue = LinkedList<Fix>()

    private val fix : MutableLiveData<Fix>;

    private val motionFixes : MutableLiveData<LinkedList<Fix>> = MutableLiveData();

    //private val fixProducer: PublishSubject<List<Fix>> = PublishSubject.create()

    private val fixProducer: PublishSubject<List<Data>> = PublishSubject.create()

    private val motionBuffer = MotionBuffer()

    private var isEnabled : Boolean


    constructor(context: Context, isEnabled : Boolean = false){
        this.isEnabled = isEnabled
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensors = SparseArray()
        for (i in SENSORS_TYPES.indices) {
            val type = SENSORS_TYPES[i]
            val sensor = sensorManager.getDefaultSensor(type)
            if (sensor != null) {
                sensors.put(type, sensor)
                Log.d(MOTION_TAG,"Found sensor {} of type: "+ SENSOR_KEYS[i])
            } else {
                Log.w(MOTION_TAG,"Found no sensor of type : "+SENSOR_KEYS[i])
            }
        }
        accuracies = SparseIntArray()
        this.fix = MutableLiveData<Fix>()
    }


    override fun provideFixProducer(): Any {
        return fixProducer;
    }

    override fun enableTracking() {
        enableTracking(true)
    }

    override fun disableTracking() {
        sensorManager.unregisterListener(this)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        if (sensor != null) {
            accuracies.put(sensor.getType(), accuracy)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val currentTimestamp =  System.currentTimeMillis()
        val sensorType = event?.sensor?.type

        // We have to maintain an array of the sensor accuracies as declared in onAccuracyChanged instead of checking
        // event.accuracy because SensorEvent objects do not report correct accuracies on some devices (e.g. Samsung Galaxy S4)
        val accuracy = sensorType?.let { accuracies.get(it, SensorManager.SENSOR_STATUS_UNRELIABLE) }
        if (accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) return
        // Check if sensor values are not NaN or infinite
        if(event != null) {
            for (value in event.values) {
                if (java.lang.Float.isNaN(value) || java.lang.Float.isInfinite(value)) {
                    Log.w(MOTION_TAG,"Skipping sensor value NaN or infinite")
                    return
                }
            }
        }


            event?.timestamp = currentTimestamp // Warning: this line convert timestamp from ns to ms
            if (sensorType == Sensor.TYPE_ACCELEROMETER) { // check if there is and event
                if (normL2(event.values) >= accelerationThreshold * GRAVITY_FORCE) {
                    if (!isOverAccelerationThreshold) {
                        Log.w(MOTION_TAG,"Acceleration event detected")
                        accelerationEventTimestamp = Date().time

                       // streamMotions()

                       // motionFixes.value = motionBufferQueue

                       // streamMotions()
                       // val streamWorker = StreamWorker()
                       // streamWorker.start()

                        val buf = motionBuffer.flush()
                        fixProducer.onNext(buf)

                      //  mDelegate.onEventDetected(Event.ACCELERATION) // with this callback I flush the accumulated motions
                       // comp.addEvent(Event.ACCELERATION)
                        isOverAccelerationThreshold = true
                    }
                } else {
                    if (isOverAccelerationThreshold) {
                        isOverAccelerationThreshold = false
                    }
                }
            } else { // add this sensor motion (gravity, linear accel. and magnetometer) to the list of motions
               /* synchronized(motionBufferQueue) {
                    val motionFix = event?.let { motionFix(it,event.timestamp, 0) }
                    motionFix?.let { motionBufferQueue.add(it) }
                    //removeExpiredMotions()
                    ExpirartionWorker().start()

                }*/
                //event?.let { ExpirartionWorker(it).start() }

                val motionFix = event?.let { motionFix(it,event.timestamp) }
                motionBuffer.addFix(motionFix)
               /* motionBufferQueue.add(motionFix!!)
                if(motionBufferQueue.size == 100){
                    fixProducer.onNext(motionBufferQueue.toList())
                    motionBufferQueue.clear()
                }*/
                //motionFix?.let { fixProducer.onNext(it) }


            }
    }


    private fun removeExpiredMotions(event: SensorEvent) {
        synchronized(motionBufferQueue) {

            val motionFix = event?.let { motionFix(it,event.timestamp) }
            motionFix?.let { motionBufferQueue.add(it) }

            val currentDate = Date().time
            while (motionBufferQueue.size > 0 && currentDate - motionBufferQueue.first.timestamp() > motionBufferLimit) {
                motionBufferQueue.removeFirst()
            }
        }
    }

    private fun streamMotions() {
        synchronized(motionBufferQueue) {

            //fixProducer.onNext(motionBufferQueue.toList())
           // motionBufferQueue.clear()

        }
    }


    inner class StreamWorker() : Thread() {

        override fun run() {
            streamMotions()
        }

    }



    private fun enableTracking(track: Boolean) {

        if (track) {
            for (i in 0 until sensors.size()) {
                sensorManager.registerListener(this, sensors.valueAt(i), stressedCaptureRate)
            }
        } else {
            sensorManager.unregisterListener(this)
        }
    }

    private fun normL2(values: FloatArray): Double {
        var value = 0f
        for (f in values) {
            value += f * f
        }
        return Math.sqrt(value.toDouble())
    }

    private fun motionFix(event: SensorEvent, timestamp:Long) :MotionFix?{
        val motion = Motion(event.values[0],event.values[1],event.values[2], timestamp);
        return when (event.sensor.type) {
            Sensor.TYPE_LINEAR_ACCELERATION -> MotionFix(acceleration = motion,timestamp = timestamp)
            Sensor.TYPE_ACCELEROMETER -> MotionFix(rawAcceleration = motion,timestamp = timestamp)
            Sensor.TYPE_GRAVITY -> MotionFix(gravity = motion,timestamp = timestamp)
            Sensor.TYPE_GYROSCOPE -> MotionFix(rotationRate = motion,timestamp = timestamp)
            Sensor.TYPE_MAGNETIC_FIELD -> MotionFix(magnetometer = motion,timestamp = timestamp)
            else -> null;
        }
    }

    override fun isEnabled() : Boolean{
        return isEnabled
    }
}