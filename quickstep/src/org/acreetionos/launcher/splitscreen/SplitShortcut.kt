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

package org.acreetionos.launcher.splitscreen

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import android.view.View
import org.acreetionos.launcher.model.data.ItemInfo
import org.acreetionos.launcher.model.data.WorkspaceItemInfo
import org.acreetionos.launcher.popup.QuickstepSystemShortcut
import org.acreetionos.launcher.popup.SystemShortcut
import org.acreetionos.launcher.util.SplitConfigurationOptions
import org.acreetionos.launcher.util.SplitConfigurationOptions.SplitPositionOption
import org.acreetionos.launcher.util.SplitConfigurationOptions.SplitSelectSource
import org.acreetionos.launcher.views.ActivityContext

/**
 * Shortcut to allow starting split. Default interaction for [onClick] is to launch split selection
 * mode
 */
abstract class SplitShortcut<T>(
    iconResId: Int,
    labelResId: Int,
    target: T,
    itemInfo: ItemInfo?,
    originalView: View?,
    protected val position: SplitPositionOption
) : SystemShortcut<T>(iconResId, labelResId, target, itemInfo, originalView) where
T : Context?,
T : ActivityContext? {
    private val TAG = "SplitShortcut"

    // Initiate splitscreen from the Home screen or Home All Apps
    protected val splitSelectSource: SplitSelectSource?
        get() {
            // Initiate splitscreen from the Home screen or Home All Apps
            val bitmap: Bitmap
            val intent: Intent
            when (mItemInfo) {
                is WorkspaceItemInfo -> {
                    val workspaceItemInfo = mItemInfo
                    bitmap = workspaceItemInfo.bitmap.icon
                    intent = workspaceItemInfo.intent
                }
                is org.acreetionos.launcher.model.data.AppInfo -> {
                    val appInfo = mItemInfo
                    bitmap = appInfo.bitmap.icon
                    intent = appInfo.intent
                }
                else -> {
                    Log.e(TAG, "unknown item type")
                    return null
                }
            }
            val splitEvent =
                SplitConfigurationOptions.getLogEventForPosition(position.stagePosition)
            return SplitSelectSource(
                mOriginalView,
                BitmapDrawable(bitmap),
                intent,
                position,
                mItemInfo,
                splitEvent
            )
        }

    /** Starts split selection on the provided [mTarget] */
    override fun onClick(view: View?) {
        val splitSelectSource = splitSelectSource
        if (splitSelectSource == null) {
            Log.w(QuickstepSystemShortcut.TAG, "no split selection source")
            return
        }
        mTarget!!.startSplitSelection(splitSelectSource)
    }
}
