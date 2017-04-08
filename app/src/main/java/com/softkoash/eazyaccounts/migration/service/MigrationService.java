package com.softkoash.eazyaccounts.migration.service;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;

import com.softkoash.eazyaccounts.migration.MigrationException;
import com.softkoash.eazyaccounts.migration.MigrationStats;
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

public class MigrationService extends IntentService {
    private static final String TAG = MigrationService.class.getSimpleName();

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
    private String dbFilePath;
    private final int workProgressPercentage = 100;
    private final MigrationStats migrationStats = new MigrationStats();
    private final Map<String, Configuration> currencyConfigs = new HashMap<>();
    private Company company = null;

    public MigrationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        dbFilePath = intent.getStringExtra("DB_FILE_PATH");
        if (dbFilePath != null && !dbFilePath.isEmpty()) {
            executeDBMigration();
        } else {
            Log.e(TAG, "No SQLite file provided to migration service!!!");
        }
    }

    public void executeDBMigration() {
        File file = new File(dbFilePath);
        SQLiteDatabase existingDb = null;
        if (file.exists() && !file.isDirectory()) {
            try {
                existingDb = SQLiteDatabase.openDatabase(dbFilePath, null, SQLiteDatabase.OPEN_READONLY);
                this.company = migrateCompanyData(existingDb);
                migrateConfigurationData(existingDb);
                migrateUnitData(existingDb);
                migrateCurrencyData(existingDb);
            } catch(MigrationException me){
                Log.e(TAG, "Failed to migrate data", me);
            } finally {
                if (existingDb != null) {
                    existingDb.close();
                }
            }
        }
    }

    private Company migrateCompanyData(SQLiteDatabase existingDb) throws MigrationException {
        Log.d(TAG, "Called migrate company data...");
        Cursor companiesCursor = null;
        Company rvCompany = null;
        try {
            Realm realm = Realm.getDefaultInstance();
            companiesCursor = existingDb.rawQuery("SELECT Id, CompanyName, Version, IsDirty, IsDeleted FROM COMPANYINFO", null);
            if (null != companiesCursor) {
                //NB: there will be only one company in existing sqlite database...
                if (companiesCursor.moveToNext()) {
                    int i = 0;
                    final Company company = new Company();
                    company.setId(companiesCursor.getInt(i++));
                    String companyName = companiesCursor.getString(i++);
                    company.setName(companyName);
                    company.setCode(companyName);
                    company.setAppVersion(companiesCursor.getString(i++));
                    company.setDirty(companiesCursor.getInt(i++) == 1 ? true : false);
                    company.setDeleted(companiesCursor.getInt(i++) == 1 ? true : false);
                    company.setCreatedDate(new Date());
                    company.setCreatedBy(getDeviceId());
                    Log.d(TAG, "Loaded company: " + company);
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                realm.copyToRealm(company);
                                migrationStats.addCompaniesCreated();
                            } catch (Exception e) {
                                Log.e(TAG, "Error writing company " + company.getName() + " to realm", e);
                                throw e;
                            }
                        }
                    });
                    rvCompany = company;
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error migrating the company table", ex);
            throw new MigrationException("Error migrating the company table", ex);
        } finally {
            if (companiesCursor != null) {
                companiesCursor.close();
            }
        }
        return rvCompany;
    }

    private void migrateConfigurationData(SQLiteDatabase existingDb) throws MigrationException {
        Log.d(TAG, "Called migrate configuration data...");
        Cursor configurationCursor = null;
        try {
            Realm realm = Realm.getDefaultInstance();
            String rawQuery = "select ConfigurationID, Name, Category, Value, IsDirty, IsDeleted, ModifiedTime from Configuration";
            configurationCursor = existingDb.rawQuery(rawQuery, null);
            if (null != configurationCursor) {
                while (configurationCursor.moveToNext()) {
                    int i = 0;
                    final Configuration configuration = new Configuration();
                    configuration.setId(configurationCursor.getInt(i++));
                    configuration.setName(configurationCursor.getString(i++));
                    String category = configurationCursor.getString(i++);
                    configuration.setCategory(category);
                    configuration.setValue(configurationCursor.getString(i++));
                    configuration.setDirty((configurationCursor.getInt(i++) == 1 ? true : false));
                    configuration.setDeleted((configurationCursor.getInt(i++) == 1 ? true : false));
                    String modifiedTimeStr = configurationCursor.getString(i++);
                    if (modifiedTimeStr != null && !modifiedTimeStr.isEmpty()) {
                        Date modifiedTime = DATE_FORMAT.parse(modifiedTimeStr);
                        configuration.setUpdateDate(modifiedTime);
                    }
                    configuration.setCreatedDate(new Date());
                    configuration.setCreatedBy(getDeviceId());
                    if (category.equals("Currency")) {
                        currencyConfigs.put(configuration.getName(), configuration);
                        //continue so we do not end up adding the currency config in the configuration table of realm!
                        continue;
                    }
                    Log.d(TAG, "Loaded configuration: " + configuration);
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                realm.copyToRealm(configuration);
                                migrationStats.addConfigurationCreated();
                            } catch (Exception ex) {
                                Log.e(TAG, "Error while writing configuration " + configuration, ex);
                                throw ex;
                            }
                        }
                    });
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error migrating the configuration table", ex);
            throw new MigrationException("Error migrating the configuration table", ex);
        } finally {
            if (configurationCursor != null) {
                configurationCursor.close();
            }
        }
    }
//
//    private void populateCurrencyMap(Cursor configurationCursor, Configuration configuration) {
//        String name = configuration.getName();
//        String value = configuration.getValue();
//        if (null == currencyConfigs.get(name) && !name.startsWith("S")) {
//            Currency currency = new Currency();
//            currency.setDirty(configuration.isDirty());
//            currency.setDeleted(configuration.isDeleted());
//            currency.setDecimalScale(2); //Note: We don't have unitPrecision column in Configuration table, hence set to 2 as default.
//            currency.setCreatedDate(new Date());
//            currency.setCreatedBy(getDeviceId());
//            currency.setOrderNumber(Integer.toString(configurationCursor.getInt(0)));
//            currency.setName(value);
//            currencyConfigs.put(name, currency);
//        } else if (name.startsWith("S") && currencyConfigs.containsKey(name.replace("S", "")) && (null != currencyConfigs.get(name.replace("S", "")))) {
//            Currency c = currencyConfigs.get(name.replace("S", ""));
//            c.setCode(value);
//        }
//    }

    private Currency createCurrency(Configuration nameConfig, Configuration codeConfig, Configuration scaleConfig) {
        Currency currency = null;
        if (null != nameConfig && null != codeConfig && null != scaleConfig) {
            currency = new Currency();
            currency.setName(nameConfig.getName());
            currency.setCode(codeConfig.getName());
            currency.setDecimalScale(Integer.parseInt(scaleConfig.getName()));
            currency.setDirty(nameConfig.isDirty());
            currency.setDeleted(nameConfig.isDeleted());
        } else {
            Log.e(TAG, "Missing currency config: name=" + nameConfig + ", code=" + codeConfig + ", scale=" + scaleConfig);
        }
        return currency;
    }

    private void migrateCurrencyData(SQLiteDatabase existingDb) throws MigrationException {
        Log.d(TAG, "Called migrate currency data...");
        Cursor currencyCursor = null;
        try {
            Realm realm = Realm.getDefaultInstance();
            if (currencyConfigs.size() > 0) {
                for (int i = 1; i < 4; i++) {
                    final Currency currency = createCurrency(currencyConfigs.get("C" + i), currencyConfigs.get("SC" + i), currencyConfigs.get("D" + i));
                    if (null != currency) {
                        currency.setCompany(this.company);
                        currency.setOrderNumber(i);
                        currency.setCreatedDate(new Date());
                        currency.setCreatedBy(getDeviceId());
                        Log.d(TAG, "Loaded currency: " + currency);
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                try {
                                    currency.setPrimaryKey(currency.getNextPrimaryKey(realm));
                                    realm.copyToRealm(currency);
                                    migrationStats.addCurrencyCreated();
                                } catch (Exception ex) {
                                    Log.e(TAG, "Error while writing currency " + currency, ex);
                                    throw ex;
                                }
                            }
                        });
                    }
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error migrating the configuration table currency data", ex);
            throw new MigrationException("Error migrating the configuration table currency data", ex);
        } finally {
            if (currencyCursor != null) {
                currencyCursor.close();
            }
        }
    }

    private void migrateUnitData(SQLiteDatabase existingDb) throws MigrationException {
        Log.d(TAG, "Called migrate unit data...");
        Cursor unitCursor = null;
        try {
            Realm realm = Realm.getDefaultInstance();
            unitCursor = existingDb.rawQuery("select Id, Name, ShortName, IsDirty, IsDeleted, UnitPrecision from Unit", null);
            if (null != unitCursor) {
                while (unitCursor.moveToNext()) {
                    int i = 0;
                    final Unit unit = new Unit();
                    String deviceId = getDeviceId();
                    unit.setId(unitCursor.getInt(i++));
                    unit.setName(unitCursor.getString(i++));
                    unit.setCode(unitCursor.getString(i++));
                    unit.setDirty((unitCursor.getInt(i++) == 1 ? true : false));
                    unit.setDeleted((unitCursor.getInt(i++) == 1 ? true : false));
                    unit.setDecimalScale(unitCursor.getInt(i++));
                    unit.setCreatedDate(new Date());
                    unit.setCreatedBy(deviceId);
                    Log.d(TAG, "Loaded unit: " + unit);
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                realm.copyToRealm(unit);
                                migrationStats.addUnitCreated();
                            } catch (Exception ex) {
                                Log.e(TAG, "Error while writing unit " + unit, ex);
                                throw ex;
                            }
                        }
                    });
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error migrating the Unit table", ex);
            throw new MigrationException("Error migrating the Unit table", ex);
        } finally {
            if (unitCursor != null) {
                unitCursor.close();
            }
        }
    }

    // TODO Should be moved to Util class...
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
