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
package org.acreetionos.launcher.popup;

import android.view.View;

import org.acreetionos.launcher.model.data.ItemInfo;
import org.acreetionos.launcher.splitscreen.SplitShortcut;
import org.acreetionos.launcher.uioverrides.QuickstepLauncher;
import org.acreetionos.launcher.util.SplitConfigurationOptions.SplitPositionOption;

/** {@link SystemShortcut.Factory} implementation to create workspace split shortcuts */
public interface QuickstepSystemShortcut {

    String TAG = "QuickstepSystemShortcut";

    static SystemShortcut.Factory<QuickstepLauncher> getSplitSelectShortcutByPosition(
            SplitPositionOption position) {
        return (activity, itemInfo, originalView) ->
                new QuickstepSystemShortcut.SplitSelectSystemShortcut(activity, itemInfo,
                        originalView, position);
    }

    class SplitSelectSystemShortcut extends SplitShortcut<QuickstepLauncher> {

        public SplitSelectSystemShortcut(QuickstepLauncher launcher, ItemInfo itemInfo,
                View originalView, SplitPositionOption position) {
            super(position.iconResId, position.textResId, launcher, itemInfo, originalView,
                    position);
        }
    }
}
