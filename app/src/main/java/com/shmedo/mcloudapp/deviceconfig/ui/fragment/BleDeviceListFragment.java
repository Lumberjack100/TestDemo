package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.bluetooth.MdBluetoothManager;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.deviceconfig.adapter.BleDeviceAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.MDevice;
import com.shmedo.mcloudapp.util.permission.XPermissionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class BleDeviceListFragment extends BaseFragment {
    private static final int REQUEST_ENABLE_BT = 0x002;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    @BindView(R.id.tv_ble_device_count)
    TextView mTvDeviceCount;

    @BindView(R.id.iv_ble_scan_refresh)
    ImageView mIvBleScanRefresh;

    @BindView(R.id.tv_ble_scan_state)
    TextView mTvScanState;

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    private BleDeviceAdapter bleDeviceAdapter;

    private BluetoothAdapter mBluetoothAdapter;

    private MdBluetoothManager mdBluetoothManager;

    private BluetoothLeScannerCompat scanner;

    private ScanCallback scanCallback = new MdLeScanCallback();

    private Handler mHandler;

    private boolean mScanning;

    private Animator animator;


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_ble_device_list;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mHandler = new Handler(Looper.getMainLooper());
        initBluetooth();
        initAdapter();
        initRefreshAnimation();
        startDiscoveryDevice();
    }


    @Override
    public void onPause() {
        super.onPause();
        scanLeDevice(false);
    }

    /**
     * 初始化蓝牙
     */
    private void initBluetooth() {
        final BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = Objects.requireNonNull(bluetoothManager).getAdapter();
//        mdBluetoothManager = MdBluetoothManager.getInstance();
    }

    private void initAdapter() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        bleDeviceAdapter = new BleDeviceAdapter();
        mRecyclerView.setAdapter(bleDeviceAdapter);
        bleDeviceAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {

            }
        });
    }

    private void initRefreshAnimation() {
        animator = AnimatorInflater.loadAnimator(mActivity, R.animator.rotation);
        animator.setTarget(mIvBleScanRefresh);
    }

    /**
     * 扫描蓝牙设备，主要用来判断要连接的设备是否能被搜索到
     */
    private void startDiscoveryDevice() {
        //未打开蓝牙
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        checkBluetoothPermissions();
    }

    private void checkBluetoothPermissions() {
        XPermissionUtils.requestPermissionsResult(getActivity(), 200, new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                new XPermissionUtils.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        bleDeviceAdapter.setNewInstance(new ArrayList<>());
                        scanLeDevice(true);
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        boolean allNeverAskAgain = XPermissionUtils.isAllNeverAskAgain(getActivity(), deniedPermissions);
                        // 所有的权限都被勾上不再询问时，跳转到应用设置界面，引导用户手动打开权限
                        if (allNeverAskAgain) {
                            XPermissionUtils.showRefusePermissionDialog(getActivity(), GlobalUtil.getString(R.string.message_permission_bluetooth_location_rational));
                        } else {
                            ToastUtils.show(GlobalUtil.getString(R.string.message_permission_location_denied));
                        }
                    }
                });
    }

    private void updateRefreshView(boolean isRefresh) {
        if (isRefresh) {
            if (animator != null) {
                animator.start();
            }
            mTvScanState.setText("刷新中...");
        } else {
            if (animator != null) {
                animator.end();
            }
            mTvScanState.setText("重新刷新");
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    scanner.stopScan(scanCallback);
                    updateRefreshView(false);
                }
            }, SCAN_PERIOD);

            mScanning = true;
            initScan();
            updateRefreshView(true);
        } else {
            if (mScanning) {
                mScanning = false;
                scanner.stopScan(scanCallback);
                updateRefreshView(false);
            }
        }
    }

    private void initScan() {
        if (scanner == null) {
            scanner = BluetoothLeScannerCompat.getScanner();
        }
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
//        List<ScanFilter> filters = new ArrayList<>();
//        filters.add(new ScanFilter.Builder().setServiceUuid(ParcelUuid.fromString(GattAttributes.USR_SERVICE)).build());
        scanner.startScan(null, settings, scanCallback);
    }

    @OnClick({R.id.search_container, R.id.ll_scan_refresh})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_container:
                break;

            case R.id.ll_scan_refresh:
                if (mTvScanState.getText().toString().contains("刷新中")) {
                    scanLeDevice(false);
                } else {
                    startDiscoveryDevice();
                }
                break;
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // 判断蓝牙是否启用
                if (resultCode != Activity.RESULT_OK) {
                    ToastUtils.show("蓝牙未启用");
                    return;
                }
                startDiscoveryDevice();
                break;
        }
    }

    private class MdLeScanCallback extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, @NonNull ScanResult result) {
            Timber.d("在线程 " + Thread.currentThread().getName() + " 中扫描到设备");

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Timber.d("在线程 " + Thread.currentThread().getId() + " 中扫描到设备");

                    BluetoothDevice device = result.getDevice();
                    if (device.getName() == null || !device.getName().startsWith("MD")) {
                        return;
                    }

                    for (MDevice mDevice : bleDeviceAdapter.getData()) {
                        if (device.getAddress().equals(mDevice.getDevice().getAddress())) {
                            return;
                        }
                    }

                    MDevice mDev = new MDevice(device, result.getRssi());
                    bleDeviceAdapter.addData(mDev);
                    mTvDeviceCount.setText(String.format(Locale.getDefault(), "(%d)", bleDeviceAdapter.getItemCount()));
                }
            });
        }

        @Override
        public void onBatchScanResults(@NonNull List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    }

}
