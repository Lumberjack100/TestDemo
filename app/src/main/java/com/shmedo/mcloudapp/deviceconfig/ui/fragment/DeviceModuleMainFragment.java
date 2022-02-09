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
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.ExplainReasonCallbackWithBeforeParam;
import com.permissionx.guolindev.callback.ForwardToSettingsCallback;
import com.permissionx.guolindev.callback.RequestCallback;
import com.permissionx.guolindev.request.ExplainScope;
import com.permissionx.guolindev.request.ForwardScope;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseTranslucentFragment;
import com.shmedo.mcloudapp.deviceconfig.model.DiscoveredBluetoothDevice;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceConfigActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.QueryDeviceDataActivity;
import com.shmedo.mcloudapp.deviceconfig.util.BleScannerUtils;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.BleScannerStateLiveData;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.BleScannerViewModel;
import com.shmedo.mcloudapp.deviceconfig.adapter.PageAdapter;
import com.shmedo.mcloudapp.util.permission.PermissionHelper;
import com.shmedo.mcloudapp.util.permission.XPermissionUtils;

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
        mFragments.add(USBDeviceListFragment.newInstance());

        pagerAdapter = new PageAdapter((FragmentActivity) mActivity, mFragments);
        viewPager.setAdapter(pagerAdapter);
        tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                if (position == 0) {
                    View tabView = LayoutInflater.from(mActivity).inflate(R.layout.custom_tab_text, null);
                    TextView textView = tabView.findViewById(R.id.tabText);
                    textView.setText("4G");
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
                } else if (position == 3) {
                    View tabView = LayoutInflater.from(mActivity).inflate(R.layout.custom_tab_text, null);
                    TextView textView = tabView.findViewById(R.id.tabText);
                    textView.setText("USB");
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

        if (tab.getPosition() == 1) {
            ivScanDeviceCode.setVisibility(View.VISIBLE);
        } else {
            ivScanDeviceCode.setVisibility(View.INVISIBLE);
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        EventBus.getDefault().register(this);
        scannerViewModel = getFragmentScopeViewModel(BleScannerViewModel.class);
        scannerViewModel.getBleScannerState().observe(getViewLifecycleOwner(), this::startScan);
        scannerViewModel.getDevices().observe(getViewLifecycleOwner(), new Observer<List<DiscoveredBluetoothDevice>>() {
            @Override
            public void onChanged(List<DiscoveredBluetoothDevice> newDevices) {
                tempDeviceList.clear();
                if (newDevices != null) {
                    tempDeviceList.addAll(newDevices);
                    for (DiscoveredBluetoothDevice device : tempDeviceList) {
                        if (!TextUtils.isEmpty(MCloudApp.getCurDeviceToken()) && device.getName().contains(MCloudApp.getCurDeviceToken())) {
                            processStopScan();
                            DeviceConfigActivity.startActivity(getActivity(), AppContants.CommunicationWay.BLE_CONNECT, device);
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
        if (resultCode != Activity.RESULT_OK || data == null) {
            return;
        }
        if (requestCode == XPermissionUtils.REQUEST_CODE_SCAN) {
            HmsScan obj = data.getParcelableExtra(ScanUtil.RESULT);
            if (obj != null) {
                Timber.d("扫描结果为：%s", obj.originalValue);
                scanResult(obj.originalValue);
            }
        }
    }

    private void scanResult(String result) {
        if (TextUtils.isEmpty(result)) {
            showTipDialog("请扫描正确的设备二维码");
            return;
        }
        if (result.contains("MEDO")) {
            if (result.contains("=")) {
                result = result.substring(result.indexOf("=") + 1);
            }
            parseOldDeviceCode(result);
        } else if (result.startsWith("https://cloud.shmedo.cn/mcloudapp/device")) {
            parseNewDeviceCode(result);
        }else{
            showTipDialog("请扫描正确的设备二维码");
            return;
        }
    }

    /**
     * 处理老设备条码规则，例如：MEDO,189150L,DAS
     */
    private void parseOldDeviceCode(String barCode) {
        String[] localData = barCode.split(",");
        if (localData.length != 3) {
            showTipDialog("请扫描正确的设备二维码");
            return;
        }
        if (TextUtils.isEmpty(localData[1])) {
            showTipDialog("请扫描正确的设备二维码");
            return;
        }
        MCloudApp.setCurDeviceToken(localData[1].replace("MD-", ""));
        processStartScan();
    }

    /**
     * 处理新设备条码规则，例如：https://cloud.shmedo.cn/mcloudapp/device?sn=189150L
     */
    private void parseNewDeviceCode(String barCode) {
        String[] localData = barCode.split("=");
        if (localData.length != 2) {
            showTipDialog("请扫描正确的设备二维码");
            return;
        }
        if (TextUtils.isEmpty(localData[1])) {
            showTipDialog("请扫描正确的设备二维码");
            return;
        }
        MCloudApp.setCurDeviceToken(localData[1].replace("MD-", ""));
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
        PermissionX.init(this)
                .permissions(Manifest.permission.ACCESS_FINE_LOCATION)
//                .explainReasonBeforeRequest()
                .onExplainRequestReason(new ExplainReasonCallbackWithBeforeParam() {
                    @Override
                    public void onExplainReason(ExplainScope scope, List<String> deniedList, boolean beforeRequest) {
                        scope.showRequestReasonDialog(deniedList, "继续操作需要以下权限", "允许", "拒绝");
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
}
