package no.nordicsemi.android.ble.livedata;

import android.bluetooth.BluetoothDevice;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import no.nordicsemi.android.ble.livedata.state.BondState;
import no.nordicsemi.android.ble.observer.BondingObserver;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/3 <br/>
 * 描述：
 */
class BondingStateLiveData extends LiveData<BondState> implements BondingObserver {

    BondingStateLiveData() {
        setValue(BondState.NotBonded.INSTANCE);
    }

    @Override
    public void onBondingRequired(@NonNull final BluetoothDevice device) {
        setValue(BondState.Bonding.INSTANCE);
    }

    @Override
    public void onBonded(@NonNull final BluetoothDevice device) {
        setValue(BondState.Bonded.INSTANCE);
    }

    @Override
    public void onBondingFailed(@NonNull final BluetoothDevice device) {
        setValue(BondState.NotBonded.INSTANCE);
    }
}
