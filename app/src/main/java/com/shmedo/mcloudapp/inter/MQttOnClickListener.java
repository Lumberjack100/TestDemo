package com.shmedo.mcloudapp.inter;

import android.view.View;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.inter
 * 创建者:   gonghe
 * 创建时间:  2019-09-28
 * 描述：    TODO
 */
public interface MQttOnClickListener {

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    boolean onSureClick(View v);

    void onCancelClick(View view);
}
