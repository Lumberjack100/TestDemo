package com.shmedo.core.event;


import com.shmedo.core.model.WifiBean;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.entity.event
 * 文件名:   WifiEvent
 * 创建者:   dpc
 * 创建时间:  2019/1/25 15:12
 *
 */
public class WifiEvent {
    private String message;
    private WifiBean wifiBean;

    public String getMessage() {
        return message;
    }


    public void setMessage(String message) {
        this.message = message;
    }


    public WifiBean getWifiBean() {
        return wifiBean;
    }


    public void setWifiBean(WifiBean wifiBean) {
        this.wifiBean = wifiBean;
    }
}
