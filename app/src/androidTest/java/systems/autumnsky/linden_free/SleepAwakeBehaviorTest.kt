package systems.autumnsky.linden_free


import android.text.format.DateFormat
import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import io.realm.Realm
import io.realm.Sort
import io.realm.kotlin.where
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import systems.autumnsky.linden_free.model.Event

@LargeTest
@RunWith(AndroidJUnit4::class)
class SleepAwakeBehaviorTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun sleepAwakeBehaviorTest() {
        val appCompatButton = onView(
            allOf(
                withId(R.id.sleep_button), withText("Sleep"),
                childAtPosition(
                    allOf(
                        withId(R.id.medicine),
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
        appCompatButton.perform(click())

        val timeString = Realm.getDefaultInstance().where<Event>()
            .sort("time", Sort.DESCENDING)
            .equalTo("event_name", "Sleep")
            .findFirst()?.time
                .let { DateFormat.format("hh:mm", it).toString() }

        val textView = onView(withId(R.id.in_sleep_time))
        textView.check(matches(withText(timeString)))

        val appCompatButton2 = onView(
            allOf(
                withId(R.id.awake_button), withText("Awake"),
                childAtPosition(
                    allOf(
                        withId(R.id.in_sleep_fragment),
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
