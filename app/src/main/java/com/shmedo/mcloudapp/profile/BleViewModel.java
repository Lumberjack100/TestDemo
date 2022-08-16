package com.shmedo.mcloudapp.profile;

import android.app.Application;
import android.bluetooth.BluetoothDevice;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.kunminx.architecture.ui.callback.ProtectedUnPeekLiveData;
import com.kunminx.architecture.ui.callback.UnPeekLiveData;
import com.shmedo.configlibrary.ble.cmd.CommandManager;
import com.shmedo.configlibrary.ble.cmd.entity.AuthenticationConfigEntity;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.utils.DesUtil;
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.deviceconfig.data.DeviceRequest;

import java.nio.charset.StandardCharsets;

import no.nordicsemi.android.ble.livedata.state.ConnectionState;
import no.nordicsemi.android.log.LogSession;
import no.nordicsemi.android.log.Logger;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/7/20 <br/>
 * 描述：   存储和管理与有人物联网蓝牙模块通讯的数据
 */
public class BleViewModel extends AndroidViewModel {
    private final CustomBleManager customBleManager;
    private BluetoothDevice device;

    public final DeviceRequest deviceRequest = new DeviceRequest();

    public BleViewModel(@NonNull Application application) {
        super(application);
        // Initialize the manager.
        customBleManager = new CustomBleManager(application);
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
        }
        reconnect();
    }

    /**
     * Reconnects to previously connected device.
     * If this device was not supported, its services were cleared on disconnection, so
     * reconnection may help.
     */
    public void reconnect() {
        if (device != null && !isConnected()) {
            customBleManager.connect(device)
                    .retry(3, 300)
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
        deviceRequest.clearDeviceApiKey();
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


    /**
     * 蓝牙连接成功,发送认证方式
     */
    public void setAuthenticateWay() {
        AuthenticationConfigEntity configEntity = new AuthenticationConfigEntity(MCloudApp.getCurDeviceToken(), 0);
        String command = CommandManager.getInstance().getCommand(CommandType.AUTHENTICATION_CONFIG, configEntity);
        Timber.d("设置认证类型指令===%s", command);
        sendIOTProtocolCommand("\r\n" + command);
    }

    /**
     * 开始认证流程
     */
    public void sendAuthenticateCodeCmd(String authenticateParam) {
//        Timber.d("解密前:%s", authenticateParam);
        byte[] resultData = StringUtil.hexStringToBytes(authenticateParam);
        try {
            String deskey = "12345678";
            //解密后认证码
            String strDecrypt = new String(DesUtil.decrypt(resultData, deskey), StandardCharsets.UTF_8);
//            Timber.d("解密后:%s", strDecrypt);

            if (!TextUtils.isEmpty(strDecrypt)) {
                //反转6位随机码
                String reverseRandomCode = StringUtil.reverseString(strDecrypt.substring(0, 6));
                byte[] byteEncryt = DesUtil.encrypt((reverseRandomCode + deskey).getBytes(), deskey);
                //加密后认证码
                String strEncryt = StringUtil.bytesToHexString(byteEncryt);
                String cmd = "##222," + MCloudApp.getCurDeviceToken() + ",0," + strEncryt.toUpperCase() + "\r\n";
                Timber.d("设备登录验证指令===%s", cmd);
                sendIOTProtocolCommand(cmd);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (customBleManager.isConnected()) {
            disconnect();
        }
    }
}
