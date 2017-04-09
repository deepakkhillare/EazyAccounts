package com.softkoash.eazyaccounts.model;

import java.util.Date;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

/**
 * Created by Deepak on 4/9/2017.
 */
public class ProductSubscription extends RealmObject {
    @PrimaryKey
    private Integer id;

    private Product product;

    private RealmList<CurrencyValue> rates;

    private RealmList<CurrencyValue> extraRates;

    private Boolean isDirty;

    private Boolean isDeleted;

    @Required
    private Date createdDate;

    private Date updatedDate;

    private String createdBy;

    private String updatedBy;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public List<CurrencyValue> getRates() {
        return rates;
    }

    public void setRates(RealmList<CurrencyValue> rates) {
        this.rates = rates;
    }

    public List<CurrencyValue> getExtraRates() {
        return extraRates;
    }

    public void setExtraRates(RealmList<CurrencyValue> extraRates) {
        this.extraRates = extraRates;
    }

    public Boolean getDirty() {
        return isDirty;
    }

    public void setDirty(Boolean dirty) {
        isDirty = dirty;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }
}
