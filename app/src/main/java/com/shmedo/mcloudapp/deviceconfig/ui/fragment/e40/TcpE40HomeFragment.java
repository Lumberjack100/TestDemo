package com.shmedo.mcloudapp.deviceconfig.ui.fragment.e40;

import android.content.Context;
import android.graphics.Paint;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.vms.VmsBasicInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.ConfigModuleAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.ConfigModule;
import com.shmedo.mcloudapp.deviceconfig.model.TcpConnectionState;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.AdvancedSettingActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DataCenterHomeActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceCurrentStateActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.tcpcommon.BaseTcpIotCommunicateFragment;
import com.thanosfisherman.wifiutils.WifiUtils;
import com.thanosfisherman.wifiutils.wifiRemove.RemoveErrorCode;
import com.thanosfisherman.wifiutils.wifiRemove.RemoveSuccessListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  3/4/21 <br/>
 * 描述：    E40设备通过 TCP 连接配置主页面
 */
public class TcpE40HomeFragment extends BaseTcpIotCommunicateFragment {
    @BindView(R.id.swipeLayout)
    SwipeRefreshLayout swipeRefresh;

    @BindView(R.id.progress_overlay)
    View progressOverlay;

    @BindView(R.id.tv_progress_text)
    TextView mTvProgressText;

    @BindView(R.id.tv_device_name)
    TextView mTvDeviceName;//设备名称

    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSn;//设备SN号

    @BindView(R.id.tv_product_model)
    TextView mTvProductModel;//版本信息

    @BindView(R.id.tv_time_or_sub_model)
    TextView mTvSubModel;//网关电压

    @BindView(R.id.tv_platform_communication_state)
    TextView mTvPlatformCommunicationState;// 与数据平台的通信状态

    @BindView(R.id.tv_device_state_flag)
    TextView mTvDeviceState;//Tcp连接状态(已连接、已断开)

    @BindView(R.id.tv_device_connect_operate)
    TextView mTvDeviceConnectOperate;//Tcp连接操作(断开连接、重新连接)

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private ConfigModuleAdapter moduleAdapter;
    protected List<ConfigModule> configModuleList = new ArrayList<>();
    protected ConfigModule selectedConfigModule;

    private final String ipAddress = "192.168.5.2";//172.168.5.250   192.168.5.2

    private VmsBasicInfo vmsBasicInfo;

    private static Handler myHander = new Handler();
    private static RefreshRunnable refreshRunnable;

    private class RefreshRunnable implements Runnable {
        @Override
        public void run() {
            refreshRunnable = null;
            swipeRefresh.setRefreshing(false);
            updateHeadInfo();
            ToastUtils.show("查询数据超时");
        }
    }

    private void startRefreshRunnable(long delayMillis) {
        if (refreshRunnable == null) {
            refreshRunnable = new RefreshRunnable();
            myHander.postDelayed(refreshRunnable, delayMillis);
        }
    }

    private void stopRefreshRunnable() {
        myHander.removeCallbacksAndMessages(null);
        refreshRunnable = null;
        swipeRefresh.setRefreshing(false);
    }

    public static TcpE40HomeFragment newInstance() {
        return new TcpE40HomeFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.tcp_e40_home_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mTvDeviceState.setVisibility(View.INVISIBLE);
        mTvDeviceConnectOperate.setVisibility(View.VISIBLE);
        mTvDeviceConnectOperate.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        initRefreshLayout();
        initAdapter();
        initConfigModuleData();
        setupTcpConnect();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateViewStateByConnectState(tcpViewModel.getConnectStatus());
    }

    private void initRefreshLayout() {
        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_light);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!tcpViewModel.getConnectStatus()) {
                    ToastUtils.show(getString(R.string.refresh_failed_while_device_disconnected));
                    swipeRefresh.setRefreshing(false);
                    return;
                }
                getGatewayBaseInfo();
            }
        });
    }

    /**
     * 建立 Tcp 通讯连接
     */
    private void setupTcpConnect() {
        tcpViewModel.initTcpClient(ipAddress, 10002);
        startProgressRunnable("建立通讯连接...", TCP_CONNECT_DELAY_MILLIS);
        tcpViewModel.connect();
    }

    /**
     * Tcp连接状态变更事件
     */
    @Override
    protected void onConnectionChange(TcpConnectionState tcpConnectionState) {
        stopProgressRunnable();
        updateViewStateByConnectState(tcpConnectionState == TcpConnectionState.CONNECT_SUCCESS);

        if (tcpConnectionState == TcpConnectionState.CONNECT_SUCCESS) {
            swipeRefresh.setRefreshing(true);
            //建立通讯连接后，查询网关基本信息
            getGatewayBaseInfo();

        } else if (tcpConnectionState == TcpConnectionState.CONNECT_CLOSED) {
            ToastUtils.show("通讯连接断开");
        }
    }

    private void updateViewStateByConnectState(boolean isConnected) {
        mTvDeviceState.setVisibility(View.VISIBLE);
        if (isConnected) {
            mTvDeviceState.setText("已连接");
            mTvDeviceState.setTextColor(ContextCompat.getColor(mActivity, R.color.text_color_50E9B9));
            mTvDeviceState.setBackgroundResource(R.drawable.bg_device_online_state_flag);

            mTvDeviceConnectOperate.setText("断开连接");
            mTvDeviceConnectOperate.setTextColor(ContextCompat.getColor(mActivity, R.color.text_color_b3b3b3));
        } else {
            mTvDeviceState.setText("已断开");
            mTvDeviceState.setTextColor(ContextCompat.getColor(mActivity, R.color.sub_title_text_color));
            mTvDeviceState.setBackgroundResource(R.drawable.bg_device_offline_state_flag);

            mTvDeviceConnectOperate.setText("重新连接");
            mTvDeviceConnectOperate.setTextColor(ContextCompat.getColor(mActivity, R.color.blue_52B4F8));
        }
    }

    private void initAdapter() {
        int spanCount = 2;//跟布局里面的spanCount属性是一致的
        int spacing = DensityUtil.Dp2Px(mActivity, 15);//每一个矩形的间距
        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, spanCount));
        //设置每个item间距
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, false));
        moduleAdapter = new ConfigModuleAdapter(configModuleList);
        moduleAdapter.setAnimationEnable(true);
        moduleAdapter.setAnimationFirstOnly(false);
        moduleAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                if (isDoubleClick(view)) {
                    return;
                }
                if (!tcpViewModel.getConnectStatus()) {
                    ToastUtils.show(getString(R.string.tcp_config_disconnect_warn));
                    return;
                }

                selectedConfigModule = configModuleList.get(position);
                processItemClick();
            }
        });
        mRecyclerView.setAdapter(moduleAdapter);
    }

    private void initConfigModuleData() {
        configModuleList.clear();

        ConfigModule configModule = new ConfigModule(R.drawable.ic_device_current_state, "状态", "获取当前设备状态");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_data_center, "数据中心", "基础参数配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_setting, "设置", "高级设置");
        configModuleList.add(configModule);
    }

    private void processItemClick() {
        switch (selectedConfigModule.getName()) {
            case "状态":
                DeviceCurrentStateActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT, AppContants.DeviceType.E40);
                break;

            case "数据中心":
                DataCenterHomeActivity.startActivity(mActivity, AppContants.DeviceType.E40, AppContants.CommunicationWay.TCP_CONNECT, AppContants.DataCenterConfigMethod.BASIC_CONFIG);
                break;

            case "设置":
                AdvancedSettingActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT, AppContants.DeviceType.E40);
                break;
        }
    }

    @OnClick({R.id.tv_device_connect_operate})
    public void onClick(View v) {
        if (isDoubleClick(v)) {
            return;
        }
        //断开/重新连接
        int id = v.getId();
        if (id == R.id.tv_device_connect_operate) {
            if (!tcpViewModel.getConnectStatus()) {
                startProgressRunnable("建立通讯连接...", TCP_CONNECT_DELAY_MILLIS);
                tcpViewModel.connect();
            } else {
                isExitMode = false;
                showDisconnectDialog(getResources().getString(R.string.disconnect_device));
            }
        }
    }

    /**
     * 获取网关的基本信息
     */
    private void getGatewayBaseInfo() {
        startRefreshRunnable(10000);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_MD_GET_GATEWAY_BASE);
        sendCommand(command);
    }

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case VMS_MD_GET_GATEWAY_BASE: {//获取网关的基本信息
                stopRefreshRunnable();
                IOTCommandResult<VmsBasicInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    stopRefreshRunnable();
                    String errMsg = String.format("%s %s", "查询网关基本信息出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                vmsBasicInfo = commandResult.getResult();
                updateHeadInfo();
            }
            break;

            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
    }

    /**
     * 更新头部信息
     */
    private void updateHeadInfo() {
        if (vmsBasicInfo != null) {
            mTvDeviceName.setText("VMS网关");
            mTvDeviceSn.setText(String.format("设备SN号：%s", vmsBasicInfo.getSn()));
            mTvProductModel.setText(String.format("版本信息：%s", vmsBasicInfo.getSwVersion()));
            mTvSubModel.setText(String.format("网关电压：%s", vmsBasicInfo.getVolt() + "V"));
            if (!TextUtils.isEmpty(vmsBasicInfo.getOnline()) && !vmsBasicInfo.getOnline().equals("0")) {
                mTvPlatformCommunicationState.setText("米度平台连接状态：在线");

            } else if (vmsBasicInfo.getOnline().equals("0")) {
                mTvPlatformCommunicationState.setText(getPlatformAbnormalMessage("离线"));
            }
        } else {
            mTvDeviceName.setText("VMS网关");
            mTvDeviceSn.setText("设备SN号：--");
            mTvProductModel.setText("版本信息：--");
            mTvSubModel.setText("网关电压：--");
            mTvPlatformCommunicationState.setText("米度平台连接状态：--");
        }
    }

    private CharSequence getPlatformAbnormalMessage(String state) {
        SpannableStringBuilder builder = new SpannableStringBuilder(state);
        ForegroundColorSpan colorSpan = new ForegroundColorSpan(getResources().getColor(R.color.red));
        builder.setSpan(colorSpan, 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.insert(0, "米度平台连接状态：");

        return builder;
    }

    @Override
    public boolean onBackPressed() {
        if (tcpViewModel.getConnectStatus()) {
            isExitMode = true;
            showDisconnectDialog(getResources().getString(R.string.finish_activity_disconnect_tcp_device));
            return true;
        }

        return false;
    }

    @Override
    public void onStop() {
        super.onStop();
        stopRefreshRunnable();
    }

    @Override
    public void onDestroy() {
        processRemoveConnectedSSID();
        super.onDestroy();
    }

    private void processRemoveConnectedSSID() {
        WifiManager manager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String connectedSSID = manager.getConnectionInfo().getSSID();
        WifiUtils.withContext(getContext())
                .remove(connectedSSID, new RemoveSuccessListener() {
                    @Override
                    public void success() {
                        ToastUtils.show(connectedSSID + " 热点已断开");
                    }

                    @Override
                    public void failed(@NonNull RemoveErrorCode errorCode) {
                        ToastUtils.show(connectedSSID + " 热点断开失败;" + errorCode);
//                        Toast.makeText(getContext(), "Failed to disconnect and remove: $errorCode", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}