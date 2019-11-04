package com.shmedo.mcloudapp.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.hjq.toast.ToastUtils;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.adapter.WifiListAdapter;
import com.shmedo.mcloudapp.entity.WifiBean;
import com.shmedo.mcloudapp.entity.event.WifiEvent;
import com.shmedo.mcloudapp.util.AppContants;
import com.shmedo.mcloudapp.util.StringUtil;
import com.shmedo.mcloudapp.util.WifiSupport;
import com.shmedo.mcloudapp.views.WifiLinkDialog;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity
 * 文件名:   WifiConnectionActivity
 * 创建者:   dpc
 * 创建时间:  2019/1/24 11:20
 * 描述：    连接wifi页面
 */
public class WifiConnectionActivity extends AppCompatActivity {
    private static final String TAG = "adu";
    @BindView(R.id.LL_close) LinearLayout mLLClose;

    private ProgressBar mPbWifiLoading;
    private RecyclerView mRecycleListWifi;
    List<WifiBean> realWifiList = new ArrayList<>();

    private WifiListAdapter adapter;

    private WifiBroadcastReceiver wifiReceiver;
    private int connectType = 0;//1：连接成功？ 2 正在连接（如果wifi热点列表发生变需要该字段）

    private WifiBean wifiBean;
    private WifiEvent event = new WifiEvent();

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_wifi_list);
        ButterKnife.bind(this);
        //setResult(Activity.RESULT_CANCELED);
        mPbWifiLoading = (ProgressBar) this.findViewById(R.id.pb_wifi_loading);
        initView();
    }


    private void initView() {
        if (WifiSupport.isOpenWifi(WifiConnectionActivity.this)) {
            initRecycler();
        } else {
            WifiSupport.openWifi(WifiConnectionActivity.this);
            initRecycler();
        }
    }


    private void initRecycler() {
        mRecycleListWifi = (RecyclerView) this.findViewById(R.id.recycle_list_wifi);
        adapter = new WifiListAdapter(this, realWifiList);
        mRecycleListWifi.setLayoutManager(new LinearLayoutManager(this));
        mRecycleListWifi.setAdapter(adapter);

        if (WifiSupport.isOpenWifi(WifiConnectionActivity.this)) {
            sortScaResult();
        } else {
            //ToastUtils.show("WIFI处于关闭状态或权限获取失败");
            WifiSupport.openWifi(WifiConnectionActivity.this);
        }
        adapter.setOnItemClickListener(new WifiListAdapter.onItemClickListener() {
            @Override
            public void onItemClick(View view, int postion, Object o) {
                wifiBean = realWifiList.get(postion);
                if (wifiBean.getState().equals(AppContants.WIFI_STATE_UNCONNECT) ||
                    wifiBean.getState().equals(AppContants.WIFI_STATE_CONNECT)) {
                    String capabilities = realWifiList.get(postion).getCapabilities();
                    if (WifiSupport.getWifiCipher(capabilities) ==
                        WifiSupport.WifiCipherType.WIFICIPHER_NOPASS) {//无需密码
                        WifiConfiguration tempConfig = WifiSupport.isExsits(wifiBean.getWifiName(),
                            WifiConnectionActivity.this);
                        if (tempConfig == null) {
                            WifiConfiguration exsits = WifiSupport.createWifiConfig(
                                wifiBean.getWifiName(), null,
                                WifiSupport.WifiCipherType.WIFICIPHER_NOPASS);
                            WifiSupport.addNetWork(exsits, WifiConnectionActivity.this);
                        } else {
                            WifiSupport.addNetWork(tempConfig, WifiConnectionActivity.this);
                        }
                    } else {   //需要密码，弹出输入密码dialog
                        noConfigurationWifi(postion);
                    }
                }
            }
        });
    }


    /**
     * 获取wifi列表然后将bean转成自己定义的WifiBean
     */
    public void sortScaResult() {
        List<ScanResult> scanResults = WifiSupport.noSameName(WifiSupport.getWifiScanResult(this));
        realWifiList.clear();
        if (!StringUtil.isNullOrEmpty(scanResults)) {
            for (int i = 0; i < scanResults.size(); i++) {
                WifiBean wifiBean = new WifiBean();
                wifiBean.setWifiName(scanResults.get(i).SSID);
                wifiBean.setState(AppContants.WIFI_STATE_UNCONNECT);   //只要获取都假设设置成未连接，真正的状态都通过广播来确定
                wifiBean.setCapabilities(scanResults.get(i).capabilities);
                wifiBean.setLevel(WifiSupport.getLevel(scanResults.get(i).level) + "");
                realWifiList.add(wifiBean);
                //排序
                Collections.sort(realWifiList);
                adapter.notifyDataSetChanged();
            }
        }
    }


    //之前没配置过该网络， 弹出输入密码界面
    private void noConfigurationWifi(int position) {
        WifiLinkDialog linkDialog = new WifiLinkDialog(this,
            R.style.dialog_download, realWifiList.get(position).getWifiName(),
            realWifiList.get(position).getCapabilities());
        if (!linkDialog.isShowing()) {
            linkDialog.show();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        //注册广播
        wifiReceiver = new WifiBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);//监听wifi是开关变化的状态
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);//监听wifi连接状态广播,是否连接了一个有效路由
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);//监听wifi列表变化（开启一个热点或者关闭一个热点）
        this.registerReceiver(wifiReceiver, filter);
    }


    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(wifiReceiver);
    }


    @OnClick(R.id.LL_close)
    public void onViewClicked() {
        event.setMessage("wifi");
        event.setWifiBean(wifiBean);
        EventBus.getDefault().post(event);
        finish();
    }


    //监听wifi状态
    public class WifiBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                switch (state) {
                    /**
                     * WIFI_STATE_DISABLED    WLAN已经关闭
                     * WIFI_STATE_DISABLING   WLAN正在关闭
                     * WIFI_STATE_ENABLED     WLAN已经打开
                     * WIFI_STATE_ENABLING    WLAN正在打开
                     * WIFI_STATE_UNKNOWN     未知
                     */
                    case WifiManager.WIFI_STATE_DISABLED: {
                        Log.d(TAG, "已经关闭");
                        ToastUtils.show("WIFI处于关闭状态");
                        break;
                    }
                    case WifiManager.WIFI_STATE_DISABLING: {
                        Log.d(TAG, "正在关闭");
                        break;
                    }
                    case WifiManager.WIFI_STATE_ENABLED: {
                        Log.d(TAG, "已经打开");
                        sortScaResult();
                        break;
                    }
                    case WifiManager.WIFI_STATE_ENABLING: {
                        Log.d(TAG, "正在打开");
                        break;
                    }
                    case WifiManager.WIFI_STATE_UNKNOWN: {
                        Log.d(TAG, "未知状态");
                        break;
                    }
                    default:
                }
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                Log.d(TAG, "--NetworkInfo--" + info.toString());
                if (NetworkInfo.State.DISCONNECTED == info.getState()) {//wifi没连接上
                    Log.d(TAG, "wifi没连接上");
                    hidingProgressBar();
                    for (int i = 0; i < realWifiList.size(); i++) {//没连接上将 所有的连接状态都置为“未连接”
                        realWifiList.get(i).setState(AppContants.WIFI_STATE_UNCONNECT);
                    }
                    adapter.notifyDataSetChanged();
                } else if (NetworkInfo.State.CONNECTED == info.getState()) {//wifi连接上了
                    Log.d(TAG, "wifi连接上了");
                    hidingProgressBar();
                    WifiInfo connectedWifiInfo = WifiSupport.getConnectedWifiInfo(
                        WifiConnectionActivity.this);

                    //连接成功 跳转界面 传递ip地址
                    ToastUtils.show("WIFI连接上了");

                    //thisToMain();
                    connectType = 1;
                    wifiListSet(connectedWifiInfo.getSSID(), connectType);
                } else if (NetworkInfo.State.CONNECTING == info.getState()) {//正在连接
                    Log.d(TAG, "wifi正在连接");
                    showProgressBar();
                    WifiInfo connectedWifiInfo = WifiSupport.getConnectedWifiInfo(
                        WifiConnectionActivity.this);
                    connectType = 2;
                    wifiListSet(connectedWifiInfo.getSSID(), connectType);
                }
            } else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                Log.d(TAG, "网络列表变化了");
                wifiListChange();
            }
        }
    }


    /*private void thisToMain() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putParcelable("wifi", wifiBean);
        intent.putExtras(bundle);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }*/


    /**
     * 将"已连接"或者"正在连接"的wifi热点放置在第一个位置
     */
    public void wifiListSet(String wifiName, int type) {
        int index = -1;
        WifiBean wifiInfo = new WifiBean();
        if (StringUtil.isNullOrEmpty(realWifiList)) {
            return;
        }
        for (int i = 0; i < realWifiList.size(); i++) {
            realWifiList.get(i).setState(AppContants.WIFI_STATE_UNCONNECT);
        }
        Collections.sort(realWifiList);//根据信号强度排序
        for (int i = 0; i < realWifiList.size(); i++) {
            WifiBean wifiBean = realWifiList.get(i);
            if (index == -1 && ("\"" + wifiBean.getWifiName() + "\"").equals(wifiName)) {
                index = i;
                wifiInfo.setLevel(wifiBean.getLevel());
                wifiInfo.setWifiName(wifiBean.getWifiName());
                wifiInfo.setCapabilities(wifiBean.getCapabilities());
                if (type == 1) {
                    wifiInfo.setState(AppContants.WIFI_STATE_CONNECT);
                } else {
                    wifiInfo.setState(AppContants.WIFI_STATE_ON_CONNECTING);
                }
            }
        }
        if (index != -1) {
            realWifiList.remove(index);
            realWifiList.add(0, wifiInfo);
            adapter.notifyDataSetChanged();
        }
    }


    /**
     * //网络状态发生改变 调用此方法！
     */
    public void wifiListChange() {
        sortScaResult();
        WifiInfo connectedWifiInfo = WifiSupport.getConnectedWifiInfo(this);
        if (connectedWifiInfo != null) {
            wifiListSet(connectedWifiInfo.getSSID(), connectType);
        }
    }


    public void showProgressBar() {
        mPbWifiLoading.setVisibility(View.VISIBLE);
    }


    public void hidingProgressBar() {
        mPbWifiLoading.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        ToastUtils.show("请点击关闭按钮关闭此页面");
    }
}
