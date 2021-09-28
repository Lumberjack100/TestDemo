package com.shmedo.mcloudapp.deviceconfig.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/9/28 <br/>
 * 描述：     振弦式传感器直线式解算参数
 */
public class LinearSensorParam {
    private Double sens_k;
    private Double temp_b;
    private Double temp_t0;
    private Double referval_f;

    public Double getSens_k() {
        return sens_k;
    }

    public void setSens_k(Double sens_k) {
        this.sens_k = sens_k;
    }

    public Double getTemp_b() {
        return temp_b;
    }

    public void setTemp_b(Double temp_b) {
        this.temp_b = temp_b;
    }

    public Double getTemp_t0() {
        return temp_t0;
    }

    public void setTemp_t0(Double temp_t0) {
        this.temp_t0 = temp_t0;
    }

    public Double getReferval_f() {
        return referval_f;
    }

    public void setReferval_f(Double referval_f) {
        this.referval_f = referval_f;
    }
}
