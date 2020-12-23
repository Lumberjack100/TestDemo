package com.shmedo.mcloudapp.profile;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kunminx.architecture.ui.callback.ProtectedUnPeekLiveData;
import com.kunminx.architecture.ui.callback.UnPeekLiveData;
import com.shmedo.mcloudapp.profile.callback.IOTCommandDataCallback;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import no.nordicsemi.android.ble.callback.FailCallback;
import no.nordicsemi.android.ble.callback.SuccessCallback;
import no.nordicsemi.android.ble.data.Data;
import no.nordicsemi.android.ble.livedata.ObservableBleManager;
import no.nordicsemi.android.log.LogContract;
import no.nordicsemi.android.log.LogSession;
import no.nordicsemi.android.log.Logger;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/7/20 <br/>
 * 描述：    有人蓝牙模块连接管理类
 */
public class USRManager extends ObservableBleManager {
    /**
     * The service UUID.<br>
     * 济南有人物联网公司低功耗蓝牙模块服务
     */
    public static final UUID USR_SERVICE_UUID = UUID.fromString("0003cdd0-0000-1000-8000-00805f9b0131");
    /**
     * A UUID of a characteristic with notify property.
     */
    private static final UUID NOTIFY_CHARACTERISTIC_UUID = UUID.fromString("0003cdd1-0000-1000-8000-00805f9b0131");
    /**
     * A UUID of a characteristic with write property.
     */
    private static final UUID WRITABLE_CHARACTERISTIC_UUID = UUID.fromString("0003cdd2-0000-1000-8000-00805f9b0131");

    private final UnPeekLiveData<String> responseMsg = new UnPeekLiveData<>();
    private final UnPeekLiveData<Boolean> logOutputModeLiveData = new UnPeekLiveData<>();

    private BluetoothGattCharacteristic notifyCharacteristic, writeCharacteristic;
    private LogSession logSession;
    private boolean supported;

    public USRManager(@NotNull Context context) {
        super(context);
    }

    public ProtectedUnPeekLiveData<String> getResponseMsg() {
        return responseMsg;
    }

    public void clearLastResponseValue() {
        responseMsg.setValue(null);
    }

    public UnPeekLiveData<Boolean> getLogOutputMode() {
        return logOutputModeLiveData;
    }

    public void updateLogOutputMode(boolean isLogOutputMode) {
        logOutputModeLiveData.postValue(isLogOutputMode);
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return new USRBleManagerGattCallback();
    }

    /**
     * Sets the log session to be used for low level logging.
     *
     * @param session the session, or null, if nRF Logger is not installed.
     */
    public void setLogger(@Nullable final LogSession session) {
        logSession = session;
    }

    @Override
    public void log(final int priority, @NonNull final String message) {
        // The priority is a Log.X constant, while the Logger accepts it's log levels.
        Logger.log(logSession, LogContract.Log.Level.fromPriority(priority), message);
    }

    @Override
    protected boolean shouldClearCacheWhenDisconnected() {
        return !supported;
    }

    private final IOTCommandDataCallback buttonCallback = new IOTCommandDataCallback() {
        @Override
        public void onResponseReceived(@NonNull BluetoothDevice device, String result) {
            Timber.d("接收数据(onResponseReceived): length=%s bytes;content: %s", result.getBytes().length, result);
            log(LogContract.Log.Level.APPLICATION, "接收数据(onResponseReceived): " + result);
            responseMsg.setValue(result);
        }

        @Override
        public void onInvalidDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
            log(Log.WARN, "Invalid data received: " + data);
        }
    };

    /**
     * BluetoothGatt callbacks object.
     */
    private class USRBleManagerGattCallback extends BleManagerGattCallback {
        @Override
        protected void initialize() {
            // Increase the MTU
            requestMtu(512)
                    .with((device, mtu) -> log(LogContract.Log.Level.APPLICATION, "MTU changed to " + mtu))
                    .done(device -> {
                        // You may do some logic in here that should be done when the request finished successfully.
                        // In case of MTU this method is called also when the MTU hasn't changed, or has changed
                        // to a different (lower) value. Use .with(...) to get the MTU value.
                    })
                    .fail((device, status) -> log(Log.WARN, "MTU change not supported"))
                    .enqueue();
            setNotificationCallback(notifyCharacteristic).with(buttonCallback);
            // Enable notifications
            enableNotifications(notifyCharacteristic)
                    // Method called after the data were sent (data will contain 0x0100 in this case)
                    .with((device, data) -> log(Log.DEBUG, "Data sent: " + data.toString()))
                    // Method called when the request finished successfully. This will be called after .with(..) callback
                    .done(device -> log(LogContract.Log.Level.APPLICATION, "Notifications enabled successfully"))
                    // Methods called in case of an error, for example when the characteristic does not have Notify property
                    .fail((device, status) -> log(Log.WARN, "Failed to enable notifications"))
                    .enqueue();
        }

        @Override
        protected boolean isRequiredServiceSupported(@NonNull BluetoothGatt gatt) {
            final BluetoothGattService service = gatt.getService(USR_SERVICE_UUID);
            if (service != null) {
                notifyCharacteristic = service.getCharacteristic(NOTIFY_CHARACTERISTIC_UUID);
                writeCharacteristic = service.getCharacteristic(WRITABLE_CHARACTERISTIC_UUID);
            }

            boolean writeRequest = false;
            boolean writeCommand = false;
            if (writeCharacteristic != null) {
                final int rxProperties = writeCharacteristic.getProperties();
                writeRequest = (rxProperties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0;
                writeCommand = (rxProperties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0;
            }

            supported = notifyCharacteristic != null && writeCharacteristic != null && (writeRequest || writeCommand);
            return supported;
        }

        @Override
        protected void onDeviceDisconnected() {
            notifyCharacteristic = null;
            writeCharacteristic = null;
        }

    }

    /**
     * This method will write important data to the device.
     *
     * @param command parameter to be written.
     */
    public void writeMessage(final String command) {
        if (writeCharacteristic == null)
            return;
//        Timber.d("准备发送数据(writeMessage): length=%s bytes;content: %s", command.getBytes().length, command);
        // Write some data to the characteristic.
        writeCharacteristic(writeCharacteristic, Data.from(command))
                // If data are longer than MTU-3, they will be chunked into multiple packets.
                // Check out other split options, with .split(...).
                .split()
                // Callback called when data were sent, or added to outgoing queue in case
                // Write Without Request type was used.
                .with((device, data) -> log(Log.DEBUG, data.size() + " bytes were sent"))
                // Callback called when data were sent, or added to outgoing queue in case
                // Write Without Request type was used. This is called after .with(...) callback.
                .done(new SuccessCallback() {
                    @Override
                    public void onRequestCompleted(@NonNull BluetoothDevice device) {
                        Timber.d("已写入数据(writeMessage): length=%s bytes;content: %s", command.getBytes().length, command);
                        log(LogContract.Log.Level.APPLICATION, "已发送数据(writeMessage): " + command);
                    }
                })
                // Callback called when write has failed.
                .fail(new FailCallback() {
                    @Override
                    public void onRequestFailed(@NonNull BluetoothDevice device, int status) {
                        Timber.d("未写入数据(writeMessage): length=%s bytes;content: %s", command.getBytes().length, command);
                        log(Log.WARN, "未发送数据(writeMessage): " + command);
                    }
                })
                .enqueue();
    }
}
