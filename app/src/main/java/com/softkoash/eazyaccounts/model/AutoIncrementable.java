package com.softkoash.eazyaccounts.model;

import io.realm.Realm;

public interface AutoIncrementable {
    void setPrimaryKey(int primaryKey);
    int getNextPrimaryKey(Realm realm);
}
