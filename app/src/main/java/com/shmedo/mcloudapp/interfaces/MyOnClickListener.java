package com.shmedo.mcloudapp.interfaces;

import android.view.View;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.inter
 * 创建者:   gonghe
 * 创建时间:  2019-09-28
 *
 */
public interface MyOnClickListener {

    boolean onSureClick(View v);

    void onCancelClick(View view, String channelNumber);
}
