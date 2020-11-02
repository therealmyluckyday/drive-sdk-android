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
    fun testEventOutputAPIV1() {
        val time = 1543938100057
        val end = Event(listOf("stop"), time)
        val resultExpected = "{\"timestamp\":1543938100057,\"event\":[\"stop\"]}"
        val json = end.toJson(false)
        Assert.assertEquals("Real $json expected $resultExpected", resultExpected, json)
    }
    @Test
    fun testEventOutputAPIV2() {
        val time = 1543938100057
        val end = Event(listOf("stop"), time)
        val json = end.toJson(true)
        val resultExpected = "{\"events\":[\"stop\"],\"timestamp\":1543938100057}"
        Assert.assertEquals("Real $json expected $resultExpected", resultExpected, json)
    }
}