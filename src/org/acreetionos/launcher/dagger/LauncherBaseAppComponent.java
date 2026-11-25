/*
 * Copyright (C) 2024 The Android Open Source Project
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

package org.acreetionos.launcher.dagger;

import android.content.Context;

import org.acreetionos.launcher.contextualeducation.ContextualEduStatsManager;
import org.acreetionos.launcher.graphics.IconShape;
import org.acreetionos.launcher.model.ItemInstallQueue;
import org.acreetionos.launcher.pm.InstallSessionHelper;
import org.acreetionos.launcher.util.ApiWrapper;
import org.acreetionos.launcher.util.DaggerSingletonTracker;
import org.acreetionos.launcher.util.DynamicResource;
import org.acreetionos.launcher.util.MSDLPlayerWrapper;
import org.acreetionos.launcher.util.PackageManagerHelper;
import org.acreetionos.launcher.util.PluginManagerWrapper;
import org.acreetionos.launcher.util.ScreenOnTracker;
import org.acreetionos.launcher.util.SettingsCache;
import org.acreetionos.launcher.util.VibratorWrapper;
import org.acreetionos.launcher.util.window.RefreshRateTracker;
import org.acreetionos.launcher.widget.custom.CustomWidgetManager;

import dagger.BindsInstance;

/**
 * Launcher base component for Dagger injection.
 *
 * This class is not actually annotated as a Dagger component, since it is not used directly as one.
 * Doing so generates unnecessary code bloat.
 *
 * See {@link LauncherAppComponent} for the one actually used by AOSP.
 */
public interface LauncherBaseAppComponent {
    DaggerSingletonTracker getDaggerSingletonTracker();
    ApiWrapper getApiWrapper();
    ContextualEduStatsManager getContextualEduStatsManager();
    CustomWidgetManager getCustomWidgetManager();
    DynamicResource getDynamicResource();
    IconShape getIconShape();
    InstallSessionHelper getInstallSessionHelper();
    ItemInstallQueue getItemInstallQueue();
    RefreshRateTracker getRefreshRateTracker();
    ScreenOnTracker getScreenOnTracker();
    SettingsCache getSettingsCache();
    PackageManagerHelper getPackageManagerHelper();
    PluginManagerWrapper getPluginManagerWrapper();
    VibratorWrapper getVibratorWrapper();
    MSDLPlayerWrapper getMSDLPlayerWrapper();

    /** Builder for LauncherBaseAppComponent. */
    interface Builder {
        @BindsInstance Builder appContext(@ApplicationContext Context context);
        LauncherBaseAppComponent build();
    }
}
