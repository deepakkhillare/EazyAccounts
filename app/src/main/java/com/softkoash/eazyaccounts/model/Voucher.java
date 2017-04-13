package com.softkoash.eazyaccounts.model;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Voucher extends RealmObject {
    @PrimaryKey
    private Integer id;

    private String voucherType;

    private Date voucherDate;

    private RealmList<CurrencyValue> amountList;

    private Account partyAccount;

    private Account voucherAccount;

    private RealmList<VoucherEntry> voucherEntries;

    private RealmList<VoucherItem> voucherItems;

    private RealmList<BhavEntry> bhavEntries;

    private Date freezeDate;

    private Boolean isFreezed;

    private String narration;

    private Boolean isDirty;

    private Boolean isDeleted;

    @Required
    private Date createdDate;

    private Date updatedDate;

    private String createdBy;

    private String updatedBy;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getVoucherType() {
        return voucherType;
    }

    public void setVoucherType(String voucherType) {
        this.voucherType = voucherType;
    }

    public Date getVoucherDate() {
        return voucherDate;
    }

    public void setVoucherDate(Date voucherDate) {
        this.voucherDate = voucherDate;
    }

    public RealmList<CurrencyValue> getAmountList() {
        return amountList;
    }

    public void setAmountList(RealmList<CurrencyValue> amountList) {
        this.amountList = amountList;
    }

    public Account getPartyAccount() {
        return partyAccount;
    }

    public void setPartyAccount(Account partyAccount) {
        this.partyAccount = partyAccount;
    }

    public Account getVoucherAccount() {
        return voucherAccount;
    }

    public void setVoucherAccount(Account voucherAccount) {
        this.voucherAccount = voucherAccount;
    }

    public Date getFreezeDate() {
        return freezeDate;
    }

    public void setFreezeDate(Date freezeDate) {
        this.freezeDate = freezeDate;
    }

    public Boolean getFreezed() {
        return isFreezed;
    }

    public void setFreezed(Boolean freezed) {
        isFreezed = freezed;
    }

    public String getNarration() {
        return narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

    public RealmList<VoucherEntry> getVoucherEntries() {
        return voucherEntries;
    }

    public void setVoucherEntries(RealmList<VoucherEntry> voucherEntries) {
        this.voucherEntries = voucherEntries;
    }

    public RealmList<VoucherItem> getVoucherItems() {
        return voucherItems;
    }

    public void setVoucherItems(RealmList<VoucherItem> voucherItems) {
        this.voucherItems = voucherItems;
    }

    public RealmList<BhavEntry> getBhavEntries() {
        return bhavEntries;
    }

    public void setBhavEntries(RealmList<BhavEntry> bhavEntries) {
        this.bhavEntries = bhavEntries;
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

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public String toString() {
        return "Voucher{" +
                "id=" + id +
                ", voucherType='" + voucherType + '\'' +
                ", voucherDate=" + voucherDate +
                ", amountList=" + amountList +
                ", partyAccount=" + partyAccount +
                ", voucherAccount=" + voucherAccount +
                ", voucherEntries=" + voucherEntries +
                ", voucherItems=" + voucherItems +
                ", freezeDate=" + freezeDate +
                ", isFreezed=" + isFreezed +
                ", narration='" + narration + '\'' +
                ", isDirty=" + isDirty +
                ", isDeleted=" + isDeleted +
                ", createdDate=" + createdDate +
                ", updatedDate=" + updatedDate +
                ", createdBy='" + createdBy + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                '}';
    }
}
