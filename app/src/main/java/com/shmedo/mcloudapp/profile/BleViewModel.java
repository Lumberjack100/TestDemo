package com.shmedo.mcloudapp.profile;

import android.app.Application;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.kunminx.architecture.ui.callback.ProtectedUnPeekLiveData;
import com.kunminx.architecture.ui.callback.UnPeekLiveData;
import com.shmedo.mcloudapp.deviceconfig.data.DeviceApiKeyRequest;

import no.nordicsemi.android.ble.livedata.state.ConnectionState;
import no.nordicsemi.android.log.LogSession;
import no.nordicsemi.android.log.Logger;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/7/20 <br/>
 * 描述：   存储和管理与有人物联网蓝牙模块通讯的数据
 */
public class BleViewModel extends AndroidViewModel {
    private final CustomBleManager customBleManager;
    private BluetoothDevice device;

    public final DeviceApiKeyRequest deviceApiKeyRequest = new DeviceApiKeyRequest();

    public BleViewModel(@NonNull Application application) {
        super(application);
        // Initialize the manager.
        customBleManager = new CustomBleManager(getApplication());
    }

    public LiveData<ConnectionState> getConnectionState() {
        return customBleManager.state;
    }

    public ProtectedUnPeekLiveData<String> getResponseMsg() {
        return customBleManager.getResponseMsg();
    }

    public void clearLastResponseValue() {
        customBleManager.clearLastResponseValue();
    }

    public UnPeekLiveData<Boolean> getLogOutputMode() {
        return customBleManager.getLogOutputMode();
    }

    public void updateLogOutputMode(boolean isLogOutputMode) {
        customBleManager.updateLogOutputMode(isLogOutputMode);
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
            customBleManager.setLogger(logSession);
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
            customBleManager.connect(device)
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
        customBleManager.disconnect().enqueue();
    }

    /**
     * This method returns true if the device is connected. Services could have not been
     * discovered yet.
     */
    public final boolean isConnected() {
        return customBleManager.isConnected();
    }

    public void clearDevice() {
        device = null;
        deviceApiKeyRequest.clearDeviceApiKey();
    }

    /**
     * 发送物联网协议指令
     */
    public void sendIOTProtocolCommand(final String command) {
        if (!isConnected()) {
            return;
        }
        customBleManager.writeMessage(command);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (customBleManager.isConnected()) {
            disconnect();
        }
    }
}
