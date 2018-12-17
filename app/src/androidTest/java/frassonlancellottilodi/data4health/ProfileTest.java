package frassonlancellottilodi.data4health;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewAssertion;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;

/**
 * This test class represent an example of how the UI of the app could be tested. The app heavily relies on UI code, so testing the UI
 * seemed like the only test that made any sense for the sake of this prototype. Running this test requires a server to be running and a test user to have been
 * created beforehand.
 * Create a male user, age 23 and name = Test, surname = user, email = test@test.com, password = test in the register page, then run the test
 * to see the results.
 * This approach should be replicated for every activity in the app and some test users.
 */
@RunWith(AndroidJUnit4.class)
public class ProfileTest {
    @Rule
    public ActivityTestRule<LoginActivity> activityTestRule =
            new ActivityTestRule<>(LoginActivity.class);


    @Test
    public void validateEditText() {
        onView(withId(R.id.emailLoginEditText)).perform(typeText("test@test.com")).check(matches(withText("test@test.com")));
        try {
            onView(withId(R.id.passwordLoginEditText)).perform(typeText("test"), closeSoftKeyboard()).check(matches(withText("test")));
        }catch (Exception e){

        }
        onView(withId(R.id.buttonlogin)).perform(click());
        onView(withId(R.id.homepageProfileButton)).perform(click());
        onView(withId(R.id.profilePageProfileName)).check(matches(withText("Test User")));
        onView(withId(R.id.profilePageProfileText1)).check(matches(withText("Male, 23")));
        onView(withId(R.id.profilePageAlertsSubText)).check(matches(withText("None")));
        onView(withId(R.id.profilePageHeartbeatSubText)).check(matches(withText("No measurements")));
        onView(withId(R.id.profilePageStepsSubText)).check(matches(withText("0")));
        onView(withId(R.id.profileSendRequestButtonContainer)).check(matches(not(isDisplayed())));
        onView(withId(R.id.profilepageRemoveFriendButtonContainer)).check(matches(not(isDisplayed())));




        pressBack();



    }
}
