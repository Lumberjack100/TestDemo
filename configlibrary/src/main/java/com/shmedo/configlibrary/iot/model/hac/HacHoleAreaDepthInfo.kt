package com.shmedo.configlibrary.iot.model.hac;

import android.text.TextUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/7/11 <br/>
 * 描述：     孔号、区号、孔深对应实体类
 */
public class HacHoleAreaDepthInfo {
    private String holeno;//孔号
    private String areano;//区号
    private String holedepth;// 孔深
    private String measdepth;// 孔深


    public String getHoleno() {
        return TextUtils.isEmpty(holeno) ? "" : holeno;
    }

    public void setHoleno(String holeno) {
        this.holeno = holeno;
    }

    public String getAreano() {
        return TextUtils.isEmpty(areano) ? "" : areano;
    }

    public void setAreano(String areano) {
        this.areano = areano;
    }

    public String getHoledepth() {
        return TextUtils.isEmpty(holedepth) ? "" : holedepth;
    }

    public void setHoledepth(String holedepth) {
        this.holedepth = holedepth;
    }

    public String getMeasdepth() {
        return TextUtils.isEmpty(measdepth) ? "" : measdepth;
    }

    public void setMeasdepth(String measdepth) {
        this.measdepth = measdepth;
    }
}
