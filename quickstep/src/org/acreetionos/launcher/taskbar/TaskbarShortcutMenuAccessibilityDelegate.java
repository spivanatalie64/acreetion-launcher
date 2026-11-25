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
package org.acreetionos.launcher.taskbar;

import static org.acreetionos.launcher.accessibility.LauncherAccessibilityDelegate.DEEP_SHORTCUTS;
import static org.acreetionos.launcher.util.SplitConfigurationOptions.STAGE_POSITION_BOTTOM_OR_RIGHT;
import static org.acreetionos.launcher.util.SplitConfigurationOptions.STAGE_POSITION_TOP_OR_LEFT;
import static org.acreetionos.launcher.util.SplitConfigurationOptions.getLogEventForPosition;

import android.content.Intent;
import android.content.pm.LauncherApps;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.View;

import com.android.internal.logging.InstanceId;
import org.acreetionos.launcher.BubbleTextView;
import org.acreetionos.launcher.LauncherSettings;
import org.acreetionos.launcher.R;
import org.acreetionos.launcher.accessibility.BaseAccessibilityDelegate;
import org.acreetionos.launcher.logging.StatsLogManager;
import org.acreetionos.launcher.model.data.ItemInfo;
import org.acreetionos.launcher.model.data.ItemInfoWithIcon;
import org.acreetionos.launcher.model.data.WorkspaceItemInfo;
import org.acreetionos.launcher.util.ShortcutUtil;
import com.android.quickstep.SystemUiProxy;
import com.android.quickstep.util.LogUtils;

import java.util.List;

/**
 * Accessibility delegate for the Taskbar. This provides an accessible interface for taskbar
 * features.
 */
public class TaskbarShortcutMenuAccessibilityDelegate
        extends BaseAccessibilityDelegate<TaskbarActivityContext> {

    public static final int MOVE_TO_TOP_OR_LEFT = R.id.action_move_to_top_or_left;
    public static final int MOVE_TO_BOTTOM_OR_RIGHT = R.id.action_move_to_bottom_or_right;

    private final LauncherApps mLauncherApps;
    private final StatsLogManager mStatsLogManager;

    public TaskbarShortcutMenuAccessibilityDelegate(TaskbarActivityContext context) {
        super(context);
        mLauncherApps = context.getSystemService(LauncherApps.class);
        mStatsLogManager = context.getStatsLogManager();

        mActions.put(DEEP_SHORTCUTS, new LauncherAction(DEEP_SHORTCUTS,
                R.string.action_deep_shortcut, KeyEvent.KEYCODE_S));
        mActions.put(MOVE_TO_TOP_OR_LEFT, new LauncherAction(
                MOVE_TO_TOP_OR_LEFT, R.string.move_drop_target_top_or_left, KeyEvent.KEYCODE_L));
        mActions.put(MOVE_TO_BOTTOM_OR_RIGHT, new LauncherAction(
                MOVE_TO_BOTTOM_OR_RIGHT,
                R.string.move_drop_target_bottom_or_right,
                KeyEvent.KEYCODE_R));
    }

    @Override
    protected void getSupportedActions(View host, ItemInfo item, List<LauncherAction> out) {
        if (ShortcutUtil.supportsShortcuts(item)) {
            out.add(mActions.get(DEEP_SHORTCUTS));
        }
        out.add(mActions.get(MOVE_TO_TOP_OR_LEFT));
        out.add(mActions.get(MOVE_TO_BOTTOM_OR_RIGHT));
    }

    @Override
    protected boolean performAction(View host, ItemInfo item, int action, boolean fromKeyboard) {
        if (item instanceof ItemInfoWithIcon
                && (action == MOVE_TO_TOP_OR_LEFT || action == MOVE_TO_BOTTOM_OR_RIGHT)) {
            ItemInfoWithIcon info = (ItemInfoWithIcon) item;
            int side = action == MOVE_TO_TOP_OR_LEFT
                    ? STAGE_POSITION_TOP_OR_LEFT : STAGE_POSITION_BOTTOM_OR_RIGHT;

            Pair<InstanceId, org.acreetionos.launcher.logging.InstanceId> instanceIds =
                    LogUtils.getShellShareableInstanceId();
            mStatsLogManager.logger()
                    .withItemInfo(item)
                    .withInstanceId(instanceIds.second)
                    .log(getLogEventForPosition(side));

            if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_DEEP_SHORTCUT
                    && item instanceof WorkspaceItemInfo) {
                SystemUiProxy.INSTANCE.get(mContext).startShortcut(
                        info.getIntent().getPackage(),
                        ((WorkspaceItemInfo) info).getDeepShortcutId(),
                        side,
                        /* bundleOpts= */ null,
                        info.user,
                        instanceIds.first);
            } else {
                SystemUiProxy.INSTANCE.get(mContext).startIntent(
                        mLauncherApps.getMainActivityLaunchIntent(
                                item.getIntent().getComponent(),
                                /* startActivityOptions= */null,
                                item.user),
                        item.user.getIdentifier(), new Intent(), side, null,
                        instanceIds.first);
            }
            return true;
        } else if (action == DEEP_SHORTCUTS) {
            mContext.showPopupMenuForIcon((BubbleTextView) host);

            return true;
        }
        return false;
    }

    @Override
    protected boolean beginAccessibleDrag(View item, ItemInfo info, boolean fromKeyboard) {
        return false;
    }
}
