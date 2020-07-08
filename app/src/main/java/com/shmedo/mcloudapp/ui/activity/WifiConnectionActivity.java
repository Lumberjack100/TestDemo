package com.shmedo.mcloudapp.ui.activity;

import android.app.Activity;
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
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dragon.core.AppContants;
import com.hjq.toast.ToastUtils;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.adapter.WifiListAdapter;
import com.shmedo.mcloudapp.entity.WifiBean;
import com.shmedo.mcloudapp.entity.event.WifiEvent;
import com.shmedo.mcloudapp.util.ActivityCollector;
import com.shmedo.mcloudapp.util.WifiSupport;
import com.shmedo.mcloudapp.views.WifiLinkDialog;

import org.greenrobot.eventbus.EventBus;

import java.lang.ref.WeakReference;
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

    @BindView(R.id.pb_wifi_loading)
    ProgressBar progressBar;

    @BindView(R.id.recycleview_wifi)
    RecyclerView mRecyclerView;

    private List<WifiBean> wifiBeanList = new ArrayList<>();

    private WifiListAdapter adapter;

    private WifiBroadcastReceiver wifiReceiver;

    private int connectType = 0;//1：连接成功？ 2 正在连接（如果wifi热点列表发生变需要该字段）

    private WifiBean curWifiBean;

    private WifiEvent event = new WifiEvent();

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, WifiConnectionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected void onResume() {
        super.onResume();
        //注册广播
        wifiReceiver = new WifiBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);//监听wifi开关变化的状态
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);//监听wifi连接状态广播,是否连接了一个有效路由
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);//监听wifi列表变化（开启一个热点或者关闭一个热点）
        this.registerReceiver(wifiReceiver, filter);
    }


    @Override
    protected void onPause() {
        super.onPause();
        this.unregisterReceiver(wifiReceiver);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_wifi_list);
        ButterKnife.bind(this);
        WeakReference<Activity> weakRefActivity = new WeakReference<>(this);
        ActivityCollector.add(weakRefActivity);

        initView();
    }


    private void initView() {
        if (WifiSupport.isOpenWifi(WifiConnectionActivity.this)) {
            initAdapter();
        } else {
            WifiSupport.openWifi(WifiConnectionActivity.this);
            initAdapter();
        }
    }


    private void initAdapter() {
        adapter = new WifiListAdapter(this, wifiBeanList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(adapter);

//        if (WifiSupport.isOpenWifi(WifiConnectionActivity.this)) {
//            updateWifiList();
//        } else {
//            //ToastUtils.show("WIFI处于关闭状态或权限获取失败");
//            WifiSupport.openWifi(WifiConnectionActivity.this);
//        }
        adapter.setOnItemClickListener(new WifiListAdapter.onItemClickListener() {
            @Override
            public void onItemClick(View view, int postion, Object o) {
                curWifiBean = wifiBeanList.get(postion);
                if (curWifiBean.getState().equals(AppContants.WiFi.WIFI_STATE_ON_CONNECTING))
                    return;

                String capabilities = curWifiBean.getCapabilities();
                if (WifiSupport.getWifiCipher(capabilities) == WifiSupport.WifiCipherType.WIFICIPHER_NOPASS) {//无需密码
                    WifiConfiguration tempConfig = WifiSupport.isExsits(curWifiBean.getWifiName(), WifiConnectionActivity.this);
                    if (tempConfig == null) {
                        WifiConfiguration exsits = WifiSupport.createWifiConfig(
                                curWifiBean.getWifiName(), null, WifiSupport.WifiCipherType.WIFICIPHER_NOPASS);
                        WifiSupport.addNetWork(exsits, WifiConnectionActivity.this);
                    } else {
                        WifiSupport.addNetWork(tempConfig, WifiConnectionActivity.this);
                    }
                } else {   //需要密码，弹出输入密码dialog
                    noConfigurationWifi();
                }
            }
        });
    }


    /**
     * 之前没配置过该网络， 弹出输入密码界面
     */
    private void noConfigurationWifi() {
        WifiLinkDialog linkDialog = new WifiLinkDialog(this,
                R.style.dialog_download, curWifiBean.getWifiName(),
                curWifiBean.getCapabilities());
        if (!linkDialog.isShowing()) {
            linkDialog.show();
        }
    }


    @OnClick({R.id.LL_close})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.LL_close:
                event.setMessage("wifi");
                event.setWifiBean(curWifiBean);
                EventBus.getDefault().post(event);
                finish();
                break;
        }
    }


    //监听wifi状态
    public class WifiBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case WifiManager.WIFI_STATE_CHANGED_ACTION:
                    checkWfiState(intent);
                    break;

                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                    checkNetworkState(intent);
                    break;

                case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:
                    wifiListChange();
                    break;

                default:
                    break;
            }
        }
    }


    private void checkWfiState(Intent intent) {
        //获取当前wifi的状态
        int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);
        switch (wifiState) {
            case WifiManager.WIFI_STATE_DISABLED:
                //wifi已关闭
                break;

            case WifiManager.WIFI_STATE_DISABLING:
                //wifi正在关闭
                break;

            case WifiManager.WIFI_STATE_ENABLED:
                //wifi已打开
                updateWifiList();
                break;

            case WifiManager.WIFI_STATE_ENABLING:
                //wifi正在打开
                break;

            case WifiManager.WIFI_STATE_UNKNOWN:
                //未知状态
                break;
        }
    }

    private void checkNetworkState(Intent intent) {
        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

        switch (info.getState()) {
            case DISCONNECTED:
                hidingProgressBar();
                for (int i = 0; i < wifiBeanList.size(); i++) {//没连接上将 所有的连接状态都置为“未连接”
                    wifiBeanList.get(i).setState(AppContants.WiFi.WIFI_STATE_UNCONNECT);
                }
                adapter.notifyDataSetChanged();
                break;

            case CONNECTED: {
                //连接成功 跳转界面 传递ip地址
                ToastUtils.show("WIFI连接上了");
                //thisToMain();

                hidingProgressBar();
                WifiInfo connectedWifiInfo = WifiSupport.getConnectedWifiInfo(WifiConnectionActivity.this);
                connectType = 1;
                setSpecifyWifiOnTop(connectedWifiInfo.getSSID(), connectType);
            }
            break;

            case CONNECTING: {
                showProgressBar();
                WifiInfo connectedWifiInfo = WifiSupport.getConnectedWifiInfo(WifiConnectionActivity.this);
                connectType = 2;
                setSpecifyWifiOnTop(connectedWifiInfo.getSSID(), connectType);
            }
            break;
        }
    }


    /**
     * 网络状态发生改变 调用此方法！
     */
    public void wifiListChange() {
        updateWifiList();
        WifiInfo connectedWifiInfo = WifiSupport.getConnectedWifiInfo(this);
        if (connectedWifiInfo != null) {
            setSpecifyWifiOnTop(connectedWifiInfo.getSSID(), connectType);
        }
    }


    /**
     * 获取wifi列表然后将bean转成自己定义的WifiBean
     */
    private void updateWifiList() {
        List<ScanResult> scanResults = WifiSupport.noSameName(WifiSupport.getWifiScanResult(this));
        if ( scanResults.isEmpty())
            return;

        wifiBeanList.clear();
        for (int i = 0; i < scanResults.size(); i++) {
            WifiBean wifiBean = new WifiBean();
            wifiBean.setWifiName(scanResults.get(i).SSID);
            wifiBean.setState(AppContants.WiFi.WIFI_STATE_UNCONNECT);   //只要获取都假设设置成未连接，真正的状态都通过广播来确定
            wifiBean.setCapabilities(scanResults.get(i).capabilities);
            wifiBean.setLevel(WifiSupport.getLevel(scanResults.get(i).level) + "");
            wifiBeanList.add(wifiBean);
        }

        //根据信号强度排序
        Collections.sort(wifiBeanList);
        adapter.notifyDataSetChanged();
    }


    /**
     * 将"已连接"或者"正在连接"的wifi热点放置在第一个位置
     */
    private void setSpecifyWifiOnTop(String wifiName, int type) {
        if (wifiBeanList.isEmpty()) {
            return;
        }

        resetWifiBeanState();
        int index = -1;
        WifiBean tempBean = new WifiBean();
        for (int i = 0; i < wifiBeanList.size(); i++) {
            WifiBean wifiBean = wifiBeanList.get(i);
            if (("\"" + wifiBean.getWifiName() + "\"").equals(wifiName)) {
                index = i;
                tempBean.setLevel(wifiBean.getLevel());
                tempBean.setWifiName(wifiBean.getWifiName());
                tempBean.setCapabilities(wifiBean.getCapabilities());
                if (type == 1) {
                    tempBean.setState(AppContants.WiFi.WIFI_STATE_CONNECT);
                } else {
                    tempBean.setState(AppContants.WiFi.WIFI_STATE_ON_CONNECTING);
                }

                break;
            }
        }

        if (index != -1) {
            wifiBeanList.remove(index);
            wifiBeanList.add(0, tempBean);
            adapter.notifyDataSetChanged();
        }
    }


    private void resetWifiBeanState() {
        for (int i = 0; i < wifiBeanList.size(); i++) {
            wifiBeanList.get(i).setState(AppContants.WiFi.WIFI_STATE_UNCONNECT);
        }
        Collections.sort(wifiBeanList);//根据信号强度排序
    }

    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }


    public void hidingProgressBar() {
        progressBar.setVisibility(View.GONE);
    }


}
