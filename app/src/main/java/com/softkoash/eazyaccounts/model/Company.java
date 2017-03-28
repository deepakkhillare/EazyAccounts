package com.softkoash.eazyaccounts.model;

import io.realm.RealmObject;

/**
 * Created by Deepak on 3/27/2017.
 */
public class Company extends RealmObject{
    private String name;

    private String systemVersion;

    public String getSystemVersion() {
        return systemVersion;
    }

    public void setSystemVersion(String systemVersion) {
        this.systemVersion = systemVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
