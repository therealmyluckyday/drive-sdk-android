package axa.tex.drive.sdk.acquisition.internal.tracker

import axa.tex.drive.sdk.acquisition.model.Fix
import axa.tex.drive.sdk.acquisition.model.Motion
import axa.tex.drive.sdk.acquisition.model.MotionFix
import org.junit.Assert
import org.junit.Test
import java.util.*

class MotionBufferTest {

    private val buffer = LinkedList<Fix>()
    private val TYPE_LINEAR_ACCELERATION: Int = 0
    private val TYPE_ACCELEROMETER: Int = 1
    private val TYPE_GRAVITY: Int = 2
    private val TYPE_GYROSCOPE: Int = 3
    private val TYPE_MAGNETIC_FIELD: Int = 4

    private var olderMotionAge: Int = DEFAULT_OLDER_MOTION_AGE
    private var motionAgeAfterAcceleration: Long = DEFAULT_MOTION_PERIOD_AFTER_ACCELERATION

    fun IntRange.random() =
            Random().nextInt((endInclusive + 1) - start) + start

    @Test
    fun testAddFix() {
        val beginTime = Date().time
        var timestamp = beginTime

        val motion = Motion(0.5f, 1f, 1.1f, beginTime);
        var sensorType = (0 until 4).random()

        var i = 0
        val buffer = MotionBuffer()
        while (i < 3000) {
            val fix = when (sensorType) {
                TYPE_LINEAR_ACCELERATION -> MotionFix(acceleration = motion, timestamp = timestamp)
                TYPE_ACCELEROMETER -> MotionFix(rawAcceleration = motion, timestamp = timestamp)
                TYPE_GRAVITY -> MotionFix(gravity = motion, timestamp = timestamp)
                TYPE_GYROSCOPE -> MotionFix(rotationRate = motion, timestamp = timestamp)
                TYPE_MAGNETIC_FIELD -> MotionFix(magnetometer = motion, timestamp = timestamp)
                else -> null;
            }
            buffer.addFix(fix)

            timestamp = Date().time
            i++

        }

        Assert.assertTrue(buffer.getPeriod() <= motionAgeAfterAcceleration)
    }


    @Test
    fun testFlush() {
        val beginTime = Date().time
        var timestamp = beginTime

        val motion = Motion(0.5f, 1f, 1.1f, beginTime);
        var sensorType = (0 until 4).random()

        var i = 0
        val buffer = MotionBuffer()
        while (i < 3000) {
            val fix = when (sensorType) {
                TYPE_LINEAR_ACCELERATION -> MotionFix(acceleration = motion, timestamp = timestamp)
                TYPE_ACCELEROMETER -> MotionFix(rawAcceleration = motion, timestamp = timestamp)
                TYPE_GRAVITY -> MotionFix(gravity = motion, timestamp = timestamp)
                TYPE_GYROSCOPE -> MotionFix(rotationRate = motion, timestamp = timestamp)
                TYPE_MAGNETIC_FIELD -> MotionFix(magnetometer = motion, timestamp = timestamp)
                else -> null;
            }
            buffer.addFix(fix)

            timestamp = Date().time
            i++

        }
        val newBuf = buffer.flush()
        Assert.assertTrue(newBuf.last().timestamp() - newBuf.first().timestamp() <= motionAgeAfterAcceleration)
        Assert.assertTrue(buffer.isBufferEmpty())
    }
}