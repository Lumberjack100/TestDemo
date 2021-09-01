package com.shmedo.mcloudapp.deviceconfig.callback;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/9/1 <br/>
 * 描述：     TODO
 */
public interface DataCenterConfigListener {
    void onCloseDataServer(String command);
    void onSaveConfig(String command);
    boolean onCheckConnect();
}
