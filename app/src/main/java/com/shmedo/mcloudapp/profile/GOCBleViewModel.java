package com.shmedo.mcloudapp.profile;

import android.app.Application;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.blankj.utilcode.util.SPStaticUtils;
import com.kunminx.architecture.ui.callback.ProtectedUnPeekLiveData;
import com.kunminx.architecture.ui.callback.UnPeekLiveData;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.deviceconfig.data.DeviceApiKeyRequest;
import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.Map;

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

    public final DeviceApiKeyRequest deviceApiKeyRequest = new DeviceApiKeyRequest();


    public GOCBleViewModel(@NonNull Application application) {
        super(application);
        // Initialize the manager.
        gocManager = new GOCManager(getApplication());
    }

    public LiveData<ConnectionState> getConnectionState() {
        return gocManager.state;
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
        deviceApiKeyRequest.clearDeviceApiKey();
    }

    /**
     * 发送物联网协议指令
     */
    public void sendIOTProtocolCommand(final String command) {
        if (!isConnected()) {
            return;
        }
        gocManager.writeMessage(command);

        try {
            Map<String, Object> valueMap = new HashMap<String, Object>();
            valueMap.put("login_user", SPStaticUtils.getString(AppContants.User.UID, ""));
            valueMap.put("device_sn", MCloudApp.getCurDeviceToken());
            valueMap.put("command_type", "##");
            valueMap.put("command_content", command);
            MobclickAgent.onEventObject(MCloudApp.getContext(), "Dispatch_Command", valueMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (gocManager.isConnected()) {
            disconnect();
        }
    }
}