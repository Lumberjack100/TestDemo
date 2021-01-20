package com.shmedo.mcloudapp.deviceconfig.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.kunminx.architecture.ui.callback.ProtectedUnPeekLiveData;
import com.kunminx.architecture.ui.callback.UnPeekLiveData;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;

import java.util.List;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  11/16/20 <br/>
 * 描述：     存储和管理设备在网络配置模式下的数据
 */
public class DeviceNetModelViewModel extends AndroidViewModel {

    private final UnPeekLiveData<List<DispatchCmdItem>> dispatchCmdItemListLiveData;

    public DeviceNetModelViewModel(@NonNull Application application) {
        super(application);
        dispatchCmdItemListLiveData = new UnPeekLiveData.Builder<List<DispatchCmdItem>>().setAllowNullValue(true).create();
    }

    public ProtectedUnPeekLiveData<List<DispatchCmdItem>> getDispatchCmdItemList() {
        return dispatchCmdItemListLiveData;
    }

    public void setDispatchCmdItemList(List<DispatchCmdItem> tempList) {
        dispatchCmdItemListLiveData.postValue(tempList);
    }

}
