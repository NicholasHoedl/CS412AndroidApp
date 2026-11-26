package com.example.cs412androidapp;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiScrollable;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * UI Automator test: starts CS412AndroidApp from the home screen by clicking its launcher icon,
 * taps "Start Activity Explicitly", and checks that SecondActivity shows at least one of the
 * mobile software engineering challenges listed in the app.
 */
@RunWith(AndroidJUnit4.class)
public class LaunchAppAndVerifyChallengesTest {

    private static final String APP_PACKAGE = "com.example.cs412androidapp";
    private static final long TIMEOUT_MS = 5_000;

    private UiDevice device;
    private Context appContext;

    @Before
    public void startFromHomeScreen() throws IOException {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        device.pressHome();
        String launcherPackage = getLauncherPackage();
        assertTrue("Launcher " + launcherPackage + " did not come to the foreground",
                device.wait(Until.hasObject(By.pkg(launcherPackage).depth(0)), TIMEOUT_MS));
    }

    /**
     * Resolves the home-screen app through the shell. UiDevice.getLauncherPackageName() is
     * subject to Android 11+ package visibility and can return com.android.settings instead.
     */
    private String getLauncherPackage() throws IOException {
        String output = device.executeShellCommand(
                "cmd package resolve-activity --brief -a android.intent.action.MAIN"
                        + " -c android.intent.category.HOME").trim();
        String component = output.substring(output.lastIndexOf('\n') + 1).trim();
        assertTrue("Could not resolve the launcher: " + output, component.contains("/"));
        return component.substring(0, component.indexOf('/'));
    }

    @Test
    public void launcherIcon_explicitButton_showsChallenges() throws UiObjectNotFoundException {
        // 1. Launch the app by clicking its launcher icon.
        String appName = appContext.getString(R.string.app_name);
        findLauncherIcon(appName).click();
        assertTrue("App did not open after clicking its launcher icon",
                device.wait(Until.hasObject(By.pkg(APP_PACKAGE).depth(0)), TIMEOUT_MS));

        // 2. Navigate to the second activity with the "Start Activity Explicitly" button.
        String explicitButtonText = appContext.getString(R.string.btn_start_explicit);
        UiObject2 explicitButton = device.wait(Until.findObject(By.text(explicitButtonText)), TIMEOUT_MS);
        assertNotNull("\"" + explicitButtonText + "\" button not found in MainActivity", explicitButton);
        explicitButton.click();

        // 3. Verify that at least one listed challenge is displayed in the second activity.
        List<String> challenges = Arrays.asList(appContext.getResources().getStringArray(R.array.challenges));
        Pattern anyChallenge = Pattern.compile(challenges.stream()
                .map(Pattern::quote)
                .collect(Collectors.joining("|")));
        UiObject2 shownChallenge = device.wait(
                Until.findObject(By.pkg(APP_PACKAGE).text(anyChallenge)), TIMEOUT_MS);
        assertNotNull("SecondActivity shows none of the challenges " + challenges, shownChallenge);
        assertTrue(challenges.contains(shownChallenge.getText()));
    }

    /**
     * Finds the app's icon on the home screen or, if it is not pinned there, in the app drawer.
     */
    private UiObject2 findLauncherIcon(String appName) throws UiObjectNotFoundException {
        UiObject2 icon = device.findObject(By.text(appName));
        if (icon != null) {
            return icon;
        }

        // Open the app drawer by swiping up from the lower part of the home screen.
        int centerX = device.getDisplayWidth() / 2;
        int height = device.getDisplayHeight();
        device.swipe(centerX, (int) (height * 0.75), centerX, (int) (height * 0.25), 20);

        icon = device.wait(Until.findObject(By.text(appName)), TIMEOUT_MS);
        if (icon == null) {
            // Long app lists need scrolling before the icon is on screen.
            new UiScrollable(new UiSelector().scrollable(true)).scrollTextIntoView(appName);
            icon = device.wait(Until.findObject(By.text(appName)), TIMEOUT_MS);
        }
        assertNotNull("Launcher icon \"" + appName + "\" not found on home screen or in app drawer", icon);
        return icon;
    }
}
