package com.shmedo.mcloudapp.deviceconfig.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.kunminx.architecture.ui.callback.ProtectedUnPeekLiveData;
import com.shmedo.mcloudapp.entity.SyncPositionBean;
import com.shmedo.mcloudapp.util.LocationUtils;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  11/16/20 <br/>
 * 描述：     定位相关
 */
public class LocationViewModel extends AndroidViewModel {

    public LocationViewModel(@NonNull Application application) {
        super(application);
    }

    public ProtectedUnPeekLiveData<SyncPositionBean> getSyncPositionBean() {
        return LocationUtils.getInstance().getSyncPositionBean();
    }

}
