package com.softkoash.eazyaccounts.model;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Deepak on 3/27/2017.
 */
public class Company extends RealmObject{
    @PrimaryKey
    private Integer id;

    @Index
    private String name;

    @Index
    private String code;

    private String appVersion;

    private boolean isDirty;

    private boolean isDeleted;

    private Date createDate;

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String systemVersion) {
        this.appVersion = systemVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean dirty) {
        isDirty = dirty;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
