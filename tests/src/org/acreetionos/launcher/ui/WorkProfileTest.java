/*
 * Copyright (C) 2021 The Android Open Source Project
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
package org.acreetionos.launcher.ui;

import static org.acreetionos.launcher.LauncherPrefs.WORK_EDU_STEP;
import static org.acreetionos.launcher.LauncherState.ALL_APPS;
import static org.acreetionos.launcher.allapps.AllAppsStore.DEFER_UPDATES_TEST;
import static org.acreetionos.launcher.util.TestUtil.installDummyAppForUser;
import static org.acreetionos.launcher.util.rule.TestStabilityRule.LOCAL;
import static org.acreetionos.launcher.util.rule.TestStabilityRule.PLATFORM_POSTSUBMIT;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import android.util.Log;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.acreetionos.launcher.Launcher;
import org.acreetionos.launcher.LauncherPrefs;
import org.acreetionos.launcher.R;
import org.acreetionos.launcher.allapps.ActivityAllAppsContainerView;
import org.acreetionos.launcher.allapps.AllAppsPagedView;
import org.acreetionos.launcher.allapps.WorkEduCard;
import org.acreetionos.launcher.allapps.WorkPausedCard;
import org.acreetionos.launcher.allapps.WorkProfileManager;
import org.acreetionos.launcher.util.BaseLauncherActivityTest;
import org.acreetionos.launcher.util.TestUtil;
import org.acreetionos.launcher.util.rule.ScreenRecordRule;
import org.acreetionos.launcher.util.rule.TestStabilityRule;
import org.acreetionos.launcher.util.rule.TestStabilityRule.Stability;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.function.Predicate;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class WorkProfileTest extends BaseLauncherActivityTest<Launcher> {

    private static final int WORK_PAGE = ActivityAllAppsContainerView.AdapterHolder.WORK;
    public static final int WAIT_TIME_MS = 30000;

    @Rule
    public ScreenRecordRule mScreenRecordRule = new ScreenRecordRule();
    @Rule
    public TestStabilityRule mTestStabilityRule = new TestStabilityRule();

    private int mProfileUserId;
    private boolean mWorkProfileSetupSuccessful;
    private static final String TAG = "WorkProfileTest";

    @Before
    public void setUp() throws Exception {
        String output = executeShellCommand("pm create-user --profileOf 0 --managed TestProfile");
        updateWorkProfileSetupSuccessful("pm create-user", output);

        String[] tokens = output.split("\\s+");
        mProfileUserId = Integer.parseInt(tokens[tokens.length - 1]);
        StringBuilder logStr = new StringBuilder().append("profileId: ").append(mProfileUserId);
        for (String str : tokens) {
            logStr.append(str).append("\n");
        }
        installDummyAppForUser(mProfileUserId);
        updateWorkProfileSetupSuccessful("am start-user", output);

        if (!mWorkProfileSetupSuccessful) {
            return; // no need to setup launcher since all tests will skip.
        }

        loadLauncherSync();
        goToState(ALL_APPS);
        waitForState("Launcher internal state didn't switch to All Apps", () -> ALL_APPS);
    }

    @After
    public void removeWorkProfile() throws Exception {
        TestUtil.uninstallDummyApp();
        executeShellCommand("pm remove-user --wait " + mProfileUserId);
    }

    private void waitForWorkTabSetup() {
        waitForLauncherCondition("Work tab not setup", launcher -> {
            if (launcher.getAppsView().getContentView() instanceof AllAppsPagedView) {
                launcher.getAppsView().getAppsStore().enableDeferUpdates(DEFER_UPDATES_TEST);
                return true;
            }
            return false;
        }, WAIT_TIME_MS);
    }

    @Test
    @org.acreetionos.launcher.util.rule.ScreenRecordRule.ScreenRecord // b/325383911
    public void workTabExists() {
        assumeTrue(mWorkProfileSetupSuccessful);
        waitForWorkTabSetup();
        waitForLauncherCondition("Personal tab is missing",
                launcher -> launcher.getAppsView().isPersonalTabVisible(),
                WAIT_TIME_MS);
        waitForLauncherCondition("Work tab is missing",
                launcher -> launcher.getAppsView().isWorkTabVisible(),
                WAIT_TIME_MS);
    }

    // Staging; will be promoted to presubmit if stable
    @Stability(flavors = LOCAL | PLATFORM_POSTSUBMIT)
    @Test
    public void toggleWorks() {
        assumeTrue(mWorkProfileSetupSuccessful);
        waitForWorkTabSetup();
        executeOnLauncher(launcher -> {
            AllAppsPagedView pagedView = (AllAppsPagedView) launcher.getAppsView().getContentView();
            pagedView.setCurrentPage(WORK_PAGE);
        });

        WorkProfileManager manager = getFromLauncher(l -> l.getAppsView().getWorkManager());

        waitForLauncherCondition("work profile initial state check failed", launcher ->
                        manager.getWorkUtilityView() != null
                                && manager.getCurrentState() == WorkProfileManager.STATE_ENABLED
                                && manager.getWorkUtilityView().isEnabled(),
                WAIT_TIME_MS);

        //start work profile toggle OFF test
        executeOnLauncher(l -> {
            // Ensure updates are not deferred so notification happens when apps pause.
            l.getAppsView().getAppsStore().disableDeferUpdates(DEFER_UPDATES_TEST);
            l.getAppsView().getWorkManager().getWorkUtilityView().getWorkFAB().performClick();
        });

        waitForLauncherCondition("Work profile toggle OFF failed", launcher -> {
            manager.reset(); // pulls current state from system
            return manager.getCurrentState() == WorkProfileManager.STATE_DISABLED;
        }, WAIT_TIME_MS);

        waitForWorkCard("Work paused card not shown", view -> view instanceof WorkPausedCard);

        // start work profile toggle ON test
        executeOnLauncher(l -> {
            ActivityAllAppsContainerView<?> allApps = l.getAppsView();
            assertEquals("Work tab is not focused", allApps.getCurrentPage(), WORK_PAGE);
            View workPausedCard = allApps.getActiveRecyclerView()
                    .findViewHolderForAdapterPosition(0).itemView;
            workPausedCard.findViewById(R.id.enable_work_apps).performClick();
        });
        waitForLauncherCondition("Work profile toggle ON failed", launcher -> {
            manager.reset(); // pulls current state from system
            return manager.getCurrentState() == WorkProfileManager.STATE_ENABLED;
        }, WAIT_TIME_MS);

    }

    @Test
    public void testEdu() {
        assumeTrue(mWorkProfileSetupSuccessful);
        waitForWorkTabSetup();
        executeOnLauncher(l -> {
            LauncherPrefs.get(l).putSync(WORK_EDU_STEP.to(0));
            ((AllAppsPagedView) l.getAppsView().getContentView()).setCurrentPage(WORK_PAGE);
            l.getAppsView().getWorkManager().reset();
        });

        waitForWorkCard("Work profile education not shown", view -> view instanceof WorkEduCard);
    }

    private void waitForWorkCard(String message, Predicate<View> workCardCheck) {
        waitForLauncherCondition(message, l -> {
            l.getAppsView().getAppsStore().disableDeferUpdates(DEFER_UPDATES_TEST);
            ViewHolder holder = l.getAppsView().getActiveRecyclerView()
                    .findViewHolderForAdapterPosition(0);
            try {
                return holder != null && workCardCheck.test(holder.itemView);
            } finally {
                l.getAppsView().getAppsStore().enableDeferUpdates(DEFER_UPDATES_TEST);
            }
        }, WAIT_TIME_MS);
    }

    private void updateWorkProfileSetupSuccessful(String cli, String output) {
        Log.d(TAG, "updateWorkProfileSetupSuccessful, cli=" + cli + " " + "output=" + output);
        if (output.startsWith("Success")) {
            assertTrue(output, output.startsWith("Success"));
            mWorkProfileSetupSuccessful = true;
        } else {
            mWorkProfileSetupSuccessful = false;
        }
    }
}
