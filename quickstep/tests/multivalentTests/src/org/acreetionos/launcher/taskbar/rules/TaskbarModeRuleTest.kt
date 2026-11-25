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

package org.acreetionos.launcher.taskbar.rules

import org.acreetionos.launcher.InvariantDeviceProfile
import org.acreetionos.launcher.taskbar.rules.TaskbarModeRule.Mode.PINNED
import org.acreetionos.launcher.taskbar.rules.TaskbarModeRule.Mode.THREE_BUTTONS
import org.acreetionos.launcher.taskbar.rules.TaskbarModeRule.Mode.TRANSIENT
import org.acreetionos.launcher.taskbar.rules.TaskbarModeRule.TaskbarMode
import org.acreetionos.launcher.util.DisplayController
import org.acreetionos.launcher.util.LauncherMultivalentJUnit
import org.acreetionos.launcher.util.LauncherMultivalentJUnit.EmulatedDevices
import org.acreetionos.launcher.util.NavigationMode
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(LauncherMultivalentJUnit::class)
@EmulatedDevices(["pixelFoldable2023", "pixelTablet2023"])
class TaskbarModeRuleTest {

    @get:Rule(order = 0) val context = TaskbarWindowSandboxContext.create()
    @get:Rule(order = 1) val taskbarModeRule = TaskbarModeRule(context)

    @Test
    @TaskbarMode(TRANSIENT)
    fun testTaskbarMode_transient_overridesDisplayController() {
        assertThat(DisplayController.isTransientTaskbar(context)).isTrue()
        assertThat(DisplayController.isPinnedTaskbar(context)).isFalse()
        assertThat(DisplayController.getNavigationMode(context)).isEqualTo(NavigationMode.NO_BUTTON)
    }

    @Test
    @TaskbarMode(TRANSIENT)
    fun testTaskbarMode_transient_overridesDeviceProfile() {
        val dp = InvariantDeviceProfile.INSTANCE.get(context).getDeviceProfile(context)
        assertThat(dp.isTransientTaskbar).isTrue()
        assertThat(dp.isGestureMode).isTrue()
    }

    @Test
    @TaskbarMode(PINNED)
    fun testTaskbarMode_pinned_overridesDisplayController() {
        assertThat(DisplayController.isTransientTaskbar(context)).isFalse()
        assertThat(DisplayController.isPinnedTaskbar(context)).isTrue()
        assertThat(DisplayController.getNavigationMode(context)).isEqualTo(NavigationMode.NO_BUTTON)
    }

    @Test
    @TaskbarMode(PINNED)
    fun testTaskbarMode_pinned_overridesDeviceProfile() {
        val dp = InvariantDeviceProfile.INSTANCE.get(context).getDeviceProfile(context)
        assertThat(dp.isTransientTaskbar).isFalse()
        assertThat(dp.isGestureMode).isTrue()
    }

    @Test
    @TaskbarMode(THREE_BUTTONS)
    fun testTaskbarMode_threeButtons_overridesDisplayController() {
        assertThat(DisplayController.isTransientTaskbar(context)).isFalse()
        assertThat(DisplayController.isPinnedTaskbar(context)).isFalse()
        assertThat(DisplayController.getNavigationMode(context))
            .isEqualTo(NavigationMode.THREE_BUTTONS)
    }

    @Test
    @TaskbarMode(THREE_BUTTONS)
    fun testTaskbarMode_threeButtons_overridesDeviceProfile() {
        val dp = InvariantDeviceProfile.INSTANCE.get(context).getDeviceProfile(context)
        assertThat(dp.isTransientTaskbar).isFalse()
        assertThat(dp.isGestureMode).isFalse()
    }
}
