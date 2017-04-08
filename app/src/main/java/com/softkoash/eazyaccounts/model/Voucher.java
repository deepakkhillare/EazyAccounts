package com.softkoash.eazyaccounts.model;

import java.util.Date;

import io.realm.RealmList;

/**
 * Created by Deepak on 4/3/2017.
 */
public class Voucher {
    private Integer id;

    private Types.VoucherType voucherType;

    private Date voucherDate;

    private Double discount;

    private RealmList<CurrencyValue> amountList;

    private Account partyAccount;

    private Account voucherAccount;

    private Date freezeDate;

    private Boolean isFreezed;

    private RealmList<CurrencyValue> taxAmountList;

    private RealmList<CurrencyValue> finalAmountList;

    private RealmList<Product> productsList;

}
