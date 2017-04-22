package com.softkoash.eazyaccounts.migration.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.util.Log;

import com.softkoash.eazyaccounts.migration.MigrationException;
import com.softkoash.eazyaccounts.migration.MigrationStats;
import com.softkoash.eazyaccounts.util.Constants;
import com.softkoash.eazyaccounts.util.SecurityUtil;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

public class MigrationService extends IntentService {
    private static final String TAG = MigrationService.class.getSimpleName();

    private String dbFilePath;
    private ResultReceiver receiver;
    private boolean verifyDBDump;
    private byte[] realmPassword;

    public MigrationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        receiver = intent.getParcelableExtra("receiver");
        dbFilePath = intent.getStringExtra("DB_FILE_PATH");
        verifyDBDump = intent.getBooleanExtra("VERIFY_MIGRATION", false);
        realmPassword = SecurityUtil.generateHashPassword(intent.getStringExtra("REALM_PASSWORD"));
        Log.d(TAG, "SQLite path: " + dbFilePath);
        if (dbFilePath != null && !dbFilePath.isEmpty()) {
            executeDBMigration();
        } else {
            Log.e(TAG, "No SQLite file provided to migration service!!!");
        }
    }

    private void notifyProgressUpdate(int progress, String message) {
        Bundle progressUpdate = new Bundle();
        progressUpdate.putInt(Constants.BUNDLE_PROGRESS_PERCENT, progress);
        progressUpdate.putString(Constants.BUNDLE_PROGRESS_MESSAGE, message);
        receiver.send(Constants.RESULT_PROGRESS_UPDATE, progressUpdate);
    }

    private void notifySuccess(MigrationStats migrationStats) {
        Bundle success = new Bundle();
        success.putParcelable(Constants.BUNDLE_MIGRATION_STATS, migrationStats);
        receiver.send(Constants.RESULT_SUCCESS, success);
    }

    private void notifyError(String errorMessage) {
        Bundle error = new Bundle();
        error.putString(Constants.BUNDLE_ERROR_MESSAGE, errorMessage);
        receiver.send(Constants.RESULT_ERROR, error);
    }

    private void verifyMigration(SqliteToRealmMigrator migrator, int workUnit) {
        //TODO
    }

    private void executeMigration(SqliteToRealmMigrator migrator, int workUnit) {
        try {
            MigrationStats migrationStats = migrator.getMigrationStats();
            migrator.migrateCompanyData();
            notifyProgressUpdate(10/workUnit, "Migrated " + migrationStats.getCompaniesCreated() + " companies...");
            migrator.migrateConfigurationData();
            notifyProgressUpdate(20/workUnit, "Migrated " + migrationStats.getConfigurationCreated() + " configurations...");
            migrator.migrateUnitData();
            notifyProgressUpdate(30/workUnit, "Migrated " + migrationStats.getUnitCreated() + " units...");
            migrator.migrateCurrencyData();
            notifyProgressUpdate(40/workUnit, "Migrated " + migrationStats.getCurrencyCreated() + " currencies...");
            migrator.migrateItemData();
            notifyProgressUpdate(60/workUnit, "Migrated " + migrationStats.getProductCreated() + " products, "
                    + migrationStats.getProductGroupCreated() + " product groups...");
            migrator.migrateLedgerData();
            notifyProgressUpdate(80/workUnit, "Migrated " + migrationStats.getAccountsCreated() + " accounts, "
                    + migrationStats.getAccountGroupsCreated() + " account groups, " +
                    +migrationStats.getProductSubscriptionsCreated() + " subscriptions...");
            migrator.migrateVoucherData();
            notifyProgressUpdate(100/workUnit, "Migrated " + migrationStats.getVouchersCreated() + " voucher, "
                    + migrationStats.getVoucherEntriesCreated() + " voucher entries, " +
                    +migrationStats.getVoucherItemsCreated() + " voucher items...");
            Log.i(TAG, "Migration completed successfully: " + migrationStats);
            notifySuccess(migrationStats);
        } catch (MigrationException me) {
            Log.e(TAG, "Failed to migrate data", me);
            notifyError(me.getMessage());
        } catch (Exception me) {
            Log.e(TAG, "Failed to migrate data", me);
            notifyError(me.getMessage());
        } finally {
            if (migrator != null) {
                migrator.cleanup();
            }
        }
    }

    public void executeDBMigration() {
        File file = new File(dbFilePath);
        if (file.exists()) {
            File[] files = null;
            try {
                if (file.isDirectory()) {
                    files = file.listFiles(new FileFilter() {
                        @Override
                        public boolean accept(File pathname) {
                            if (pathname.getPath().endsWith(".db")) {
                                return true;
                            }
                            return false;
                        }
                    });
                } else {
                    files = new File[1];
                    files[0] = file;
                }
                int workUnit = files.length;
                for (File sqliteFile : files) {
                    String sqlitePath = sqliteFile.getPath();
                    String realmPath = sqlitePath.replace(sqlitePath.substring(sqlitePath.lastIndexOf(".")), ".realm");
                    Log.d(TAG, "SQLite path: " + sqlitePath + ", Realm path: " + realmPath + ", password: " + Arrays.toString(realmPassword));
                    SqliteToRealmMigrator migrator = new SqliteToRealmMigrator.Builder()
                            .sqlitePath(sqlitePath)
                            .realmPath(realmPath)
                            .realmPassword(realmPassword)
                            .deleteExisting(true)
                            .build();
                    if (verifyDBDump) {
                        verifyMigration(migrator, workUnit);
                    } else {
                        executeMigration(migrator, workUnit);
                    }
                    workUnit--;
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to migrate data", e);
                notifyError(e.getMessage());
            }
        }
    }

//    private void exportDBFile(String realmDBFilePath) {
//        File exportFile = null;
//        Realm realm = null;
//        try {
//            realm = getRealm();
//            exportFile = new File(realmDBFilePath);
//            exportFile.delete();
//            realm.writeCopyTo(exportFile);
//        } catch (Exception ex) {
//            Log.e(TAG, "Error while exporting RealmDB file", ex);
//        } finally {
//            if (null != realm) {
//                realm.close();
//            }
//        }
//    }
}