package com.softkoash.eazyaccounts.migration.tasks;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.softkoash.eazyaccounts.migration.MigrationListener;
import com.softkoash.eazyaccounts.migration.MigrationStats;
import com.softkoash.eazyaccounts.model.Company;

import io.realm.Realm;

/**
 * Created by Deepak on 3/28/2017.
 */
public class DbMigrationTask extends AsyncTask<SQLiteDatabase, Integer, MigrationStats> {
    private static final String TAG = DbMigrationTask.class.getName();

    private MigrationListener migrationListener;

    public DbMigrationTask(MigrationListener migrationListener) {
        this.migrationListener = migrationListener;
    }

    @Override
    protected MigrationStats doInBackground(SQLiteDatabase[] params) {
        final MigrationStats migrationStats = new MigrationStats();
        try {
            migrateCompanyData(params[0], migrationStats);

        } catch(Exception e) {
            Log.e(TAG, "Failed to migrate database", e);
            migrationListener.onFail("Failed to migrate database", e);
        }
        return migrationStats;
    }

    private void migrateCompanyData(SQLiteDatabase existingDb, final MigrationStats migrationStats) {
            Cursor companiesCursor = null;
        try {
            Realm realm = Realm.getDefaultInstance();
            companiesCursor = existingDb.rawQuery("SELECT CompanyName, Version FROM COMPANYINFO", null);
            if (null != companiesCursor) {
                while (companiesCursor.moveToNext()) {
                    final Company existingCompany = new Company();
                    existingCompany.setName(companiesCursor.getString(0));
                    existingCompany.setSystemVersion(companiesCursor.getString(1));
                    Log.d(TAG, "Loaded company: " + existingCompany.getName());
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                Company company = realm.createObject(Company.class);
                                company.setName(existingCompany.getName());
                                company.setSystemVersion(existingCompany.getSystemVersion());
                                migrationStats.addCompaniesCreated();
                            } catch (Exception e) {
                                Log.e(TAG, "Error writing company " + existingCompany.getName() + " to realm", e);
                                migrationListener.onFail("Error writing company " + existingCompany.getName() + " to realm", e);
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error reading the company table", e);
            migrationListener.onFail("Error reading the company table", e);
        } finally {
            companiesCursor.close();
        }
    }

    protected void onPostExecute(MigrationStats migrationStats) {
        super.onPostExecute(migrationStats);
        migrationListener.onSuccess(migrationStats);
    }
}
