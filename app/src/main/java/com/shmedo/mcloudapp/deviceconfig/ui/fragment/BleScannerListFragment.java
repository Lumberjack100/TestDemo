package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Build;
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
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ExplainReasonCallbackWithBeforeParam;
import com.permissionx.guolindev.callback.ForwardToSettingsCallback;
import com.permissionx.guolindev.callback.RequestCallback;
import com.permissionx.guolindev.request.ExplainScope;
import com.permissionx.guolindev.request.ForwardScope;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
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
import com.shmedo.mcloudapp.util.permission.PermissionHelper;

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
    View bluetoothOffView;

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

    public static BleScannerListFragment newInstance() {
        return new BleScannerListFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_ble_scanner_list;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHandler = new Handler(Looper.getMainLooper());
        initAdapter();
        initRefreshAnimation();
        setEditTextListener();

        scannerViewModel = getFragmentScopeViewModel(BleScannerViewModel.class);
        scannerViewModel.getBleScannerState().observe(getViewLifecycleOwner(), this::startScanDevices);
        scannerViewModel.getDevices().observe(getViewLifecycleOwner(), new Observer<List<DiscoveredBluetoothDevice>>() {
            @Override
            public void onChanged(List<DiscoveredBluetoothDevice> newDevices) {
                final DiffUtil.DiffResult result = DiffUtil.calculateDiff(
                        new DeviceDiffCallback(bleDeviceAdapter.getData(), newDevices), false);

                tempDeviceList.clear();
                if (newDevices != null) {
                    tempDeviceList.addAll(newDevices);
                }
                bleDeviceAdapter.setList(tempDeviceList);
                result.dispatchUpdatesTo(bleDeviceAdapter);

                mTvDeviceCount.setText(String.format(Locale.getDefault(), "(%d)", tempDeviceList.size()));
            }
        });
        //进入页面刷新蓝牙设备列表
        if (!scannerViewModel.isScanning()) {
            processStartScan();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        processStopScan();
    }

    @SuppressLint("MissingPermission")
    private void initAdapter() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        bleDeviceAdapter = new BleDeviceAdapter();
        mRecyclerView.setAdapter(bleDeviceAdapter);
        bleDeviceAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                processStopScan();
                DiscoveredBluetoothDevice bluetoothDevice = bleDeviceAdapter.getItem(position);
                String deviceName = bluetoothDevice.getDevice().getName();
                MCloudApp.setCurDeviceToken(deviceName.replace("MD-", ""));
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
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(mEtKeyWords);
            searchPlaceholder.setVisibility(View.VISIBLE);
            searchContainer.setVisibility(View.GONE);
            refreshLayout.setVisibility(View.VISIBLE);
            bleDeviceAdapter.setList(tempDeviceList);

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

        //位置服务开关未开启
        if (!BleScannerUtils.isLocationEnabled(mActivity)) {
            PermissionHelper.showGPSSettingDialog(mActivity);
            return;
        }
        //位置服务开关已经开启, 但缺少定位权限
        if (!BleScannerUtils.isLocationPermissionGranted(mActivity)) {
            checkPermissionForLocation();
            return;
        }
        // Bluetooth must be enabled.
        if (state.isBluetoothEnabled()) {
            bluetoothOffView.setVisibility(View.GONE);
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
            bluetoothOffView.setVisibility(View.VISIBLE);
            searchLayoutGroup.setVisibility(View.GONE);
            refreshLayout.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.GONE);
            //emptyView.setVisibility(View.GONE);
            if (bleDeviceAdapter.getItemCount() > 0) {
                clear();
            }
        }
    }

    private void startScanDevices(final BleScannerStateLiveData state) {
        // First, check the Location permission.
        // This is required since Marshmallow up until Android 11 in order to scan for Bluetooth LE devices.
        if (!BleScannerUtils.isLocationPermissionRequired() ||
                BleScannerUtils.isLocationPermissionGranted(mActivity)) {

            // On Android 12+ a new BLUETOOTH_SCAN and BLUETOOTH_CONNECT permissions need to be requested.
            //
            // Note: This has to be done before asking user to enable Bluetooth, as
            //       sending BluetoothAdapter.ACTION_REQUEST_ENABLE intent requires
            //       BLUETOOTH_CONNECT permission.
            if (!BleScannerUtils.isSorAbove() || BleScannerUtils.isBluetoothScanPermissionGranted(mActivity)) {
                // Bluetooth must be enabled.
                if (state.isBluetoothEnabled()) {
                    bluetoothOffView.setVisibility(View.GONE);
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
                    bluetoothOffView.setVisibility(View.VISIBLE);
                    searchLayoutGroup.setVisibility(View.GONE);
                    refreshLayout.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.GONE);
                    //emptyView.setVisibility(View.GONE);
                    if (bleDeviceAdapter.getItemCount() > 0) {
                        clear();
                    }
                }
            } else {
                checkPermissionForBluetoothScan();
            }
        } else {
            //位置服务开关未开启
            if (!BleScannerUtils.isLocationEnabled(mActivity)) {
                PermissionHelper.showGPSSettingDialog(mActivity);
                return;
            }
            //位置服务开关已经开启, 但缺少定位权限
            if (!BleScannerUtils.isLocationPermissionGranted(mActivity)) {
                checkPermissionForLocation();
                return;
            }
        }
    }

    private void checkPermissionForBluetoothScan() {
        List<String> requestList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestList.add(Manifest.permission.BLUETOOTH_SCAN);
            requestList.add(Manifest.permission.BLUETOOTH_CONNECT);
        }
        if (!requestList.isEmpty()) {
            PermissionX.init(this)
                    .permissions(requestList)
                    .explainReasonBeforeRequest()
                    .onExplainRequestReason(new ExplainReasonCallbackWithBeforeParam() {
                        @Override
                        public void onExplainReason(ExplainScope scope, List<String> deniedList, boolean beforeRequest) {
                            scope.showRequestReasonDialog(deniedList, "米易通需要以下权限继续", "允许", "拒绝");
                        }
                    })
                    .onForwardToSettings(new ForwardToSettingsCallback() {
                        @Override
                        public void onForwardToSettings(ForwardScope scope, List<String> deniedList) {
                            scope.showForwardToSettingsDialog(deniedList, "请前往设置页面授予权限", "去设置");
                        }
                    })
                    .request(new RequestCallback() {
                        @Override
                        public void onResult(boolean allGranted, List<String> grantedList, List<String> deniedList) {
                            if (allGranted) {
                                processStartScan();

                            } else {
                                ToastUtils.show("下列权限被拒绝：" + deniedList);
                            }
                        }
                    });
        }
    }

    private void checkPermissionForLocation() {
        PermissionX.init(this)
                .permissions(Manifest.permission.ACCESS_FINE_LOCATION)
//                .explainReasonBeforeRequest()
                .onExplainRequestReason(new ExplainReasonCallbackWithBeforeParam() {
                    @Override
                    public void onExplainReason(ExplainScope scope, List<String> deniedList, boolean beforeRequest) {
                        scope.showRequestReasonDialog(deniedList, "米易通需要以下权限继续", "允许", "拒绝");
                    }
                })
                .onForwardToSettings(new ForwardToSettingsCallback() {
                    @Override
                    public void onForwardToSettings(ForwardScope scope, List<String> deniedList) {
                        scope.showForwardToSettingsDialog(deniedList, "请前往设置页面授予权限", "去设置");
                    }
                })
                .request(new RequestCallback() {
                    @Override
                    public void onResult(boolean allGranted, List<String> grantedList, List<String> deniedList) {
                        if (allGranted) {
                            processStartScan();

                        } else {
                            ToastUtils.show("下列权限被拒绝：" + deniedList);
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
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence text, int start, int before, int count) {
        if (!TextUtils.isEmpty(text)) {
            searchProcess(text.toString().trim());
        } else {
            com.blankj.utilcode.util.KeyboardUtils.showSoftInput(mEtKeyWords);
            bleDeviceAdapter.setList(tempDeviceList);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            // 当按了搜索之后关闭软键盘
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(mEtKeyWords);

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
        bleDeviceAdapter.setList(new ArrayList<>());
        for (DiscoveredBluetoothDevice device : tempDeviceList) {
            if (!TextUtils.isEmpty(device.getName()) && device.getName().contains(queryText)) {
                bleDeviceAdapter.addData(device);
            }
        }
    }
}