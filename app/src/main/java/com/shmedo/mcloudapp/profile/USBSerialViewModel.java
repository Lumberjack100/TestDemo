package com.shmedo.mcloudapp.profile;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.kunminx.architecture.ui.callback.ProtectedUnPeekLiveData;
import com.shmedo.core.usbserial.livedata.state.USBConnectionState;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/11 <br/>
 * 描述：     TODO
 */
public class USBSerialViewModel extends AndroidViewModel {
    private final UsbSerialManager usbSerialManager;


    public USBSerialViewModel(@NonNull Application application) {
        super(application);
        usbSerialManager = new UsbSerialManager(getApplication());
    }

    public LiveData<USBConnectionState> getConnectionState() {
        return usbSerialManager.getUSBConnectionState();
    }

    public ProtectedUnPeekLiveData<byte[]> getResponseMsg() {
        return usbSerialManager.getResponseMsg();
    }

    public void clearLastResponseValue() {
        usbSerialManager.clearLastResponseValue();
    }


    public void initUsb(int deviceId, int port, int baudRate) {
        usbSerialManager.initUsb(deviceId, port, baudRate);
    }

    public void connect() {
        usbSerialManager.connect(null);
    }

    public void disconnect() {
        usbSerialManager.disconnect();
        usbSerialManager.onActiveDisconnect();
    }

    public final boolean isConnected() {
        return usbSerialManager.isConnected();
    }

    public void sendData(final String msg) {
        if (!isConnected()) {
            return;
        }
        usbSerialManager.writeMessage(msg);
    }

    public void sendData(final byte[] data) {
        if (!isConnected()) {
            return;
        }
        usbSerialManager.writeMessage(data);
    }

    @Override
    protected void onCleared() {
        disconnect();
        super.onCleared();
    }
}
