package com.softkoash.eazyaccounts.model;

import io.realm.RealmList;

/**
 * Created by Deepak on 4/3/2017.
 */
public class ProductStock {
    private Integer id;

    private Double grossQuantity;

    private Double netQuantity;

    private Double rate;

    private RealmList<CurrencyValue> valueList;
}
