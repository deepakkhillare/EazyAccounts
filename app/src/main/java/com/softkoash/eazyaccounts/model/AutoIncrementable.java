package com.softkoash.eazyaccounts.model;

import io.realm.Realm;

/**
 * Created by Nirav on 03-04-2017.
 */

public interface AutoIncrementable {
    public void setPrimaryKey(int primaryKey);
    public int getNextPrimaryKey(Realm realm);
}
