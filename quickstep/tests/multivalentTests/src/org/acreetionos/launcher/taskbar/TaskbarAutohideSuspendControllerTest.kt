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

import android.animation.AnimatorTestRule
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import org.acreetionos.launcher.taskbar.TaskbarAutohideSuspendController.FLAG_AUTOHIDE_SUSPEND_DRAGGING
import org.acreetionos.launcher.taskbar.TaskbarAutohideSuspendController.FLAG_AUTOHIDE_SUSPEND_IN_LAUNCHER
import org.acreetionos.launcher.taskbar.TaskbarAutohideSuspendController.FLAG_AUTOHIDE_SUSPEND_TOUCHING
import org.acreetionos.launcher.taskbar.rules.TaskbarModeRule
import org.acreetionos.launcher.taskbar.rules.TaskbarModeRule.Mode.TRANSIENT
import org.acreetionos.launcher.taskbar.rules.TaskbarModeRule.TaskbarMode
import org.acreetionos.launcher.taskbar.rules.TaskbarUnitTestRule
import org.acreetionos.launcher.taskbar.rules.TaskbarUnitTestRule.InjectController
import org.acreetionos.launcher.taskbar.rules.TaskbarWindowSandboxContext
import org.acreetionos.launcher.util.LauncherMultivalentJUnit
import org.acreetionos.launcher.util.LauncherMultivalentJUnit.EmulatedDevices
import com.android.quickstep.SystemUiProxy
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(LauncherMultivalentJUnit::class)
@EmulatedDevices(["pixelTablet2023"])
class TaskbarAutohideSuspendControllerTest {

    @get:Rule(order = 0)
    val context =
        TaskbarWindowSandboxContext.create { builder ->
            builder.bindSystemUiProxy(
                object : SystemUiProxy(this) {
                    override fun notifyTaskbarAutohideSuspend(suspend: Boolean) {
                        super.notifyTaskbarAutohideSuspend(suspend)
                        latestSuspendNotification = suspend
                    }
                }
            )
        }
    @get:Rule(order = 1) val animatorTestRule = AnimatorTestRule(this)
    @get:Rule(order = 2) val taskbarModeRule = TaskbarModeRule(context)
    @get:Rule(order = 3) val taskbarUnitTestRule = TaskbarUnitTestRule(this, context)

    @InjectController lateinit var autohideSuspendController: TaskbarAutohideSuspendController
    @InjectController lateinit var stashController: TaskbarStashController

    private var latestSuspendNotification: Boolean? = null

    @Test
    fun testUpdateFlag_suspendInLauncher_notifiesSuspend() {
        getInstrumentation().runOnMainSync {
            autohideSuspendController.updateFlag(FLAG_AUTOHIDE_SUSPEND_IN_LAUNCHER, true)
        }
        assertThat(latestSuspendNotification).isTrue()
    }

    @Test
    fun testUpdateFlag_toggleSuspendDraggingTwice_notifiesUnsuspend() {
        getInstrumentation().runOnMainSync {
            autohideSuspendController.updateFlag(FLAG_AUTOHIDE_SUSPEND_DRAGGING, true)
            autohideSuspendController.updateFlag(FLAG_AUTOHIDE_SUSPEND_DRAGGING, false)
        }
        assertThat(latestSuspendNotification).isFalse()
    }

    @Test
    fun testUpdateFlag_resetsAlreadyUnsetFlag_noNotifyUnsuspend() {
        getInstrumentation().runOnMainSync {
            autohideSuspendController.updateFlag(FLAG_AUTOHIDE_SUSPEND_DRAGGING, false)
        }
        assertThat(latestSuspendNotification).isNull()
    }

    @Test
    @TaskbarMode(TRANSIENT)
    fun testUpdateFlag_suspendTransientTaskbarForTouch_cancelsAutoStashTimeout() {
        // Unstash and verify alarm.
        getInstrumentation().runOnMainSync {
            stashController.updateAndAnimateTransientTaskbar(false)
            animatorTestRule.advanceTimeBy(stashController.stashDuration)
        }
        assertThat(stashController.timeoutAlarm.alarmPending()).isTrue()

        // EDU opens while unstashed.
        getInstrumentation().runOnMainSync {
            autohideSuspendController.updateFlag(FLAG_AUTOHIDE_SUSPEND_TOUCHING, true)
        }
        assertThat(stashController.timeoutAlarm.alarmPending()).isFalse()
    }
}
