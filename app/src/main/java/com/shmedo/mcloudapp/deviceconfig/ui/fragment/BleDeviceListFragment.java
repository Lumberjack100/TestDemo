package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.deviceconfig.adapter.BleDeviceAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.DiscoveredBluetoothDevice;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceConfigActivity;
import com.shmedo.mcloudapp.util.KeyBordUtils;
import com.shmedo.mcloudapp.util.LocationUtils;
import com.shmedo.mcloudapp.util.permission.PermissionHelper;
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

/**
 * @deprecated Use {@link BleScannerListFragment} instead.
 */
public class BleDeviceListFragment extends BaseFragment implements TextWatcher, TextView.OnEditorActionListener {
    private static final int REQUEST_ENABLE_BT = 0x002;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    @BindView(R.id.search_placeholder)
    View searchPlaceholder;

    @BindView(R.id.search_container)
    View searchContainer;

    @BindView(R.id.ll_refresh)
    View refreshLayout;

    @BindView(R.id.et_keywords)
    ClearEditText mEtKeyWords;

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

    private BluetoothLeScannerCompat scanner;

    private ScanCallback scanCallback = new MdLeScanCallback();

    private Handler mHandler;

    private boolean mScanning = false;

    private List<DiscoveredBluetoothDevice> tempDeviceList = new ArrayList<>();

    private Animator animator;

    private final Runnable mStopScanRunnable = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };

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
        setEditTextListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mScanning) {
            return;
        }
        MCloudApp.getMainHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startDiscoveryDevice();
            }
        }, 500);
    }

    @Override
    public void onStop() {
        super.onStop();
        scanLeDevice(false);
        mHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 初始化蓝牙
     */
    private void initBluetooth() {
        final BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = Objects.requireNonNull(bluetoothManager).getAdapter();
    }

    private void initAdapter() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        bleDeviceAdapter = new BleDeviceAdapter();
        mRecyclerView.setAdapter(bleDeviceAdapter);
        bleDeviceAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                scanLeDevice(false);
                DiscoveredBluetoothDevice bluetoothDevice = bleDeviceAdapter.getItem(position);
                String deviceName = bluetoothDevice.getName();
                MCloudApp.setCurDeviceToken(deviceName.substring(3));
//
//                String deviceInfo = "";
//                if (deviceName.endsWith("T")) {
//                    deviceInfo = "MEDO," + deviceName.substring(3) + ",ADME";
//                } else if (deviceName.endsWith("L")) {
//                    deviceInfo = "MEDO," + deviceName.substring(3) + ",DAS";
//                }
                DeviceConfigActivity.startActivity(getActivity(), AppContants.CommunicationWay.BLE_CONNECT, bluetoothDevice);
            }
        });
    }

    private void initRefreshAnimation() {
        animator = AnimatorInflater.loadAnimator(mActivity, R.animator.rotation);
        animator.setTarget(mIvBleScanRefresh);
    }

    private void setEditTextListener() {
        mEtKeyWords.addTextChangedListener(this);
        mEtKeyWords.setOnEditorActionListener(this);
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

        checkPermissionForGPS();
    }

    /**
     * 检查是否打开系统位置服务，如果开启了，接着检查是否授予 APP 定位权限
     */
    public void checkPermissionForGPS() {
        if (LocationUtils.getInstance().isGpsEnabled()) {
            checkPermissionForLocation();

        } else {
            PermissionHelper.showGPSSettingDialog(mActivity);
        }
    }

    private void checkPermissionForLocation() {
        XPermissionUtils.requestPermissionsResult(getActivity(), 200, new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                new XPermissionUtils.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        tempDeviceList.clear();
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

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(mStopScanRunnable, SCAN_PERIOD);
            mScanning = true;
            startScan();
            updateRefreshView(true);
        } else {
            if (mScanning) {
                stopScan();
            }
        }
    }

    private void startScan() {
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

    private void stopScan() {
        mScanning = false;
        scanner.stopScan(scanCallback);
        updateRefreshView(false);
    }

    private void updateRefreshView(boolean isRefresh) {
        if (isRefresh) {
            if (animator != null) {
                animator.start();
            }
            mTvDeviceCount.setText("(0)");
            mTvScanState.setText("刷新中...");
        } else {
            if (animator != null) {
                animator.end();
            }
            mTvScanState.setText("重新刷新");
        }
    }

    @OnClick({R.id.search_placeholder, R.id.tv_cancel, R.id.ll_scan_refresh})
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.search_placeholder) {
            scanLeDevice(false);
            searchPlaceholder.setVisibility(View.GONE);
            searchContainer.setVisibility(View.VISIBLE);
            refreshLayout.setVisibility(View.GONE);
            mEtKeyWords.setText("");
        } else if (id == R.id.tv_cancel) {// 当按了搜索之后关闭软键盘
            KeyBordUtils.hideSoftKeyboard(mEtKeyWords);
            searchPlaceholder.setVisibility(View.VISIBLE);
            searchContainer.setVisibility(View.GONE);
            refreshLayout.setVisibility(View.VISIBLE);
            bleDeviceAdapter.setNewInstance(tempDeviceList);
        } else if (id == R.id.ll_scan_refresh) {
            if (mTvScanState.getText().toString().contains("刷新中")) {
                scanLeDevice(false);
            } else {
                startDiscoveryDevice();
            }
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
//            Timber.d("在线程 Name= " + Thread.currentThread().getName() + ";Id= " + Thread.currentThread().getId() + " 中扫描到设备");
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (result.getDevice().getName() == null || !result.getDevice().getName().startsWith("MD") || !result.getDevice().getName().endsWith("L")) {
                        return;
                    }
                    for (DiscoveredBluetoothDevice mDevice : bleDeviceAdapter.getData()) {
                        if (result.getDevice().getAddress().equals(mDevice.getAddress())) {
                            return;
                        }
                    }

                    DiscoveredBluetoothDevice discoveredBluetoothDevice = new DiscoveredBluetoothDevice(result);
                    tempDeviceList.add(discoveredBluetoothDevice);
                    bleDeviceAdapter.addData(discoveredBluetoothDevice);
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

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence text, int start, int before, int count) {
        if (!TextUtils.isEmpty(text)) {
            searchProcess(text.toString().trim());
        } else {
            KeyBordUtils.popSoftKeyboard(mEtKeyWords, true);
            bleDeviceAdapter.setNewInstance(tempDeviceList);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            // 当按了搜索之后关闭软键盘
            KeyBordUtils.hideSoftKeyboard(mEtKeyWords);

            String text = mEtKeyWords.getText().toString();
            if (TextUtils.isEmpty(text)) {
                mEtKeyWords.clearFocus();
                return true;
            }
            searchProcess(text.trim());
            return true;
        }
        return false;
    }

    private void searchProcess(String queryText) {
        bleDeviceAdapter.setNewInstance(new ArrayList<>());
        for (DiscoveredBluetoothDevice device : tempDeviceList) {
            if (!TextUtils.isEmpty(device.getName()) && device.getName().contains(queryText)) {
                bleDeviceAdapter.addData(device);
            }
        }
    }
}
