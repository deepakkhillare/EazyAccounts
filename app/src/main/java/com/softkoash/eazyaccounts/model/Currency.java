package com.softkoash.eazyaccounts.model;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Currency extends RealmObject implements AutoIncrementable {
    @PrimaryKey
    private Integer id;
    @Required
    @Index
    private Integer orderNumber;
    @Required
    @Index
    private String name;
    @Required
    @Index
    private String code;
    @Required
    private Integer decimalScale;
    private Boolean isDirty;
    private Boolean isDeleted;
    @Required
    private Date createdDate;
    private Date updateDate;
    private Company company;
    private String createdBy;
    private String updatedBy;

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

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(Integer orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getDecimalScale() {
        return decimalScale;
    }

    public void setDecimalScale(Integer decimalScale) {
        this.decimalScale = decimalScale;
    }

    public Boolean isDirty() {
        return isDirty;
    }

    public void setDirty(Boolean dirty) {
        isDirty = dirty;
    }

    public Boolean isDeleted() {
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

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    @Override
    public void setPrimaryKey(int primaryKey) {
        this.id = primaryKey;
    }

    @Override
    public int getNextPrimaryKey(Realm realm) {
        Number primaryKey = realm.where(Currency.class).max("id");
        int primaryKeyIntValue;
        if (primaryKey == null) {
            primaryKeyIntValue = 1;
        } else {
            primaryKeyIntValue = primaryKey.intValue() + 100;
        }
        return primaryKeyIntValue;
    }
}
