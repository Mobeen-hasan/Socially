package com.visioncoders.i220795_i210611

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(DetailedLoginActivity::class.java)

    @Test
    fun testLoginSuccess() {
        // Use replaceText and a valid email format for the username
        onView(withId(R.id.username_input)).perform(replaceText("aman@gmail.com"), closeSoftKeyboard())

        // Use replaceText for the password
        onView(withId(R.id.password_input)).perform(replaceText("Test@123"), closeSoftKeyboard())

        // Click the login button
        onView(withId(R.id.login_button_detailed)).perform(click())

        // It's possible the test is running faster than the app can respond.
        // Add a delay to allow time for the login to process and the new activity to load.
        // Note: In a real-world scenario, it's better to use Idling Resources for this.
        Thread.sleep(3000)

        // Check if the top_bar in the HomeActivity is displayed
        onView(withId(R.id.top_bar)).check(matches(isDisplayed()))
    }
}