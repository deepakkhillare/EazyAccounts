package com.softkoash.eazyaccounts.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Deepak on 4/3/2017.
 */
public class CurrencyValue extends RealmObject {
    @PrimaryKey
    private Integer id;

    @Required
    private Currency currency;

    @Required
    private Double value;

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }


    @Override
    public String toString() {
        return "CurrencyValue{" +
                "currency=" + currency +
                ", value=" + value +
                '}';
    }
}
