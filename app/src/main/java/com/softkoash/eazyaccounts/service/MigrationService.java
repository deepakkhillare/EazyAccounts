package com.softkoash.eazyaccounts.service;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.softkoash.eazyaccounts.migration.MigrationListener;
import com.softkoash.eazyaccounts.migration.MigrationStats;
import com.softkoash.eazyaccounts.model.AutoIncrementable;
import com.softkoash.eazyaccounts.model.Company;
import com.softkoash.eazyaccounts.model.Configuration;
import com.softkoash.eazyaccounts.model.Currency;
import com.softkoash.eazyaccounts.model.Unit;
import com.softkoash.eazyaccounts.util.UiUtil;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmObject;

/**
 * Created by Nirav on 03-04-2017.
 */

public class MigrationService {
    private static final String TAG = MigrationService.class.getSimpleName();
    private String dbFilePath;
    private final int workProgressPercentage = 100;
    final MigrationStats migrationStats = new MigrationStats();

    public MigrationService(String dbFilePath) {
        this.dbFilePath = dbFilePath;
    }

    public void executeDBMigration(final Context uiContext) {
        invokeCompanyMigrationTask(uiContext);
        invokeConfigurationDataMigrationTask(uiContext);
        invokeUnitDataMigrationService(uiContext);
        invokeCurrencyDataMigration(uiContext);
    }
    private void invokeCompanyMigrationTask(final Context uiContext) {
        SQLiteDatabase existingDb = SQLiteDatabase.openDatabase(dbFilePath, null, SQLiteDatabase.OPEN_READONLY);
        migrateCompanyData(existingDb, migrationStats);
    }

    private void migrateCompanyData(SQLiteDatabase existingDb, final MigrationStats migrationStats) {
        Log.d(TAG, "Called migrate company data...");
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
                                realm.copyToRealm(existingCompany);
                                migrationStats.addCompaniesCreated();

                            } catch (Exception e) {
                                Log.e(TAG, "Error writing company " + existingCompany.getName() + " to realm", e);
                                throw e;
                            }
                        }
                    });
                }
            }
        } catch(Exception ex) {
            Log.e(TAG, "Error reading the company table", ex);
        } finally {
            if(companiesCursor != null) {
                companiesCursor.close();
            }
        }
    }

    private void invokeConfigurationDataMigrationTask(final Context uiContext) {
        SQLiteDatabase existingDb = SQLiteDatabase.openDatabase(dbFilePath, null, SQLiteDatabase.OPEN_READONLY);
        migrateConfigurationData(existingDb, migrationStats);
    }

    private void migrateConfigurationData(SQLiteDatabase existingDb, final MigrationStats migrationStats) {
        Log.d(TAG, "Called migrate configuration data...");
        Cursor configurationCursor = null;
        try {
            Realm realm = Realm.getDefaultInstance();
            String rawQuery = "select ConfigurationID, Name, Category, Value, IsDirty, IsDeleted, ModifiedBy, ModifiedTime, ServerUpdateTime from Configuration";
            configurationCursor = existingDb.rawQuery(rawQuery, null);
            if(null != configurationCursor) {
                while(configurationCursor.moveToNext()) {
                    final Configuration existingConfiguration = new Configuration();
                    existingConfiguration.setConfigurationId(configurationCursor.getInt(0));
                    existingConfiguration.setName(configurationCursor.getString(1));
                    existingConfiguration.setCategory(configurationCursor.getString(2));
                    existingConfiguration.setValue(configurationCursor.getString(3));
                    existingConfiguration.setIsDirty(configurationCursor.getInt(4));
                    existingConfiguration.setIsDeleted(configurationCursor.getInt(5));
                    existingConfiguration.setModifiedBy(new Date(configurationCursor.getLong(6)));
                    existingConfiguration.setModifiedTime(new Date(configurationCursor.getLong(7)));
                    existingConfiguration.setServerUpdateTime(new Date(configurationCursor.getLong(8)));
                    Log.d(TAG, "Loaded company: " + existingConfiguration.getName());
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                if(existingConfiguration instanceof AutoIncrementable) {
                                    AutoIncrementable autoIncrementable = (AutoIncrementable) existingConfiguration;
                                    autoIncrementable.setPrimaryKey(autoIncrementable.getNextPrimaryKey(realm));
                                    realm.copyToRealm((RealmObject)autoIncrementable);
                                } else {
                                    realm.copyToRealm(existingConfiguration);
                                }
                                migrationStats.addCompaniesCreated();
                            } catch (Exception ex) {
                                Log.e(TAG, "Error while writing configuration "+ existingConfiguration.getName()  +"data");
                                throw ex;
                            }
                        }
                    });
                }
            }
        } catch(Exception ex) {
            Log.e(TAG, "Error reading the configuration table", ex);
        } finally {
            if(configurationCursor != null) {
                configurationCursor.close();
            }
        }
    }
    private void invokeCurrencyDataMigration(final Context uiContext) {
        SQLiteDatabase existingDb = SQLiteDatabase.openDatabase(dbFilePath, null, SQLiteDatabase.OPEN_READONLY);
        migrateCurrencyData(existingDb, migrationStats);
    }

    private void migrateCurrencyData(SQLiteDatabase existingDb, final MigrationStats migrationStats) {
        Log.d(TAG, "Called migrate currency data...");
        Cursor currencyCursor = null;
        try {
            Realm realm = Realm.getDefaultInstance();
            currencyCursor = existingDb.rawQuery("Select Name, IsDirty, IsDeleted from configuration where category='currency'", null);
            if(currencyCursor != null) {
                while (currencyCursor.moveToNext()) {
                    final Currency existingCurrency =  new Currency();
                    existingCurrency.setName(currencyCursor.getString(0));
                    existingCurrency.setIsDirty(currencyCursor.getInt(1));
                    existingCurrency.setIsDeleted(currencyCursor.getInt(2));
                    existingCurrency.setCreatedDate(new Date()); //TODO : confirm this its working on it.
                    Log.d(TAG, "Loaded currency: " + existingCurrency.getName());
                    realm.executeTransaction(new Realm.Transaction(){
                        @Override
                        public void execute(Realm realm) {
                            if(existingCurrency instanceof AutoIncrementable){
                                AutoIncrementable autoIncrementable = (AutoIncrementable) realm;
                                autoIncrementable.setPrimaryKey(autoIncrementable.getNextPrimaryKey(realm));
                                realm.copyToRealm((RealmObject)autoIncrementable);
                            } else {
                                realm.copyToRealm(existingCurrency);
                            }
                        }
                    });
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error reading the configuration table currency data", ex);
            throw ex;
        } finally {
            if(currencyCursor != null) {
                currencyCursor.close();
            }
        }
    }

    private void invokeUnitDataMigrationService(final Context uiContext) {
        SQLiteDatabase existingDb = SQLiteDatabase.openDatabase(dbFilePath, null, SQLiteDatabase.OPEN_READONLY);
        migrateUnitData(existingDb, migrationStats);
    }
    private void migrateUnitData(SQLiteDatabase existingDb, final MigrationStats migrationStats) {
        Log.d(TAG, "Called migrate unit data...");
        Cursor unitCursor = null;
        try {
            Realm realm = Realm.getDefaultInstance();
            unitCursor = existingDb.rawQuery("select Name, ShortName, IsDirty, IsDeleted, UnitPrecision from Unit",null);
            if(null != unitCursor) {
                while(unitCursor.moveToNext()) {
                    final Unit existingUnit = new Unit();
                    existingUnit.setName(unitCursor.getString(0));
                    existingUnit.setShortName(unitCursor.getString(1));
                    existingUnit.setIsDirty(unitCursor.getInt(2));
                    existingUnit.setIsDeleted(unitCursor.getInt(3));
                    existingUnit.setDecimalPoints(unitCursor.getInt(4));
                    Log.d(TAG, "Loaded unit: " + existingUnit.getName());
                    realm.executeTransaction(new Realm.Transaction(){
                        @Override
                        public void execute(Realm realm) {
                            if(existingUnit instanceof AutoIncrementable) {
                                AutoIncrementable autoIncrementable = (AutoIncrementable) existingUnit;
                                autoIncrementable.setPrimaryKey(autoIncrementable.getNextPrimaryKey(realm));
                                realm.copyToRealm((RealmObject)autoIncrementable);
                            } else {
                                realm.copyToRealm(existingUnit);
                            }
                        }
                    });
                }
            }
        } catch (Exception ex ) {
            Log.e(TAG, "Error reading the Unit table data", ex);
        } finally {
            if(unitCursor != null) {
                unitCursor.close();
            }
        }
    }

}
