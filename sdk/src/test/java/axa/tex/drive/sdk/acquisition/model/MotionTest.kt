package axa.tex.drive.sdk.acquisition.model

import axa.tex.drive.sdk.internal.extension.toJson
import org.junit.Assert
import org.junit.Test
import java.util.*

class MotionTest {

    @Test
    fun testMotionInitialization() {

        val timestamp = Date().time
        val motion = Motion(x= 0f, y= 1f, z= 2f, timestamp = timestamp)
        Assert.assertTrue(motion.x==0f)
        Assert.assertTrue(motion.y==1f)
        Assert.assertTrue(motion.z==2f)
        Assert.assertTrue(motion.timestamp==timestamp)
    }

    @Test
    fun testMotionOutput() {
        val timestamp = 1543940435374
        val motion = Motion(x= 0f, y= 1f, z= 2f, timestamp = timestamp)
        val expectedJson = "{\"Motion\":{\"x\":0.0,\"y\":1.0,\"z\":2.0}}"
        Assert.assertTrue(expectedJson == motion.toJson())
    }
}