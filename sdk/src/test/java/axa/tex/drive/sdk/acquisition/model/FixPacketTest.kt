package axa.tex.drive.sdk.acquisition.model

import org.junit.Assert
import org.junit.Test

class FixPacketTest {


    @Test
    fun testFixPacketOutput() {
        val expectedJson = "{\"model\":\"Nexus 5x\",\"os\":\"Android\",\"timezone\":\"1544313314301\",\"uid\":\"4260e592-008b-4fcf-877d-fe8d3923b5f5\",\"version\":\"7\",\"trip_id\":\"4260e592-008b-4fcf-877d-fe8d3923b5s1\",\"app_name\":\"APP-TEST\",\"client_id\":\"00001111\",\"fixes\":[{\"location\":{\"latitude\":19.00001,\"longitude\":1.0,\"precision\":5.0,\"speed\":21.0,\"bearing\":10.0,\"altitude\":18.0},\"timestamp\":14531415313},{\"location\":{\"latitude\":20.00001,\"longitude\":2.0,\"precision\":15.0,\"speed\":23.0,\"bearing\":11.0,\"altitude\":17.0},\"timestamp\":145314151}]}"
        val firstLocationFix = LocationFix(19.00001, 1.000, 5.0f, 21f, 10f, 18.0, 14531415313)
        val secondLocationFix = LocationFix(20.00001, 2.000, 15.0f, 23f, 11f, 17.0, 145314151)
        val timezone = "1544313314301"

        val fixes = listOf<Fix>(firstLocationFix, secondLocationFix)

        val model = "Nexus 5x"
        val os = "Android"
        val uid = "4260e592-008b-4fcf-877d-fe8d3923b5f5"
        val version = "7"
        val tripId = "4260e592-008b-4fcf-877d-fe8d3923b5s1"
        val appName = "APP-TEST"
        val clientId = "00001111"
        val packet = FixPacket(fixes, model, os, timezone, uid, version, tripId, appName)

        val json = packet.toJson()

        Assert.assertTrue(expectedJson == json)
    }

    @Test
    fun testFixPacketInitialization() {

        val firstLocationFix = LocationFix(19.00001, 1.000, 5.0f, 21f, 10f, 18.0, 14531415313)
        val secondLocationFix = LocationFix(20.00001, 2.000, 15.0f, 23f, 11f, 17.0, 145314151)
        val timezone = "1544313314301"

        val fixes = listOf<Fix>(firstLocationFix, secondLocationFix)

        val model = "Nexus 5x"
        val os = "Android"
        val uid = "4260e592-008b-4fcf-877d-fe8d3923b5f5"
        val version = "7"
        val tripId = "4260e592-008b-4fcf-877d-fe8d3923b5s1"
        val appName = "APP-TEST"
        val clientId = "00001111"
        val packet = FixPacket(fixes, model, os, timezone, uid, version, tripId, appName)

        Assert.assertTrue(packet.fixes.size == 2)
        Assert.assertTrue(packet.fixes[0] == firstLocationFix)
        Assert.assertTrue(packet.fixes[1] == secondLocationFix)
        Assert.assertTrue(packet.app_name == appName)
        Assert.assertTrue(packet.model == model)
        Assert.assertTrue(packet.os == os)
        Assert.assertTrue(packet.timezone == timezone)
        Assert.assertTrue(packet.uid == uid)
        Assert.assertTrue(packet.version == version)
        Assert.assertTrue(packet.trip_id == tripId)
        Assert.assertTrue(packet.trip_id == tripId)
        Assert.assertTrue(packet.fixes == fixes)
    }


}