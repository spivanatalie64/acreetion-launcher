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
package org.acreetionos.launcher;

import com.android.launcher3.Flags;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.view.KeyEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import org.acreetionos.launcher.accessibility.LauncherAccessibilityDelegate;
import org.acreetionos.launcher.allapps.AllAppsRecyclerView;
import org.acreetionos.launcher.allapps.SearchRecyclerView;
import org.acreetionos.launcher.model.data.ItemInfo;
import org.acreetionos.launcher.uioverrides.PredictedAppIcon;
import org.acreetionos.launcher.uioverrides.QuickstepLauncher;

import java.util.List;

public class QuickstepAccessibilityDelegate extends LauncherAccessibilityDelegate {
    private QuickstepLauncher mLauncher;

    public QuickstepAccessibilityDelegate(QuickstepLauncher launcher) {
        super(launcher);
        mLauncher = launcher;
        mActions.put(PIN_PREDICTION, new LauncherAction(
                PIN_PREDICTION, R.string.pin_prediction, KeyEvent.KEYCODE_P));
    }

    @Override
    public void onPopulateAccessibilityEvent(View view, AccessibilityEvent event) {
        super.onPopulateAccessibilityEvent(view, event);
        // Scroll to the position if focused view in main allapps list and not completely visible.
        scrollToPositionIfNeeded(view);
    }

    private void scrollToPositionIfNeeded(View view) {
        if (!Flags.accessibilityScrollOnAllapps()) {
            return;
        }
        AllAppsRecyclerView contentView = mLauncher.getAppsView().getActiveRecyclerView();
        if (contentView instanceof SearchRecyclerView) {
            return;
        }
        LinearLayoutManager layoutManager = (LinearLayoutManager) contentView.getLayoutManager();
        if (layoutManager == null) {
            return;
        }
        RecyclerView.ViewHolder vh = contentView.findContainingViewHolder(view);
        if (vh == null) {
            return;
        }
        int itemPosition = vh.getBindingAdapterPosition();
        if (itemPosition == NO_POSITION) {
            return;
        }
        int firstCompletelyVisible = layoutManager.findFirstCompletelyVisibleItemPosition();
        int lastCompletelyVisible = layoutManager.findLastCompletelyVisibleItemPosition();
        boolean itemCompletelyVisible = firstCompletelyVisible <= itemPosition
                && lastCompletelyVisible >= itemPosition;
        if (itemCompletelyVisible) {
            return;
        }
        RecyclerView.SmoothScroller smoothScroller =
                new LinearSmoothScroller(mLauncher.asContext()) {
                    @Override
                    protected int getVerticalSnapPreference() {
                        return LinearSmoothScroller.SNAP_TO_ANY;
                    }
                };
        smoothScroller.setTargetPosition(itemPosition);
        layoutManager.startSmoothScroll(smoothScroller);
    }

    @Override
    protected void getSupportedActions(View host, ItemInfo item, List<LauncherAction> out) {
        if (host instanceof PredictedAppIcon && !((PredictedAppIcon) host).isPinned()) {
            out.add(new LauncherAction(PIN_PREDICTION, R.string.pin_prediction,
                    KeyEvent.KEYCODE_P));
        }
        super.getSupportedActions(host, item, out);
    }

    @Override
    protected boolean performAction(View host, ItemInfo item, int action, boolean fromKeyboard) {
        QuickstepLauncher launcher = (QuickstepLauncher) mContext;
        if (action == PIN_PREDICTION) {
            if (launcher.getHotseatPredictionController() == null) {
                return false;
            }
            launcher.getHotseatPredictionController().pinPrediction(item);
            return true;
        }
        return super.performAction(host, item, action, fromKeyboard);
    }
}
