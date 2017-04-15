package com.softkoash.eazyaccounts.migration.service;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.util.Log;

import com.softkoash.eazyaccounts.migration.MigrationException;
import com.softkoash.eazyaccounts.migration.MigrationStats;
import com.softkoash.eazyaccounts.model.Account;
import com.softkoash.eazyaccounts.model.AccountGroup;
import com.softkoash.eazyaccounts.model.BhavEntry;
import com.softkoash.eazyaccounts.model.Company;
import com.softkoash.eazyaccounts.model.Configuration;
import com.softkoash.eazyaccounts.model.Contact;
import com.softkoash.eazyaccounts.model.CreditInfo;
import com.softkoash.eazyaccounts.model.Currency;
import com.softkoash.eazyaccounts.model.CurrencyValue;
import com.softkoash.eazyaccounts.model.Product;
import com.softkoash.eazyaccounts.model.ProductGroup;
import com.softkoash.eazyaccounts.model.ProductSubscription;
import com.softkoash.eazyaccounts.model.Unit;
import com.softkoash.eazyaccounts.model.Voucher;
import com.softkoash.eazyaccounts.model.VoucherEntry;
import com.softkoash.eazyaccounts.model.VoucherItem;
import com.softkoash.eazyaccounts.util.Constants;
import com.softkoash.eazyaccounts.util.RealmUtil;
import com.softkoash.eazyaccounts.util.ReflectionUtil;
import com.softkoash.eazyaccounts.util.SystemUtil;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmList;

public class MigrationService extends IntentService {
    private static final String TAG = MigrationService.class.getSimpleName();

    private String dbFilePath;
    private ResultReceiver receiver;
    private final MigrationStats migrationStats = new MigrationStats();
    private final Map<String, Configuration> currencyConfigs = new HashMap<>();
    private Company company = null;
    private String realmDBFilePath;

    public MigrationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        receiver = intent.getParcelableExtra("receiver");
        dbFilePath = intent.getStringExtra("DB_FILE_PATH");
        realmDBFilePath = dbFilePath.replace(dbFilePath.substring(dbFilePath.lastIndexOf(".")), ".realm");
        Log.d(TAG, "SQLite path: " + dbFilePath +", realm path: " + realmDBFilePath);
        if (dbFilePath != null && !dbFilePath.isEmpty()) {
            executeDBMigration();
            Log.i(TAG, "Migration completed successfully: " + migrationStats);
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

    private void notifySuccess() {
        Bundle success = new Bundle();
        success.putParcelable(Constants.BUNDLE_MIGRATION_STATS, migrationStats);
        receiver.send(Constants.RESULT_SUCCESS, success);
    }

    private void notifyError(String errorMessage) {
        Bundle error = new Bundle();
        error.putString(Constants.BUNDLE_ERROR_MESSAGE, errorMessage);
        receiver.send(Constants.RESULT_ERROR, error);
    }

    public void executeDBMigration() {
        File file = new File(dbFilePath);
        SQLiteDatabase existingDb = null;
        if (file.exists() && !file.isDirectory()) {
            try {
                existingDb = SQLiteDatabase.openDatabase(dbFilePath, null, SQLiteDatabase.OPEN_READONLY);
                this.company = migrateCompanyData(existingDb);
                notifyProgressUpdate(10, "Migrated " + migrationStats.getCompaniesCreated() + " companies...");
                migrateConfigurationData(existingDb);
                notifyProgressUpdate(20, "Migrated " + migrationStats.getConfigurationCreated() + " configurations...");
                migrateUnitData(existingDb);
                notifyProgressUpdate(30, "Migrated " + migrationStats.getUnitCreated() + " units...");
                migrateCurrencyData(existingDb);
                notifyProgressUpdate(40, "Migrated " + migrationStats.getCurrencyCreated() + " currencies...");
                migrateItemData(existingDb);
                notifyProgressUpdate(60, "Migrated " + migrationStats.getProductCreated() + " products, "
                        + migrationStats.getProductGroupCreated() + " product groups...");
                migrateLedgerData(existingDb);
                notifyProgressUpdate(80, "Migrated " + migrationStats.getAccountsCreated() + " accounts, "
                        + migrationStats.getAccountGroupsCreated() + " account groups, " +
                        +migrationStats.getProductSubscriptionsCreated() + " subscriptions...");
                migrateVoucherData(existingDb);
                notifyProgressUpdate(100, "Migrated " + migrationStats.getVouchersCreated() + " voucher, "
                        + migrationStats.getVoucherEntriesCreated() + " voucher entries, " +
                        +migrationStats.getVoucherItemsCreated() + " voucher items...");
                notifySuccess();
                exportDBFile(realmDBFilePath);
            } catch (MigrationException me) {
                Log.e(TAG, "Failed to migrate data", me);
                notifyError(me.getMessage());
            } catch (Exception me) {
                Log.e(TAG, "Failed to migrate data", me);
                notifyError(me.getMessage());
            } finally {
                if (existingDb != null) {
                    existingDb.close();
                }
            }
        }
    }

    private void exportDBFile(String realmDBFilePath) {
        File exportFile = null;
        Realm realm = null;
        try {
            realm = getRealm();
            exportFile = new File(realmDBFilePath);
            exportFile.delete();
            realm.writeCopyTo(exportFile);
        } catch (Exception ex) {
            Log.e(TAG, "Error while exporting RealmDB file", ex);
        } finally {
            if (null != realm) {
                realm.close();
            }
        }
    }

    private void migrateItemData(SQLiteDatabase existingDb) throws MigrationException {
        Log.d(TAG, "Migrating Item data...");
        migrateProductGroupData(existingDb);
        migrateProductData(existingDb);
    }

    private void migrateProductData(SQLiteDatabase existingDb) throws MigrationException {
        Log.d(TAG, "Migrating Product data...");
        Cursor productDataCursor = null;
        Realm realm = null;
        try {
            realm = getRealm();
            StringBuilder sql = new StringBuilder();
            sql.append(" select Id");
            sql.append(", Name");
            sql.append(", Remark as Remarks");
            sql.append(", Unit");
            sql.append(", Under as ProductGroup");
            sql.append(", IsDirty as Dirty");
            sql.append(", IsDeleted as Deleted");
            sql.append(", GrossQuantity");
            sql.append(", NettQuantity as NetQuantity");
            sql.append(", GrossOpeningStock");
            sql.append(", NettOpeningStock as NetOpeningStock");
            sql.append(", PriceCurrency1");
            sql.append(", PriceCurrency2");
            sql.append(", PriceCurrency3");
            sql.append(", ExtraChargerate1");
            sql.append(", ExtraChargerate2");
            sql.append(", ExtraChargerate3");
            sql.append(" from Item");
            sql.append(" where IsGroup = 0");
            sql.append(" order by Id");
            productDataCursor = existingDb.rawQuery(sql.toString(), null);
            if (null != productDataCursor) {
                while (productDataCursor.moveToNext()) {
                    final Product product = (Product) ReflectionUtil.convertToRealm(productDataCursor, 0, 10, Product.class, realm);
                    int i = 11;
                    product.setPriceList(buildCurrencyValueList(realm, productDataCursor.getDouble(i++), productDataCursor.getDouble(i++), productDataCursor.getDouble(i++)));
                    product.setExtraChargeRateList(buildCurrencyValueList(realm, productDataCursor.getDouble(i++), productDataCursor.getDouble(i++), productDataCursor.getDouble(i++)));
                    Log.d(TAG, "Loaded product: " + product);
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                realm.copyToRealmOrUpdate(product);
                                migrationStats.addProductCreated();
                            } catch (Exception ex) {
                                Log.e(TAG, "Error while writing Product " + product, ex);
                                throw ex;
                            }
                        }
                    });
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error migrating the Product table", ex);
            throw new MigrationException("Error migrating the ProductGroup table", ex);
        } finally {
            if (null != productDataCursor) {
                productDataCursor.close();
            }
            if (null != realm) {
                realm.close();
            }
        }
    }

    private void migrateProductGroupData(SQLiteDatabase existingDb) throws MigrationException {
        Log.d(TAG, "Migrating ProductGroup data...");
        Cursor productGroupCursor = null;
        Realm realm = null;
        try {
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("select Id");
            sqlBuilder.append(", Name");
            sqlBuilder.append(", Remark as Description");
            sqlBuilder.append(", IsDirty as Dirty");
            sqlBuilder.append(", IsDeleted as Deleted");
            sqlBuilder.append(" from Item");
            sqlBuilder.append(" where IsGroup = 1");
            sqlBuilder.append(" Order by Id");
            realm = getRealm();
            productGroupCursor = existingDb.rawQuery(sqlBuilder.toString(), null);
            if (null != productGroupCursor) {
                while (productGroupCursor.moveToNext()) {
                    final ProductGroup productGroup = (ProductGroup) ReflectionUtil.convertToRealm(productGroupCursor, 0, productGroupCursor.getColumnCount() - 1, ProductGroup.class, realm);
                    Log.d(TAG, "Loaded ProductGroup : " + productGroup);
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                realm.copyToRealmOrUpdate(productGroup);
                                migrationStats.addProductGroupCreated();
                            } catch (Exception ex) {
                                Log.e(TAG, "Error while writing ProductGroup " + productGroup, ex);
                                throw ex;
                            }
                        }
                    });
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error migrating the ProductGroup table", ex);
            throw new MigrationException("Error migrating the ProductGroup table", ex);
        } finally {
            if (productGroupCursor != null) {
                productGroupCursor.close();
            }
            if (null != realm) {
                realm.close();
            }
        }
    }

    private Company migrateCompanyData(SQLiteDatabase existingDb) throws MigrationException {
        Log.d(TAG, "Migrating company data...");
        Cursor companiesCursor = null;
        Company rvCompany = null;
        Realm realm = null;
        try {
            realm = getRealm();
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT Id as Id");
            sqlBuilder.append(", CompanyName as Name");
            sqlBuilder.append(", CompanyName as Code");
            sqlBuilder.append(", Version as AppVersion");
            sqlBuilder.append(", IsDirty as Dirty");
            sqlBuilder.append(", IsDeleted as Deleted");
            sqlBuilder.append(" FROM COMPANYINFO");
            companiesCursor = existingDb.rawQuery(sqlBuilder.toString(), null);
            if (null != companiesCursor) {
                //NB: there will be only one company in existing sqlite database...
                if (companiesCursor.moveToNext()) {
                    final Company company = (Company) ReflectionUtil.convertToRealm(companiesCursor, 0, companiesCursor.getColumnCount() - 1, Company.class, realm);
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                realm.copyToRealmOrUpdate(company);
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
            if (null != realm) {
                realm.close();
            }
        }
        return rvCompany;
    }

    private void migrateConfigurationData(SQLiteDatabase existingDb) throws MigrationException {
        Log.d(TAG, "Migrating configuration data...");
        Cursor configurationCursor = null;
        Realm realm = null;
        try {
            realm = getRealm();
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("select ConfigurationID as Id");
            sqlBuilder.append(", Name");
            sqlBuilder.append(", Category");
            sqlBuilder.append(", Value");
            sqlBuilder.append(", IsDirty as Dirty");
            sqlBuilder.append(", IsDeleted as Deleted");
            sqlBuilder.append(", ModifiedTime as UpdateDate");
            sqlBuilder.append(" from Configuration");
            configurationCursor = existingDb.rawQuery(sqlBuilder.toString(), null);
            if (null != configurationCursor) {
                while (configurationCursor.moveToNext()) {
                    final Configuration configuration = (Configuration) ReflectionUtil.convertToRealm(configurationCursor, 0, 5, Configuration.class, realm);
                    String modifiedTimeStr = configurationCursor.getString(6);
                    if (modifiedTimeStr != null && !modifiedTimeStr.isEmpty()) {
                        Date modifiedTime = Constants.LONG_DATE_FORMAT.parse(modifiedTimeStr);
                        configuration.setUpdateDate(modifiedTime);
                    }
                    if ("C1".equals(configuration.getName()) || "SC1".equals(configuration.getName()) || "D1".equals(configuration.getName())
                            || "C2".equals(configuration.getName()) || "SC2".equals(configuration.getName()) || "D2".equals(configuration.getName())
                            || "C3".equals(configuration.getName()) || "SC3".equals(configuration.getName()) || "D3".equals(configuration.getName())) {
                        currencyConfigs.put(configuration.getName(), configuration);
                        //continue so we do not end up adding the currency config in the configuration table of realm!
                        continue;
                    }
                    Log.d(TAG, "Loaded configuration: " + configuration);
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                realm.copyToRealmOrUpdate(configuration);
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
            if (null != realm) {
                realm.close();
            }
        }
    }

    private Currency createCurrency(Configuration nameConfig, Configuration codeConfig, Configuration scaleConfig) {
        Currency currency = null;
        if (null != nameConfig && null != codeConfig && null != scaleConfig) {
            currency = new Currency();
            currency.setName(nameConfig.getValue());
            currency.setCode(codeConfig.getValue());
            currency.setDecimalScale(Integer.parseInt(scaleConfig.getValue()));
            currency.setDirty(nameConfig.isDirty());
            currency.setDeleted(nameConfig.isDeleted());
        } else {
            Log.e(TAG, "Missing currency config: name=" + nameConfig + ", code=" + codeConfig + ", scale=" + scaleConfig);
        }
        return currency;
    }

    private void migrateCurrencyData(SQLiteDatabase existingDb) throws MigrationException {
        Log.d(TAG, "Migrating currency data...");
        Cursor currencyCursor = null;
        Realm realm = null;
        try {
            realm = getRealm();
            if (currencyConfigs.size() > 0) {
                for (int i = 1; i < 4; i++) {
                    final Currency currency = createCurrency(currencyConfigs.get("C" + i), currencyConfigs.get("SC" + i), currencyConfigs.get("D" + i));
                    if (null != currency) {
                        currency.setCompany(this.company);
                        currency.setOrderNumber(i);
                        currency.setCreatedDate(new Date());
                        currency.setCreatedBy(SystemUtil.getDeviceId());
                        Log.d(TAG, "Loaded currency: " + currency);
                        realm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                try {
                                    currency.setId(RealmUtil.getNextPrimaryKey(realm, Currency.class));
                                    realm.copyToRealmOrUpdate(currency);
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
            if (null != realm) {
                realm.close();
            }
        }
    }

    private void migrateUnitData(SQLiteDatabase existingDb) throws MigrationException {
        Log.d(TAG, "Migrating unit data...");
        Cursor unitCursor = null;
        Realm realm = null;
        try {
            realm = getRealm();
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("select Id");
            sqlBuilder.append(", Name");
            sqlBuilder.append(", ShortName as Code");
            sqlBuilder.append(", IsDirty as Dirty");
            sqlBuilder.append(", IsDeleted as Deleted");
            sqlBuilder.append(", UnitPrecision as DecimalScale");
            sqlBuilder.append(" from Unit");
            unitCursor = existingDb.rawQuery(sqlBuilder.toString(), null);
            if (null != unitCursor) {
                while (unitCursor.moveToNext()) {
                    final Unit unit = (Unit) ReflectionUtil.convertToRealm(unitCursor, 0, unitCursor.getColumnCount() - 1, Unit.class, realm);
                    Log.d(TAG, "Loaded unit: " + unit);
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                realm.copyToRealmOrUpdate(unit);
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
            if (null != realm) {
                realm.close();
            }
        }
    }

    private void migrateLedgerGroups(SQLiteDatabase sqLiteDatabase) throws MigrationException {
        Log.d(TAG, "Migrating ledger group data...");
        Cursor ledgerCursor = null;
        Realm realm = null;
        try {
            realm = getRealm();
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT Id");
            sqlBuilder.append(", Name");
            sqlBuilder.append(", IsDirty as Dirty");
            sqlBuilder.append(", IsDeleted as Deleted");
            sqlBuilder.append(" FROM Ledger");
            sqlBuilder.append(" where IsGroup = 1");
            ledgerCursor = sqLiteDatabase.rawQuery(sqlBuilder.toString(), null);
            if (null != ledgerCursor) {
                while (ledgerCursor.moveToNext()) {
                    final AccountGroup accountGroup = (AccountGroup) ReflectionUtil.convertToRealm(ledgerCursor, 0, ledgerCursor.getColumnCount() - 1, AccountGroup.class, realm);
                    Log.d(TAG, "Loaded account group: " + accountGroup);
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                realm.copyToRealmOrUpdate(accountGroup);
                                migrationStats.addAccountGroupsCreated();
                            } catch (Exception e) {
                                Log.e(TAG, "Error writing account group " + accountGroup + " to realm", e);
                                throw e;
                            }
                        }
                    });
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error migrating the leger group table", ex);
            throw new MigrationException("Error migrating the leger group table", ex);
        } finally {
            if (ledgerCursor != null) {
                ledgerCursor.close();
            }
            if (null != realm) {
                realm.close();
            }
        }
    }

    private void migrateLedgerData(SQLiteDatabase sqLiteDatabase) throws MigrationException {
        // first add the groups...
        migrateLedgerGroups(sqLiteDatabase);

        // Ledger -> Account
        Log.d(TAG, "Migrating ledger data...");
        Cursor ledgerCursor = null;
        Realm realm = null;
        try {
            realm = getRealm();
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT Id");
            sqlBuilder.append(", Name");
            sqlBuilder.append(", IsDirty as Dirty");
            sqlBuilder.append(", IsDeleted as Deleted");
            sqlBuilder.append(", Under as AccountGroup");
            sqlBuilder.append(", IsSystem as CompanyAccount");
            sqlBuilder.append(", OpeningBalanceCurrency1");
            sqlBuilder.append(", OpeningBalanceCurrency2");
            sqlBuilder.append(", OpeningBalanceCurrency3");
            sqlBuilder.append(", Locality");
            sqlBuilder.append(", Address");
            sqlBuilder.append(", City");
            sqlBuilder.append(", PrimaryMobileNo as PrimaryMobile");
            sqlBuilder.append(", CreditLimitLevel1 as CreditLimit1");
            sqlBuilder.append(", CreditLimitLevel2 as CreditLimit2");
            sqlBuilder.append(" FROM Ledger");
            sqlBuilder.append(" where IsLedger = 1 ");
            ledgerCursor = sqLiteDatabase.rawQuery(sqlBuilder.toString(), null);
            if (null != ledgerCursor) {
                while (ledgerCursor.moveToNext()) {
                    final Account account = (Account) ReflectionUtil.convertToRealm(ledgerCursor, 0, 5, Account.class, realm);
                    account.setOpeningBalances(buildCurrencyValueList(realm, ledgerCursor.getDouble(6),
                            ledgerCursor.getDouble(7), ledgerCursor.getDouble(8)));

                    final Contact contact = (Contact) ReflectionUtil.convertToRealm(ledgerCursor, 9, 12, Contact.class, realm);
                    Log.d(TAG, "Loaded contact: " + contact);
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                contact.setId(RealmUtil.getNextPrimaryKey(realm, Contact.class));
                                realm.copyToRealmOrUpdate(contact);
                                migrationStats.addContactCreated();
                            } catch (Exception e) {
                                Log.e(TAG, "Error writing contact " + contact + " to realm", e);
                                throw e;
                            }
                        }
                    });
                    account.setContact(contact);

                    final CreditInfo creditInfo = (CreditInfo) ReflectionUtil.convertToRealm(ledgerCursor, 13, 14, CreditInfo.class, realm);
                    Log.d(TAG, "Loaded credit info: " + creditInfo);
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                creditInfo.setId(RealmUtil.getNextPrimaryKey(realm, CreditInfo.class));
                                realm.copyToRealmOrUpdate(creditInfo);
                                migrationStats.addCreditInfoCreated();
                            } catch (Exception e) {
                                Log.e(TAG, "Error writing credit info " + creditInfo + " to realm", e);
                                throw e;
                            }
                        }
                    });
                    account.setCreditInfo(creditInfo);

                    migrateLedgerBalances(sqLiteDatabase, account);

                    migrateLedgerPriceList(sqLiteDatabase, account);

                    Log.d(TAG, "Loaded account: " + account);
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                realm.copyToRealmOrUpdate(account);
                                migrationStats.addAccountsCreated();
                            } catch (Exception e) {
                                Log.e(TAG, "Error writing account " + account + " to realm", e);
                                throw e;
                            }
                        }
                    });
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error migrating the leger table", ex);
            throw new MigrationException("Error migrating the leger table", ex);
        } finally {
            if (ledgerCursor != null) {
                ledgerCursor.close();
            }
            if (null != realm) {
                realm.close();
            }
        }
    }

    private void migrateLedgerBalances(SQLiteDatabase sqLiteDatabase, Account account) throws MigrationException {
        Log.d(TAG, "Migrating ledger balances...");
        Cursor ledgerCursor = null;
        Realm realm = null;
        try {
            realm = getRealm();
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT BalanceCurrency1");
            sqlBuilder.append(", BalanceCurrency2");
            sqlBuilder.append(", BalanceCurrency3");
            sqlBuilder.append(" FROM LedgerBalance");
            sqlBuilder.append(" where LedgerID = " + account.getId());
            ledgerCursor = sqLiteDatabase.rawQuery(sqlBuilder.toString(), null);
            if (null != ledgerCursor) {
                //Only one balance for a given account
                if (ledgerCursor.moveToNext()) {
                    int i = 0;
                    account.setCurrentBalances(buildCurrencyValueList(realm, ledgerCursor.getDouble(i++),
                            ledgerCursor.getDouble(i++), ledgerCursor.getDouble(i++)));
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error migrating the leger balances table", ex);
            throw new MigrationException("Error migrating the leger balances table", ex);
        } finally {
            if (ledgerCursor != null) {
                ledgerCursor.close();
            }
            if (null != realm) {
                realm.close();
            }
        }
    }

    private void migrateLedgerPriceList(SQLiteDatabase sqLiteDatabase, Account account) throws MigrationException {
        Log.d(TAG, "Migrating ledger price list...");
        Cursor ledgerCursor = null;
        Realm realm = null;
        try {
            realm = getRealm();
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT Id");
            sqlBuilder.append(", ItemID as Product");
            sqlBuilder.append(", IsDirty as Dirty");
            sqlBuilder.append(", IsDeleted as Deleted");
            sqlBuilder.append(", PriceCurrency1");
            sqlBuilder.append(", PriceCurrency2");
            sqlBuilder.append(", PriceCurrency3");
            sqlBuilder.append(", ExtraChargerate1");
            sqlBuilder.append(", ExtraChargerate2");
            sqlBuilder.append(", ExtraChargerate3");
            sqlBuilder.append(" FROM LedgerPriceList");
            sqlBuilder.append(" where LedgerID = " + account.getId());
            ledgerCursor = sqLiteDatabase.rawQuery(sqlBuilder.toString(), null);
            if (null != ledgerCursor) {
                RealmList<ProductSubscription> productSubscriptions = new RealmList<>();
                while (ledgerCursor.moveToNext()) {
                    final ProductSubscription productSubscription = (ProductSubscription) ReflectionUtil.convertToRealm(ledgerCursor, 0, 3, ProductSubscription.class, realm);
                    int i = 4;
                    productSubscription.setRates(buildCurrencyValueList(realm, ledgerCursor.getDouble(i++),
                            ledgerCursor.getDouble(i++), ledgerCursor.getDouble(i++)));
                    productSubscription.setExtraRates(buildCurrencyValueList(realm, ledgerCursor.getDouble(i++),
                            ledgerCursor.getDouble(i++), ledgerCursor.getDouble(i++)));

                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                realm.copyToRealmOrUpdate(productSubscription);
                                migrationStats.addProductSubscriptionsCreated();
                            } catch (Exception e) {
                                Log.e(TAG, "Error writing product subscription " + productSubscription + " to realm", e);
                                throw e;
                            }
                        }
                    });
                    productSubscriptions.add(productSubscription);
                }
                account.setProductSubscriptions(productSubscriptions);
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error migrating the ledger price list table", ex);
            throw new MigrationException("Error migrating the ledger price list table", ex);
        } finally {
            if (ledgerCursor != null) {
                ledgerCursor.close();
            }
            if (null != realm) {
                realm.close();
            }
        }
    }

    private RealmList<CurrencyValue> buildCurrencyValueList(Realm realm, Double... values) {
        RealmList<CurrencyValue> currencyValues = new RealmList<>();
        for (int i = 1; i <= 3; i++) {
            if (null == values[i - 1]) {
                continue;
            }
            Currency currency = realm.where(Currency.class).equalTo("orderNumber", i).findFirst();
            if (null == currency) {
                continue;
            }
            final CurrencyValue cv = new CurrencyValue();
            cv.setValue(values[i - 1]);
            cv.setCurrency(currency);
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    try {
                        realm.copyToRealmOrUpdate(cv);
                    } catch (Exception e) {
                        Log.e(TAG, "Error writing currency value " + cv + " to realm", e);
                        throw e;
                    }
                }
            });
            currencyValues.add(cv);
        }
        return currencyValues;
    }

    private void migrateVoucherData(SQLiteDatabase sqLiteDatabase) throws MigrationException {
        Log.d(TAG, "Migrating voucher data...");
        Cursor voucherCursor = null;
        Realm realm = null;
        try {
            realm = getRealm();
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT Id");
            sqlBuilder.append(", Type as VoucherType");
            sqlBuilder.append(", Date as VoucherDate");
            sqlBuilder.append(", Narration");
            sqlBuilder.append(", PartyLedgerID as PartyAccount");
            sqlBuilder.append(", VoucherLedgerID as VoucherAccount");
            sqlBuilder.append(", IsFreeze as Freezed");
            sqlBuilder.append(", FreezeDate as FreezeDate");
            sqlBuilder.append(", IsDirty as Dirty");
            sqlBuilder.append(", IsDeleted as Deleted");
            sqlBuilder.append(", AmountCurrency1");
            sqlBuilder.append(", AmountCurrency2");
            sqlBuilder.append(", AmountCurrency3");
            sqlBuilder.append(" FROM Voucher");
            voucherCursor = sqLiteDatabase.rawQuery(sqlBuilder.toString(), null);
            if (null != voucherCursor) {
                while (voucherCursor.moveToNext()) {
                    final Voucher voucher = (Voucher) ReflectionUtil.convertToRealm(voucherCursor, 0, 9, Voucher.class, realm);
                    voucher.setAmountList(buildCurrencyValueList(realm, voucherCursor.getDouble(10),
                            voucherCursor.getDouble(11), voucherCursor.getDouble(12)));
                    migrateVoucherEntries(sqLiteDatabase, voucher);
                    migrateVoucherItems(sqLiteDatabase, voucher);
                    migrateBhavEntry(sqLiteDatabase, voucher);
                    Log.d(TAG, "Loaded voucher: " + voucher);
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                realm.copyToRealmOrUpdate(voucher);
                                migrationStats.addVouchersCreated();
                            } catch (Exception e) {
                                Log.e(TAG, "Error writing voucher " + voucher + " to realm", e);
                                throw e;
                            }
                        }
                    });
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error migrating the voucher table", ex);
            throw new MigrationException("Error migrating the voucher table", ex);
        } finally {
            if (voucherCursor != null) {
                voucherCursor.close();
            }
            if (null != realm) {
                realm.close();
            }
        }
    }

    private void migrateVoucherEntries(SQLiteDatabase sqLiteDatabase, Voucher voucher) throws MigrationException {
        Log.d(TAG, "Migrating voucher entries for voucher: " + voucher.getId());
        Cursor voucherCursor = null;
        Realm realm = null;
        try {
            realm = getRealm();
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT Id");
            sqlBuilder.append(", Type");
            sqlBuilder.append(", LedgerID as Account");
            sqlBuilder.append(", IsDirty as Dirty");
            sqlBuilder.append(", IsDeleted as Deleted");
            sqlBuilder.append(", AmountCurrency1");
            sqlBuilder.append(", AmountCurrency2");
            sqlBuilder.append(", AmountCurrency3");
            sqlBuilder.append(" FROM VoucherEntry");
            sqlBuilder.append(" where VoucherID = " + voucher.getId());
            voucherCursor = sqLiteDatabase.rawQuery(sqlBuilder.toString(), null);
            if (null != voucherCursor) {
                RealmList<VoucherEntry> voucherEntries = new RealmList<>();
                while (voucherCursor.moveToNext()) {
                    final VoucherEntry voucherEntry = (VoucherEntry) ReflectionUtil.convertToRealm(voucherCursor, 0, 4, VoucherEntry.class, realm);
                    voucherEntry.setAmount(buildCurrencyValueList(realm, voucherCursor.getDouble(5),
                            voucherCursor.getDouble(7), voucherCursor.getDouble(7)));
                    Log.d(TAG, "Loaded voucher entry: " + voucherEntry);
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                if (voucherEntry.getId() <= 0) {
                                    voucherEntry.setId(RealmUtil.getNextPrimaryKey(realm, VoucherEntry.class));
                                }
                                realm.copyToRealmOrUpdate(voucherEntry);
                                migrationStats.addVoucherEntriesCreated();
                            } catch (Exception e) {
                                Log.e(TAG, "Error writing voucher " + voucherEntry + " to realm", e);
                                throw e;
                            }
                        }
                    });
                    voucherEntries.add(voucherEntry);
                }
                voucher.setVoucherEntries(voucherEntries);
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error migrating the voucher entries table", ex);
            throw new MigrationException("Error migrating the voucher entries table", ex);
        } finally {
            if (voucherCursor != null) {
                voucherCursor.close();
            }
            if (null != realm) {
                realm.close();
            }
        }
    }

    private void migrateVoucherItems(SQLiteDatabase sqLiteDatabase, Voucher voucher) throws MigrationException {
        Log.d(TAG, "Migrating voucher items for voucher: " + voucher.getId());
        Cursor voucherCursor = null;
        Realm realm = null;
        try {
            realm = getRealm();
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT ItemID as Product");
            sqlBuilder.append(", Quantity");
            sqlBuilder.append(", LessQuantity");
            sqlBuilder.append(", ExtraChargeQuantity");
            sqlBuilder.append(", IsDirty as Dirty");
            sqlBuilder.append(", IsDeleted as Deleted");
            sqlBuilder.append(", Rate1");
            sqlBuilder.append(", Rate2");
            sqlBuilder.append(", Rate3");
            sqlBuilder.append(", ExtraChargeRate1");
            sqlBuilder.append(", ExtraChargeRate2");
            sqlBuilder.append(", ExtraChargeRate3");
            sqlBuilder.append(", Total1");
            sqlBuilder.append(", Total2");
            sqlBuilder.append(", Total3");
            sqlBuilder.append(" FROM VoucherItem");
            sqlBuilder.append(" where VoucherID = " + voucher.getId());
            voucherCursor = sqLiteDatabase.rawQuery(sqlBuilder.toString(), null);
            if (null != voucherCursor) {
                RealmList<VoucherItem> voucherItems = new RealmList<>();
                while (voucherCursor.moveToNext()) {
                    final VoucherItem voucherItem = (VoucherItem) ReflectionUtil.convertToRealm(voucherCursor, 0, 5, VoucherItem.class, realm);
                    int i = 6;
                    voucherItem.setRates(buildCurrencyValueList(realm, voucherCursor.getDouble(i++),
                            voucherCursor.getDouble(i++), voucherCursor.getDouble(i++)));
                    voucherItem.setExtraCharges(buildCurrencyValueList(realm, voucherCursor.getDouble(i++),
                            voucherCursor.getDouble(i++), voucherCursor.getDouble(i++)));
                    voucherItem.setTotals(buildCurrencyValueList(realm, voucherCursor.getDouble(i++),
                            voucherCursor.getDouble(i++), voucherCursor.getDouble(i++)));
                    Log.d(TAG, "Loaded voucher item: " + voucherItem);
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                voucherItem.setId(RealmUtil.getNextPrimaryKey(realm, VoucherItem.class));
                                realm.copyToRealmOrUpdate(voucherItem);
                                migrationStats.addVoucherItemsCreated();
                            } catch (Exception e) {
                                Log.e(TAG, "Error writing voucher item " + voucherItem + " to realm", e);
                                throw e;
                            }
                        }
                    });
                    voucherItems.add(voucherItem);
                }
                voucher.setVoucherItems(voucherItems);
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error migrating the voucher item table", ex);
            throw new MigrationException("Error migrating the voucher item table", ex);
        } finally {
            if (voucherCursor != null) {
                voucherCursor.close();
            }
            if (null != realm) {
                realm.close();
            }
        }
    }

    private void migrateBhavEntry(SQLiteDatabase existingDb, Voucher voucher) throws MigrationException {
        Log.d(TAG, "Migrating bhaventry for voucher: " + voucher.getId());
        Cursor bhavEntryCursor = null;
        Realm realm = null;
        try {
            realm = getRealm();
            StringBuilder sql = new StringBuilder();
            sql.append("Select Id");
            sql.append(", PartyAccountID as PartyAccount");
            sql.append(", BhavType as Type");
            sql.append(", Date as BhavDate");
            sql.append(", IsDirty as Dirty");
            sql.append(", IsDeleted as Deleted");
            sql.append(", DebitAmount1");
            sql.append(", DebitAmount2");
            sql.append(", DebitAmount3");
            sql.append(", CreditAmount1");
            sql.append(", CreditAmount2");
            sql.append(", CreditAmount3");
            sql.append(" from bhaventry");
            sql.append(" where VoucherId = " + voucher.getId());
            sql.append(" order by Id");

            bhavEntryCursor = existingDb.rawQuery(sql.toString(), null);
            if (null != bhavEntryCursor) {
                RealmList<BhavEntry> bhavEntries = new RealmList<>();
                while (bhavEntryCursor.moveToNext()) {
                    final BhavEntry bhavEntry = (BhavEntry) ReflectionUtil.convertToRealm(bhavEntryCursor, 0, 5, BhavEntry.class, realm);
                    int i = 6;
                    bhavEntry.setDebitAmount(buildCurrencyValueList(realm, bhavEntryCursor.getDouble(i++),
                            bhavEntryCursor.getDouble(i++), bhavEntryCursor.getDouble(i++)));
                    bhavEntry.setCreditAmount(buildCurrencyValueList(realm, bhavEntryCursor.getDouble(i++),
                            bhavEntryCursor.getDouble(i++), bhavEntryCursor.getDouble(i++)));
                    Log.d(TAG, "Loaded BhavEntry: " + bhavEntry);
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                if (bhavEntry.getId() <= 0) {
                                    bhavEntry.setId(RealmUtil.getNextPrimaryKey(realm, BhavEntry.class));
                                }
                                realm.copyToRealmOrUpdate(bhavEntry);
                                migrationStats.addBhavEntryCreated();
                            } catch (Exception ex) {
                                Log.e(TAG, "Error while writing BhavEntry Data...", ex);
                                throw ex;
                            }
                        }
                    });
                    bhavEntries.add(bhavEntry);
                }
                voucher.setBhavEntries(bhavEntries);
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error migrating the BhavEntry  table", ex);
            throw new MigrationException("Error migrating the BhavEntry table", ex);
        } finally {
            if (bhavEntryCursor != null) {
                bhavEntryCursor.close();
            }
            if (realm != null) {
                realm.close();
            }
        }
    }

    private Realm getRealm() {
        return Realm.getDefaultInstance();
    }
}