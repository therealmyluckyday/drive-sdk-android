package axa.tex.drive.sdk.acquisition.model

import org.junit.Assert
import org.junit.Test

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