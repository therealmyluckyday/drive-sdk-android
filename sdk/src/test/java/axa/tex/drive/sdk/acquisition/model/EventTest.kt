package axa.tex.drive.sdk.acquisition.model

import org.junit.Assert
import org.junit.Test
import java.util.*

class EventTest {

    @Test
    fun testEventInitialization() {
        val time = Date().time
        val end = Event(listOf("stop"), time)
        Assert.assertTrue(end.event.size == 1)
        Assert.assertTrue(end.event[0] == "stop")
        Assert.assertTrue(time == end.timestamp())
    }

    @Test
    fun testEventOutput() {
        val time = 1543938100057
        val end = Event(listOf("stop"), time);
        val json = end.toJson()
        Assert.assertTrue(json == "{\"timestamp\":1543938100057,\"event\":[\"stop\"]}")
    }
}