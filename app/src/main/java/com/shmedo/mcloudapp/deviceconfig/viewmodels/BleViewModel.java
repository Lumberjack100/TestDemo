package com.shmedo.mcloudapp.deviceconfig.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.kunminx.architecture.ui.callback.ProtectedUnPeekLiveData;
import com.kunminx.architecture.ui.callback.UnPeekLiveData;
import com.shmedo.mcloudapp.bluetooth.BluetoothEvent;
import com.shmedo.mcloudapp.bluetooth.NewBleManager;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/5 <br/>
 * 描述：     TODO
 */
public class BleViewModel extends AndroidViewModel {
    public final NewBleManager bleManager = new NewBleManager();

    public BleViewModel(@NonNull Application application) {
        super(application);
    }


    public ProtectedUnPeekLiveData<BluetoothEvent> getBluetoothEvent() {
        return bleManager.getBluetoothEvent();
    }

    public void clearLastValue(){
        bleManager.clearLastValue();
    }

    public UnPeekLiveData<Boolean> getLogOutputMode() {
        return bleManager.getLogOutputMode();
    }

    public void updateLogOutputMode(boolean isLogOutputMode) {
        bleManager.updateLogOutputMode(isLogOutputMode);
    }
}
