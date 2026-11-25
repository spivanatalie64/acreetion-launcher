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

package org.acreetionos.launcher.taskbar

import android.content.Context
import android.content.Intent.FLAG_ACTIVITY_MULTIPLE_TASK
import android.view.View
import org.acreetionos.launcher.AbstractFloatingView
import org.acreetionos.launcher.R
import org.acreetionos.launcher.model.data.ItemInfo
import org.acreetionos.launcher.popup.SystemShortcut
import org.acreetionos.launcher.views.ActivityContext

/**
 * A single menu item shortcut to execute creating a new instance of an app. Default interaction for
 * [onClick] is to launch the app in full screen or as a floating window in Desktop Mode.
 */
class NewWindowTaskbarShortcut<T>(target: T, itemInfo: ItemInfo?, originalView: View?) :
    SystemShortcut<T>(
        R.drawable.desktop_mode_ic_taskbar_menu_new_window,
        R.string.new_window_option_taskbar,
        target,
        itemInfo,
        originalView
    ) where T : Context?, T : ActivityContext? {

    override fun onClick(v: View?) {
        val intent = mItemInfo.intent ?: return
        intent.addFlags(FLAG_ACTIVITY_MULTIPLE_TASK)
        mTarget?.startActivitySafely(v, intent, mItemInfo)
        AbstractFloatingView.closeAllOpenViews(mTarget)
    }
}
