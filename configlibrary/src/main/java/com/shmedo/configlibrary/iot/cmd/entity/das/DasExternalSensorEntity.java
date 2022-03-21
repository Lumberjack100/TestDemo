package com.shmedo.configlibrary.iot.cmd.entity.das;

import com.shmedo.configlibrary.ble.interfaces.Validater;

import java.lang.reflect.Field;

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

    //量水堰计
    private String lsycsds;//初始读数
    private String lsyysst;//堰上水头

    //倾角仪
    private String initvalx;//X轴初始值
    private String initvaly;//Y轴初始值


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

    public void setLsycsds(String lsycsds) {
        this.lsycsds = lsycsds;
    }

    public void setLsyysst(String lsyysst) {
        this.lsyysst = lsyysst;
    }

    public void setInitvalx(String initvalx) {
        this.initvalx = initvalx;
    }

    public void setInitvaly(String initvaly) {
        this.initvaly = initvaly;
    }

    @Override
    public void validate() {

    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            stringBuilder.append("index");
            stringBuilder.append("=");
            stringBuilder.append(index);
            stringBuilder.append("&");

            for (Field f : getClass().getDeclaredFields()) {
                Object value = f.get(this);
                if (value != null && !f.getName().equals("index")&& !value.equals("NullKey")) {
                    stringBuilder.append(f.getName());
                    stringBuilder.append("=");
                    stringBuilder.append(value);
                    stringBuilder.append("&");
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        if (stringBuilder.toString().endsWith("&")) {
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
        }
        return stringBuilder.toString();
    }
}
