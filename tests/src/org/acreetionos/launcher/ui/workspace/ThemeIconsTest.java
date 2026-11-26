/*
 * Copyright (C) 2022 The Android Open Source Project
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
package org.acreetionos.launcher.ui.workspace;

import static org.acreetionos.launcher.AbstractFloatingView.TYPE_ACTION_POPUP;
import static org.acreetionos.launcher.util.Executors.MAIN_EXECUTOR;
import static org.acreetionos.launcher.util.TestConstants.AppNames.TEST_APP_NAME;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.view.ViewGroup;

import androidx.test.filters.LargeTest;

import org.acreetionos.launcher.AbstractFloatingView;
import org.acreetionos.launcher.BubbleTextView;
import org.acreetionos.launcher.Launcher;
import org.acreetionos.launcher.LauncherState;
import org.acreetionos.launcher.allapps.AllAppsRecyclerView;
import org.acreetionos.launcher.celllayout.FavoriteItemsTransaction;
import com.android.launcher3.icons.mono.ThemedIconDrawable;
import org.acreetionos.launcher.popup.ArrowPopup;
import org.acreetionos.launcher.util.BaseLauncherActivityTest;
import org.acreetionos.launcher.util.Executors;
import org.acreetionos.launcher.util.TestUtil;

import org.junit.Test;

/**
 * Tests for theme icon support in Launcher
 *
 * Note running these tests will clear the workspace on the device.
 */
@LargeTest
public class ThemeIconsTest extends BaseLauncherActivityTest<Launcher> {

    private static final String APP_NAME = "IconThemedActivity";
    private static final String SHORTCUT_NAME = "Shortcut 1";

    @Test
    public void testIconWithoutTheme() throws Exception {
        setThemeEnabled(false);
        new FavoriteItemsTransaction(targetContext()).commit();
        loadLauncherSync();
        goToState(LauncherState.ALL_APPS);
        freezeAllApps();

        scrollToAppIcon(APP_NAME);
        BubbleTextView btv = getFromLauncher(
                l -> verifyIconTheme(APP_NAME, l.getAppsView(), false));
        addToWorkspace(btv);
        executeOnLauncher(l -> verifyIconTheme(APP_NAME, l.getWorkspace(), false));
    }

    @Test
    public void testShortcutIconWithoutTheme() throws Exception {
        setThemeEnabled(false);
        new FavoriteItemsTransaction(targetContext()).commit();
        loadLauncherSync();
        goToState(LauncherState.ALL_APPS);
        freezeAllApps();

        scrollToAppIcon(TEST_APP_NAME);
        BubbleTextView btv = getFromLauncher(l -> findBtv(TEST_APP_NAME, l.getAppsView()));
        TestUtil.runOnExecutorSync(MAIN_EXECUTOR, btv::performLongClick);

        BubbleTextView menuItem = getOnceNotNull("Popup menu not open", l ->
                (AbstractFloatingView.getOpenView(l, TYPE_ACTION_POPUP) instanceof ArrowPopup ap)
                        ? findBtv(SHORTCUT_NAME, ap) : null);
        addToWorkspace(menuItem);
        executeOnLauncher(l -> verifyIconTheme(SHORTCUT_NAME, l.getWorkspace(), false));
    }

    @Test
    public void testIconWithTheme() throws Exception {
        setThemeEnabled(true);
        new FavoriteItemsTransaction(targetContext()).commit();
        loadLauncherSync();
        goToState(LauncherState.ALL_APPS);
        freezeAllApps();

        scrollToAppIcon(APP_NAME);
        BubbleTextView btv = getFromLauncher(l ->
                verifyIconTheme(APP_NAME, l.getAppsView(), false));
        addToWorkspace(btv);
        executeOnLauncher(l -> verifyIconTheme(APP_NAME, l.getWorkspace(), true));
    }

    @Test
    public void testShortcutIconWithTheme() throws Exception {
        setThemeEnabled(true);
        loadLauncherSync();
        goToState(LauncherState.ALL_APPS);
        freezeAllApps();

        scrollToAppIcon(TEST_APP_NAME);
        BubbleTextView btv = getFromLauncher(l -> findBtv(TEST_APP_NAME, l.getAppsView()));
        TestUtil.runOnExecutorSync(MAIN_EXECUTOR, btv::performLongClick);

        BubbleTextView menuItem = getOnceNotNull("Popup menu not open", l ->
                (AbstractFloatingView.getOpenView(l, TYPE_ACTION_POPUP) instanceof ArrowPopup ap)
                        ? findBtv(SHORTCUT_NAME, ap) : null);
        addToWorkspace(menuItem);
        executeOnLauncher(l -> verifyIconTheme(SHORTCUT_NAME, l.getWorkspace(), true));
    }

    private BubbleTextView findBtv(String title, ViewGroup parent) {
        // Wait for Launcher model to be completed
        try {
            Executors.MODEL_EXECUTOR.submit(() -> { }).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return (BubbleTextView) searchView(parent, v ->
                v instanceof BubbleTextView btv
                    && btv.getContentDescription() != null
                        && title.equals(btv.getContentDescription().toString()));
    }

    private BubbleTextView verifyIconTheme(String title, ViewGroup parent, boolean isThemed) {
        BubbleTextView icon = findBtv(title, parent);
        assertNotNull(icon.getIcon());
        assertEquals(isThemed, icon.getIcon() instanceof ThemedIconDrawable);
        return icon;
    }

    private void setThemeEnabled(boolean isEnabled) throws Exception {
        Uri uri = new Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(targetContext().getPackageName() + ".grid_control")
                .appendPath("set_icon_themed")
                .build();
        ContentValues values = new ContentValues();
        values.put("boolean_value", isEnabled);
        try (ContentProviderClient client = targetContext().getContentResolver()
                .acquireContentProviderClient(uri)) {
            int result = client.update(uri, values, null);
            assertTrue(result > 0);
        }
    }

    private void scrollToAppIcon(String appName) {
        executeOnLauncher(l -> {
            l.hideKeyboard();
            AllAppsRecyclerView rv = l.getAppsView().getActiveRecyclerView();
            int pos = rv.getApps().getAdapterItems().indexOf(rv.getApps().getAdapterItems().stream()
                    .filter(i -> i.itemInfo != null && appName.equals(i.itemInfo.title.toString()))
                    .findFirst()
                    .get());
            rv.getLayoutManager().scrollToPosition(pos);
        });
    }
}
