package com.shmedo.mcloudapp.interfaces;

import android.view.View;

import com.shmedo.core.model.MqttConfigInfo;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.inter
 * 创建者:   gonghe
 * 创建时间:  2019-09-28
 *
 */
public interface MQttOnClickListener {

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    boolean onSureClick(View v, MqttConfigInfo mqttConfigInfoSub, String linkNumber);

    void onCancelClick(View view,String linkNumber);
}
