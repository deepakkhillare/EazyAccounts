package com.softkoash.eazyaccounts.model;

import io.realm.RealmObject;

public class CreditInfo extends RealmObject {
    private Integer id;

    private Integer periodInDays;

    private Double creditLimit1;

    private Double creditLimit2;
}
