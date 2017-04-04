package com.softkoash.eazyaccounts.migration.tasks;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.softkoash.eazyaccounts.migration.MigrationListener;
import com.softkoash.eazyaccounts.migration.MigrationStats;
import com.softkoash.eazyaccounts.model.Account;
import com.softkoash.eazyaccounts.model.Company;
import com.softkoash.eazyaccounts.model.Currency;
import com.softkoash.eazyaccounts.model.CurrencyValue;

import java.util.Random;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;

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

    private Realm getRealm(String company) {
        RealmConfiguration companyRealmConfig = new RealmConfiguration.Builder()
//                .directory(new File(Environment.getExternalStorageDirectory() + "/softkoashdb/"))
                .name(company+".realm")
//                .encryptionKey(EazyAccountsApp.encryption.getBytes())
                .build();
        Log.i(TAG, "Company realm config: " + companyRealmConfig.getPath());
        return Realm.getInstance(companyRealmConfig);
    }

    private void migrateCompanyData(SQLiteDatabase existingDb, final MigrationStats migrationStats) {
        Cursor companiesCursor = null;
        try {
            Realm realm = getRealm("c1");
            RealmResults<Company> companies = realm.where(Company.class).findAll();
            RealmResults<Account> ledgers = realm.where(Account.class).findAll();
            for (Account ledger: ledgers) {
                Log.i(TAG, "Opening balances:"+ledger.getOpeningBalances());
            }
//            realm.executeTransaction(new Realm.Transaction() {
//                @Override
//                public void execute(Realm realm) {
//                    realm.copyToRealm(usdCurrency);
//                }
//            });
//            realm.executeTransaction(new Realm.Transaction() {
//                @Override
//                public void execute(Realm realm) {
//                    realm.copyToRealm(inrCurrency);
//                }
//            });

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    final Currency usdCurrency = new Currency();
                    usdCurrency.setName("USD");
                    realm.copyToRealm(usdCurrency);
                    final Currency inrCurrency = new Currency();
                    inrCurrency.setName("INR");
                    realm.copyToRealm(inrCurrency);
                    RealmList<CurrencyValue> openingBals = new RealmList<>();
                    CurrencyValue cv = new CurrencyValue();
                    cv.setCurrency(usdCurrency);
                    cv.setValue(new Random().nextDouble());
                    openingBals.add(cv);
                    cv = new CurrencyValue();
                    cv.setCurrency(inrCurrency);
                    cv.setValue(new Random().nextDouble());
                    openingBals.add(cv);
                    final Account ledger = new Account();
                    ledger.setOpeningBalances(openingBals);
                    realm.copyToRealm(ledger);
                }
            });

            companiesCursor = existingDb.rawQuery("SELECT Id, CompanyName, Version, IsDirty, IsDeleted FROM COMPANYINFO", null);
            if (null != companiesCursor) {
                while (companiesCursor.moveToNext()) {
                    int i = 0;
                    final Company company = new Company();
//                    company.setId(companiesCursor.getInt(i++));
                    company.setName(companiesCursor.getString(i++));
                    company.setAppVersion(companiesCursor.getString(i++));
//                    company.setDirty(companiesCursor.getInt(i++) == 0 ? false : true);
//                    company.setDeleted(companiesCursor.getInt(i++) == 0 ? false : true);
                    Log.d(TAG, "Loaded company: " + company.getName());
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                realm.copyToRealm(company);
//                                Company company = realm.createObject(Company.class);
//                                company.setName(existingCompany.getName());
//                                company.setAppVersion(existingCompany.getAppVersion());
                                migrationStats.addCompaniesCreated();
                            } catch (Exception e) {
                                Log.e(TAG, "Error writing company " + company.getName() + " to realm", e);
                                migrationListener.onFail("Error writing company " + company.getName() + " to realm", e);
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error migrating company", e);
            migrationListener.onFail("Error migrating company", e);
        } finally {
            companiesCursor.close();
        }
    }

    protected void onPostExecute(MigrationStats migrationStats) {
        super.onPostExecute(migrationStats);
        migrationListener.onSuccess(migrationStats);
    }
}
