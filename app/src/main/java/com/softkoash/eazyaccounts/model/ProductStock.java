package com.softkoash.eazyaccounts.model;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ProductStock extends RealmObject{
    @PrimaryKey
    private Integer id;

    private Double grossQuantity;

    private Double netQuantity;

    private Double rate;

    private RealmList<CurrencyValue> valueList;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getGrossQuantity() {
        return grossQuantity;
    }

    public void setGrossQuantity(Double grossQuantity) {
        this.grossQuantity = grossQuantity;
    }

    public Double getNetQuantity() {
        return netQuantity;
    }

    public void setNetQuantity(Double netQuantity) {
        this.netQuantity = netQuantity;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    public RealmList<CurrencyValue> getValueList() {
        return valueList;
    }

    public void setValueList(RealmList<CurrencyValue> valueList) {
        this.valueList = valueList;
    }
}
