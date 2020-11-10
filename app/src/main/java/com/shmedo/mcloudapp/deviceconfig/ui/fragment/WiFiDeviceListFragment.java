package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
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
import com.shmedo.core.MCloudApp;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.core.wifimanager.IWifi;
import com.shmedo.core.wifimanager.Wifi;
import com.shmedo.core.wifimanager.WifiHelper;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.deviceconfig.adapter.WiFiAdapter;
import com.shmedo.mcloudapp.util.KeyBordUtils;
import com.shmedo.mcloudapp.util.permission.XPermissionUtils;
import com.thanosfisherman.wifiutils.WifiUtils;
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionErrorCode;
import com.thanosfisherman.wifiutils.wifiConnect.ConnectionSuccessListener;
import com.thanosfisherman.wifiutils.wifiScan.ScanResultsListener;
import com.thanosfisherman.wifiutils.wifiState.WifiStateListener;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * WiFi设备列表页面
 */
public class WiFiDeviceListFragment extends BaseFragment implements TextWatcher, TextView.OnEditorActionListener {

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

    private List<IWifi> tempWiFiList = new ArrayList<>();

    private Animator animator;

    private WifiManager manager;


    @Override
    protected int getLayoutId() {
        return R.layout.fragment_wi_fi_device_list;
    }

    @Override
    public void onStart() {
        super.onStart();
        MCloudApp.getMainHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doConditionsCheckBeforeScan();
            }
        }, 500);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        manager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        initAdapter();
        initRefreshAnimation();
        setEditTextListener();
    }

    private void initAdapter() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        wiFiAdapter = new WiFiAdapter();
        mRecyclerView.setAdapter(wiFiAdapter);
        wiFiAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                IWifi iWifi = wiFiAdapter.getItem(position);
                processConnectWiFi(iWifi);
            }
        });
    }

    private void processConnectWiFi(IWifi iWifi) {
        WifiUtils.withContext(getContext().getApplicationContext())
                .connectWith(iWifi.name(), "medo33923627")
                .setTimeout(40000)
                .onConnectionResult(successListener)
                .start();
    }

    private ConnectionSuccessListener successListener = new ConnectionSuccessListener() {
        @Override
        public void success() {

            ToastUtils.show("SUCCESS!");
        }

        @Override
        public void failed(@NonNull ConnectionErrorCode errorCode) {
            ToastUtils.show("EPIC FAIL!" + errorCode.toString());
        }
    };

    private void initRefreshAnimation() {
        animator = AnimatorInflater.loadAnimator(mActivity, R.animator.rotation);
        animator.setTarget(mIvRefreshScan);
    }

    private void setEditTextListener() {
        mTvWiFiCount.setText("(0)");
        mEtKeyWords.addTextChangedListener(this);
        mEtKeyWords.setOnEditorActionListener(this);
    }

    /**
     * 执行扫描前需要满足的条件检查
     */
    private void doConditionsCheckBeforeScan() {
        XPermissionUtils.requestPermissionsResult(getActivity(), 200, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION},
                new XPermissionUtils.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        enableWiFi();
                    }

                    @Override
                    public void onPermissionDenied(List<String> deniedPermissions) {
                        boolean allNeverAskAgain = XPermissionUtils.isAllNeverAskAgain(getActivity(), deniedPermissions);
                        // 所有的权限都被勾上不再询问时，跳转到应用设置界面，引导用户手动打开权限
                        if (allNeverAskAgain) {
                            XPermissionUtils.showRefusePermissionDialog(getActivity(), GlobalUtil.getString(R.string.message_permission_wifi_location_rational));
                        } else {
                            ToastUtils.show(GlobalUtil.getString(R.string.message_permission_location_denied));
                        }
                    }
                });
    }

    private void enableWiFi() {
        WifiUtils.withContext(getContext().getApplicationContext()).enableWifi(new WifiStateListener() {
            @Override
            public void isSuccess(boolean isSuccess) {
                if (isSuccess) {
                    WifiUtils.withContext(getContext().getApplicationContext()).scanWifi(scanResultsListener).start();
                    updateRefreshView(true);
                } else {
                    ToastUtils.show("无法开启 WiFi");
                }
            }
        });
    }

    private final ScanResultsListener scanResultsListener = new ScanResultsListener() {
        @Override
        public void onScanResults(@NonNull List<ScanResult> scanResults) {
//            Timber.d("在线程 Name= " + Thread.currentThread().getName() + ";Id= " + Thread.currentThread().getId() + " 中扫描到设备");
            updateRefreshView(false);
            modifyWifi(scanResults);
//            for (ScanResult scanResult : scanResults) {
//                if (!TextUtils.isEmpty(scanResult.SSID)) {//&& scanResult.SSID.startsWith("medo")
//                    tempScanResults.add(scanResult);
//                    wiFiAdapter.addData(scanResult);
//
//                    Timber.e("ScanResult=" + scanResult.SSID + "->" + scanResult.BSSID);
//                }
//            }
            mTvWiFiCount.setText(String.format(Locale.getDefault(), "(%d)", wiFiAdapter.getItemCount()));
        }
    };


    private void modifyWifi(List<ScanResult> results) {
        synchronized (tempWiFiList) {
            List<IWifi> wifiList = new LinkedList<>();
            List<IWifi> mergeList = new ArrayList<>();
            List<WifiConfiguration> configurations = manager.getConfiguredNetworks();
            String connectedSSID = manager.getConnectionInfo().getSSID();
            int ipAddress = manager.getConnectionInfo().getIpAddress();
            for (ScanResult result : results) {
                IWifi mergeObj = Wifi.create(result, configurations, connectedSSID, ipAddress);
                if (mergeObj == null) continue;
                mergeList.add(mergeObj);
            }
            mergeList = WifiHelper.removeDuplicate(mergeList);
            for (IWifi merge : mergeList) {
                boolean isMerge = false;
                for (IWifi wifi : tempWiFiList) {
                    if (wifi.equals(merge)) {
                        wifiList.add(wifi.merge(merge));
                        isMerge = true;
                    }
                }
                if (!isMerge)
                    wifiList.add(merge);
            }
            tempWiFiList.clear();
            tempWiFiList.addAll(wifiList);
            wiFiAdapter.setList(wifiList);
        }
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

    @OnClick({R.id.search_placeholder, R.id.tv_cancel, R.id.ll_scan_refresh})
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
            wiFiAdapter.setList(tempWiFiList);
        } else if (id == R.id.ll_scan_refresh) {
            if (mTvScanState.getText().toString().contains("刷新中")) {

            } else {
                doConditionsCheckBeforeScan();
            }
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
        wiFiAdapter.getData().clear();
        for (IWifi iWifi : tempWiFiList) {
            if (!TextUtils.isEmpty(iWifi.name()) && iWifi.name().contains(queryText)) {
                wiFiAdapter.addData(iWifi);
            }
        }
    }
}