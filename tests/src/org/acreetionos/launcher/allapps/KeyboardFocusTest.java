/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.acreetionos.launcher.allapps;

import android.view.KeyEvent;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import org.acreetionos.launcher.Launcher;
import org.acreetionos.launcher.LauncherState;
import org.acreetionos.launcher.util.BaseLauncherActivityTest;
import org.acreetionos.launcher.util.rule.ScreenRecordRule.ScreenRecord;
import org.acreetionos.launcher.views.ActivityContext;

import org.junit.Test;
import org.junit.runner.RunWith;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class KeyboardFocusTest extends BaseLauncherActivityTest<Launcher> {

    @Test
    public void testAllAppsFocusApp() {
        loadLauncherSync();
        goToState(LauncherState.ALL_APPS);
        freezeAllApps();

        injectKeyEvent(KeyEvent.KEYCODE_DPAD_DOWN, true);
        injectKeyEvent(KeyEvent.KEYCODE_DPAD_DOWN, false);
        waitForLauncherCondition("No focused child", launcher ->
                launcher.getAppsView().getActiveRecyclerView().getApps().getFocusedChild()
                        != null);
    }

    @Test
    public void testAllAppsExitSearchAndFocusApp() {
        loadLauncherSync();
        goToState(LauncherState.ALL_APPS);
        freezeAllApps();

        executeOnLauncher(launcher -> launcher.getAppsView().getSearchView().requestFocus());
        waitForLauncherCondition("Search view does not have focus.",
                launcher -> launcher.getAppsView().getSearchView().hasFocus());

        injectKeyEvent(KeyEvent.KEYCODE_DPAD_DOWN, true);
        injectKeyEvent(KeyEvent.KEYCODE_DPAD_DOWN, false);
        waitForLauncherCondition("No focused child", launcher ->
                launcher.getAppsView().getActiveRecyclerView().getApps().getFocusedChild()
                        != null);
    }

    @Test
    @ScreenRecord  //b/378167329
    public void testAllAppsExitSearchAndFocusSearchResults() {
        loadLauncherSync();
        goToState(LauncherState.ALL_APPS);
        freezeAllApps();

        executeOnLauncher(launcher -> launcher.getAppsView().getSearchView().requestFocus());
        waitForLauncherCondition("Search view does not have focus.",
                launcher -> launcher.getAppsView().getSearchView().hasFocus());

        injectKeyEvent(KeyEvent.KEYCODE_C, true);
        waitForLauncherCondition("Search view not active.",
                launcher -> launcher.getAppsView().getActiveRecyclerView()
                        instanceof SearchRecyclerView);
        injectKeyEvent(KeyEvent.KEYCODE_C, false);

        executeOnLauncher(launcher -> launcher.getAppsView().getSearchUiManager().getEditText()
                .hideKeyboard(/* clearFocus= */ false));
        waitForLauncherCondition("Keyboard still visible.",
                ActivityContext::isSoftwareKeyboardHidden);

        injectKeyEvent(KeyEvent.KEYCODE_DPAD_DOWN, true);
        injectKeyEvent(KeyEvent.KEYCODE_DPAD_DOWN, false);
        waitForLauncherCondition("No focused child", launcher ->
                launcher.getAppsView().getActiveRecyclerView().getApps().getFocusedChild()
                        != null);
    }
}
