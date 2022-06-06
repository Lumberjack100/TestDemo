package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
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
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.AttachPopupView;
import com.lxj.xpopup.enums.PopupAnimation;
import com.lxj.xpopup.interfaces.OnSelectListener;
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
import com.shmedo.mcloudapp.deviceconfig.adapter.PageAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.DiscoveredBluetoothDevice;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceConfigActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceListActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.QueryDeviceDataActivity;
import com.shmedo.mcloudapp.deviceconfig.util.BleScannerUtils;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.BleScannerStateLiveData;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.BleScannerViewModel;
import com.shmedo.mcloudapp.util.permission.PermissionHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class DeviceModuleMainFragment extends BaseFragment implements TabLayout.OnTabSelectedListener {
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

    private final String[] addMore = new String[]{"扫一扫", "WIFI 设备", "USB 设备", "查询数据"};


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_device_module_main;
    }

    @Override
    protected void initView() {
        List<Fragment> mFragments = new ArrayList<>();
        mFragments.add(new NetDeviceListFragment());
        mFragments.add(new BleScannerListFragment());

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
        scannerViewModel = getFragmentScopeViewModel(BleScannerViewModel.class);
        scannerViewModel.getBleScannerState().observe(getViewLifecycleOwner(), this::startScanDevices);
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

    @OnClick({R.id.iv_scan_device_code})
    public void onClick(View v) {
        if (v.getId() == R.id.iv_scan_device_code) {
            AttachPopupView attachPopupView = new XPopup.Builder(getContext())
                    .hasShadowBg(false)
                    .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                    .isDarkTheme(false)
                    .popupAnimation(PopupAnimation.TranslateFromRight) //NoAnimation表示禁用动画
                    .atView(v)  // 依附于所点击的View，内部会自动判断在上方或者下方显示
                    .asAttachList(addMore, null, new OnSelectListener() {
                        @Override
                        public void onSelect(int position, String text) {
                            if (text.equals(addMore[0])) {
                                PermissionHelper.requestScanPermissions(DeviceModuleMainFragment.this);
                            } else if (text.equals(addMore[1])) {
                                DeviceListActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT);
                            } else if (text.equals(addMore[2])) {
                                DeviceListActivity.startActivity(mActivity, AppContants.CommunicationWay.USB_SERIAL);
                            } else if (text.equals(addMore[3])) {
                                QueryDeviceDataActivity.startActivity(mActivity, "");
                            }
                        }
                    }, 0, 0);
            attachPopupView.show();
        }
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || data == null) {
            return;
        }
        if (requestCode == PermissionHelper.REQUEST_CODE_SCAN) {
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
        } else {
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
