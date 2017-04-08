package com.softkoash.eazyaccounts.model;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Configuration extends RealmObject implements AutoIncrementable {
    @PrimaryKey
    private Integer id;

    @Required
    @Index
    private String name;

    @Required
    @Index
    private String category;

    private String value;
    private Boolean isDirty;
    private Boolean isDeleted;
    private String updatedBy;
    private Date updateDate;

    @Required
    private Date createdDate;
    private String createdBy;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public void setPrimaryKey(int primaryKey) {
        this.id = primaryKey;
    }

    @Override
    public int getNextPrimaryKey(Realm realm) {
        Number primaryKey = realm.where(Configuration.class).max("id");
        int primaryKeyIntValue;
        if (primaryKey == null) {
            primaryKeyIntValue = 1;
        } else {
            primaryKeyIntValue = primaryKey.intValue() + 1;
        }
        return primaryKeyIntValue;
    }
}
