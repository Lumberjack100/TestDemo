package com.shmedo.mcloudapp.deviceconfig.viewmodels;

import androidx.lifecycle.ViewModel;

import com.kunminx.architecture.ui.callback.UnPeekLiveData;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/30/20 <br/>
 * 描述：     配置页面
 */
public class ConfigPageViewModel extends ViewModel {

    //配置页面可编辑状态布尔值
    public final UnPeekLiveData<Boolean> configPageEditableChanged = new UnPeekLiveData<>();

}
