package com.softkoash.eazyaccounts.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nirav on 13-04-2017.
 */

public enum BhavType {
    SALES("Sales"),  PAYMENT("Payment"), PURCHASE("Purchase"), RECEIPT("Receipt"), SALES_RETURN("Sales Return");

    private String type;

    BhavType(String type) {
        this.type = type;
    }

}
