package com.shmedo.mcloudapp.profile;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import com.hjq.toast.ToastUtils;
import com.hoho.android.usbserial.driver.SerialTimeoutException;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.kunminx.architecture.ui.callback.ProtectedUnPeekLiveData;
import com.kunminx.architecture.ui.callback.UnPeekLiveData;
import com.shmedo.core.AppContants;
import com.shmedo.core.usbserial.livedata.USBConnectionStateLiveData;
import com.shmedo.core.usbserial.livedata.state.USBConnectionState;
import com.shmedo.mcloudapp.profile.callback.SerialListener;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/11 <br/>
 * 描述：     TODO
 */
public class UsbSerialManager implements SerialListener {

    private enum Connected {False, Pending, True}

    private final BroadcastReceiver broadcastReceiver;

    private Context mContext;
    private int deviceId, portNum, baudRate;
    private UsbSerialPort usbSerialPort;
    private SerialSocket socket;

    private Connected connected = Connected.False;

    public final UnPeekLiveData<USBConnectionState> state;
    private final UnPeekLiveData<String> responseMsg = new UnPeekLiveData<>();

    public UsbSerialManager(@NotNull Context context) {
        mContext = context.getApplicationContext();
        state = new USBConnectionStateLiveData();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (AppContants.UsbSerial.INTENT_ACTION_GRANT_USB.equals(intent.getAction())) {
                    Boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                    connect(granted);
                }
            }
        };
    }

    public ProtectedUnPeekLiveData<String> getResponseMsg() {
        return responseMsg;
    }

    public void clearLastResponseValue() {
        responseMsg.setValue(null);
    }

    public void initUsb(int deviceId, int port, int baudRate) {
        this.deviceId = deviceId;
        this.portNum = port;
        this.baudRate = baudRate;
    }

    public void connect(Boolean permissionGranted) {
        mContext.registerReceiver(broadcastReceiver, new IntentFilter(AppContants.UsbSerial.INTENT_ACTION_GRANT_USB));

        UsbDevice device = null;
        UsbManager usbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        for (UsbDevice v : usbManager.getDeviceList().values())
            if (v.getDeviceId() == deviceId)
                device = v;
        if (device == null) {
            state.postValue(new USBConnectionState.Disconnected("device not found"));
//            status("connection failed: device not found");
            return;
        }
        UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(device);
//        if (driver == null) {
//            driver = CustomProber.getCustomProber().probeDevice(device);
//        }
        if (driver == null) {
            state.postValue(new USBConnectionState.Disconnected("no driver for device"));
//            status("connection failed: no driver for device");
            return;
        }
        if (driver.getPorts().size() < portNum) {
            state.postValue(new USBConnectionState.Disconnected("not enough ports at device"));
//            status("connection failed: not enough ports at device");
            return;
        }
        usbSerialPort = driver.getPorts().get(portNum);
        UsbDeviceConnection usbConnection = usbManager.openDevice(driver.getDevice());
        if (usbConnection == null && permissionGranted == null && !usbManager.hasPermission(driver.getDevice())) {
            PendingIntent usbPermissionIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(AppContants.UsbSerial.INTENT_ACTION_GRANT_USB), 0);
            usbManager.requestPermission(driver.getDevice(), usbPermissionIntent);
            return;
        }
        if (usbConnection == null) {
            if (!usbManager.hasPermission(driver.getDevice())) {
                state.postValue(new USBConnectionState.Disconnected("permission denied"));
//                status("connection failed: permission denied");
            } else {
                state.postValue(new USBConnectionState.Disconnected("open failed"));
//                status("connection failed: open failed");
            }
            return;
        }

        connected = Connected.Pending;
        try {
            usbSerialPort.open(usbConnection);
            usbSerialPort.setParameters(baudRate, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            socket = new SerialSocket(mContext.getApplicationContext(), usbConnection, usbSerialPort);
            socket.connect(this);
            // usb connect is not asynchronous. connect-success and connect-error are returned immediately from socket.connect
            // for consistency to bluetooth/bluetooth-LE app use same SerialListener and SerialService classes
            onSerialConnect();
        } catch (Exception e) {
            onSerialConnectError(e);
        }
    }

    public void disconnect() {
        connected = Connected.False;
        if (socket != null) {
            socket.disconnect();
            socket = null;
        }
        usbSerialPort = null;
        try {
            mContext.unregisterReceiver(broadcastReceiver);
        } catch (Exception ignored) {
        }
    }

    public final boolean isConnected() {
        return connected == Connected.True;
    }

    public void writeMessage(final String msg) {
        if (connected != Connected.True)
            return;

        byte[] data = msg.getBytes(StandardCharsets.UTF_8);
        try {
            socket.write(data);
        } catch (SerialTimeoutException e) {
//            status("write timeout: " + e.getMessage());
        } catch (Exception e) {
            onSerialIoError(e);
        }
    }

    @Override
    public void onSerialConnect() {
        connected = Connected.True;
        state.postValue(USBConnectionState.Ready.INSTANCE);

    }

    @Override
    public void onSerialConnectError(Exception e) {
        state.postValue(new USBConnectionState.Disconnected(e.getMessage()));
    }

    @Override
    public void onSerialRead(byte[] data) {
        String msg = new String(data, StandardCharsets.UTF_8);
        ToastUtils.show(msg);
        responseMsg.postValue(msg);
    }

    @Override
    public void onSerialIoError(Exception e) {
        synchronized (this) {
            state.postValue(new USBConnectionState.Disconnected("connection lost: " + e.getMessage()));
//        status("connection lost: " + e.getMessage());
            disconnect();
        }
    }
}
