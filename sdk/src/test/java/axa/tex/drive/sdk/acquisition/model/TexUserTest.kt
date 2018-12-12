package axa.tex.drive.sdk.acquisition.model

import axa.tex.drive.sdk.acquisition.internal.tracker.DEFAULT_MOTION_AGE_AFTER_ACCELERATION
import axa.tex.drive.sdk.acquisition.internal.tracker.DEFAULT_OLDER_MOTION_AGE
import org.junit.Assert
import org.junit.Test
import java.util.*

class TexUserTest {


    @Test
    fun testMotionInitialization() {
        val userId = "1234abcdefg13"
        val userToken = "ABC123"

      val user = TexUser(userId, userToken)
        Assert.assertTrue(user.authToken == userToken)
        Assert.assertTrue(user.userId == userId)
    }
}