package com.shmedo.mcloudapp.deviceconfig.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/12/23 <br/>
 * 描述：     产品信息
 */
@Entity
public class ProductInfo {

    @Unique
    private int id;
    private String productName;
    private String productType;
    private String productTypeChName;
    private String useProtocol;
    private int deviceNum;
    private String createTime;
    private String productToken;
    private int companyID;
    private int modelNum;

    private boolean isChecked = false;


    @Generated(hash = 591314857)
    public ProductInfo(int id, String productName, String productType,
            String productTypeChName, String useProtocol, int deviceNum,
            String createTime, String productToken, int companyID, int modelNum,
            boolean isChecked) {
        this.id = id;
        this.productName = productName;
        this.productType = productType;
        this.productTypeChName = productTypeChName;
        this.useProtocol = useProtocol;
        this.deviceNum = deviceNum;
        this.createTime = createTime;
        this.productToken = productToken;
        this.companyID = companyID;
        this.modelNum = modelNum;
        this.isChecked = isChecked;
    }

    @Generated(hash = 49329718)
    public ProductInfo() {
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getProductTypeChName() {
        return productTypeChName;
    }

    public void setProductTypeChName(String productTypeChName) {
        this.productTypeChName = productTypeChName;
    }

    public String getUseProtocol() {
        return useProtocol;
    }

    public void setUseProtocol(String useProtocol) {
        this.useProtocol = useProtocol;
    }

    public int getDeviceNum() {
        return deviceNum;
    }

    public void setDeviceNum(int deviceNum) {
        this.deviceNum = deviceNum;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getProductToken() {
        return productToken;
    }

    public void setProductToken(String productToken) {
        this.productToken = productToken;
    }

    public int getCompanyID() {
        return companyID;
    }

    public void setCompanyID(int companyID) {
        this.companyID = companyID;
    }

    public int getModelNum() {
        return modelNum;
    }

    public void setModelNum(int modelNum) {
        this.modelNum = modelNum;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public boolean getIsChecked() {
        return this.isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }
}
