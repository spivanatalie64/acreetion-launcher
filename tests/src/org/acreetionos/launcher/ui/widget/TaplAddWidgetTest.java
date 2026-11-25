/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.acreetionos.launcher.ui.widget;

import static org.junit.Assert.assertNotNull;

import android.platform.test.annotations.PlatinumTest;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.acreetionos.launcher.Launcher;
import org.acreetionos.launcher.celllayout.FavoriteItemsTransaction;
import org.acreetionos.launcher.tapl.Widget;
import org.acreetionos.launcher.tapl.WidgetResizeFrame;
import org.acreetionos.launcher.ui.AbstractLauncherUiTest;
import org.acreetionos.launcher.ui.PortraitLandscapeRunner.PortraitLandscape;
import org.acreetionos.launcher.ui.TestViewHelpers;
import org.acreetionos.launcher.util.TestUtil;
import org.acreetionos.launcher.util.rule.ShellCommandRule;
import org.acreetionos.launcher.widget.LauncherAppWidgetProviderInfo;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test to add widget from widget tray
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class TaplAddWidgetTest extends AbstractLauncherUiTest<Launcher> {

    @Rule
    public ShellCommandRule mGrantWidgetRule = ShellCommandRule.grantWidgetBind();

    @Test
    @PortraitLandscape
    public void testDragIcon() throws Throwable {
        mLauncher.enableDebugTracing(); // b/289161193
        commitTransactionAndLoadHome(new FavoriteItemsTransaction(mTargetContext));

        waitForLauncherCondition("Workspace didn't finish loading", l -> !l.isWorkspaceLoading());

        final LauncherAppWidgetProviderInfo widgetInfo =
                TestViewHelpers.findWidgetProvider(false /* hasConfigureScreen */);

        WidgetResizeFrame resizeFrame = mLauncher
                .getWorkspace()
                .openAllWidgets()
                .getWidget(widgetInfo.getLabel())
                .dragWidgetToWorkspace();

        assertNotNull("Widget resize frame not shown after widget add", resizeFrame);
        resizeFrame.dismiss();

        final Widget widget = mLauncher.getWorkspace().tryGetWidget(widgetInfo.label,
                TestUtil.DEFAULT_UI_TIMEOUT);
        assertNotNull("Widget not found on the workspace", widget);
        widget.launch(getAppPackageName());
        mLauncher.disableDebugTracing(); // b/289161193
    }

    /**
     * Test dragging a custom shortcut to the workspace and launch it.
     *
     * A custom shortcut is a 1x1 widget that launches a specific intent when user tap on it.
     * Custom shortcuts are replaced by deep shortcuts after api 25.
     */
    @Test
    @PortraitLandscape
    public void testDragCustomShortcut() throws Throwable {
        // TODO(b/322820039): Enable test for tablets - the picker UI has changed and test needs to
        //  be updated to look for appropriate UI elements.
        Assume.assumeFalse(mLauncher.isTablet());
        commitTransactionAndLoadHome(new FavoriteItemsTransaction(mTargetContext));

        mLauncher.getWorkspace().openAllWidgets()
                .getWidget("org.acreetionos.launcher.testcomponent.CustomShortcutConfigActivity")
                .dragToWorkspace(false, true);
        mLauncher.getWorkspace().getWorkspaceAppIcon("Shortcut")
                .launch(getAppPackageName());
    }

    /**
     * Test dragging a widget to the workspace and resize it.
     */
    @PlatinumTest(focusArea = "launcher")
    @Test
    public void testResizeWidget() throws Throwable {
        commitTransactionAndLoadHome(new FavoriteItemsTransaction(mTargetContext));

        waitForLauncherCondition("Workspace didn't finish loading", l -> !l.isWorkspaceLoading());

        final LauncherAppWidgetProviderInfo widgetInfo =
                TestViewHelpers.findWidgetProvider(false /* hasConfigureScreen */);

        WidgetResizeFrame resizeFrame = mLauncher
                .getWorkspace()
                .openAllWidgets()
                .getWidget(widgetInfo.getLabel())
                .dragWidgetToWorkspace();

        assertNotNull("Widget resize frame not shown after widget add", resizeFrame);
        resizeFrame.resize();
    }
}
