package axa.tex.drive.sdk.acquisition.model

import org.junit.Assert
import org.junit.Test
import java.util.*

class MotionFixTest {


    @Test
    fun testLocationFixInitialization() {
        val timestamp = Date().time
        val rotationRate = Motion(x = 0f, y = 1f, z = 2f, timestamp = timestamp)
        val acceleration = Motion(x = 1f, y = 3f, z = 0f, timestamp = timestamp)
        val rawAcceleration = Motion(x = 2f, y = 2f, z = 0f, timestamp = timestamp)
        val gravity = Motion(x = 1f, y = 1f, z = 0f, timestamp = timestamp)
        val magnetometer = Motion(x = 2f, y = 2f, z = 0f, timestamp = timestamp)

        var motionFix = MotionFix(rotationRate = rotationRate, timestamp = timestamp)
        Assert.assertTrue(motionFix.acceleration == null)
        Assert.assertTrue(motionFix.timestamp == timestamp)
        Assert.assertTrue(motionFix.rotationRate == rotationRate)
        Assert.assertTrue(motionFix.gravity == null)
        Assert.assertTrue(motionFix.magnetometer == null)


        motionFix = MotionFix(acceleration = acceleration, timestamp = timestamp)
        Assert.assertTrue(motionFix.acceleration == acceleration)
        Assert.assertTrue(motionFix.timestamp == timestamp)
        Assert.assertTrue(motionFix.rotationRate == null)
        Assert.assertTrue(motionFix.gravity == null)
        Assert.assertTrue(motionFix.magnetometer == null)


        motionFix = MotionFix(gravity = gravity, timestamp = timestamp)
        Assert.assertTrue(motionFix.acceleration == null)
        Assert.assertTrue(motionFix.timestamp == timestamp)
        Assert.assertTrue(motionFix.rotationRate == null)
        Assert.assertTrue(motionFix.gravity == gravity)
        Assert.assertTrue(motionFix.magnetometer == null)

        motionFix = MotionFix(magnetometer = magnetometer, timestamp = timestamp)
        Assert.assertTrue(motionFix.acceleration == null)
        Assert.assertTrue(motionFix.timestamp == timestamp)
        Assert.assertTrue(motionFix.rotationRate == null)
        Assert.assertTrue(motionFix.gravity == null)
        Assert.assertTrue(motionFix.magnetometer == magnetometer)

    }

}