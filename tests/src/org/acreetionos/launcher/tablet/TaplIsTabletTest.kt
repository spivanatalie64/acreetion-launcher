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

package org.acreetionos.launcher.tablet

import android.platform.test.rule.AllowedDevices
import android.platform.test.rule.DeviceProduct
import org.acreetionos.launcher.Launcher
import org.acreetionos.launcher.ui.AbstractLauncherUiTest
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Test

class TaplIsTabletTest : AbstractLauncherUiTest<Launcher>() {

    /** Investigating b/366237798 by isolating and seeing flake rate of mLauncher.isTablet */
    @Test
    @AllowedDevices(
        DeviceProduct.CF_FOLDABLE,
        DeviceProduct.CF_TABLET,
        DeviceProduct.TANGORPRO,
        DeviceProduct.FELIX,
        DeviceProduct.COMET,
    )
    fun isTabletShouldBeTrue() {
        assertTrue(mLauncher.isTablet)
    }

    /** Investigating b/366237798 by isolating and seeing flake rate of mLauncher.isTablet */
    @Test
    @AllowedDevices(DeviceProduct.CF_PHONE, DeviceProduct.CHEETAH)
    fun isTabletShouldBeFalse() {
        assertFalse(mLauncher.isTablet)
    }
}
