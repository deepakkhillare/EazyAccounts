package com.softkoash.eazyaccounts.model;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Account extends RealmObject {
    @PrimaryKey
    private Integer id;

    private Boolean isCompanyAccount;

    @Index
    private String name;

    private AccountGroup group;

    private String displayName;

    private Contact contact;

    private String vatNumber;

    private String panNumber;

    private RealmList<CurrencyValue> openingBalances;

    private RealmList<CurrencyValue> currentBalances;

    private CreditInfo creditInfo;

    private RealmList<ProductSubscription> productSubscriptions;

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

    public Boolean getCompanyAccount() {
        return isCompanyAccount;
    }

    public void setCompanyAccount(Boolean companyAccount) {
        isCompanyAccount = companyAccount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public String getVatNumber() {
        return vatNumber;
    }

    public void setVatNumber(String vatNumber) {
        this.vatNumber = vatNumber;
    }

    public String getPanNumber() {
        return panNumber;
    }

    public void setPanNumber(String panNumber) {
        this.panNumber = panNumber;
    }

    public RealmList<CurrencyValue> getOpeningBalances() {
        return openingBalances;
    }

    public void setOpeningBalances(RealmList<CurrencyValue> openingBalances) {
        this.openingBalances = openingBalances;
    }

    public RealmList<CurrencyValue> getCurrentBalances() {
        return currentBalances;
    }

    public void setCurrentBalances(RealmList<CurrencyValue> currentBalances) {
        this.currentBalances = currentBalances;
    }

    public CreditInfo getCreditInfo() {
        return creditInfo;
    }

    public void setCreditInfo(CreditInfo creditInfo) {
        this.creditInfo = creditInfo;
    }

    public RealmList<ProductSubscription> getProductSubscriptions() {
        return productSubscriptions;
    }

    public void setProductSubscriptions(RealmList<ProductSubscription> productSubscriptions) {
        this.productSubscriptions = productSubscriptions;
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

    public AccountGroup getGroup() {
        return group;
    }

    public void setGroup(AccountGroup group) {
        this.group = group;
    }
}
