package com.softkoash.eazyaccounts.model;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Nirav on 03-04-2017.
 */

public class Configuration extends RealmObject implements AutoIncrementable {
    @PrimaryKey
    private int configurationId;
    private String name;
    private String category;
    private String value;
    private int isDirty;
    private int isDeleted;
    private Date modifiedBy;
    private Date modifiedTime;
    private Date serverUpdateTime;

    public int getConfigurationId() {
        return configurationId;
    }

    public void setConfigurationId(int configurationId) {
        this.configurationId = configurationId;
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

    public int getIsDirty() {
        return isDirty;
    }

    public void setIsDirty(int isDirty) {
        this.isDirty = isDirty;
    }

    public int getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(int isDeleted) {
        this.isDeleted = isDeleted;
    }

    public Date getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(Date modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Date getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(Date modifiedTime) {
        this.modifiedTime = modifiedTime;
    }

    public Date getServerUpdateTime() {
        return serverUpdateTime;
    }

    public void setServerUpdateTime(Date serverUpdateTime) {
        this.serverUpdateTime = serverUpdateTime;
    }

    @Override
    public void setPrimaryKey(int primaryKey) {
        this.configurationId = primaryKey;
    }

    @Override
    public int getNextPrimaryKey(Realm realm) {
        Number primaryKey = realm.where(Configuration.class).max("ConfigurationId");
        int primaryKeyIntValue;
        if(primaryKey == null) {
            primaryKeyIntValue = 1;
        } else {
            primaryKeyIntValue = primaryKey.intValue() + 1 ;
        }
        return primaryKeyIntValue;
    }
}
