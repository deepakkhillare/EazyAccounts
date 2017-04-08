package com.softkoash.eazyaccounts.model;

import io.realm.RealmList;
import io.realm.RealmObject;

public class ProductStock extends RealmObject{
    private Integer id;

    private Double grossQuantity;

    private Double netQuantity;

    private Double rate;

    private RealmList<CurrencyValue> valueList;
}
