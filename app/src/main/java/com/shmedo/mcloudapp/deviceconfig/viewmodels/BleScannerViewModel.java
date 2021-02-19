package com.shmedo.mcloudapp.deviceconfig.viewmodels;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.shmedo.mcloudapp.deviceconfig.util.BleScannerUtils;

import java.util.List;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/8/20 <br/>
 * 描述：     TODO #gh#
 */
public class BleScannerViewModel extends AndroidViewModel {
    /**
     * MutableLiveData containing the list of devices.
     */
    private final BleDevicesLiveData devicesLiveData;
    /**
     * MutableLiveData containing the scanner state.
     */
    private final BleScannerStateLiveData bleScannerStateLiveData;


    public BleDevicesLiveData getDevices() {
        return devicesLiveData;
    }

    public BleScannerStateLiveData getBleScannerState() {
        return bleScannerStateLiveData;
    }

    public BleScannerViewModel(@NonNull Application application) {
        super(application);

        bleScannerStateLiveData = new BleScannerStateLiveData(BleScannerUtils.isBleEnabled(),
                BleScannerUtils.isLocationEnabled(application));
        devicesLiveData = new BleDevicesLiveData(true, false, false);
        registerBroadcastReceivers(application);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        getApplication().unregisterReceiver(bluetoothStateBroadcastReceiver);

        if (BleScannerUtils.isMarshmallowOrAbove()) {
            getApplication().unregisterReceiver(locationProviderChangedReceiver);
        }
    }

    /**
     * Forces the observers to be notified. This method is used to refresh the screen after the
     * location permission has been granted. In result, the observer in
     * {@link com.shmedo.mcloudapp.deviceconfig.ui.fragment.BleScannerListFragment} will try to start scanning.
     */
    public void refresh() {
        bleScannerStateLiveData.refresh();
    }

    /**
     * Updates the device filter. Devices that once passed the filter will still be shown
     * even if they move away from the phone, or change the advertising packet. This is to
     * avoid removing devices from the list.
     *
     * @param uuidRequired if true, the list will display only devices with Led-Button Service UUID
     *                     in the advertising packet.
     */
//    public void filterByUuid(final boolean uuidRequired) {
//        if (devicesLiveData.filterByUuid(uuidRequired))
//            bleScannerStateLiveData.recordFound();
//        else
//            bleScannerStateLiveData.clearRecords();
//    }

    /**
     * Updates the device filter. Devices that once passed the filter will still be shown
     * even if they move away from the phone, or change the advertising packet. This is to
     * avoid removing devices from the list.
     *
     * @param nearbyOnly if true, the list will show only devices with high RSSI.
     */
//    public void filterByDistance(final boolean nearbyOnly) {
//        if (devicesLiveData.filterByDistance(nearbyOnly))
//            bleScannerStateLiveData.recordFound();
//        else
//            bleScannerStateLiveData.clearRecords();
//    }

    /**
     * Start scanning for Bluetooth devices.
     */
    public void startScan() {
        if (bleScannerStateLiveData.isScanning()) {
            return;
        }

        // Scanning settings
        final ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(500)
                .setUseHardwareBatchingIfSupported(false)
                .build();

        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        scanner.startScan(null, settings, scanCallback);
        bleScannerStateLiveData.scanningStarted();
    }

    /**
     * Stop scanning for bluetooth devices.
     */
    public void stopScan() {
        if (bleScannerStateLiveData.isScanning() && bleScannerStateLiveData.isBluetoothEnabled()) {
            final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.stopScan(scanCallback);
            bleScannerStateLiveData.scanningStopped();
        }
    }

    public boolean isScanning() {
        return bleScannerStateLiveData.isScanning();
    }

    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(final int callbackType, @NonNull final ScanResult result) {
            // This callback will be called only if the scan report delay is not set or is set to 0.

            // If the packet has been obtained while Location was disabled, mark Location as not required
            if (BleScannerUtils.isLocationRequired(getApplication()) && !BleScannerUtils.isLocationEnabled(getApplication()))
                BleScannerUtils.markLocationNotRequired(getApplication());

            if (devicesLiveData.deviceDiscovered(result)) {
                devicesLiveData.applyFilter();
                bleScannerStateLiveData.recordFound();
            }
        }

        @Override
        public void onBatchScanResults(@NonNull final List<ScanResult> results) {
            // This callback will be called only if the report delay set above is greater then 0.

            // If the packet has been obtained while Location was disabled, mark Location as not required
            if (BleScannerUtils.isLocationRequired(getApplication()) && !BleScannerUtils.isLocationEnabled(getApplication()))
                BleScannerUtils.markLocationNotRequired(getApplication());

            boolean atLeastOneMatchedFilter = false;
            for (final ScanResult result : results) {
                atLeastOneMatchedFilter = devicesLiveData.deviceDiscovered(result) || atLeastOneMatchedFilter;
            }
            if (atLeastOneMatchedFilter) {
                devicesLiveData.applyFilter();
                bleScannerStateLiveData.recordFound();
            }
        }

        @Override
        public void onScanFailed(final int errorCode) {
            // TODO #gh# This should be handled
            bleScannerStateLiveData.scanningStopped();
        }
    };

    /**
     * Register for required broadcast receivers.
     */
    private void registerBroadcastReceivers(@NonNull final Application application) {
        application.registerReceiver(bluetoothStateBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        if (BleScannerUtils.isMarshmallowOrAbove()) {
            application.registerReceiver(locationProviderChangedReceiver, new IntentFilter(LocationManager.MODE_CHANGED_ACTION));
        }
    }

    /**
     * Broadcast receiver to monitor the changes in the location provider.
     */
    private final BroadcastReceiver locationProviderChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final boolean enabled = BleScannerUtils.isLocationEnabled(context);
            bleScannerStateLiveData.setLocationEnabled(enabled);
        }
    };

    /**
     * Broadcast receiver to monitor the changes in the bluetooth adapter.
     */
    private final BroadcastReceiver bluetoothStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
            final int previousState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, BluetoothAdapter.STATE_OFF);

            switch (state) {
                case BluetoothAdapter.STATE_ON:
                    bleScannerStateLiveData.bluetoothEnabled();
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                case BluetoothAdapter.STATE_OFF:
                    if (previousState != BluetoothAdapter.STATE_TURNING_OFF && previousState != BluetoothAdapter.STATE_OFF) {
                        stopScan();
                        bleScannerStateLiveData.bluetoothDisabled();
                    }
                    break;
            }
        }
    };
}
