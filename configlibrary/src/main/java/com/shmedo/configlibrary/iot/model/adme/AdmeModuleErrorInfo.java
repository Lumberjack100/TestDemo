package com.shmedo.configlibrary.iot.model.adme;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/11/23 <br/>
 * 描述：     ADME 模块异常信息
 */
public class AdmeModuleErrorInfo {
    /**
     * VOLT_POWER_UNDER：设备欠压
     * VOLT_POWER_OVER：设备过压
     * VOLT_SENSOR_UNDER：测斜仪欠压
     * FAIL：测斜仪配对失败
     * OVER_C_SF：伺服电机过流
     * OVER_T_SF：伺服电机过力矩
     * DZ_SF：伺服电机低力矩
     * OVER_V_SF：伺服电机超速
     * ERROR_WIRING_SF：伺服电机接线错误
     * DZ_JMQ：计米器堵转
     * FZ_RUN_JMQ：计米器反转
     * ERROR_WIRING_JMQ：计米器接线错误
     */
    private String errinfo;//异常内容

    public String getErrinfo() {
        return TextUtils.isEmpty(errinfo) ? "" : errinfo;
    }

    public void setErrinfo(String errinfo) {
        this.errinfo = errinfo;
    }
}
