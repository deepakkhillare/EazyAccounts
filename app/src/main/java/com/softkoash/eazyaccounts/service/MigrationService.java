package com.softkoash.eazyaccounts.service;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

import com.softkoash.eazyaccounts.migration.MigrationStats;
import com.softkoash.eazyaccounts.model.AutoIncrementable;
import com.softkoash.eazyaccounts.model.Company;
import com.softkoash.eazyaccounts.model.Configuration;
import com.softkoash.eazyaccounts.model.Currency;
import com.softkoash.eazyaccounts.model.Unit;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmObject;

/**
 * Created by Nirav on 03-04-2017.
 */

public class MigrationService extends IntentService {
    private static final String TAG = MigrationService.class.getSimpleName();
    private String dbFilePath;
    private final int workProgressPercentage = 100;
    final MigrationStats migrationStats = new MigrationStats();
    final Map<String, Currency> currencyMap = new HashMap<>();

    public MigrationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        dbFilePath = intent.getStringExtra("DB_FILE_PATH");
        if(dbFilePath != null && !dbFilePath.isEmpty()) {
            executeDBMigration();
        }
    }

    public void executeDBMigration() {
        invokeCompanyMigrationTask();
        invokeConfigurationDataMigrationTask();
        invokeUnitDataMigrationService();
        invokeCurrencyDataMigration();
    }
    private void invokeCompanyMigrationTask() {
        File file = new File(dbFilePath);
        SQLiteDatabase existingDb = null;
        if(file.exists() && !file.isDirectory()) {
            existingDb = SQLiteDatabase.openDatabase(dbFilePath, null, SQLiteDatabase.OPEN_READONLY);
            migrateCompanyData(existingDb, migrationStats);
        }
        if( existingDb != null) {
            existingDb.close();
        }
    }

    private void migrateCompanyData(SQLiteDatabase existingDb, final MigrationStats migrationStats) {
        Log.d(TAG, "Called migrate company data...");
        Cursor companiesCursor = null;
        try {
            Realm realm = Realm.getDefaultInstance();
            companiesCursor = existingDb.rawQuery("SELECT CompanyName, Version, IsDirty, IsDeleted FROM COMPANYINFO", null);
            if (null != companiesCursor) {
                while (companiesCursor.moveToNext()) {
                    final Company existingCompany = new Company();
                    String companyName = companiesCursor.getString(0);
                    existingCompany.setName(companyName);
                    existingCompany.setCode(companyName);
                    existingCompany.setAppVersion(companiesCursor.getString(1));
                    existingCompany.setDirty(companiesCursor.getInt(2) == 1 ? true : false);
                    existingCompany.setDeleted(companiesCursor.getInt(3) == 1 ? true : false);
                    existingCompany.setCreatedDate(new Date());
                    existingCompany.setCreatedBy(getDeviceId());
                    existingCompany.setUpdatedDate(new Date());
                    existingCompany.setUpdatedBy(getDeviceId());
                    Log.d(TAG, "Loaded company: " + existingCompany.getName());
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                if(existingCompany instanceof AutoIncrementable) {
                                    AutoIncrementable autoIncrementable = (AutoIncrementable) existingCompany;
                                    autoIncrementable.setPrimaryKey(autoIncrementable.getNextPrimaryKey(realm));
                                    realm.copyToRealm((RealmObject)autoIncrementable);
                                } else {
                                    realm.copyToRealm(existingCompany);
                                }
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

    private void invokeConfigurationDataMigrationTask() {
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
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
                while(configurationCursor.moveToNext()) {
                    final Configuration existingConfiguration = new Configuration();
                    String category = configurationCursor.getString(2);
                    existingConfiguration.setId(configurationCursor.getInt(0));
                    existingConfiguration.setName(configurationCursor.getString(1));
                    existingConfiguration.setCategory(category);
                    existingConfiguration.setValue(configurationCursor.getString(3));
                    existingConfiguration.setDirty((configurationCursor.getInt(4) == 1 ? true : false));
                    existingConfiguration.setDeleted((configurationCursor.getInt(5) == 1 ? true : false));
                    String modifiedTimeStr = configurationCursor.getString(7);
                    if(modifiedTimeStr != null && !modifiedTimeStr.isEmpty()) {
                        Date modifiedTime = sdf.parse(modifiedTimeStr);
                        existingConfiguration.setUpdateDate(modifiedTime);
                    }
                    existingConfiguration.setCreatedDate(new Date());
                    existingConfiguration.setCreatedBy(getDeviceId());
                    if(category.equals("Currency")) {
                        populateCurrencyMap(configurationCursor, existingConfiguration, category);
                    }
                    Log.d(TAG, "Loaded configuration: " + existingConfiguration.getName());
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
                                migrationStats.addConfigurationCreated();
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

    private void populateCurrencyMap(Cursor configurationCursor, Configuration existingConfiguration, String category) {
            String name = existingConfiguration.getName();
            String value = existingConfiguration.getValue();
            if(null == currencyMap.get(name) && !name.startsWith("S")) {
                Currency currency = new Currency();
                //currency.setCode(currencyCursor.getString(1));
                currency.setDirty(existingConfiguration.isDirty());
                currency.setDeleted(existingConfiguration.isDeleted());
                currency.setDecimalScale(2); //Note: We don't have unitPrecision column in Configuration table, hence set to 2 as default.
                currency.setCreatedDate(new Date());
                currency.setCreatedBy(getDeviceId());
                currency.setOrderNumber(Integer.toString(configurationCursor.getInt(0)));
                currency.setName(value);
               currencyMap.put(name, currency);
            } else if(name.startsWith("S") && currencyMap.containsKey(name.replace("S","")) && (null != currencyMap.get(name.replace("S","")))) {
                Currency c = currencyMap.get(name.replace("S",""));
                c.setCode(value);
            }
    }

    private void invokeCurrencyDataMigration() {
        SQLiteDatabase existingDb = SQLiteDatabase.openDatabase(dbFilePath, null, SQLiteDatabase.OPEN_READONLY);
        migrateCurrencyData(existingDb, migrationStats);
    }

    private void migrateCurrencyData(SQLiteDatabase existingDb, final MigrationStats migrationStats) {
        Log.d(TAG, "Called migrate currency data...");
        Cursor currencyCursor = null;
        try {
            Realm realm = Realm.getDefaultInstance();
            if(currencyMap.size() > 0) {
                for (final Currency c : currencyMap.values()) {
                    Log.d(TAG, "Loaded currency: " + c.getName());
                    realm.executeTransaction(new Realm.Transaction(){
                        @Override
                        public void execute(Realm realm) {
                            try {
                                if (c instanceof AutoIncrementable) {
                                    AutoIncrementable autoIncrementable = (AutoIncrementable) c;
                                    int id = autoIncrementable.getNextPrimaryKey(realm);
                                    autoIncrementable.setPrimaryKey(id);
                                    realm.copyToRealm((RealmObject) autoIncrementable);
                                } else {
                                    realm.copyToRealm(c);
                                }
                                migrationStats.addCurrencyCreated();
                            }catch (Exception ex) {
                                Log.e(TAG, "Error while writing currency "+ c.getName()  +"data");
                                throw ex;
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

    private void invokeUnitDataMigrationService() {
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
                    String deviceId = getDeviceId();
                    existingUnit.setCode(unitCursor.getString(1));
                    existingUnit.setName(unitCursor.getString(0));
                    existingUnit.setDirty((unitCursor.getInt(2) == 1 ? true : false));
                    existingUnit.setDeleted((unitCursor.getInt(3) == 1 ? true : false));
                    existingUnit.setDecimalScale(unitCursor.getInt(4));
                    existingUnit.setCreatedDate(new Date());
                    existingUnit.setCreatedBy(deviceId);
                    existingUnit.setUpdatedBy(deviceId);
                    existingUnit.setUpdatedDate(new Date());
                    Log.d(TAG, "Loaded unit: " + existingUnit.getName());
                    realm.executeTransaction(new Realm.Transaction(){
                        @Override
                        public void execute(Realm realm) {
                         try{
                            if (existingUnit instanceof AutoIncrementable) {
                                AutoIncrementable autoIncrementable = (AutoIncrementable) existingUnit;
                                autoIncrementable.setPrimaryKey(autoIncrementable.getNextPrimaryKey(realm));
                                realm.copyToRealm((RealmObject) autoIncrementable);
                            } else {
                                realm.copyToRealm(existingUnit);
                            }
                            migrationStats.addUnitCreated();
                          } catch (Exception ex) {
                             Log.e(TAG, "Error while writing unit "+ existingUnit.getName()  +"data");
                             throw ex;
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

    public String getDeviceId() {

        String UniqueDeviceID = convertStringToNumber(Build.SERIAL);
        if (UniqueDeviceID.length() < 15) {
            UniqueDeviceID += convertStringToNumber(Build.ID);
        }
        if (UniqueDeviceID.length() < 15) {
            UniqueDeviceID += convertStringToNumber(Build.HARDWARE);
        }
        if (UniqueDeviceID.length() > 15) {
            UniqueDeviceID = UniqueDeviceID.substring(0, 15);
        }
        return UniqueDeviceID;
    }

    private String convertStringToNumber(String StringToConvert) {
        String result = "";
        char[] buffer = StringToConvert.toCharArray();
        for (int i = buffer.length - 1; i > -1; i--) {
            result += (byte) buffer[i];
        }
        if (result.length() > 15) {
            return result.substring(0, 15);
        } else
            return result;
    }

}
