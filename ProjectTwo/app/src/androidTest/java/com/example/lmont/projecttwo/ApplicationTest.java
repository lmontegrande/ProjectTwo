package com.example.lmont.projecttwo;

import android.app.Application;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.AppCompatEditText;
import android.test.ApplicationTestCase;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.*;
import static android.support.test.espresso.action.ViewActions.*;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static android.support.test.espresso.assertion.ViewAssertions.*;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {super(Application.class);}

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<MainActivity>(MainActivity.class);

    @Before
    public void setup() throws Exception {

    }

    @After
    public void tearDown() throws Exception{

    }

    @Test
    public void testActivity() throws Exception {
        populate(); // populate the game with stuff

        String test = "New Game";
        int count;

        test1(test); // Add card game
        test3(test); // Delete card game
        test2(test); // Add card game
        test4(test); // Open game
        test6(test5(test)); // Create Card, go to the card's card activity
        // Go back to game activity, delete new card, go back to main activity
        test7(test); // Delete card game
    }

    private void test7(String test) {
        onView(withText(test))
                .perform(longClick());
    }

    private void test6(int test) {
        onView(withId(R.id.card_activity_back_button))
                .perform(click());

        onData(anything())
                .inAdapterView(withId(R.id.game_activity_listview))
                .atPosition(test)
                .perform(longClick());

        onView(withId(R.id.game_activity_back_button))
                .perform(click());
    }

    private int test5(String test) {
        final int[] counts = new int[1];

        onView(withId(R.id.game_activity_listview)).check(matches(new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View view) {
                ListView listView = (ListView) view;
                counts[0] = listView.getCount();
                return true;
            }
            @Override
            public void describeTo(Description description) {
            }
        }));

        onView(withId(R.id.game_activity_add_button))
                .perform(click());

        onView(withId(android.R.id.button1))
                .perform(click());

        onData(anything())
                .inAdapterView(withId(R.id.game_activity_listview))
                .atPosition(counts[0])
                .perform(click());

        return counts[0];
    }

    private void test4(String test) {
        onView(withText(test))
                .perform(click());
    }

    private void test3(String test) {
        onView(withText(test))
                .perform(longClick());
    }

    private int test2(String test) {
        final int[] counts = new int[1];

        onView(withId(R.id.game_listview)).check(matches(new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View view) {
                ListView listView = (ListView) view;
                counts[0] = listView.getCount();
                return true;
            }
            @Override
            public void describeTo(Description description) {
            }
        }));

        onView(withId(R.id.game_add_button))
                .perform(click());
        onView(withId(R.id.game_name))
                .perform(typeText(test));
        onView(withId(R.id.game_numof_attributes))
                .perform(typeText("Health, Attack, Power"));
        onView(withId(android.R.id.button1))
                .perform(click());

        onData(anything())
                .inAdapterView(withId(R.id.game_listview))
                .atPosition(counts[0])
                .onChildView(withId(R.id.game_list_item_textView))
                .check(matches(withText(startsWith(test))));

        return counts[0];
    }

    private void test1(String test) {
        final int[] counts = new int[1];

        onView(withId(R.id.game_listview)).check(matches(new TypeSafeMatcher<View>() {
            @Override
            public boolean matchesSafely(View view) {
                ListView listView = (ListView) view;
                counts[0] = listView.getCount();
                return true;
            }
            @Override
            public void describeTo(Description description) {
            }
        }));

        onView(withId(R.id.game_add_button))
                .perform(click());
        onView(withId(R.id.game_name))
                .perform(typeText(test));
        onView(withId(R.id.game_numof_attributes))
                .perform(typeText("Health, Attack, Power"));
        onView(withId(android.R.id.button1))
                .perform(click());

        onData(anything())
                .inAdapterView(withId(R.id.game_listview))
                .atPosition(counts[0])
                .onChildView(withId(R.id.game_list_item_textView))
                .check(matches(withText(startsWith(test))));
    }

    private void populate() {
        for (int x=0; x<10; x++) {
            String test = "New Game " + x;
            int count;

            test2(test); // Add card game
        }
    }
}