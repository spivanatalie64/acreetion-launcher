package org.acreetionos.launcher.util

import android.content.ContentValues
import org.acreetionos.launcher.Flags
import org.acreetionos.launcher.LauncherModel
import org.acreetionos.launcher.LauncherSettings.Favorites
import org.acreetionos.launcher.LauncherSettings.Favorites.APPWIDGET_ID
import org.acreetionos.launcher.LauncherSettings.Favorites.APPWIDGET_PROVIDER
import org.acreetionos.launcher.LauncherSettings.Favorites.APPWIDGET_SOURCE
import org.acreetionos.launcher.LauncherSettings.Favorites.CELLX
import org.acreetionos.launcher.LauncherSettings.Favorites.CELLY
import org.acreetionos.launcher.LauncherSettings.Favorites.CONTAINER
import org.acreetionos.launcher.LauncherSettings.Favorites.CONTAINER_DESKTOP
import org.acreetionos.launcher.LauncherSettings.Favorites.INTENT
import org.acreetionos.launcher.LauncherSettings.Favorites.ITEM_TYPE
import org.acreetionos.launcher.LauncherSettings.Favorites.ITEM_TYPE_APPLICATION
import org.acreetionos.launcher.LauncherSettings.Favorites.PROFILE_ID
import org.acreetionos.launcher.LauncherSettings.Favorites.RESTORED
import org.acreetionos.launcher.LauncherSettings.Favorites.SCREEN
import org.acreetionos.launcher.LauncherSettings.Favorites.SPANX
import org.acreetionos.launcher.LauncherSettings.Favorites.SPANY
import org.acreetionos.launcher.LauncherSettings.Favorites.TITLE
import org.acreetionos.launcher.LauncherSettings.Favorites._ID
import org.acreetionos.launcher.model.BgDataModel
import org.acreetionos.launcher.model.ModelDbController

object ModelTestExtensions {
    /** Clears and reloads Launcher db to cleanup the workspace */
    fun LauncherModel.clearModelDb() {
        // Load the model once so that there is no pending migration:
        loadModelSync()
        TestUtil.runOnExecutorSync(Executors.MODEL_EXECUTOR) {
            modelDbController.run {
                if (Flags.gridMigrationRefactor()) attemptMigrateDb(null /* restoreEventLogger */)
                else tryMigrateDB(null /* restoreEventLogger */)
                createEmptyDB()
                clearEmptyDbFlag()
            }
        }
        // Reload model
        TestUtil.runOnExecutorSync(Executors.MAIN_EXECUTOR) { forceReload() }
        loadModelSync()
    }

    /** Loads the model in memory synchronously */
    fun LauncherModel.loadModelSync() {
        val mockCb: BgDataModel.Callbacks = object : BgDataModel.Callbacks {}
        TestUtil.runOnExecutorSync(Executors.MAIN_EXECUTOR) { addCallbacksAndLoad(mockCb) }
        TestUtil.runOnExecutorSync(Executors.MODEL_EXECUTOR) {}
        TestUtil.runOnExecutorSync(Executors.MAIN_EXECUTOR) {}
        TestUtil.runOnExecutorSync(Executors.MAIN_EXECUTOR) { removeCallbacks(mockCb) }
    }

    /** Adds and commits a new item to Launcher.db */
    fun LauncherModel.addItem(
        title: String = "LauncherTestApp",
        intent: String =
            "#Intent;action=android.intent.action.MAIN;category=android.intent.category.LAUNCHER;component=com.google.android.apps.nexuslauncher.tests/org.acreetionos.launcher.testcomponent.BaseTestingActivity;launchFlags=0x10200000;end",
        type: Int = ITEM_TYPE_APPLICATION,
        restoreFlags: Int = 0,
        screen: Int = 0,
        container: Int = CONTAINER_DESKTOP,
        x: Int,
        y: Int,
        spanX: Int = 1,
        spanY: Int = 1,
        id: Int = 0,
        profileId: Int = 0,
        tableName: String = Favorites.TABLE_NAME,
        appWidgetId: Int = -1,
        appWidgetSource: Int = -1,
        appWidgetProvider: String? = null,
    ) {
        loadModelSync()
        TestUtil.runOnExecutorSync(Executors.MODEL_EXECUTOR) {
            val controller: ModelDbController = modelDbController
            controller.attemptMigrateDb(null /* restoreEventLogger */)
            modelDbController.newTransaction().use { transaction ->
                val values =
                    ContentValues().apply {
                        values[_ID] = id
                        values[TITLE] = title
                        values[PROFILE_ID] = profileId
                        values[CONTAINER] = container
                        values[SCREEN] = screen
                        values[CELLX] = x
                        values[CELLY] = y
                        values[SPANX] = spanX
                        values[SPANY] = spanY
                        values[ITEM_TYPE] = type
                        values[RESTORED] = restoreFlags
                        values[INTENT] = intent
                        values[APPWIDGET_ID] = appWidgetId
                        values[APPWIDGET_SOURCE] = appWidgetSource
                        values[APPWIDGET_PROVIDER] = appWidgetProvider
                    }
                // Migrate any previous data so that the DB state is correct
                controller.insert(tableName, values)
                transaction.commit()
            }
        }
    }
}
