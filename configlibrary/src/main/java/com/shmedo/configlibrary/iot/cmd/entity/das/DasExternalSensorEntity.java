package com.shmedo.configlibrary.iot.cmd.entity.das;

import android.text.TextUtils;

import com.shmedo.configlibrary.ble.interfaces.Validater;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/21 <br/>
 * 描述：     生成扩展传感器参数拼接指令
 */
public class DasExternalSensorEntity implements Validater {
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


    public void setIndex(String index) {
        this.index = index;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public void setThreshold(String threshold) {
        this.threshold = threshold;
    }

    public void setCorrval(String corrval) {
        this.corrval = corrval;
    }

    public void setSpacing(String spacing) {
        this.spacing = spacing;
    }

    public void setHolenum(String holenum) {
        this.holenum = holenum;
    }

    public void setTubealti(String tubealti) {
        this.tubealti = tubealti;
    }

    public void setRopelen(String ropelen) {
        this.ropelen = ropelen;
    }

    public void setPoly_a(String poly_a) {
        this.poly_a = poly_a;
    }

    public void setPoly_b(String poly_b) {
        this.poly_b = poly_b;
    }

    public void setPoly_c(String poly_c) {
        this.poly_c = poly_c;
    }

    public void setTemp_k(String temp_k) {
        this.temp_k = temp_k;
    }

    public void setTemp_t0(String temp_t0) {
        this.temp_t0 = temp_t0;
    }

    public void setSens_k(String sens_k) {
        this.sens_k = sens_k;
    }

    public void setTemp_b(String temp_b) {
        this.temp_b = temp_b;
    }

    public void setReferval_f(String referval_f) {
        this.referval_f = referval_f;
    }

    public void setElastic_mod(String elastic_mod) {
        this.elastic_mod = elastic_mod;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("index=" + index);
        stringBuilder.append("&");

        stringBuilder.append("type=" + type);
        stringBuilder.append("&");

        if (!TextUtils.isEmpty(addr)) {
            stringBuilder.append("addr=" + addr);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(threshold)) {
            stringBuilder.append("threshold=" + threshold);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(corrval)) {
            stringBuilder.append("corrval=" + corrval);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(spacing)) {
            stringBuilder.append("spacing=" + spacing);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(holenum)) {
            stringBuilder.append("holenum=" + holenum);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(tubealti)) {
            stringBuilder.append("tubealti=" + tubealti);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(ropelen)) {
            stringBuilder.append("ropelen=" + ropelen);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(poly_a)) {
            stringBuilder.append("poly_a=" + poly_a);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(poly_b)) {
            stringBuilder.append("poly_b=" + poly_b);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(poly_c)) {
            stringBuilder.append("poly_c=" + poly_c);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(temp_k)) {
            stringBuilder.append("temp_k=" + temp_k);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(temp_t0)) {
            stringBuilder.append("temp_t0=" + temp_t0);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(sens_k)) {
            stringBuilder.append("sens_k=" + sens_k);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(temp_b)) {
            stringBuilder.append("temp_b=" + temp_b);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(referval_f)) {
            stringBuilder.append("referval_f=" + referval_f);
            stringBuilder.append("&");
        }
        if (!TextUtils.isEmpty(elastic_mod)) {
            stringBuilder.append("elastic_mod=" + elastic_mod);
            stringBuilder.append("&");
        }

        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        }
        return stringBuilder.toString();
    }
}
