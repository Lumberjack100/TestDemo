package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.StringUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hacknife.wifimanager.HackWifiManager;
import com.hacknife.wifimanager.IWifi;
import com.hacknife.wifimanager.IWifiManager;
import com.hacknife.wifimanager.OnWifiChangeListener;
import com.hacknife.wifimanager.OnWifiConnectListener;
import com.hacknife.wifimanager.OnWifiStateChangeListener;
import com.hacknife.wifimanager.State;
import com.hjq.toast.ToastUtils;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.kongzue.dialogx.interfaces.OnBackPressedListener;
import com.shmedo.configlibrary.iot.enums.ProductType;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.deviceconfig.adapter.WiFiAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceBaseInfo;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceConfigActivity;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.DeviceApiKeyViewModel;
import com.shmedo.mcloudapp.util.permission.XPermissionUtils;
import com.thanosfisherman.wifiutils.WifiUtils;
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionErrorCode;
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionSuccessListener;
import com.thanosfisherman.wifiutils.wifiScan.ScanResultsListener;
import com.thanosfisherman.wifiutils.wifiState.WifiStateListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * WiFi设备列表页面
 */
public class WiFiDeviceListFragment extends BaseFragment implements TextWatcher, TextView.OnEditorActionListener {
    private static final int REQUEST_CODE_INTERNET_CONNECTIVITY = 0x1000;

    @BindView(R.id.normalView)
    View normalView;

    @BindView(R.id.wifiDisabledView)
    View wifiDisabledView;

    @BindView(R.id.search_placeholder)
    View searchPlaceholder;

    @BindView(R.id.search_container)
    View searchContainer;

    @BindView(R.id.ll_refresh)
    View refreshLayout;

    @BindView(R.id.et_keywords)
    ClearEditText mEtKeyWords;

    @BindView(R.id.tv_wifi_count)
    TextView mTvWiFiCount;

    @BindView(R.id.iv_refresh_scan)
    ImageView mIvRefreshScan;

    @BindView(R.id.tv_scan_state)
    TextView mTvScanState;

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    private WiFiAdapter wiFiAdapter;

    private ObjectAnimator heightAnimator;

    private WifiManager manager;

    private IWifiManager hackWiFiManager;

    private final List<IWifi> tempWiFiList = new ArrayList<>();
    private IWifi curWiFi;

    private DeviceApiKeyViewModel deviceApiKeyViewModel;


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_wi_fi_device_list;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        deviceApiKeyViewModel = getApplicationScopeViewModel(DeviceApiKeyViewModel.class);
        deviceApiKeyViewModel.deviceApiKeyRequest.getDeviceApiKeyLiveData().observe(getViewLifecycleOwner(), new Observer<DeviceBaseInfo>() {
            @Override
            public void onChanged(DeviceBaseInfo deviceBaseInfo) {
                //获取到物联网指令的设备 ApiKey 后，处理 WiFi 连接
                if (WiFiDeviceListFragment.this.isVisible() && curWiFi != null) {
                    processWiFiUseSecondLibrary(curWiFi);
                }
            }
        });
        manager = (WifiManager) mActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        initAdapter();
        initRefreshAnimation();
        setEditTextListener();
        doConditionsCheckBeforeScan();
    }

    private void processWiFiUseSecondLibrary(IWifi curWiFi) {
        if (curWiFi.isConnected()) {//已连接
            hideProgressBar();
            if (curWiFi.name().contains("VMS")) {
                DeviceConfigActivity.startActivity(getActivity(), AppContants.CommunicationWay.TCP_CONNECT, ProductType.VMS);

            } else if (curWiFi.name().contains("E40")) {
                DeviceConfigActivity.startActivity(getActivity(), AppContants.CommunicationWay.TCP_CONNECT, ProductType.E40);
            }
        } else if (curWiFi.isSaved() || !curWiFi.isEncrypt()) {//已保存/未加密
            WifiUtils.withContext(getContext().getApplicationContext())
                    .connectWith(curWiFi.name(), "")
                    .setTimeout(30000)
                    .onConnectionResult(successListener)
                    .start();
        } else {//加密
            WifiUtils.withContext(getContext().getApplicationContext())
                    .connectWith(curWiFi.name(), "medo33923627")
                    .setTimeout(30000)
                    .onConnectionResult(successListener)
                    .start();
        }
    }

    private final ConnectionSuccessListener successListener = new ConnectionSuccessListener() {
        @Override
        public void success() {
            hideProgressBar();
            if (curWiFi.name().contains("VMS")) {
                DeviceConfigActivity.startActivity(getActivity(), AppContants.CommunicationWay.TCP_CONNECT, ProductType.VMS);

            } else if (curWiFi.name().contains("E40")) {
                DeviceConfigActivity.startActivity(getActivity(), AppContants.CommunicationWay.TCP_CONNECT, ProductType.E40);
            }
        }

        @Override
        public void failed(@NonNull ConnectionErrorCode errorCode) {
            hideProgressBar();
            ToastUtils.show("连接失败!" + errorCode.toString());
        }
    };

    private void showProgressBar() {
        WaitDialog.show("处理中...")
                .setOnBackPressedListener(new OnBackPressedListener() {//返回按键监听
                    @Override
                    public boolean onBackPressed() {
                        WaitDialog.dismiss();
                        return false;
                    }
                });
    }

    private void hideProgressBar() {
        WaitDialog.dismiss();
    }

    private void initAdapter() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        wiFiAdapter = new WiFiAdapter();
        mRecyclerView.setAdapter(wiFiAdapter);
        wiFiAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                curWiFi = wiFiAdapter.getItem(position);
                String[] strs = curWiFi.name().split("-");
                showProgressBar();
                //先查询下发物联网指令时用到的设备的 ApiKey
                deviceApiKeyViewModel.deviceApiKeyRequest.queryDeviceApiKeyBySn(strs[strs.length - 1]);
            }
        });
    }

    private void initRefreshAnimation() {
        heightAnimator = ObjectAnimator
                .ofFloat(mIvRefreshScan, "rotation", 0f, 360f)
                .setDuration(1000);
        heightAnimator.setRepeatCount(-1);
    }

    private void setEditTextListener() {
        mTvWiFiCount.setText("(0)");
        mEtKeyWords.addTextChangedListener(this);
        mEtKeyWords.setOnEditorActionListener(this);
    }

    /**
     * 执行WiFi扫描前检查需要满足的权限
     */
    private void doConditionsCheckBeforeScan() {
        XPermissionUtils.requestPermissionsResult(getActivity(), 200, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION},
                new XPermissionUtils.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        refreshWifi();
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        boolean allNeverAskAgain = XPermissionUtils.isAllNeverAskAgain(getActivity(), deniedPermissions);
                        // 所有的权限都被勾上不再询问时，跳转到应用设置界面，引导用户手动打开权限
                        if (allNeverAskAgain) {
                            XPermissionUtils.showRefusePermissionDialog(getActivity(), StringUtils.getString(R.string.message_permission_wifi_location_rational));
                        } else {
                            ToastUtils.show(StringUtils.getString(R.string.message_permission_location_denied));
                        }
                    }
                });
    }

    /**
     * 扫描刷新 WiFi 列表
     */
    private void refreshWifi() {
        updateRefreshView(true);
        WifiUtils.withContext(getContext().getApplicationContext()).scanWifi(scanResultsListener).start();
        if (hackWiFiManager == null) {
            initThirdWiFiManager();
        }
    }

    private final ScanResultsListener scanResultsListener = new ScanResultsListener() {
        @Override
        public void onScanResults(@NonNull List<ScanResult> scanResults) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateRefreshView(false);
                }
            });
        }
    };

    /**
     * 刷新动画处理
     *
     * @param isRefresh
     */
    private void updateRefreshView(boolean isRefresh) {
        if (heightAnimator == null || mTvScanState == null)
            return;

        if (isRefresh) {
            heightAnimator.start();
            mTvScanState.setText("刷新中...");
        } else {
            heightAnimator.end();
            mTvScanState.setText("重新刷新");
        }
    }

    private void initThirdWiFiManager() {
        hackWiFiManager = HackWifiManager.create(mActivity);
        hackWiFiManager.setOnWifiChangeListener(new OnWifiChangeListener() {
            @Override
            public void onWifiChanged(List<IWifi> wifiList) {
                if (mTvWiFiCount == null) {
                    return;
                }
                tempWiFiList.clear();
                for (IWifi iWifi : wifiList) {
                    if (iWifi.name() == null || (!iWifi.name().toUpperCase().startsWith("VMS"))) {
                        continue;
                    }
                    tempWiFiList.add(iWifi);
                }
                wiFiAdapter.setList(tempWiFiList);
                mTvWiFiCount.setText(String.format(Locale.getDefault(), "(%d)", wiFiAdapter.getItemCount()));
            }
        });
        hackWiFiManager.setOnWifiStateChangeListener(new OnWifiStateChangeListener() {
            @Override
            public void onStateChanged(State state) {
                if (normalView == null) {
                    return;
                }
                if (state == State.DISABLED) {
                    ToastUtils.show("WiFi 未开启");
                    normalView.setVisibility(View.GONE);
                    wifiDisabledView.setVisibility(View.VISIBLE);
                    Button openWiFi = wifiDisabledView.findViewById(R.id.btn_open_wifi);
                    openWiFi.setOnClickListener(openWiFiListener);

                } else if (state == State.ENABLED) {
                    normalView.setVisibility(View.VISIBLE);
                    wifiDisabledView.setVisibility(View.GONE);
                }
            }
        });

        hackWiFiManager.setOnWifiConnectListener(new OnWifiConnectListener() {
            @Override
            public void onConnectChanged(boolean status) {

            }
        });
    }

    private View.OnClickListener openWiFiListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            prrocessEnableWiFi();
        }
    };

    /**
     * 检测 WiFI 是否开启<br>
     * 注意：Android Q 以上无法通过代码 mWifiManager.setWifiEnabled(true) 打开 WiFi
     */
    private void prrocessEnableWiFi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
            startActivityForResult(panelIntent, REQUEST_CODE_INTERNET_CONNECTIVITY);
        } else {
            WifiUtils.withContext(getContext().getApplicationContext()).enableWifi(new WifiStateListener() {
                @Override
                public void isSuccess(boolean isSuccess) {
                    if (isSuccess) {
                        updateRefreshView(true);
                        WifiUtils.withContext(getContext().getApplicationContext()).scanWifi(scanResultsListener).start();
                    } else {
                        ToastUtils.show("无法开启 WiFi");
                    }
                }
            });
        }
    }

    @OnClick({R.id.search_placeholder, R.id.tv_cancel, R.id.ll_scan_refresh})
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
            wiFiAdapter.setList(tempWiFiList);
        } else if (id == R.id.ll_scan_refresh) {
            if (mTvScanState.getText().toString().contains("刷新中")) {
                updateRefreshView(false);

            } else {
                doConditionsCheckBeforeScan();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case REQUEST_CODE_INTERNET_CONNECTIVITY:
                if (manager.isWifiEnabled()) {
                    refreshWifi();
                }
                break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
//        hideProgressBar();
    }

    @Override
    public void onDestroy() {
        MCloudApp.getMainHandler().removeCallbacksAndMessages(null);
        deviceApiKeyViewModel.deviceApiKeyRequest.clearDeviceApiKey();
        super.onDestroy();
    }

    /*** 以下是WiFi搜索逻辑代码 ***/
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence text, int start, int before, int count) {
        if (!TextUtils.isEmpty(text)) {
            searchProcess(text.toString().trim());
        } else {
            com.blankj.utilcode.util.KeyboardUtils.showSoftInput(mEtKeyWords);
            wiFiAdapter.setList(tempWiFiList);
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
        wiFiAdapter.getData().clear();
        for (IWifi iWifi : tempWiFiList) {
            if (!TextUtils.isEmpty(iWifi.name()) && iWifi.name().contains(queryText)) {
                wiFiAdapter.addData(iWifi);
            }
        }
    }
}