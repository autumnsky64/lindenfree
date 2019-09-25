package systems.autumnsky.linden_free


import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.rule.GrantPermissionRule
import androidx.test.runner.AndroidJUnit4
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class LogOutputTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Rule
    @JvmField
    var mGrantPermissionRule = GrantPermissionRule.grant(
        "android.permission.WRITE_EXTERNAL_STORAGE"
    )

    @Test
    fun logOutputTest() {
        val bottomNavigationItemView = onView(
            allOf(
                withId(R.id.navigation_medicine),
                withContentDescription("Medicine"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.nav_view), 0
                    ), 0
                ),
                isDisplayed()
            )
        )
        bottomNavigationItemView.perform(click())

        val actionMenuItemView = onView(
            allOf(
                withId(R.id.add_medicine), withContentDescription("Add Medicine"), childAtPosition(
                    childAtPosition(
                        withId(R.id.action_bar), 1
                    ), 0
                ), isDisplayed()
            )
        )
        actionMenuItemView.perform(click())

        val appCompatEditText = onView(
            allOf(
                withId(R.id.input_medicine_name), childAtPosition(
                    allOf(
                        withId(R.id.edit_log_medicine_quantity), childAtPosition(
                            withId(android.R.id.content), 0
                        )
                    ), 2
                ), isDisplayed()
            )
        )
        appCompatEditText.perform(replaceText("Medicine1"), closeSoftKeyboard())

        val appCompatEditText2 = onView(
            allOf(
                withId(R.id.input_regular_quantity), childAtPosition(
                    allOf(
                        withId(R.id.edit_log_medicine_quantity), childAtPosition(
                            withId(android.R.id.content), 0
                        )
                    ), 4
                ), isDisplayed()
            )
        )
        appCompatEditText2.perform(replaceText("200"), closeSoftKeyboard())

        val appCompatButton = onView(
            allOf(
                withId(R.id.save_medicine), withText("Save"), childAtPosition(
                    allOf(
                        withId(R.id.edit_log_medicine_quantity), childAtPosition(
                            withId(android.R.id.content), 0
                        )
                    ), 0
                ), isDisplayed()
            )
        )
        appCompatButton.perform(click())

        val actionMenuItemView2 = onView(
            allOf(
                withId(R.id.add_medicine), withContentDescription("Add Medicine"), childAtPosition(
                    childAtPosition(
                        withId(R.id.action_bar), 1
                    ), 0
                ), isDisplayed()
            )
        )
        actionMenuItemView2.perform(click())

        val appCompatEditText3 = onView(
            allOf(
                withId(R.id.input_medicine_name), childAtPosition(
                    allOf(
                        withId(R.id.edit_log_medicine_quantity), childAtPosition(
                            withId(android.R.id.content), 0
                        )
                    ), 2
                ), isDisplayed()
            )
        )
        appCompatEditText3.perform(replaceText("Medicine2"), closeSoftKeyboard())

        val appCompatEditText4 = onView(
            allOf(
                withId(R.id.input_regular_quantity), childAtPosition(
                    allOf(
                        withId(R.id.edit_log_medicine_quantity), childAtPosition(
                            withId(android.R.id.content), 0
                        )
                    ), 4
                ), isDisplayed()
            )
        )
        appCompatEditText4.perform(replaceText("1.25"), closeSoftKeyboard())

        val appCompatButton2 = onView(
            allOf(
                withId(R.id.save_medicine), withText("Save"), childAtPosition(
                    allOf(
                        withId(R.id.edit_log_medicine_quantity), childAtPosition(
                            withId(android.R.id.content), 0
                        )
                    ), 0
                ), isDisplayed()
            )
        )
        appCompatButton2.perform(click())

        val actionMenuItemView3 = onView(
            allOf(
                withId(R.id.add_medicine), withContentDescription("Add Medicine"), childAtPosition(
                    childAtPosition(
                        withId(R.id.action_bar), 1
                    ), 0
                ), isDisplayed()
            )
        )
        actionMenuItemView3.perform(click())

        val appCompatEditText5 = onView(
            allOf(
                withId(R.id.input_medicine_name), childAtPosition(
                    allOf(
                        withId(R.id.edit_log_medicine_quantity), childAtPosition(
                            withId(android.R.id.content), 0
                        )
                    ), 2
                ), isDisplayed()
            )
        )
        appCompatEditText5.perform(replaceText("Medicine3"), closeSoftKeyboard())

        val appCompatButton3 = onView(
            allOf(
                withId(R.id.save_medicine), withText("Save"), childAtPosition(
                    allOf(
                        withId(R.id.edit_log_medicine_quantity), childAtPosition(
                            withId(android.R.id.content), 0
                        )
                    ), 0
                ), isDisplayed()
            )
        )
        appCompatButton3.perform(click())

        val constraintLayout = onView(
            allOf(
                withId(R.id.medicine), childAtPosition(
                    allOf(
                        withId(R.id.medicine_list), childAtPosition(
                            withId(R.id.medicine), 1
                        )
                    ), 1
                ), isDisplayed()
            )
        )
        constraintLayout.perform(click())

        val appCompatEditText6 = onView(
            allOf(
                withId(R.id.input_adjustment_step), childAtPosition(
                    allOf(
                        withId(R.id.edit_log_medicine_quantity), childAtPosition(
                            withId(android.R.id.content), 0
                        )
                    ), 5
                ), isDisplayed()
            )
        )
        appCompatEditText6.perform(replaceText("0.25"), closeSoftKeyboard())

        val appCompatButton4 = onView(
            allOf(
                withId(R.id.save_medicine), withText("Save"), childAtPosition(
                    allOf(
                        withId(R.id.edit_log_medicine_quantity), childAtPosition(
                            withId(android.R.id.content), 0
                        )
                    ), 0
                ), isDisplayed()
            )
        )
        appCompatButton4.perform(click())

        val bottomNavigationItemView2 = onView(
            allOf(
                withId(R.id.navigation_home), withContentDescription("Home"), childAtPosition(
                    childAtPosition(
                        withId(R.id.nav_view), 0
                    ), 1
                ), isDisplayed()
            )
        )
        bottomNavigationItemView2.perform(click())

        val appCompatButton5 = onView(
            allOf(
                withId(R.id.dose_button), withText("Dose"), childAtPosition(
                    allOf(
                        withId(R.id.constraintLayout), childAtPosition(
                            withId(R.id.medicine), 3
                        )
                    ), 0
                ), isDisplayed()
            )
        )
        appCompatButton5.perform(click())

        val spinner = onView(
            allOf(
                withId(R.id.adjust_spinner), childAtPosition(
                    allOf(
                        withId(R.id.medicine_with_spinner), childAtPosition(
                            withId(R.id.medicines_with_spinner), 1
                        )
                    ), 1
                ), isDisplayed()
            )
        )
        spinner.perform(click())

        val bottomNavigationItemView3 = onView(
            allOf(
                withId(R.id.navigation_log), withContentDescription("Log"), childAtPosition(
                    childAtPosition(
                        withId(R.id.nav_view), 0
                    ), 2
                ), isDisplayed()
            )
        )
        bottomNavigationItemView3.perform(click())

        val bottomNavigationItemView4 = onView(
            allOf(
                withId(R.id.navigation_home), withContentDescription("Home"), childAtPosition(
                    childAtPosition(
                        withId(R.id.nav_view), 0
                    ), 1
                ), isDisplayed()
            )
        )
        bottomNavigationItemView4.perform(click())

        val appCompatButton6 = onView(
            allOf(
                withId(R.id.sleep_button), withText("Sleep"), childAtPosition(
                    allOf(
                        withId(R.id.medicine), childAtPosition(
                            withId(android.R.id.content), 0
                        )
                    ), 2
                ), isDisplayed()
            )
        )
        appCompatButton6.perform(click())

        val appCompatButton7 = onView(
            allOf(
                withId(R.id.awake_button), withText("Awake"), childAtPosition(
                    allOf(
                        withId(R.id.in_sleep_fragment), childAtPosition(
                            withId(android.R.id.content), 0
                        )
                    ), 0
                ), isDisplayed()
            )
        )
        appCompatButton7.perform(click())

        val bottomNavigationItemView5 = onView(
            allOf(
                withId(R.id.navigation_log), withContentDescription("Log"), childAtPosition(
                    childAtPosition(
                        withId(R.id.nav_view), 0
                    ), 2
                ), isDisplayed()
            )
        )
        bottomNavigationItemView5.perform(click())

        val actionMenuItemView4 = onView(
            allOf(
                withId(R.id.dl_log), withContentDescription("Download log file"), childAtPosition(
                    childAtPosition(
                        withId(R.id.action_bar), 1
                    ), 0
                ), isDisplayed()
            )
        )
        actionMenuItemView4.perform(click())
    }

    private fun childAtPosition(
        parentMatcher: Matcher<View>, position: Int
    ): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent) && view == parent.getChildAt(
                    position
                )
            }
        }
    }
}
