package systems.autumnsky.linden_free


import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
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
class AddMedicineTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun addMedicineTest() {
        val bottomNavigationItemView = onView(
            allOf(
                withId(R.id.navigation_medicine), withContentDescription("Medicine"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.nav_view),
                        0
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        bottomNavigationItemView.perform(click())

        val actionMenuItemView = onView(
            allOf(
                withId(R.id.add_medicine), withContentDescription("Add Medicine"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.action_bar),
                        1
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        actionMenuItemView.perform(click())

        val appCompatEditText = onView(
            allOf(
                withId(R.id.input_medicine_name),
                childAtPosition(
                    allOf(
                        withId(R.id.edit_log_medicine_quantity),
                        childAtPosition(
                            withId(android.R.id.content),
                            0
                        )
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        appCompatEditText.perform(click())

        val appCompatEditText2 = onView(
            allOf(
                withId(R.id.input_medicine_name),
                childAtPosition(
                    allOf(
                        withId(R.id.edit_log_medicine_quantity),
                        childAtPosition(
                            withId(android.R.id.content),
                            0
                        )
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        appCompatEditText2.perform(replaceText("test1"), closeSoftKeyboard())

        val appCompatEditText3 = onView(
            allOf(
                withId(R.id.input_regular_quantity),
                childAtPosition(
                    allOf(
                        withId(R.id.edit_log_medicine_quantity),
                        childAtPosition(
                            withId(android.R.id.content),
                            0
                        )
                    ),
                    4
                ),
                isDisplayed()
            )
        )
        appCompatEditText3.perform(replaceText("300"), closeSoftKeyboard())

        val appCompatEditText4 = onView(
            allOf(
                withId(R.id.input_adjustment_step),
                childAtPosition(
                    allOf(
                        withId(R.id.edit_log_medicine_quantity),
                        childAtPosition(
                            withId(android.R.id.content),
                            0
                        )
                    ),
                    5
                ),
                isDisplayed()
            )
        )
        appCompatEditText4.perform(replaceText("100"), closeSoftKeyboard())

        val appCompatButton = onView(
            allOf(
                withId(R.id.save_medicine), withText("Save"),
                childAtPosition(
                    allOf(
                        withId(R.id.edit_log_medicine_quantity),
                        childAtPosition(
                            withId(android.R.id.content),
                            0
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        appCompatButton.perform(click())

        val actionMenuItemView2 = onView(
            allOf(
                withId(R.id.add_medicine), withContentDescription("Add Medicine"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.action_bar),
                        1
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        actionMenuItemView2.perform(click())

        val appCompatEditText5 = onView(
            allOf(
                withId(R.id.input_medicine_name),
                childAtPosition(
                    allOf(
                        withId(R.id.edit_log_medicine_quantity),
                        childAtPosition(
                            withId(android.R.id.content),
                            0
                        )
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        appCompatEditText5.perform(click())

        val appCompatEditText6 = onView(
            allOf(
                withId(R.id.input_medicine_name),
                childAtPosition(
                    allOf(
                        withId(R.id.edit_log_medicine_quantity),
                        childAtPosition(
                            withId(android.R.id.content),
                            0
                        )
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        appCompatEditText6.perform(replaceText("test2"), closeSoftKeyboard())

        val appCompatButton2 = onView(
            allOf(
                withId(R.id.save_medicine), withText("Save"),
                childAtPosition(
                    allOf(
                        withId(R.id.edit_log_medicine_quantity),
                        childAtPosition(
                            withId(android.R.id.content),
                            0
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        appCompatButton2.perform(click())

        val actionMenuItemView3 = onView(
            allOf(
                withId(R.id.add_medicine), withContentDescription("Add Medicine"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.action_bar),
                        1
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        actionMenuItemView3.perform(click())

        val appCompatEditText7 = onView(
            allOf(
                withId(R.id.input_medicine_name),
                childAtPosition(
                    allOf(
                        withId(R.id.edit_log_medicine_quantity),
                        childAtPosition(
                            withId(android.R.id.content),
                            0
                        )
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        appCompatEditText7.perform(click())

        val appCompatEditText8 = onView(
            allOf(
                withId(R.id.input_medicine_name),
                childAtPosition(
                    allOf(
                        withId(R.id.edit_log_medicine_quantity),
                        childAtPosition(
                            withId(android.R.id.content),
                            0
                        )
                    ),
                    2
                ),
                isDisplayed()
            )
        )
        appCompatEditText8.perform(replaceText("delete test"), closeSoftKeyboard())

        val appCompatButton3 = onView(
            allOf(
                withId(R.id.save_medicine), withText("Save"),
                childAtPosition(
                    allOf(
                        withId(R.id.edit_log_medicine_quantity),
                        childAtPosition(
                            withId(android.R.id.content),
                            0
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        appCompatButton3.perform(click())

        val constraintLayoutForDelete = onView(
            allOf(
                withId(R.id.medicine),
                childAtPosition(
                    allOf(
                        withId(R.id.medicine_list),
                        childAtPosition(
                            withId(R.id.medicine),
                            1
                        )
                    ),
                    3
                ),
                isDisplayed()
            )
        )
        constraintLayoutForDelete.perform(swipeLeft())

        val appCompatButton4 = onView(
            allOf(
                withId(android.R.id.button1), withText("Delete"),
                childAtPosition(
                    childAtPosition(
                        withId(R.id.buttonPanel),
                        0
                    ),
                    3
                )
            )
        )
        appCompatButton4.perform(click())

        val constraintLayout = onView(
            allOf(
                withId(R.id.medicine),
                childAtPosition(
                    allOf(
                        withId(R.id.medicine_list),
                        childAtPosition(
                            withId(R.id.medicine),
                            1
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        constraintLayout.perform(click())

        val appCompatEditText9 = onView(
            allOf(
                withId(R.id.input_regular_quantity),
                childAtPosition(
                    allOf(
                        withId(R.id.edit_log_medicine_quantity),
                        childAtPosition(
                            withId(android.R.id.content),
                            0
                        )
                    ),
                    4
                ),
                isDisplayed()
            )
        )
        appCompatEditText9.perform(replaceText("2"), closeSoftKeyboard())

        val appCompatButton5 = onView(
            allOf(
                withId(R.id.cancel_medicine), withText("Cancel"),
                childAtPosition(
                    allOf(
                        withId(R.id.edit_log_medicine_quantity),
                        childAtPosition(
                            withId(android.R.id.content),
                            0
                        )
                    ),
                    11
                ),
                isDisplayed()
            )
        )
        appCompatButton5.perform(click())

        val constraintLayout2 = onView(
            allOf(
                withId(R.id.medicine),
                childAtPosition(
                    allOf(
                        withId(R.id.medicine_list),
                        childAtPosition(
                            withId(R.id.medicine),
                            1
                        )
                    ),
                    1
                ),
                isDisplayed()
            )
        )
        constraintLayout2.perform(click())

        val appCompatEditText10 = onView(
            allOf(
                withId(R.id.input_regular_quantity),
                childAtPosition(
                    allOf(
                        withId(R.id.edit_log_medicine_quantity),
                        childAtPosition(
                            withId(android.R.id.content),
                            0
                        )
                    ),
                    4
                ),
                isDisplayed()
            )
        )
        appCompatEditText10.perform(replaceText("2"), closeSoftKeyboard())

        val appCompatEditText11 = onView(
            allOf(
                withId(R.id.input_adjustment_step),
                childAtPosition(
                    allOf(
                        withId(R.id.edit_log_medicine_quantity),
                        childAtPosition(
                            withId(android.R.id.content),
                            0
                        )
                    ),
                    5
                ),
                isDisplayed()
            )
        )
        appCompatEditText11.perform(replaceText("0.5"), closeSoftKeyboard())

        val appCompatButton6 = onView(
            allOf(
                withId(R.id.save_medicine), withText("Save"),
                childAtPosition(
                    allOf(
                        withId(R.id.edit_log_medicine_quantity),
                        childAtPosition(
                            withId(android.R.id.content),
                            0
                        )
                    ),
                    0
                ),
                isDisplayed()
            )
        )
        appCompatButton6.perform(click())
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
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}
