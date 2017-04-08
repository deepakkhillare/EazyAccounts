package com.softkoash.eazyaccounts.migration;

/**
 * Created by Deepak on 3/28/2017.
 */
public class MigrationStats {
    private int companiesCreated;
    private int ledgersCreated;
    private int vouchersCreated;
    private int configurationCreated;
    private int currencyCreated;
    private int unitCreated;

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
    public int getLedgersCreated() {
        return ledgersCreated;
    }

    public void setLedgersCreated(int ledgersCreated) {
        this.ledgersCreated = ledgersCreated;
    }

    public void addLedgersCreated() {
        this.ledgersCreated++;
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
}
