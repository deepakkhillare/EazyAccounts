package com.softkoash.eazyaccounts.model;

import java.util.Date;
import java.util.List;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class CreditInfo extends RealmObject {
    @PrimaryKey
    private Integer id;

    private Integer periodInDays;

    private Double creditLimit1;

    private Double creditLimit2;

    private String createdBy;

    private Date createdDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPeriodInDays() {
        return periodInDays;
    }

    public void setPeriodInDays(Integer periodInDays) {
        this.periodInDays = periodInDays;
    }

    public Double getCreditLimit1() {
        return creditLimit1;
    }

    public void setCreditLimit1(Double creditLimit1) {
        this.creditLimit1 = creditLimit1;
    }

    public Double getCreditLimit2() {
        return creditLimit2;
    }

    public void setCreditLimit2(Double creditLimit2) {
        this.creditLimit2 = creditLimit2;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public String toString() {
        return "CreditInfo{" +
                "id=" + id +
                ", periodInDays=" + periodInDays +
                ", creditLimit1=" + creditLimit1 +
                ", creditLimit2=" + creditLimit2 +
                '}';
    }
}
