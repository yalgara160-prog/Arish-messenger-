package com.example

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.UserProfile
import com.example.data.repository.ArishRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ArishRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun unauthenticatedUserCannotWriteProfile() {
        val repo = ArishRepository(firestore)
        var caughtException = false
        try {
            runBlocking {
                repo.saveUserProfile(UserProfile(displayName = "Test", username = "tester"))
            }
        } catch (e: Exception) {
            caughtException = true
        }
        assertTrue("Expected failure for unauthenticated write", caughtException)
    }

    @Test
    fun authenticatedUserCanSaveAndRetrieveProfile() = runBlocking {
        val uid = signInTestUser("user1@example.com")
        val repo = ArishRepository(firestore)

        val profile = UserProfile(
            userId = uid,
            displayName = "Arish Tester",
            username = "arishtest",
            phoneNumber = "+1234567890",
            about = "Hello Arish!"
        )

        repo.saveUserProfile(profile)

        val retrieved = repo.observeUserProfile(uid).first()
        assertNotNull("Retrieved profile should not be null", retrieved)
        assertEquals("Arish Tester", retrieved?.displayName)
        assertEquals("arishtest", retrieved?.username)
    }
}
