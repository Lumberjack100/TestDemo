package com.shmedo.mcloudapp.entity.ble;

import android.webkit.JavascriptInterface;

/**
 * 项目名：  das-config-app
 * 包名：    com.example.medoDas.entity
 * 文件名:   DASWorkModelSub
 * 创建者:   dpc
 * 创建时间:  2018/4/14 13:29
 * 描述：    设置das工作模式——待机/激活
 */
public class DASWorkModelSub {
    private int dasWorkModel;

    @JavascriptInterface
    public int getDasWorkModel() {
        return dasWorkModel;
    }


    public void setDasWorkModel(int dasWorkModel) {
        this.dasWorkModel = dasWorkModel;
    }
}
