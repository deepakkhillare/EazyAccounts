package com.softkoash.eazyaccounts.model;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Nirav on 13-04-2017.
 */

public class BhavEntry extends RealmObject {
    @PrimaryKey
    private Integer id;

    private Voucher voucher;
    private Account account;
    private RealmList<CurrencyValue> debitAmount;
    private RealmList<CurrencyValue> creditAmount;
    private String type;
    private Date bhavDate;
    private Boolean isDirty;
    private Boolean isDeleted;

    public Voucher getVoucher() {
        return voucher;
    }

    public void setVoucher(Voucher voucher) {
        this.voucher = voucher;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
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
}
