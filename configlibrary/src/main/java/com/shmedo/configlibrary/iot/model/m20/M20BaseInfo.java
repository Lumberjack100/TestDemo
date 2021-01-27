package com.shmedo.configlibrary.iot.model.m20;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/27/21 <br/>
 * 描述：     M20的基本信息
 */
public class M20BaseInfo {
    private String sn;
    private String productid;//产品型号
    private String firversion;//固件版本

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

    public String getFirversion() {
        return TextUtils.isEmpty(firversion) ? "" : firversion;
    }

    public void setFirversion(String firversion) {
        this.firversion = firversion;
    }
}
