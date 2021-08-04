package no.nordicsemi.android.ble.livedata;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import no.nordicsemi.android.ble.annotation.DisconnectionReason;
import no.nordicsemi.android.ble.livedata.state.ConnectionState;
import no.nordicsemi.android.ble.observer.ConnectionObserver;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/3 <br/>
 * 描述：     TODO
 */
class ConnectionStateLiveData extends LiveData<ConnectionState> implements ConnectionObserver {

    ConnectionStateLiveData() {
        setValue(new ConnectionState.Disconnected(ConnectionObserver.REASON_UNKNOWN));
    }

    @Override
    public void onDeviceConnecting(@NonNull final BluetoothDevice device) {
        setValue(ConnectionState.Connecting.INSTANCE);
    }

    @Override
    public void onDeviceConnected(@NonNull final BluetoothDevice device) {
        setValue(ConnectionState.Initializing.INSTANCE);
    }

    @Override
    public void onDeviceFailedToConnect(
            @NonNull final BluetoothDevice device,
            @DisconnectionReason final int reason) {
        setValue(new ConnectionState.Disconnected(reason));
    }

    @Override
    public void onDeviceReady(@NonNull final BluetoothDevice device) {
        setValue(ConnectionState.Ready.INSTANCE);
    }

    @Override
    public void onDeviceDisconnecting(@NonNull final BluetoothDevice device) {
        setValue(ConnectionState.Disconnecting.INSTANCE);
    }

    @Override
    public void onDeviceDisconnected(
            @NonNull final BluetoothDevice device,
            @DisconnectionReason final int reason) {
        setValue(new ConnectionState.Disconnected(reason));
    }
}
