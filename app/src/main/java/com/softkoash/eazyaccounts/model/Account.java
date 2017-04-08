package com.softkoash.eazyaccounts.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Deepak on 4/3/2017.
 */
public class Account extends RealmObject {
    @PrimaryKey
    private Integer id;

    private boolean isSystemAccount;

    @Index
    private String name;

    private String displayName;

    private Contact contact;

    private String vatNumber;

    private String panNumber;

    private RealmList<CurrencyValue> openingBalances;

    private RealmList<CurrencyValue> currentBalances;

    private CreditInfo creditInfo;

    private RealmList<Product> products;

    public RealmList<CurrencyValue> getOpeningBalances() {
        return openingBalances;
    }

    public void setOpeningBalances(RealmList<CurrencyValue> openingBalances) {
        this.openingBalances = openingBalances;
    }

}
