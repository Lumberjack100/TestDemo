package com.shmedo.mcloudapp.deviceconfig.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.kunminx.architecture.ui.callback.ProtectedUnPeekLiveData;
import com.shmedo.mcloudapp.bluetooth.BluetoothEvent;
import com.shmedo.mcloudapp.bluetooth.NewBleManager;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/5 <br/>
 * 描述：     TODO
 */
public class BleViewModel extends AndroidViewModel {
    public final NewBleManager bleManager = NewBleManager.getInstance();

    public BleViewModel(@NonNull Application application) {
        super(application);
    }


    public ProtectedUnPeekLiveData<BluetoothEvent> getBluetoothEventLiveData() {
        return bleManager.getBluetoothEventLiveData();
    }

    public void clearLastValue(){
        bleManager.clearLastValue();
    }

}
