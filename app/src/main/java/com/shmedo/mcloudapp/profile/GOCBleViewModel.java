package com.shmedo.mcloudapp.profile;

import android.app.Application;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.kunminx.architecture.ui.callback.ProtectedUnPeekLiveData;
import com.kunminx.architecture.ui.callback.UnPeekLiveData;
import com.shmedo.mcloudapp.deviceconfig.data.repository.DeviceRepository;

import no.nordicsemi.android.ble.livedata.state.ConnectionState;
import no.nordicsemi.android.log.LogSession;
import no.nordicsemi.android.log.Logger;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/18/21 <br/>
 * 描述：    存储和管理与 深圳市顾凯信息技术有限公司GOC-MD-400蓝牙模块通讯的数据
 */
public class GOCBleViewModel extends AndroidViewModel {
    private final GOCManager gocManager;
    private BluetoothDevice device;
    
    public GOCBleViewModel(@NonNull Application application) {
        super(application);
        // Initialize the manager.
        gocManager = new GOCManager(getApplication());
    }

    public LiveData<ConnectionState> getConnectionState() {
        return gocManager.getState();
    }

    public ProtectedUnPeekLiveData<String> getResponseMsg() {
        return gocManager.getResponseMsg();
    }

    public UnPeekLiveData<Boolean> getLogOutputMode() {
        return gocManager.getLogOutputMode();
    }

    public void updateLogOutputMode(boolean isLogOutputMode) {
        gocManager.updateLogOutputMode(isLogOutputMode);
    }

    public ProtectedUnPeekLiveData<String> getDeviceApiKey() {
        return DeviceRepository.getInstance().getDeviceApiKeyLiveData();
    }

    public void queryDeviceApiKeyBySn(String sn) {
        DeviceRepository.getInstance().queryDeviceApiKeyBySn(sn);
    }

    /**
     * Connect to the given peripheral.
     *
     * @param target the target device.
     */
    public void connect(@NonNull final BluetoothDevice target) {
        // Prevent from calling again when called again (screen orientation changed).
        if (device == null) {
            device = target;
            final LogSession logSession = Logger.newSession(getApplication(), null, target.getAddress(), target.getName());
            gocManager.setLogger(logSession);
            reconnect();
        }
    }

    /**
     * Reconnects to previously connected device.
     * If this device was not supported, its services were cleared on disconnection, so
     * reconnection may help.
     */
    public void reconnect() {
        if (device != null) {
            gocManager.connect(device)
                    .retry(3, 100)
                    .useAutoConnect(false)
                    .enqueue();
        }
    }

    /**
     * Disconnect from peripheral.
     */
    public void disconnect() {
        device = null;
        gocManager.disconnect().enqueue();
    }

    /**
     * This method returns true if the device is connected. Services could have not been
     * discovered yet.
     */
    public final boolean isConnected() {
        return gocManager.isConnected();
    }

    public void clearDevice() {
        device = null;
    }

    /**
     * 发送物联网协议指令
     */
    public void sendIOTProtocolCommand(final String command) {
//        Timber.v("准备发送指令：%s", command);
        gocManager.writeMessage(command);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (gocManager.isConnected()) {
            disconnect();
        }
    }
}