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

package org.acreetionos.launcher.taskbar.allapps

import android.content.Context
import android.view.View
import org.acreetionos.launcher.R
import org.acreetionos.launcher.allapps.AllAppsTransitionListener
import org.acreetionos.launcher.anim.PendingAnimation
import org.acreetionos.launcher.dragndrop.DragOptions.PreDragCondition
import org.acreetionos.launcher.model.data.ItemInfo
import org.acreetionos.launcher.util.ResourceBasedOverride
import org.acreetionos.launcher.util.ResourceBasedOverride.Overrides

/** Stub for managing the Taskbar search session. */
open class TaskbarSearchSessionController : ResourceBasedOverride, AllAppsTransitionListener {

    /** Start the search session lifecycle. */
    open fun startLifecycle() = Unit

    /** Destroy the search session. */
    open fun onDestroy() = Unit

    /** Updates the predicted items shown in the zero-state. */
    open fun setZeroStatePredictedItems(items: List<ItemInfo>) = Unit

    /** Updates the search suggestions shown in the zero-state. */
    open fun setZeroStateSearchSuggestions(items: List<ItemInfo>) = Unit

    override fun onAllAppsTransitionStart(toAllApps: Boolean) = Unit

    override fun onAllAppsTransitionEnd(toAllApps: Boolean) = Unit

    /** Creates a [PreDragCondition] for [view], if it is a search result that requires one. */
    open fun createPreDragConditionForSearch(view: View): PreDragCondition? = null

    open fun canHandleBackInvoked(): Boolean = false

    open fun handleBackInvoked(): Boolean = false

    open fun onAllAppsAnimationPending(
        animation: PendingAnimation,
        toAllApps: Boolean,
        showKeyboard: Boolean,
    ) = Unit

    companion object {
        @JvmStatic
        fun newInstance(context: Context): TaskbarSearchSessionController =
            Overrides.getObject(
                TaskbarSearchSessionController::class.java,
                context,
                R.string.taskbar_search_session_controller_class,
            )
    }
}
