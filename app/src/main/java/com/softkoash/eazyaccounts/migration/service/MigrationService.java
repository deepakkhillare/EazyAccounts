package com.softkoash.eazyaccounts.migration.service;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;
import android.util.Log;

import com.softkoash.eazyaccounts.migration.MigrationException;
import com.softkoash.eazyaccounts.migration.MigrationStats;
import com.softkoash.eazyaccounts.model.Account;
import com.softkoash.eazyaccounts.model.AccountGroup;
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
import com.softkoash.eazyaccounts.util.RealmUtil;
import com.softkoash.eazyaccounts.util.SystemUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;

public class MigrationService extends IntentService {
    private static final String TAG = MigrationService.class.getSimpleName();

    private static final SimpleDateFormat LONG_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
    private static final SimpleDateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
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
                migrateItemData(existingDb);
                migrateLedgerData(existingDb);
                migrateVoucherData(existingDb);
            } catch(MigrationException me){
                Log.e(TAG, "Failed to migrate data", me);
            } finally {
                if (existingDb != null) {
                    existingDb.close();
                }
            }
        }
    }

    private void migrateItemData(SQLiteDatabase existingDb) throws MigrationException {
        Log.d(TAG, "Called migrate Item data...");
        insertProductGroupData(existingDb);
        insertProductData(existingDb);
    }

    private void insertProductData(SQLiteDatabase existingDb) throws MigrationException {
        Log.d(TAG, "Called migrate Product data...");
        Cursor productDataCursor = null;
        Realm realm = null;
        try {
            realm = getRealm();
            StringBuilder sql = new StringBuilder();
            sql.append(" select Id, Name, Remark, ");
            sql.append(" PriceCurrency1, PriceCurrency2, PriceCurrency3, Unit,");
            sql.append(" ExtraChargerate1, ExtraChargerate2, ExtraChargerate3, Under,");
            sql.append(" IsDirty, IsDeleted, GrossQuantity, NettQuantity, GrossOpeningStock, NettOpeningStock ");
            sql.append(" from Item where Item.IsGroup = 0 order by Item.Id");
            productDataCursor = existingDb.rawQuery(sql.toString(), null);
            if(null != productDataCursor) {
                while (productDataCursor.moveToNext()) {
                    final Product product = new Product();
                    final RealmList<CurrencyValue> currencyValueList = new RealmList<>();
                    final RealmList<CurrencyValue> extraChargeList = new RealmList<>();

                    //Find Product Group
                    int searchProductGroupId = productDataCursor.getInt(10);
                    RealmResults<ProductGroup> productGroupResult = realm.where(ProductGroup.class).equalTo("id", searchProductGroupId).findAll();
                    ProductGroup prodGroup = productGroupResult.first();

                    //Find Unit
                    String searchUnitData = productDataCursor.getString(6);
                    RealmResults<Unit> unitRealmResult = realm.where(Unit.class).equalTo("name",searchUnitData).findAll();
                    Unit unit = unitRealmResult.first();

                    //Find Currency to link priceList and extraChargeList
                    RealmResults<Currency>  currencies = realm.where(Currency.class).findAllSorted("id");
                    Iterator<Currency> currencyIterator = currencies.listIterator();
                    for(int i = 1 ; currencyIterator.hasNext(); i++) {
                        Currency currency = currencyIterator.next();
                        CurrencyValue currencyValue = new CurrencyValue();
                        CurrencyValue extraChargeCurrency = new CurrencyValue();
                        extraChargeCurrency.setCurrency(currency);
                        currencyValue.setCurrency(currency);
                        switch (i) {
                            case 1 :
                                currencyValue.setValue(productDataCursor.getDouble(3));
                                extraChargeCurrency.setValue(productDataCursor.getDouble(7));
                                break;
                            case 2 :
                                currencyValue.setValue(productDataCursor.getDouble(4));
                                extraChargeCurrency.setValue(productDataCursor.getDouble(8));
                                break;
                            case 3 :
                                currencyValue.setValue(productDataCursor.getDouble(5));
                                extraChargeCurrency.setValue(productDataCursor.getDouble(9));
                                break;
                        }
                        currencyValueList.add(currencyValue);
                        extraChargeList.add(extraChargeCurrency);
                    }

                    product.setId(productDataCursor.getInt(0));
                    product.setName(productDataCursor.getString(1));
                    product.setPriceList(currencyValueList);
                    product.setExtraChargeRateList(extraChargeList);
                    product.setRemarks(productDataCursor.getString(2));
                    product.setUnit(unit);
                    product.setProductGroup(prodGroup);
                    product.setDirty(productDataCursor.getInt(11) == 1? true : false);
                    product.setDeleted(productDataCursor.getInt(12) == 1? true : false);
                    product.setGrossQuantity(productDataCursor.getDouble(13));
                    product.setNetQuantity(productDataCursor.getDouble(14));
                    product.setGrossOpeningStock(productDataCursor.getDouble(15));
                    product.setNetOpeningStock(productDataCursor.getDouble(16));
                    product.setCreatedDate(new Date());
                    product.setCreatedBy(SystemUtil.getDeviceId());
                    product.setUpdatedDate(new Date());
                    product.setUpdatedBy(SystemUtil.getDeviceId());

                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                realm.copyToRealmOrUpdate(product);
                                migrationStats.addProductCreated();
                            } catch (Exception ex) {
                                Log.e(TAG, "Error while writing ProductGroup " + product, ex);
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

    private void insertProductGroupData(SQLiteDatabase existingDb) throws MigrationException {
        Log.d(TAG, "Called inserting ProductGroup data...");
        Cursor productGroupCursor = null;
        Realm realm = null;
        try {
            String sql = "select Id, Name, Remark, IsDirty, IsDeleted from Item where Item.IsGroup = 1 Order by Id";
            realm = getRealm();
            productGroupCursor = existingDb.rawQuery(sql, null);
            if( null != productGroupCursor) {
                while(productGroupCursor.moveToNext()) {
                    final ProductGroup productGroup = new ProductGroup();
                    productGroup.setId(productGroupCursor.getInt(0));
                    productGroup.setName(productGroupCursor.getString(1));
                    productGroup.setDescription(productGroupCursor.getString(2));
                    productGroup.setDirty(productGroupCursor.getInt(3) == 1 ? true : false);
                    productGroup.setDeleted(productGroupCursor.getInt(4) == 1 ? true : false);
                    productGroup.setCreatedDate(new Date());
                    productGroup.setCreatedBy(SystemUtil.getDeviceId());
                    productGroup.setUpdatedDate(new Date());
                    productGroup.setUpdatedBy(SystemUtil.getDeviceId());
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
        Log.d(TAG, "Called migrate company data...");
        Cursor companiesCursor = null;
        Company rvCompany = null;
        Realm realm = null;
        try {
            realm = getRealm();
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
                    company.setCreatedBy(SystemUtil.getDeviceId());
                    Log.d(TAG, "Loaded company: " + company);
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
        Log.d(TAG, "Called migrate configuration data...");
        Cursor configurationCursor = null;
        Realm realm = null;
        try {
            realm = getRealm();
            Log.d(TAG, "Realm file path:" + realm.getPath());
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
                        Date modifiedTime = LONG_DATE_FORMAT.parse(modifiedTimeStr);
                        configuration.setUpdateDate(modifiedTime);
                    }
                    configuration.setCreatedDate(new Date());
                    configuration.setCreatedBy(SystemUtil.getDeviceId());
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
            currency.setName(nameConfig.getValue()); // TODO: We should take value instead of name
            currency.setCode(codeConfig.getValue()); // TODO: We should take value instead of name
            currency.setDecimalScale(Integer.parseInt(scaleConfig.getValue()));
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
        Log.d(TAG, "Called migrate unit data...");
        Cursor unitCursor = null;
        Realm realm = null;
        try {
            realm = getRealm();
            unitCursor = existingDb.rawQuery("select Id, Name, ShortName, IsDirty, IsDeleted, UnitPrecision from Unit", null);
            if (null != unitCursor) {
                while (unitCursor.moveToNext()) {
                    int i = 0;
                    final Unit unit = new Unit();
                    String deviceId = SystemUtil.getDeviceId();
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
        Cursor ledgerCursor = null;
        Realm realm = null;
        try {
            realm = getRealm();
            ledgerCursor = sqLiteDatabase.rawQuery("SELECT Id, Name, IsDirty, IsDeleted FROM Ledger where IsGroup = 1", null);
            if (null != ledgerCursor) {
                while (ledgerCursor.moveToNext()) {
                    int i = 0;
                    final AccountGroup accountGroup = new AccountGroup();
                    accountGroup.setId(ledgerCursor.getInt(i++));
                    accountGroup.setName(ledgerCursor.getString(i++));
                    accountGroup.setDirty(ledgerCursor.getInt(i++) == 1 ? true : false);
                    accountGroup.setDeleted(ledgerCursor.getInt(i++) == 1 ? true : false);
                    accountGroup.setCreatedDate(new Date());
                    accountGroup.setCreatedBy(SystemUtil.getDeviceId());
                    Log.d(TAG, "Loaded account group: " + accountGroup);
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                realm.copyToRealmOrUpdate(accountGroup);
                                migrationStats.addLedgersCreated();
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
        // Ledger -> Account
        Log.d(TAG, "Called migrate ledger data...");
        // first add the groups...
        migrateLedgerGroups(sqLiteDatabase);
        Cursor ledgerCursor = null;
        Realm realm = null;
        try {
            realm = getRealm();
            ledgerCursor = sqLiteDatabase.rawQuery("SELECT Id, Name, IsDirty, IsDeleted, Under, IsSystem" +
                    ", OpeningBalanceCurrency1, OpeningBalanceCurrency2, OpeningBalanceCurrency3 " +
                    ", Locality, Address, City, PrimaryMobileNo, CreditLimitLevel1, CreditLimitLevel2 " +
                    " FROM Ledger", null);
            if (null != ledgerCursor) {
                //NB: there will be only one company in existing sqlite database...
                while (ledgerCursor.moveToNext()) {
                    int i = 0;
                    final Account account = new Account();
                    account.setId(ledgerCursor.getInt(i++));
                    account.setName(ledgerCursor.getString(i++));
                    account.setDirty(ledgerCursor.getInt(i++) == 1 ? true : false);
                    account.setDeleted(ledgerCursor.getInt(i++) == 1 ? true : false);
                    account.setCreatedDate(new Date());
                    account.setCreatedBy(SystemUtil.getDeviceId());

                    AccountGroup accountGroup = realm.where(AccountGroup.class).equalTo("id", ledgerCursor.getInt(i++)).findFirst();
                    account.setGroup(accountGroup);
                    account.setCompanyAccount(ledgerCursor.getInt(i++) == 1 ? true : false);

                    account.setOpeningBalances(buildCurrencyValueList(realm, ledgerCursor.getDouble(i++),
                            ledgerCursor.getDouble(i++), ledgerCursor.getDouble(i++)));

                    final Contact contact = new Contact();
                    contact.setLocality(ledgerCursor.getString(i++));
                    contact.setAddress(ledgerCursor.getString(i++));
                    contact.setCity(ledgerCursor.getString(i++));
                    contact.setPrimaryMobile(ledgerCursor.getString(i++));
                    Log.d(TAG, "Loaded contact: " + contact);
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                contact.setId(RealmUtil.getNextPrimaryKey(realm, Contact.class));
                                realm.copyToRealmOrUpdate(contact);
                            } catch (Exception e) {
                                Log.e(TAG, "Error writing contact " + contact + " to realm", e);
                                throw e;
                            }
                        }
                    });
                    account.setContact(contact);

                    final CreditInfo creditInfo = new CreditInfo();
                    //for now there is no such value defined!
                    creditInfo.setPeriodInDays(Integer.MAX_VALUE);
                    creditInfo.setCreditLimit1(ledgerCursor.getDouble(i++));
                    creditInfo.setCreditLimit2(ledgerCursor.getDouble(i++));
                    Log.d(TAG, "Loaded credit info: " + creditInfo);
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                creditInfo.setId(RealmUtil.getNextPrimaryKey(realm, CreditInfo.class));
                                realm.copyToRealmOrUpdate(creditInfo);
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
                                migrationStats.addLedgersCreated();
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
        Cursor ledgerCursor = null;
        Realm realm = null;
        try {
            realm = getRealm();
            ledgerCursor = sqLiteDatabase.rawQuery("SELECT BalanceCurrency1, BalanceCurrency2, BalanceCurrency3 FROM LedgerBalance where LedgerID = " + account.getId(), null);
            if (null != ledgerCursor) {
                //Only one balance for a given account
                if (ledgerCursor.moveToNext()) {
                    int i = 0;
                    account.setCurrentBalances(buildCurrencyValueList(realm, ledgerCursor.getDouble(i++),
                            ledgerCursor.getDouble(i++), ledgerCursor.getDouble(i++)));
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

    private void migrateLedgerPriceList(SQLiteDatabase sqLiteDatabase, Account account) throws MigrationException{
        Cursor ledgerCursor = null;
        Realm realm = null;
        try {
            realm = getRealm();
            ledgerCursor = sqLiteDatabase.rawQuery("SELECT Id, ItemID, PriceCurrency1, PriceCurrency2, PriceCurrency3, ExtraChargerate1, ExtraChargerate2, ExtraChargerate3, IsDirty, IsDeleted FROM LedgerPriceList where LedgerID = " + account.getId(), null);
            if (null != ledgerCursor) {
                RealmList<ProductSubscription> productSubscriptions = new RealmList<>();
                while (ledgerCursor.moveToNext()) {
                    int i = 0;
                    final ProductSubscription productSubscription = new ProductSubscription();
                    productSubscription.setId(ledgerCursor.getInt(i++));
                    productSubscription.setProduct(realm.where(Product.class).equalTo("id", ledgerCursor.getInt(i++)).findFirst());
                    productSubscription.setRates(buildCurrencyValueList(realm, ledgerCursor.getDouble(i++),
                            ledgerCursor.getDouble(i++), ledgerCursor.getDouble(i++)));
                    productSubscription.setExtraRates(buildCurrencyValueList(realm, ledgerCursor.getDouble(i++),
                            ledgerCursor.getDouble(i++), ledgerCursor.getDouble(i++)));
                    productSubscription.setCreatedDate(new Date());
                    productSubscription.setCreatedBy(SystemUtil.getDeviceId());
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                realm.copyToRealmOrUpdate(productSubscription);
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
        for (int j = 1; j <= 3; j++) {
            if (null == values[j-1]) {
                continue;
            }
            final CurrencyValue cv = new CurrencyValue();
            cv.setValue(values[j-1]);
            // TODO Need to test this part thoroughly
            Currency currency = realm.where(Currency.class).equalTo("orderNumber", j).findFirst();
            if (null != currency) {
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
        }
        return currencyValues;
    }

    private void migrateVoucherData(SQLiteDatabase sqLiteDatabase) throws MigrationException {
        Cursor voucherCursor = null;
        Realm realm = null;
        try {
            realm = getRealm();
            voucherCursor = sqLiteDatabase.rawQuery("SELECT Id, Type, Date, AmountCurrency1, AmountCurrency2, AmountCurrency3" +
                    ", Narration, PartyLedgerID, VoucherLedgerID, IsFreeze, FreezeDate, FreezeIn, IsDirty, IsDeleted " +
                    " FROM Voucher", null);
            if (null != voucherCursor) {
                while (voucherCursor.moveToNext()) {
                    int i = 0;
                    final Voucher voucher = new Voucher();
                    voucher.setId(voucherCursor.getInt(i++));
                    voucher.setVoucherType(voucherCursor.getString(i++));
                    String voucherDateStr = voucherCursor.getString(i++);
                    if (null != voucherDateStr && !voucherDateStr.trim().isEmpty()) {
                        voucher.setVoucherDate(SHORT_DATE_FORMAT.parse(voucherDateStr));
                    }
                    voucher.setAmountList(buildCurrencyValueList(realm, voucherCursor.getDouble(i++),
                            voucherCursor.getDouble(i++), voucherCursor.getDouble(i++)));
                    voucher.setNarration(voucherCursor.getString(i++));
                    voucher.setPartyAccount(realm.where(Account.class).equalTo("id", voucherCursor.getInt(i++)).findFirst());
                    voucher.setVoucherAccount(realm.where(Account.class).equalTo("id", voucherCursor.getInt(i++)).findFirst());
                    voucher.setFreezed(voucherCursor.getInt(i++) == 1 ? true : false);
                    String freezeDateStr = voucherCursor.getString(i++);
                    if (null != freezeDateStr && !freezeDateStr.trim().isEmpty()) {
                        voucher.setFreezeDate(SHORT_DATE_FORMAT.parse(freezeDateStr));
                    }
                    voucher.setDirty(voucherCursor.getInt(i++) == 1 ? true : false);
                    voucher.setDeleted(voucherCursor.getInt(i++) == 1 ? true : false);
                    voucher.setCreatedDate(new Date());
                    voucher.setCreatedBy(SystemUtil.getDeviceId());
                    migrateVoucherEntries(sqLiteDatabase, voucher);
                    migrateVoucherItems(sqLiteDatabase, voucher);
                    Log.d(TAG, "Loaded voucher: " + voucher);
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                realm.copyToRealmOrUpdate(voucher);
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
        Cursor voucherCursor = null;
        Realm realm = null;
        try {
            realm = getRealm();
            voucherCursor = sqLiteDatabase.rawQuery("SELECT Id, Type, LedgerID, AmountCurrency1, AmountCurrency2, AmountCurrency3" +
                    ", IsDirty, IsDeleted FROM VoucherEntry where VoucherID = " + voucher.getId(), null);
            if (null != voucherCursor) {
                RealmList<VoucherEntry> voucherEntries = new RealmList<>();
                while (voucherCursor.moveToNext()) {
                    int i = 0;
                    final VoucherEntry voucherEntry = new VoucherEntry();
                    voucherEntry.setId(voucherCursor.getInt(i++));
                    voucherEntry.setType(voucherCursor.getString(i++));
                    voucherEntry.setAccount(realm.where(Account.class).equalTo("id", voucherCursor.getInt(i++)).findFirst());
                    voucherEntry.setAmount(buildCurrencyValueList(realm, voucherCursor.getDouble(i++),
                            voucherCursor.getDouble(i++), voucherCursor.getDouble(i++)));
                    voucherEntry.setDirty(voucherCursor.getInt(i++) == 1 ? true : false);
                    voucherEntry.setDeleted(voucherCursor.getInt(i++) == 1 ? true : false);
                    voucherEntry.setCreatedDate(new Date());
                    voucherEntry.setCreatedBy(SystemUtil.getDeviceId());
                    Log.d(TAG, "Loaded voucher entry: " + voucherEntry);
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                realm.copyToRealmOrUpdate(voucherEntry);
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
        Cursor voucherCursor = null;
        Realm realm = null;
        try {
            realm = getRealm();
            voucherCursor = sqLiteDatabase.rawQuery("SELECT ItemID, Quantity, LessQuantity" +
                    ", Rate1, Rate2, Rate3, ExtraChargeQuantity, ExtraChargeRate1, ExtraChargeRate2" +
                    ", ExtraChargeRate3, Total1, Total2, Total3, IsDirty, IsDeleted" +
                    " FROM VoucherItem where VoucherID = " + voucher.getId(), null);
            if (null != voucherCursor) {
                RealmList<VoucherItem> voucherItems = new RealmList<>();
                while (voucherCursor.moveToNext()) {
                    int i = 0;
                    final VoucherItem voucherItem = new VoucherItem();
                    voucherItem.setProduct(realm.where(Product.class).equalTo("id", voucherCursor.getInt(i++)).findFirst());;
                    voucherItem.setQuantity(voucherCursor.getDouble(i++));
                    voucherItem.setLessQuantity(voucherCursor.getDouble(i++));
                    voucherItem.setRates(buildCurrencyValueList(realm, voucherCursor.getDouble(i++),
                            voucherCursor.getDouble(i++), voucherCursor.getDouble(i++)));
                    voucherItem.setExtraChargeQuantity(voucherCursor.getDouble(i++));
                    voucherItem.setExtraCharges(buildCurrencyValueList(realm, voucherCursor.getDouble(i++),
                            voucherCursor.getDouble(i++), voucherCursor.getDouble(i++)));
                    voucherItem.setTotals(buildCurrencyValueList(realm, voucherCursor.getDouble(i++),
                            voucherCursor.getDouble(i++), voucherCursor.getDouble(i++)));
                    voucherItem.setDirty(voucherCursor.getInt(i++) == 1 ? true : false);
                    voucherItem.setDeleted(voucherCursor.getInt(i++) == 1 ? true : false);
                    voucherItem.setCreatedDate(new Date());
                    voucherItem.setCreatedBy(SystemUtil.getDeviceId());
                    Log.d(TAG, "Loaded voucher item: " + voucherItem);
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            try {
                                voucherItem.setId(RealmUtil.getNextPrimaryKey(realm, VoucherItem.class));
                                realm.copyToRealmOrUpdate(voucherItem);
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

    private Realm getRealm() {
        return Realm.getInstance(new RealmConfiguration.Builder().name("somerealm9").build());
    }
}
