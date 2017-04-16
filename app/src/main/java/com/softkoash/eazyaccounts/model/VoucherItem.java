package com.softkoash.eazyaccounts.model;

import java.util.Date;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class VoucherItem extends RealmObject {
    @PrimaryKey
    private Integer id;

    private Product product;

    private Double quantity;

    private Double lessQuantity;

    private RealmList<CurrencyValue> rates;

    private Double extraChargeQuantity;

    private RealmList<CurrencyValue> extraCharges;

    private RealmList<CurrencyValue> totals;

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

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getLessQuantity() {
        return lessQuantity;
    }

    public void setLessQuantity(Double lessQuantity) {
        this.lessQuantity = lessQuantity;
    }

    public Double getExtraChargeQuantity() {
        return extraChargeQuantity;
    }

    public void setExtraChargeQuantity(Double extraChargeQuantity) {
        this.extraChargeQuantity = extraChargeQuantity;
    }

    public RealmList<CurrencyValue> getRates() {
        return rates;
    }

    public void setRates(RealmList<CurrencyValue> rates) {
        this.rates = rates;
    }

    public RealmList<CurrencyValue> getExtraCharges() {
        return extraCharges;
    }

    public void setExtraCharges(RealmList<CurrencyValue> extraCharges) {
        this.extraCharges = extraCharges;
    }

    public RealmList<CurrencyValue> getTotals() {
        return totals;
    }

    public void setTotals(RealmList<CurrencyValue> totals) {
        this.totals = totals;
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

    @Override
    public String toString() {
        return "VoucherItem{" +
                "id=" + id +
                ", product=" + product +
                ", quantity=" + quantity +
                ", lessQuantity=" + lessQuantity +
                ", rates=" + rates +
                ", extraChargeQuantity=" + extraChargeQuantity +
                ", extraCharges=" + extraCharges +
                ", totals=" + totals +
                ", isDirty=" + isDirty +
                ", isDeleted=" + isDeleted +
                ", createdDate=" + createdDate +
                ", updatedDate=" + updatedDate +
                ", createdBy='" + createdBy + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                '}';
    }
}
