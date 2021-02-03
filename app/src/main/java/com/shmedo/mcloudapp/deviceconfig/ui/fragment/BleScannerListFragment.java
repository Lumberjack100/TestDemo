package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.bluetooth.BluetoothAdapter;
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
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DiffUtil;
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
import com.shmedo.mcloudapp.deviceconfig.model.DeviceDiffCallback;
import com.shmedo.mcloudapp.deviceconfig.model.DiscoveredBluetoothDevice;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceConfigActivity;
import com.shmedo.mcloudapp.deviceconfig.util.BleScannerUtils;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.BleScannerStateLiveData;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.BleScannerViewModel;
import com.shmedo.mcloudapp.util.KeyBordUtils;
import com.shmedo.mcloudapp.util.permission.PermissionHelper;
import com.shmedo.mcloudapp.util.permission.XPermissionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 低功耗蓝牙设备扫描列表页面
 */
public class BleScannerListFragment extends BaseFragment implements TextWatcher, TextView.OnEditorActionListener {
    private static final int REQUEST_ACCESS_FINE_LOCATION = 1022;

    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    @BindView(R.id.search_layout_group)
    View searchLayoutGroup;

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

    @BindView(R.id.no_devices)
    View emptyView;

    @BindView(R.id.bluetooth_off)
    View noBluetoothView;

    private BleDeviceAdapter bleDeviceAdapter;

    private List<DiscoveredBluetoothDevice> tempDeviceList = new ArrayList<>();

    private Animator animator;

    private BleScannerViewModel scannerViewModel;

    private boolean enableScan = false;

    private Handler mHandler;

    private final Runnable mStopScanRunnable = new Runnable() {
        @Override
        public void run() {
            processStopScan();
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_ble_scanner_list;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mHandler = new Handler(Looper.getMainLooper());
        initAdapter();
        initRefreshAnimation();
        setEditTextListener();

        scannerViewModel = getFragmentScopeViewModel(BleScannerViewModel.class);
        scannerViewModel.getBleScannerState().observeInFragment(this, this::startScan);
        scannerViewModel.getDevices().observeInFragment(this, new Observer<List<DiscoveredBluetoothDevice>>() {
            @Override
            public void onChanged(List<DiscoveredBluetoothDevice> newDevices) {
                final DiffUtil.DiffResult result = DiffUtil.calculateDiff(
                        new DeviceDiffCallback(bleDeviceAdapter.getData(), newDevices), false);

                tempDeviceList.clear();
                if (newDevices != null) {
                    tempDeviceList.addAll(newDevices);
                }
                bleDeviceAdapter.setNewInstance(tempDeviceList);
                result.dispatchUpdatesTo(bleDeviceAdapter);

                mTvDeviceCount.setText(String.format(Locale.getDefault(), "(%d)", tempDeviceList.size()));
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!scannerViewModel.isScanning()) {
            processStartScan();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        processStopScan();
    }

    private void initAdapter() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        bleDeviceAdapter = new BleDeviceAdapter();
        mRecyclerView.setAdapter(bleDeviceAdapter);
        bleDeviceAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                processStopScan();
                DiscoveredBluetoothDevice bluetoothDevice = bleDeviceAdapter.getItem(position);
                String deviceName = bluetoothDevice.getName();
                if (!(deviceName.endsWith("L") || deviceName.endsWith("T") || deviceName.endsWith("V"))) {
                    ToastUtils.show("不支持此设备类型");
                    return;
                }
                MCloudApp.setCurDeviceToken(deviceName.replace("MD-", ""));
                int deviceType = AppContants.DeviceType.DAS;
                if (deviceName.endsWith("L")) {
                    deviceType = AppContants.DeviceType.DAS;
                } else if (deviceName.endsWith("T")) {
                    if (deviceName.startsWith("M20"))
                        deviceType = AppContants.DeviceType.M20;
                    else
                        deviceType = AppContants.DeviceType.ADME;
                } else if (deviceName.endsWith("V")) {
                    deviceType = AppContants.DeviceType.M20;
                }
                DeviceConfigActivity.startActivity(getActivity(), AppContants.CommunicationWay.BLE_CONNECT, bluetoothDevice, deviceType);
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

    private void updateRefreshView(boolean isScanning) {
        if (isScanning) {
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

    @OnClick({R.id.search_placeholder, R.id.tv_cancel, R.id.ll_scan_refresh, R.id.action_enable_bluetooth})
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.search_placeholder) {
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
            if (scannerViewModel.isScanning()) {
                processStopScan();
            } else {
                processStartScan();
            }
        } else if (id == R.id.action_enable_bluetooth) {
            final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableIntent);
        }
    }

    private void processStartScan() {
        enableScan = true;
        clear();
    }

    private void processStopScan() {
        enableScan = false;
        scannerViewModel.stopScan();
        mHandler.removeCallbacksAndMessages(null);
        updateRefreshView(false);
    }

    /**
     * Start scanning for Bluetooth devices or displays a message based on the scanner state.
     * <br>
     * BleScannerStateLiveData 实例每次更新值时，回调此方法
     */
    private void startScan(final BleScannerStateLiveData state) {
        //if (BleScannerUtils.isLocationRequired(mActivity) && !BleScannerUtils.isLocationEnabled(mActivity)) {
        //位置服务开关未开启
        if (!BleScannerUtils.isLocationEnabled(mActivity)) {
            PermissionHelper.showGPSSettingDialog(mActivity);
            return;
        }

        //位置服务开关已经开启, 但缺少定位权限
        if (!BleScannerUtils.isLocationPermissionsGranted(mActivity)) {
            checkPermissionForLocation();
            return;
        }

        // Bluetooth must be enabled.
        if (state.isBluetoothEnabled()) {
            noBluetoothView.setVisibility(View.GONE);
            searchLayoutGroup.setVisibility(View.VISIBLE);
            refreshLayout.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.VISIBLE);
            if (enableScan && !scannerViewModel.isScanning()) {
                // We are now OK to start scanning.
                scannerViewModel.startScan();
                updateRefreshView(true);
                mHandler.postDelayed(mStopScanRunnable, SCAN_PERIOD);
            }
        } else {
            noBluetoothView.setVisibility(View.VISIBLE);
            searchLayoutGroup.setVisibility(View.GONE);
            refreshLayout.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.GONE);
            //emptyView.setVisibility(View.GONE);
            if (bleDeviceAdapter.getItemCount() > 0) {
                clear();
            }
        }
    }

    private void checkPermissionForLocation() {
        XPermissionUtils.requestPermissionsResult(getActivity(), REQUEST_ACCESS_FINE_LOCATION, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION},
                new XPermissionUtils.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
//                        processStartScan();
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

    /**
     * Clears the list of devices, which will notify the observer.
     */
    private void clear() {
        scannerViewModel.getDevices().clear();
        scannerViewModel.getBleScannerState().clearRecords();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ACCESS_FINE_LOCATION) {
            scannerViewModel.refresh();
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