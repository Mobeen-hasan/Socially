
package com.visioncoders.i220795_i210611

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DmActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(DmActivity::class.java)

    @Test
    fun testNewMessage() {
        onView(withId(R.id.new_message_button)).perform(click())
        // Add an assertion to check if the new message activity is opened
        onView(withId(R.id.message_toolbar)).check(matches(isDisplayed()))
    }
}