package com.shmedo.configlibrary.iot.model.hac;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/7/22 <br/>
 * 描述：     ADME AC10 孔深测量配置参数
 */
public class HacMeasuringHoleDepthInfo {
    private String address;//MAC 地址
    private String lowtbtss;//下放堵转检测（0:关闭，1:开启）
    private List<HacHoleAreaDepthInfo> holelist;

    public String getAddress() {
        return TextUtils.isEmpty(address) ? "" : address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    public String getLowtbtss() {
        return lowtbtss;
    }

    public void setLowtbtss(String lowtbtss) {
        this.lowtbtss = lowtbtss;
    }


    public List<HacHoleAreaDepthInfo> getHolelist() {
        return holelist == null ? new ArrayList<>() : holelist;
    }

    public void setHolelist(List<HacHoleAreaDepthInfo> holelist) {
        this.holelist = holelist;
    }
}
