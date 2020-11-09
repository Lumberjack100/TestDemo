package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.net.wifi.ScanResult;
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
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.deviceconfig.adapter.WiFiAdapter;
import com.shmedo.mcloudapp.util.KeyBordUtils;
import com.shmedo.mcloudapp.util.permission.XPermissionUtils;
import com.thanosfisherman.wifiutils.WifiUtils;
import com.thanosfisherman.wifiutils.wifiScan.ScanResultsListener;
import com.thanosfisherman.wifiutils.wifiState.WifiStateListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

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

    private List<android.net.wifi.ScanResult> tempScanResults = new ArrayList<>();

    private Animator animator;

    private boolean mScanning;


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
                android.net.wifi.ScanResult scanResult = wiFiAdapter.getItem(position);
            }
        });
    }

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
                            XPermissionUtils.showRefusePermissionDialog(getActivity(), GlobalUtil.getString(R.string.message_permission_bluetooth_location_rational));
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
                    mScanning = true;
                    updateRefreshView(true);
                } else {
                    ToastUtils.show("无法开启 WiFi");
                }
            }
        });
    }

    private final ScanResultsListener scanResultsListener = new ScanResultsListener() {
        @Override
        public void onScanResults(@NonNull List<android.net.wifi.ScanResult> scanResults) {
            // Timber.d("在线程 Name= " + Thread.currentThread().getName() + ";Id= " + Thread.currentThread().getId() + " 中扫描到设备");
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    updateRefreshView(false);
                    tempScanResults.clear();
                    wiFiAdapter.setNewInstance(new ArrayList<>());

                    for (ScanResult scanResult : scanResults) {
                        if (!TextUtils.isEmpty(scanResult.SSID)) {//&& scanResult.SSID.startsWith("medo")
                            tempScanResults.add(scanResult);
                            wiFiAdapter.addData(scanResult);

                            Timber.e("ScanResult=" + scanResult.SSID + "->" + scanResult.BSSID);
                        }
                    }
                    mTvWiFiCount.setText(String.format(Locale.getDefault(), "(%d)", wiFiAdapter.getItemCount()));
                }
            });
        }
    };

    private void updateRefreshView(boolean isRefresh) {
        if (isRefresh) {
            if (animator != null) {
                animator.start();
            }
//            mTvWiFiCount.setText("(0)");
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
            wiFiAdapter.setNewInstance(tempScanResults);
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
            wiFiAdapter.setNewInstance(tempScanResults);
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
        wiFiAdapter.setNewInstance(new ArrayList<>());
        for (ScanResult scanResult : tempScanResults) {
            if (!TextUtils.isEmpty(scanResult.SSID) && scanResult.SSID.contains(queryText)) {
                wiFiAdapter.addData(scanResult);
            }
        }
    }
}