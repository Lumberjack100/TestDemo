package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.event.DeviceModuleSwitchTabEvent;
import com.shmedo.core.event.MessageEvent;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.ScanActivity;
import com.shmedo.mcloudapp.common.ui.fragment.BaseTranslucentFragment;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceTypeEnum;
import com.shmedo.mcloudapp.deviceconfig.model.DiscoveredBluetoothDevice;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceConfigActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.QueryDeviceDataActivity;
import com.shmedo.mcloudapp.deviceconfig.util.BleScannerUtils;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.BleScannerStateLiveData;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.BleScannerViewModel;
import com.shmedo.mcloudapp.projects.adapter.ProjectPageAdapter;
import com.shmedo.mcloudapp.util.permission.PermissionHelper;
import com.shmedo.mcloudapp.util.permission.XPermissionUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class DeviceModuleMainFragment extends BaseTranslucentFragment implements TabLayout.OnTabSelectedListener {
    @BindView(R.id.tabs)
    TabLayout tabLayout;

    @BindView(R.id.viewpager)
    ViewPager2 viewPager;

    @BindView(R.id.iv_scan_device_code)
    ImageView ivScanDeviceCode;

    private FragmentStateAdapter pagerAdapter;
    private TabLayoutMediator tabLayoutMediator;

    private static final int REQUEST_ACCESS_FINE_LOCATION = 1022;
    private BleScannerViewModel scannerViewModel;
    private boolean enableScan = false;
    private List<DiscoveredBluetoothDevice> tempDeviceList = new ArrayList<>();


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_device_module_main;
    }

    @Override
    protected void initView() {
        List<Fragment> mFragments = new ArrayList<>();
        mFragments.add(new NetDeviceListFragment());
        mFragments.add(new BleScannerListFragment());
        mFragments.add(new WiFiDeviceListFragment());
        pagerAdapter = new ProjectPageAdapter((FragmentActivity) mActivity, mFragments);
        viewPager.setAdapter(pagerAdapter);
        tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                if (position == 0) {
                    View tabView = LayoutInflater.from(mActivity).inflate(R.layout.custom_tab_text, null);
                    TextView textView = tabView.findViewById(R.id.tabText);
                    textView.setText("网络");
                    textView.setTextColor(ContextCompat.getColor(mActivity, R.color.title_text_color));
                    textView.setTextSize(18);
                    tab.setCustomView(textView);
                } else if (position == 1) {
                    View tabView = LayoutInflater.from(mActivity).inflate(R.layout.custom_tab_text, null);
                    TextView textView = tabView.findViewById(R.id.tabText);
                    textView.setText("蓝牙");
                    textView.setTextColor(ContextCompat.getColor(mActivity, R.color.sub_title_text_color));
                    textView.setTextSize(17);
                    tab.setCustomView(textView);
                } else if (position == 2) {
                    View tabView = LayoutInflater.from(mActivity).inflate(R.layout.custom_tab_text, null);
                    TextView textView = tabView.findViewById(R.id.tabText);
                    textView.setText("WIFI");
                    textView.setTextColor(ContextCompat.getColor(mActivity, R.color.sub_title_text_color));
                    textView.setTextSize(17);
                    tab.setCustomView(textView);
                }
            }
        });
        tabLayoutMediator.attach();
        tabLayout.addOnTabSelectedListener(this);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        TextView textView = (TextView) tab.getCustomView();
        textView.setTextColor(ContextCompat.getColor(mActivity, R.color.title_text_color));
        textView.setTextSize(18);

        if (tab.getPosition() == 0 || tab.getPosition() == 2) {
            ivScanDeviceCode.setVisibility(View.INVISIBLE);
        } else {
            ivScanDeviceCode.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        TextView textView = (TextView) tab.getCustomView();
        textView.setTextColor(ContextCompat.getColor(mActivity, R.color.sub_title_text_color));
        textView.setTextSize(17);
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        EventBus.getDefault().register(this);

        scannerViewModel = getFragmentScopeViewModel(BleScannerViewModel.class);
        scannerViewModel.getBleScannerState().observeInFragment(this, this::startScan);
        scannerViewModel.getDevices().observeInFragment(this, new Observer<List<DiscoveredBluetoothDevice>>() {
            @Override
            public void onChanged(List<DiscoveredBluetoothDevice> newDevices) {
                tempDeviceList.clear();
                if (newDevices != null) {
                    tempDeviceList.addAll(newDevices);
                    for (DiscoveredBluetoothDevice device : tempDeviceList) {
                        if (!TextUtils.isEmpty(MCloudApp.getCurDeviceToken()) && device.getName().contains(MCloudApp.getCurDeviceToken())) {
                            processStopScan();
                            int deviceType = AppContants.DeviceType.DAS;
                            if (device.getName().endsWith("T")) {
                                deviceType = AppContants.DeviceType.ADME;
                            } else if (device.getName().endsWith("L")) {
                                deviceType = AppContants.DeviceType.DAS;
                            }
                            DeviceConfigActivity.startActivity(getActivity(), AppContants.CommunicationWay.BLE_CONNECT, device, deviceType);

                            break;
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        processStopScan();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent messageEvent) {
        if (messageEvent instanceof DeviceModuleSwitchTabEvent) {
            DeviceModuleSwitchTabEvent switchTabEvent = (DeviceModuleSwitchTabEvent) messageEvent;
            viewPager.setCurrentItem(switchTabEvent.getTabPosition());
        }
    }

    @OnClick({R.id.iv_query_data, R.id.iv_scan_device_code})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_query_data:
                QueryDeviceDataActivity.startActivity(mActivity, "");
                break;

            case R.id.iv_scan_device_code:
                PermissionHelper.requestScanPermissions(DeviceModuleMainFragment.this);
                break;
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case XPermissionUtils.REQUEST_CODE_SCAN:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        String content = data.getStringExtra(ScanActivity.CODED_CONTENT);
                        Timber.d("扫描结果为：%s", content);
                        scanResult(content);
                    }
                }
                break;
        }
    }

    private void scanResult(String result) {
        if (TextUtils.isEmpty(result)) {
            showTipDialog("请扫码正确的设备二维码");
            return;
        }

        if (result.contains("=")) {
            String results = result.substring(result.indexOf("=") + 1);
            parseScanResult(results);
        } else {
            parseScanResult(result);
        }
    }

    /**
     * 处理扫描结果，例如：MEDO,189150L,DAS
     */
    private void parseScanResult(String deviceInfo) {
        if (!deviceInfo.startsWith("MEDO")) {
            showTipDialog("请扫码正确的设备二维码");
            return;
        }

        String[] localData = deviceInfo.split(",");
        if (localData.length != 3) {
            showTipDialog("请扫码正确的设备二维码");
            return;
        }

        if (TextUtils.isEmpty(localData[0]) || TextUtils.isEmpty(localData[1]) || TextUtils.isEmpty(localData[2])) {
            showTipDialog("二维码信息不能为空");
            return;
        }

        if (localData[1].length() != 7) {
            showTipDialog("设备标识有误,请扫码正确的设备二维码");
            return;
        }

        if (!DeviceTypeEnum.value(localData[2])) {
            showTipDialog("此设备类型暂时不支持");
            return;
        }

        MCloudApp.setCurDeviceToken(localData[1]);

        processStartScan();
    }

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private final Runnable mStopScanRunnable = new Runnable() {
        @Override
        public void run() {
            processStopScan();
            ToastUtils.show("未搜索到 " + MCloudApp.getCurDeviceToken() + " 的蓝牙广播");
        }
    };

    private void processStartScan() {
        enableScan = true;
        clear();
    }

    private void processStopScan() {
        dismissProgressDialog();
        enableScan = false;
        scannerViewModel.stopScan();
        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * Start scanning for Bluetooth devices or displays a message based on the scanner state.
     * <br>
     * BleScannerStateLiveData 实例每次更新值时，回调此方法
     */
    private void startScan(final BleScannerStateLiveData state) {
        //位置服务开关未开启
//        if (BleScannerUtils.isLocationRequired(mActivity) && !BleScannerUtils.isLocationEnabled(mActivity)) {
        if (!BleScannerUtils.isLocationEnabled(mActivity)) {
            PermissionHelper.showGPSSettingDialog(mActivity);
        } else {
            //缺少定位权限
            if (!BleScannerUtils.isLocationPermissionsGranted(mActivity)) {
                checkPermissionForLocation();
            } else {
                // Bluetooth must be enabled.
                if (state.isBluetoothEnabled()) {
                    if (enableScan && !scannerViewModel.isScanning()) {
                        // We are now OK to start scanning.
                        scannerViewModel.startScan();
                        showProgressDialog("搜索 " + MCloudApp.getCurDeviceToken() + " 的蓝牙广播...");
                        mHandler.postDelayed(mStopScanRunnable, 10000);
                    }
                } else {
                    if (tempDeviceList.size() > 0) {
                        clear();
                    }
                }
            }
        }
    }

    private void checkPermissionForLocation() {
        XPermissionUtils.requestPermissionsResult(getActivity(), REQUEST_ACCESS_FINE_LOCATION, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION},
                new XPermissionUtils.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {

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
}
