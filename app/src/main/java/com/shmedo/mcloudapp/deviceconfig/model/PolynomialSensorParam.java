package com.shmedo.mcloudapp.deviceconfig.model;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/9/28 <br/>
 * 描述：     振弦式传感器多项式解算参数
 */
public class PolynomialSensorParam {

    private Double poly_a;
    private Double poly_b;
    private Integer poly_c;
    private Double poly_k;
    private Integer temp_t0;

    public Double getPoly_a() {
        return poly_a;
    }

    public void setPoly_a(Double poly_a) {
        this.poly_a = poly_a;
    }

    public Double getPoly_b() {
        return poly_b;
    }

    public void setPoly_b(Double poly_b) {
        this.poly_b = poly_b;
    }

    public Integer getPoly_c() {
        return poly_c;
    }

    public void setPoly_c(Integer poly_c) {
        this.poly_c = poly_c;
    }

    public Double getPoly_k() {
        return poly_k;
    }

    public void setPoly_k(Double poly_k) {
        this.poly_k = poly_k;
    }

    public Integer getTemp_t0() {
        return temp_t0;
    }

    public void setTemp_t0(Integer temp_t0) {
        this.temp_t0 = temp_t0;
    }
}
