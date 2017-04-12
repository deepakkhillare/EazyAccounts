package com.softkoash.eazyaccounts.migration;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Deepak on 3/28/2017.
 */
public class MigrationStats implements Parcelable {
    private int companiesCreated;
    private int accountsCreated;
    private int accountGroupsCreated;
    private int contactCreated;
    private int creditInfoCreated;
    private int productSubscriptionsCreated;
    private int vouchersCreated;
    private int voucherItemsCreated;
    private int voucherEntriesCreated;
    private int configurationCreated;
    private int currencyCreated;
    private int unitCreated;
    private int productGroupCreated;
    private int productCreated;

    public int getProductGroupCreated() {
        return productGroupCreated;
    }

    public void setProductGroupCreated(int productGroupCreated) {
        this.productGroupCreated = productGroupCreated;
    }

    public int getCurrencyCreated() {
        return currencyCreated;
    }

    public void setCurrencyCreated(int currencyCreated) {
        this.currencyCreated = currencyCreated;
    }

    public int getUnitCreated() {
        return unitCreated;
    }

    public void setUnitCreated(int unitCreated) {
        this.unitCreated = unitCreated;
    }

    public int getCompaniesCreated() {
        return companiesCreated;
    }

    public void setCompaniesCreated(int companiesCreated) {
        this.companiesCreated = companiesCreated;
    }

    public int getConfigurationCreated() {
        return configurationCreated;
    }

    public void setConfigurationCreated(int configurationCreated) {
        this.configurationCreated = configurationCreated;
    }

    public void addCompaniesCreated() {
        this.companiesCreated++;
    }

    public void addConfigurationCreated() {
        this.configurationCreated++;
    }

    public void addCurrencyCreated() {
        this.currencyCreated++;
    }

    public void addUnitCreated() {
        this.unitCreated++;
    }

    public  void addProductGroupCreated() {
        this.productGroupCreated++;
    }

    public void addProductCreated() {
        this.productCreated++;
    }

    public int getAccountsCreated() {
        return accountsCreated;
    }

    public void setAccountsCreated(int accountsCreated) {
        this.accountsCreated = accountsCreated;
    }

    public void addAccountsCreated() {
        this.accountsCreated++;
    }

    public int getVouchersCreated() {
        return vouchersCreated;
    }

    public void setVouchersCreated(int vouchersCreated) {
        this.vouchersCreated = vouchersCreated;
    }

    public void addVouchersCreated() {
        this.vouchersCreated++;
    }

    public int getAccountGroupsCreated() {
        return accountGroupsCreated;
    }

    public void setAccountGroupsCreated(int accountGroupsCreated) {
        this.accountGroupsCreated = accountGroupsCreated;
    }

    public void addAccountGroupsCreated() {
        this.accountGroupsCreated++;
    }

    public int getContactCreated() {
        return contactCreated;
    }

    public void setContactCreated(int contactCreated) {
        this.contactCreated = contactCreated;
    }

    public void addContactCreated() {
        this.contactCreated++;
    }

    public int getCreditInfoCreated() {
        return creditInfoCreated;
    }

    public void setCreditInfoCreated(int creditInfoCreated) {
        this.creditInfoCreated = creditInfoCreated;
    }

    public void addCreditInfoCreated() {
        this.creditInfoCreated++;
    }

    public int getProductSubscriptionsCreated() {
        return productSubscriptionsCreated;
    }

    public void setProductSubscriptionsCreated(int productSubscriptionsCreated) {
        this.productSubscriptionsCreated = productSubscriptionsCreated;
    }

    public void addProductSubscriptionsCreated() {
        this.productSubscriptionsCreated++;
    }

    public int getVoucherItemsCreated() {
        return voucherItemsCreated;
    }

    public void setVoucherItemsCreated(int voucherItemsCreated) {
        this.voucherItemsCreated = voucherItemsCreated;
    }

    public void addVoucherItemsCreated() {
        this.voucherItemsCreated++;
    }

    public int getVoucherEntriesCreated() {
        return voucherEntriesCreated;
    }

    public void setVoucherEntriesCreated(int voucherEntriesCreated) {
        this.voucherEntriesCreated = voucherEntriesCreated;
    }

    public void addVoucherEntriesCreated() {
        this.voucherEntriesCreated++;
    }

    public int getProductCreated() {
        return productCreated;
    }

    public void setProductCreated(int productCreated) {
        this.productCreated = productCreated;
    }


    @Override
    public String toString() {
        return "MigrationStats{" +
                "companiesCreated=" + companiesCreated +
                ", accountsCreated=" + accountsCreated +
                ", accountGroupsCreated=" + accountGroupsCreated +
                ", contactCreated=" + contactCreated +
                ", creditInfoCreated=" + creditInfoCreated +
                ", productSubscriptionsCreated=" + productSubscriptionsCreated +
                ", vouchersCreated=" + vouchersCreated +
                ", voucherItemsCreated=" + voucherItemsCreated +
                ", voucherEntriesCreated=" + voucherEntriesCreated +
                ", configurationCreated=" + configurationCreated +
                ", currencyCreated=" + currencyCreated +
                ", unitCreated=" + unitCreated +
                ", productGroupCreated=" + productGroupCreated +
                ", productCreated=" + productCreated +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeIntArray(new int[]{companiesCreated,
                accountsCreated,
                accountGroupsCreated,
                contactCreated,
                creditInfoCreated,
                productSubscriptionsCreated,
                vouchersCreated,
                voucherItemsCreated,
                voucherEntriesCreated,
                configurationCreated,
                currencyCreated,
                unitCreated,
                productGroupCreated,
                productCreated});

    }

    public MigrationStats() {

    }

    public MigrationStats(Parcel in) {
        int[] arr = new int[14];
        in.readIntArray(arr);
        companiesCreated = arr[0];
        accountsCreated = arr[1];
        accountGroupsCreated = arr[2];
        contactCreated = arr[3];
        creditInfoCreated = arr[4];
        productSubscriptionsCreated = arr[5];
        vouchersCreated = arr[6];
        voucherItemsCreated = arr[7];
        voucherEntriesCreated = arr[8];
        configurationCreated = arr[9];
        currencyCreated = arr[10];
        unitCreated = arr[11];
        productGroupCreated = arr[12];
        productCreated = arr[13];
    }
}
