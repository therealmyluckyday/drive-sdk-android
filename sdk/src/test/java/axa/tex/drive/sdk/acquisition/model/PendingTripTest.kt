package axa.tex.drive.sdk.acquisition.model

import org.junit.Assert
import org.junit.Test

class PendingTripTest {


        @Test
        fun testEventInitialization() {
            val id = "1234"
            val tripId = "f15aega43938fs1d0d00057"
            val pendingTrip = PendingTrip(id, tripId, false)
            Assert.assertTrue(pendingTrip.id == id)
            Assert.assertTrue(pendingTrip.tripId == tripId)
        }
}