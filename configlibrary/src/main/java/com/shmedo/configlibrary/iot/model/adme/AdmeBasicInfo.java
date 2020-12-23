package com.shmedo.configlibrary.iot.model.adme;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/23/20 <br/>
 * 描述：     ADME的基本信息
 */
public class AdmeBasicInfo {
    private String sn;
    private String productid;//产品型号
    private String equimodel;//设备模式（0：设备配置模式，1：自动检测模式）

    public String getSn() {
        return TextUtils.isEmpty(sn) ? "" : sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getProductid() {
        return TextUtils.isEmpty(productid) ? "" : productid;
    }

    public void setProductid(String productid) {
        this.productid = productid;
    }

    public String getEquimodel() {
        return TextUtils.isEmpty(equimodel) ? "" : equimodel;
    }

    public void setEquimodel(String equimodel) {
        this.equimodel = equimodel;
    }
}
