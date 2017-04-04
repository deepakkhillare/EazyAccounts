package com.softkoash.eazyaccounts.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Deepak on 4/3/2017.
 */
public class Contact extends RealmObject{
    @PrimaryKey
    private Integer id;

    private String address;

    private String locality;

    private String city;

    private String phone;

    private String email;

    private String mobile;

}
