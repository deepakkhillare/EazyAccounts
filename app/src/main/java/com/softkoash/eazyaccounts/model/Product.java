package com.softkoash.eazyaccounts.model;

import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Product extends RealmObject {
    @PrimaryKey
    private Integer id;

    @Required @Index
    private String name;

    private Unit unit;

    private RealmList<CurrencyValue> priceList;

    private RealmList<CurrencyValue> extraChargeRateList;

    private String remarks;

    private boolean isDirty;

    private boolean isDeleted;

    private ProductGroup productGroup;

    private Double grossQuantity;

    private Double netQuantity;

    private Double grossOpeningStock;

    private Double NetOpeningStock;

    @Required
    private Date createdDate;

    private String createdBy;

    private Date updatedDate;

    private String updatedBy;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public RealmList<CurrencyValue> getPriceList() {
        return priceList;
    }

    public void setPriceList(RealmList<CurrencyValue> priceList) {
        this.priceList = priceList;
    }

    public RealmList<CurrencyValue> getExtraChargeRateList() {
        return extraChargeRateList;
    }

    public void setExtraChargeRateList(RealmList<CurrencyValue> extraChargeRateList) {
        this.extraChargeRateList = extraChargeRateList;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean dirty) {
        isDirty = dirty;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public ProductGroup getProductGroup() {
        return productGroup;
    }

    public void setProductGroup(ProductGroup productGroup) {
        this.productGroup = productGroup;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
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

    public Double getGrossOpeningStock() {
        return grossOpeningStock;
    }

    public void setGrossOpeningStock(Double grossOpeningStock) {
        this.grossOpeningStock = grossOpeningStock;
    }

    public Double getNetOpeningStock() {
        return NetOpeningStock;
    }

    public void setNetOpeningStock(Double netOpeningStock) {
        NetOpeningStock = netOpeningStock;
    }
}
