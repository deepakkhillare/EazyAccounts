package com.softkoash.eazyaccounts.migration;

/**
 * Created by Deepak on 3/28/2017.
 */
public class MigrationStats {
    private int companiesCreated;
    private int ledgersCreated;
    private int vouchersCreated;

    public int getCompaniesCreated() {
        return companiesCreated;
    }

    public void setCompaniesCreated(int companiesCreated) {
        this.companiesCreated = companiesCreated;
    }

    public void addCompaniesCreated() {
        this.companiesCreated++;
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
