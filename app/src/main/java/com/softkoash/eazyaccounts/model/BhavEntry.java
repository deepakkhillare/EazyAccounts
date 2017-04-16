package com.softkoash.eazyaccounts.model;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class BhavEntry extends RealmObject {
    @PrimaryKey
    private Integer id;

    private Account partyAccount;
    private RealmList<CurrencyValue> debitAmount;
    private RealmList<CurrencyValue> creditAmount;
    private String type;
    private Date bhavDate;
    private Boolean isDirty;
    private Boolean isDeleted;
    private String createdBy;
    private Date createdDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Account getPartyAccount() {
        return partyAccount;
    }

    public void setPartyAccount(Account partyAccount) {
        this.partyAccount = partyAccount;
    }

    public RealmList<CurrencyValue> getDebitAmount() {
        return debitAmount;
    }

    public void setDebitAmount(RealmList<CurrencyValue> debitAmount) {
        this.debitAmount = debitAmount;
    }

    public RealmList<CurrencyValue> getCreditAmount() {
        return creditAmount;
    }

    public void setCreditAmount(RealmList<CurrencyValue> creditAmount) {
        this.creditAmount = creditAmount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getBhavDate() {
        return bhavDate;
    }

    public void setBhavDate(Date bhavDate) {
        this.bhavDate = bhavDate;
    }

    public Boolean getDirty() {
        return isDirty;
    }

    public void setDirty(Boolean dirty) {
        isDirty = dirty;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public String toString() {
        return "BhavEntry{" +
                "id=" + id +
                ", partyAccount=" + partyAccount +
                ", debitAmount=" + debitAmount +
                ", creditAmount=" + creditAmount +
                ", type='" + type + '\'' +
                ", bhavDate=" + bhavDate +
                ", isDirty=" + isDirty +
                ", isDeleted=" + isDeleted +
                ", createdBy='" + createdBy + '\'' +
                ", createdDate=" + createdDate +
                '}';
    }
}
