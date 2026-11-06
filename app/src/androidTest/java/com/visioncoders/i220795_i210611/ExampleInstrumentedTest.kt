package com.visioncoders.i220795_i210611

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.auth.FirebaseAuth
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    private val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var auth: FirebaseAuth

    // --- IMPORTANT: Use a real, existing test user's credentials ---
    private val TEST_EMAIL = "aman@gmail.com"
    private val TEST_PASSWORD = "Test@123"

    @Before
    fun setupAuthentication() {
        auth = FirebaseAuth.getInstance()

        // 1. Attempt to sign in synchronously (waits for success or failure)
        val task = auth.signInWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD)


        if (task.isSuccessful) {
            println("Test user successfully logged in: ${auth.currentUser?.uid}")
        } else {
            // If sign-in fails, the test cannot proceed. Log the error.
            throw IllegalStateException("Firebase Sign-in Failed! Check credentials and network. Error: ${task.exception?.message}")
        }
    }

    @After
    fun cleanupAuthentication() {
        // Sign out the user after every test run
        auth.signOut()
    }

    // ===================================================================
    // TEST CASE 1: Launch Video Call from Message Activity
    // =================================================================
    @Test
    fun testVideoCallButtonLaunchesVideoCallActivity() {
        // Ensure user is logged in via @Before

        val messageIntent = Intent(targetContext, MessageActivity::class.java).apply {
            putExtra("CHAT_ID", "test_chat_id")
            putExtra("OTHER_USER_ID", "other_user_uid")
        }

        val messageScenario = ActivityScenario.launch<MessageActivity>(messageIntent)
        Thread.sleep(3000)

        onView(withId(R.id.video_call_button)).perform(click())

        onView(withId(R.id.local_video_view_container))
            .check(matches(isDisplayed()))

        messageScenario.close()
    }

    // ===================================================================
    // TEST CASE 2: Terminate Video Call
    // ===================================================================

    @Test
    fun testEndCallButtonFinishesVideoCallActivity() {
        // Ensure user is logged in via @Before

        val videoCallIntent = Intent(targetContext, VideoCallActivity::class.java).apply {
            putExtra("CHANNEL_ID", "testChannel123")
            putExtra("IS_VIDEO_CALL", true)
        }

        val videoCallScenario = ActivityScenario.launch<VideoCallActivity>(videoCallIntent)

        Thread.sleep(3000)

        onView(withId(R.id.end_call_button))
            .check(matches(isDisplayed()))
            .perform(click())

        videoCallScenario.onActivity { activity ->
            assert(activity.isFinishing) { "VideoCallActivity did not finish after clicking End Call." }
        }

        videoCallScenario.close()
    }
}