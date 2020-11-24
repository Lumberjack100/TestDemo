package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

import android.content.Context;
import android.graphics.Paint;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.VmsAisleNumberEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.enums.VmsAisleNumber;
import com.shmedo.configlibrary.iot.model.TerminalInfo;
import com.shmedo.configlibrary.iot.model.VmsAisleTerminalInfo;
import com.shmedo.configlibrary.iot.model.VmsBasicInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.VmsAisleAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.TcpConnectionState;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.vms.VmsTerminalSearchActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.BaseTcpConnectFragment;
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
 * 创建时间:  2020/11/20 <br/>
 * 描述：     Vms 网关主页面
 */
public class TcpVmsHomeFragment extends BaseTcpConnectFragment {
    @BindView(R.id.tv_device_name)
    TextView mTvDeviceName;//设备名称

    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSn;//设备SN号

    @BindView(R.id.tv_product_model)
    TextView mTvProductModel;//版本信息

    @BindView(R.id.tv_time_or_sub_model)
    TextView mTvSubModel;//网关电压

    @BindView(R.id.tv_device_state_flag)
    TextView mTvDeviceState;//通信状态(在线、离线)

    @BindView(R.id.tv_device_connect_operate)
    TextView mTvDeviceConnectOperate;//Tcp连接状态(断开连接、重新连接)

    @BindView(R.id.tv_device_communication_way_switch)
    TextView mTvDeviceCommunicationWaySwitch;//通信方式(网络、蓝牙)

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private VmsAisleAdapter vmsAisleAdapter;

    private List<VmsAisleTerminalInfo> vmsAisleTerminalInfoList = new ArrayList<>();

    private String ipAddress = "192.168.5.2";//172.168.5.250   192.168.5.2

    private VmsBasicInfo vmsBasicInfo = new VmsBasicInfo();


    public static TcpVmsHomeFragment newInstance() {
        return new TcpVmsHomeFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.tcp_vms_home_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initAdapter();
        observerRefreshTerminal();
        setupTcpConnect();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateViewStateByConnectState(tcpShareViewModel.getConnectStatus());
    }

    @Override
    protected void initView() {
        mTvDeviceState.setVisibility(View.INVISIBLE);
        mTvDeviceConnectOperate.setVisibility(View.VISIBLE);
        mTvDeviceConnectOperate.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        mTvDeviceCommunicationWaySwitch.setVisibility(View.INVISIBLE);
    }

    private void observerRefreshTerminal() {
        vmsViewModel.getVmsRefreshTerminal().observeInFragment(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isRefresh) {
                if (isRefresh) {
                    getGatewayStatus(VmsAisleNumber.NUMBER_ONE);
                }
            }
        });
    }

    private void setupTcpConnect() {
        tcpShareViewModel.initTcpClient(ipAddress, 10002);
        startProgressRunnable("建立通讯连接...", TCP_CONNECT_DELAY_MILLIS);
        tcpShareViewModel.connect();
    }

    @Override
    protected void onConnectionChange(TcpConnectionState tcpConnectionState) {
        if (tcpConnectionState == TcpConnectionState.CONNECT_SUCCESS) {
            //建立通讯连接后，查询网关基本信息
            String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_GET_GATEWAY_BASE);
            sendCommand(command);
        } else if (tcpConnectionState == TcpConnectionState.CONNECT_CLOSED) {
            dismissProgressDialog();
        }

        updateViewStateByConnectState(tcpConnectionState == TcpConnectionState.CONNECT_SUCCESS);
    }

    private void updateViewStateByConnectState(boolean isConnected) {
        if (isConnected) {
            mTvDeviceConnectOperate.setText("断开连接");
            mTvDeviceConnectOperate.setTextColor(ContextCompat.getColor(mActivity, R.color.text_color_b3b3b3));
        } else {
            mTvDeviceConnectOperate.setText("重新连接");
            mTvDeviceConnectOperate.setTextColor(ContextCompat.getColor(mActivity, R.color.blue_52B4F8));
        }
    }

    private void initAdapter() {
        int spanCount = 1;//跟布局里面的spanCount属性是一致的
        int spacing = DensityUtil.Dp2Px(mActivity, 14);//每一个矩形的间距
        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, spanCount));
        //设置每个item间距
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
        vmsAisleAdapter = new VmsAisleAdapter(vmsAisleTerminalInfoList);
        vmsAisleAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {
            @Override
            public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                if (isDoubleClick(view)) {
                    return;
                }
                if (!tcpShareViewModel.getConnectStatus()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    return;
                }

                VmsAisleTerminalInfo vmsAisleTerminalInfo = vmsAisleTerminalInfoList.get(position);
                TcpVmsTerminalListFragment newFragment = new TcpVmsTerminalListFragment(vmsAisleTerminalInfo);
                newFragment.show(getChildFragmentManager(), "dialog");
            }
        });
        mRecyclerView.setAdapter(vmsAisleAdapter);
    }

    @OnClick({R.id.search_placeholder, R.id.tv_device_connect_operate})
    public void onClick(View v) {
        if (isDoubleClick(v)) {
            return;
        }
        //断开/重新连接
        int id = v.getId();
        if (id == R.id.tv_device_connect_operate) {
            if (!tcpShareViewModel.getConnectStatus()) {
                startProgressRunnable("建立通讯连接...", TCP_CONNECT_DELAY_MILLIS);
                tcpShareViewModel.connect();
            } else {
                isExitMode = false;
                showDisconnectDialog(getResources().getString(R.string.disconnect_device));
            }
        } else if (id == R.id.search_placeholder) {
            VmsTerminalSearchActivity.startActivity(mActivity);
        }
    }

    /**
     * 获取网关不同通道下，挂载终端的运行情况
     *
     * @param vmsAisleNumber
     */
    private void getGatewayStatus(VmsAisleNumber vmsAisleNumber) {
        VmsAisleNumberEntity vmsAisleNumberEntity = new VmsAisleNumberEntity(vmsAisleNumber.toInt());
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_GET_GATEWAY_STATUS, vmsAisleNumberEntity);
        sendCommand(command);
    }

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case MD_GET_GATEWAY_BASE: {//获取网关的基本信息
                IOTCommandResult<VmsBasicInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    stopProgressRunnable();
                    String errMsg = "查询网关基本信息出错!";
                    Timber.e("%s%s", errMsg, commandResult.getMessage());
                    ToastUtils.show(errMsg);
                    return;
                }
                vmsBasicInfo = commandResult.getResult();
                updateHeadInfo();
                getGatewayStatus(VmsAisleNumber.NUMBER_ONE);
            }
            break;

            case MD_GET_GATEWAY_STATUS: {//获取网关的状态
                IOTCommandResult<VmsAisleTerminalInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    stopProgressRunnable();
                    String errMsg = "查询网关基本信息出错!";
                    Timber.e("%s%s", errMsg, commandResult.getMessage());
                    ToastUtils.show(errMsg);
                    return;
                }
                VmsAisleTerminalInfo vmsAisleTerminalInfo = commandResult.getResult();
                modifyAisleTerminalInfo(vmsAisleTerminalInfo);

                if (vmsAisleTerminalInfo.getChannel() == 0) {
                    vmsViewModel.clearTerminalList();
                    vmsAisleTerminalInfoList.clear();
                    vmsAisleTerminalInfoList.add(vmsAisleTerminalInfo);
                    getGatewayStatus(VmsAisleNumber.NUMBER_TWO);

                } else if (vmsAisleTerminalInfo.getChannel() == 1) {
                    vmsViewModel.addTerminalList(vmsAisleTerminalInfo.getTerminal());
                    vmsAisleTerminalInfoList.add(vmsAisleTerminalInfo);
                    getGatewayStatus(VmsAisleNumber.NUMBER_THREE);

                } else if (vmsAisleTerminalInfo.getChannel() == 2) {
                    stopProgressRunnable();
                    vmsViewModel.addTerminalList(vmsAisleTerminalInfo.getTerminal());
                    vmsAisleTerminalInfoList.add(vmsAisleTerminalInfo);
                    vmsAisleAdapter.notifyDataSetChanged();
                }
            }
            break;

            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
    }

    private void updateHeadInfo() {
        if (vmsBasicInfo != null) {
            mTvDeviceName.setText("VMS网关");
            mTvDeviceSn.setText(String.format("设备SN号：%s", vmsBasicInfo.getSn()));
            mTvProductModel.setText(String.format("版本信息：%s", vmsBasicInfo.getSwVersion()));
            mTvSubModel.setText(String.format("网关电压：%s", vmsBasicInfo.getVolt() + "V"));
            if (!vmsBasicInfo.getOnline().trim().equals("0")) {
                mTvDeviceState.setVisibility(View.VISIBLE);
                mTvDeviceState.setText("在线");
                mTvDeviceState.setTextColor(ContextCompat.getColor(mActivity, R.color.text_color_50E9B9));
                mTvDeviceState.setBackgroundResource(R.drawable.bg_device_online_state_flag);
            } else {
                mTvDeviceState.setVisibility(View.VISIBLE);
                mTvDeviceState.setText("离线");
                mTvDeviceState.setTextColor(ContextCompat.getColor(mActivity, R.color.sub_title_text_color));
                mTvDeviceState.setBackgroundResource(R.drawable.bg_device_offline_state_flag);
            }
        }
    }

    /**
     * 修改某个通道下接入的的终端信息，设置终端的网络号、信道号与所属通道一致
     *
     * @param vmsAisleTerminalInfo
     */
    private void modifyAisleTerminalInfo(VmsAisleTerminalInfo vmsAisleTerminalInfo) {
        if (vmsAisleTerminalInfo == null)
            return;

        if (vmsAisleTerminalInfo.getTerminal() == null)
            return;

        for (TerminalInfo terminalInfo : vmsAisleTerminalInfo.getTerminal()) {
            terminalInfo.setNetid(vmsAisleTerminalInfo.getNetid());
            terminalInfo.setChl(vmsAisleTerminalInfo.getChl());
        }
    }

    @Override
    public boolean onBackPressed() {
        if (tcpShareViewModel.getConnectStatus()) {
            isExitMode = true;
            showDisconnectDialog(getResources().getString(R.string.finish_activity_disconnect_tcp_device));
            return true;
        }

        return false;
    }

    @Override
    public void onDestroy() {
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

        super.onDestroy();
    }

}