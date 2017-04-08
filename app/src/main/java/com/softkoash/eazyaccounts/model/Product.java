package com.softkoash.eazyaccounts.model;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Deepak on 4/3/2017.
 */
public class Product extends RealmObject {

    private Integer id;

    private String code;

    private String name;

    private Unit unit;

    private ProductStock productStock;

    private RealmList<CurrencyValue> priceList;

    private RealmList<CurrencyValue> extraChargeRateList;

    private String remarks;
}
