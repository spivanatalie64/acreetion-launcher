/*
 * Copyright (C) 2020 The Android Open Source Project
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
package com.android.quickstep.util;

import static org.acreetionos.launcher.AbstractFloatingView.TYPE_ALL_APPS_EDU;
import static org.acreetionos.launcher.AbstractFloatingView.getOpenView;
import static org.acreetionos.launcher.LauncherState.ALL_APPS;
import static org.acreetionos.launcher.LauncherState.HINT_STATE;
import static org.acreetionos.launcher.LauncherState.NORMAL;
import static org.acreetionos.launcher.LauncherState.OVERVIEW;
import static org.acreetionos.launcher.util.NavigationMode.NO_BUTTON;
import static org.acreetionos.launcher.util.OnboardingPrefs.ALL_APPS_VISITED_COUNT;
import static org.acreetionos.launcher.util.OnboardingPrefs.HOME_BOUNCE_COUNT;
import static org.acreetionos.launcher.util.OnboardingPrefs.HOME_BOUNCE_SEEN;
import static org.acreetionos.launcher.util.OnboardingPrefs.HOTSEAT_DISCOVERY_TIP_COUNT;

import org.acreetionos.launcher.Launcher;
import org.acreetionos.launcher.LauncherPrefs;
import org.acreetionos.launcher.LauncherState;
import org.acreetionos.launcher.Utilities;
import org.acreetionos.launcher.Workspace;
import org.acreetionos.launcher.appprediction.AppsDividerView;
import org.acreetionos.launcher.hybridhotseat.HotseatPredictionController;
import org.acreetionos.launcher.statemanager.StateManager;
import org.acreetionos.launcher.statemanager.StateManager.StateListener;
import org.acreetionos.launcher.uioverrides.QuickstepLauncher;
import org.acreetionos.launcher.util.DisplayController;
import com.android.quickstep.views.AllAppsEduView;

/**
 * Class to setup onboarding behavior for quickstep launcher
 */
public class QuickstepOnboardingPrefs {

    /**
     * Sets up the initial onboarding behavior for the launcher
     */
    public static void setup(QuickstepLauncher launcher) {
        StateManager<LauncherState, Launcher> stateManager = launcher.getStateManager();
        if (!HOME_BOUNCE_SEEN.get(launcher)) {
            stateManager.addStateListener(new StateListener<LauncherState>() {
                @Override
                public void onStateTransitionComplete(LauncherState finalState) {
                    boolean swipeUpEnabled =
                            DisplayController.getNavigationMode(launcher).hasGestures;
                    LauncherState prevState = stateManager.getLastState();

                    if (((swipeUpEnabled && finalState == OVERVIEW) || (!swipeUpEnabled
                            && finalState == ALL_APPS && prevState == NORMAL) ||
                            HOME_BOUNCE_COUNT.hasReachedMax(launcher))) {
                        LauncherPrefs.get(launcher).put(HOME_BOUNCE_SEEN, true);
                        stateManager.removeStateListener(this);
                    }
                }
            });
        }

        if (!Utilities.isRunningInTestHarness()
                && !HOTSEAT_DISCOVERY_TIP_COUNT.hasReachedMax(launcher)) {
            stateManager.addStateListener(new StateListener<LauncherState>() {
                boolean mFromAllApps = false;

                @Override
                public void onStateTransitionStart(LauncherState toState) {
                    mFromAllApps = launcher.getStateManager().getCurrentStableState() == ALL_APPS;
                }

                @Override
                public void onStateTransitionComplete(LauncherState finalState) {
                    HotseatPredictionController client = launcher.getHotseatPredictionController();
                    if (mFromAllApps && finalState == NORMAL && client.hasPredictions()) {
                        if (!launcher.getDeviceProfile().isTablet
                                && HOTSEAT_DISCOVERY_TIP_COUNT.increment(launcher)) {
                            client.showEdu();
                            stateManager.removeStateListener(this);
                        }
                    }
                }
            });
        }

        if (DisplayController.getNavigationMode(launcher) == NO_BUTTON) {
            stateManager.addStateListener(new StateListener<LauncherState>() {
                private static final int MAX_NUM_SWIPES_TO_TRIGGER_EDU = 3;

                // Counts the number of consecutive swipes on nav bar without moving screens.
                private int mCount = 0;
                private boolean mShouldIncreaseCount;

                @Override
                public void onStateTransitionStart(LauncherState toState) {
                    if (toState == NORMAL) {
                        return;
                    }
                    mShouldIncreaseCount = toState == HINT_STATE
                            && launcher.getWorkspace().getNextPage() == Workspace.DEFAULT_PAGE
                            && launcher.isCanShowAllAppsEducationView();
                }

                @Override
                public void onStateTransitionComplete(LauncherState finalState) {
                    if (finalState == NORMAL) {
                        if (mCount >= MAX_NUM_SWIPES_TO_TRIGGER_EDU) {
                            if (getOpenView(launcher, TYPE_ALL_APPS_EDU) == null) {
                                AllAppsEduView.show(launcher);
                            }
                            mCount = 0;
                        }
                        return;
                    }

                    if (mShouldIncreaseCount && finalState == HINT_STATE) {
                        mCount++;
                    } else {
                        mCount = 0;
                    }

                    if (finalState == ALL_APPS) {
                        AllAppsEduView view = getOpenView(launcher, TYPE_ALL_APPS_EDU);
                        if (view != null) {
                            view.close(false);
                        }
                    }
                }
            });
        }

        if (!Utilities.isRunningInTestHarness()) {
            launcher.getStateManager().addStateListener(new StateListener<LauncherState>() {
                @Override
                public void onStateTransitionComplete(LauncherState finalState) {
                    if (finalState == ALL_APPS) {
                        ALL_APPS_VISITED_COUNT.increment(launcher);
                    }
                    launcher.getAppsView().getFloatingHeaderView()
                            .findFixedRowByType(AppsDividerView.class)
                            .setShowAllAppsLabel(!ALL_APPS_VISITED_COUNT.hasReachedMax(launcher));
                }
            });
        }
    }
}
