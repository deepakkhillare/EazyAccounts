package com.softkoash.eazyaccounts.util;

import android.os.Build;

import io.realm.Realm;

/**
 * Created by Deepak on 4/9/2017.
 */
public class RealmUtil {

    public static int getNextPrimaryKey(Realm realm, Class realmClass) {
        Number primaryKey = realm.where(realmClass).max("id");
        int primaryKeyIntValue;

        if (primaryKey == null) {
            primaryKeyIntValue = 1;
        } else {
            primaryKeyIntValue = primaryKey.intValue() + 1;
        }
        return primaryKeyIntValue;
    }
}
