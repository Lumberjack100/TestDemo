package com.shmedo.configlibrary.iot.model.das;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/21 <br/>
 * 描述：    DAS 扩展传感器参数
 */
public class DasExternalSensorInfo implements Serializable {
    private String index;//传感器接入顺序（第一支、第二支...）
    private String type;//传感器类型
    private String addr;//传感器地址/通道
    private String threshold;//触发值
    private String corrval;//修正值

    private String spacing;//测段长
    private String holenum;//测孔编号

    private String tubealti;//安装高程
    private String ropelen;//安装绳长

    private String poly_a;//多项式系数A
    private String poly_b;//多项式系数B
    private String poly_c;//多项式系数C
    private String temp_k;//温度系数K
    private String temp_t0;//初始温度T0

    private String sens_k;//灵敏度K
    private String temp_b;//温度系数b
    private String referval_f;//基准值F

    private String elastic_mod;//膨胀系数(应力计)

    //量水堰计
    private String lsycsds;//初始读数
    private String lsyysst;//堰上水头

    //倾角仪
    private String initvalx;//X轴初始值
    private String initvaly;//Y轴初始值

    private String child_type;//子传感器类型



    public String getIndex() {
        return TextUtils.isEmpty(index) ? "" : index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getType() {
        return TextUtils.isEmpty(type) ? "" : type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAddr() {
        return TextUtils.isEmpty(addr) ? "" : addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public String getThreshold() {
        return TextUtils.isEmpty(threshold) ? "" : threshold;
    }

    public void setThreshold(String threshold) {
        this.threshold = threshold;
    }

    public String getCorrval() {
        return TextUtils.isEmpty(corrval) ? "" : corrval;
    }

    public void setCorrval(String corrval) {
        this.corrval = corrval;
    }

    public String getSpacing() {
        return TextUtils.isEmpty(spacing) ? "" : spacing;
    }

    public void setSpacing(String spacing) {
        this.spacing = spacing;
    }

    public String getHolenum() {
        return TextUtils.isEmpty(holenum) ? "" : holenum;
    }

    public void setHolenum(String holenum) {
        this.holenum = holenum;
    }

    public String getTubealti() {
        return TextUtils.isEmpty(tubealti) ? "" : tubealti;
    }

    public void setTubealti(String tubealti) {
        this.tubealti = tubealti;
    }

    public String getRopelen() {
        return TextUtils.isEmpty(ropelen) ? "" : ropelen;
    }

    public void setRopelen(String ropelen) {
        this.ropelen = ropelen;
    }

    public String getPoly_a() {
        return TextUtils.isEmpty(poly_a) ? "" : poly_a;
    }

    public void setPoly_a(String poly_a) {
        this.poly_a = poly_a;
    }

    public String getPoly_b() {
        return TextUtils.isEmpty(poly_b) ? "" : poly_b;
    }

    public void setPoly_b(String poly_b) {
        this.poly_b = poly_b;
    }

    public String getPoly_c() {
        return TextUtils.isEmpty(poly_c) ? "" : poly_c;
    }

    public void setPoly_c(String poly_c) {
        this.poly_c = poly_c;
    }

    public String getTemp_k() {
        return TextUtils.isEmpty(temp_k) ? "" : temp_k;
    }

    public void setTemp_k(String temp_k) {
        this.temp_k = temp_k;
    }

    public String getTemp_t0() {
        return TextUtils.isEmpty(temp_t0) ? "" : temp_t0;
    }

    public void setTemp_t0(String temp_t0) {
        this.temp_t0 = temp_t0;
    }

    public String getSens_k() {
        return TextUtils.isEmpty(sens_k) ? "" : sens_k;
    }

    public void setSens_k(String sens_k) {
        this.sens_k = sens_k;
    }

    public String getTemp_b() {
        return TextUtils.isEmpty(temp_b) ? "" : temp_b;
    }

    public void setTemp_b(String temp_b) {
        this.temp_b = temp_b;
    }

    public String getReferval_f() {
        return TextUtils.isEmpty(referval_f) ? "" : referval_f;
    }

    public void setReferval_f(String referval_f) {
        this.referval_f = referval_f;
    }

    public String getElastic_mod() {
        return TextUtils.isEmpty(elastic_mod) ? "" : elastic_mod;
    }

    public void setElastic_mod(String elastic_mod) {
        this.elastic_mod = elastic_mod;
    }

    public String getLsycsds() {
        return TextUtils.isEmpty(lsycsds) ? "" : lsycsds;
    }

    public void setLsycsds(String lsycsds) {
        this.lsycsds = lsycsds;
    }

    public String getLsyysst() {
        return TextUtils.isEmpty(lsyysst) ? "" : lsyysst;
    }

    public void setLsyysst(String lsyysst) {
        this.lsyysst = lsyysst;
    }

    public String getInitvalx() {
        return TextUtils.isEmpty(initvalx) ? "" : initvalx;
    }

    public void setInitvalx(String initvalx) {
        this.initvalx = initvalx;
    }

    public String getInitvaly() {
        return TextUtils.isEmpty(initvaly) ? "" : initvaly;
    }

    public void setInitvaly(String initvaly) {
        this.initvaly = initvaly;
    }

    public String getChild_type() {
        return TextUtils.isEmpty(child_type) ? "" : child_type;
    }

    public void setChild_type(String child_type) {
        this.child_type = child_type;
    }
}
