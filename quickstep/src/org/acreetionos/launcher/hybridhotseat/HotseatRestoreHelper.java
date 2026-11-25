/*
 * Copyright (C) 2020 The Android Open Source Project
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
package org.acreetionos.launcher.hybridhotseat;

import static org.acreetionos.launcher.LauncherSettings.Favorites.HYBRID_HOTSEAT_BACKUP_TABLE;
import static org.acreetionos.launcher.provider.LauncherDbUtils.tableExists;
import static org.acreetionos.launcher.util.Executors.MODEL_EXECUTOR;

import android.content.Context;

import org.acreetionos.launcher.LauncherAppState;
import org.acreetionos.launcher.LauncherModel;
import org.acreetionos.launcher.model.GridBackupTable;
import org.acreetionos.launcher.model.ModelDbController;
import org.acreetionos.launcher.provider.LauncherDbUtils.SQLiteTransaction;

/**
 * A helper class to manage migration revert restoration for hybrid hotseat
 */
public class HotseatRestoreHelper {

    /**
     * Creates a snapshot backup of Favorite table for future restoration use.
     */
    public static void createBackup(Context context) {
        MODEL_EXECUTOR.execute(() -> {
            ModelDbController dbController = LauncherAppState.getInstance(context)
                    .getModel().getModelDbController();
            try (SQLiteTransaction transaction = dbController.newTransaction()) {
                GridBackupTable backupTable = new GridBackupTable(context, transaction.getDb());
                backupTable.createCustomBackupTable(HYBRID_HOTSEAT_BACKUP_TABLE);
                transaction.commit();
                dbController.refreshHotseatRestoreTable();
            }
        });
    }

    /**
     * Finds and restores a previously saved snapshow of Favorites table
     */
    public static void restoreBackup(Context context) {
        MODEL_EXECUTOR.execute(() -> {
            LauncherModel model = LauncherAppState.getInstance(context).getModel();
            try (SQLiteTransaction transaction = model.getModelDbController().newTransaction()) {
                if (!tableExists(transaction.getDb(), HYBRID_HOTSEAT_BACKUP_TABLE)) {
                    return;
                }
                GridBackupTable backupTable = new GridBackupTable(context, transaction.getDb());
                backupTable.restoreFromCustomBackupTable(HYBRID_HOTSEAT_BACKUP_TABLE, true);
                transaction.commit();
                model.forceReload();
            }
        });
    }
}
