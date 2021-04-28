package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

import android.content.Context;
import android.graphics.Paint;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
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
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.TerminalSNEntity;
import com.shmedo.configlibrary.iot.cmd.entity.vms.VmsAisleNumberEntity;
import com.shmedo.configlibrary.iot.cmd.entity.vms.VmsTerminalStatusEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.enums.VmsAisleNumber;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.vms.VmsAisleInfo;
import com.shmedo.configlibrary.iot.model.vms.VmsAisleTerminalInfo;
import com.shmedo.configlibrary.iot.model.vms.VmsBasicInfo;
import com.shmedo.configlibrary.iot.model.vms.VmsTerminalInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.TcpConnectionState;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.vms.VmsTerminalSearchActivity;
import com.shmedo.mcloudapp.projects.adapter.ProjectPageAdapter;
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
 * 描述：     Vms 网关 TCP 配置主页面
 */
public class TcpVmsHomeFragment extends BaseVmsTcpCommunicateFragment implements TabLayout.OnTabSelectedListener {
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;

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

    @BindView(R.id.tabs)
    TabLayout tabLayout;

    @BindView(R.id.viewpager)
    ViewPager2 viewPager;

    private FragmentStateAdapter pagerAdapter;
    private TabLayoutMediator tabLayoutMediator;

    private NetVmsAisleListFragment vmsAisleListFragment;
    private VmsTerminalListFragment vmsTerminalListFragmentTest;

    private final String ipAddress = "192.168.5.2";//172.168.5.250   192.168.5.2

    private VmsBasicInfo vmsBasicInfo;
    private VmsAisleInfo vmsAisleInfo1, vmsAisleInfo2;
    private int terminalIndex1 = 0;//通道一终端索引号
    private int terminalIndex2 = 0;//通道二终端索引号


    public static TcpVmsHomeFragment newInstance() {
        return new TcpVmsHomeFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.vms_home_fragment;
    }

    @Override
    protected void initView() {
        vmsAisleListFragment = new NetVmsAisleListFragment();
        vmsTerminalListFragmentTest = new VmsTerminalListFragment();

        List<Fragment> mFragments = new ArrayList<>();
        mFragments.add(vmsAisleListFragment);
        mFragments.add(vmsTerminalListFragmentTest);
        pagerAdapter = new ProjectPageAdapter((FragmentActivity) mActivity, mFragments);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(1);
        tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                if (position == 0) {
                    View tabView = LayoutInflater.from(mActivity).inflate(R.layout.custom_tab_text, null);
                    TextView textView = tabView.findViewById(R.id.tabText);
                    textView.setText("通道(3)");
                    textView.setTextColor(ContextCompat.getColor(mActivity, R.color.title_text_color));
                    textView.setTextSize(18);
                    tab.setCustomView(textView);
                } else if (position == 1) {
                    View tabView = LayoutInflater.from(mActivity).inflate(R.layout.custom_tab_text, null);
                    TextView textView = tabView.findViewById(R.id.tabText);
                    textView.setText("设备");
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mTvDeviceState.setVisibility(View.INVISIBLE);
        mTvDeviceConnectOperate.setVisibility(View.VISIBLE);
        mTvDeviceConnectOperate.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        mRefreshLayout.setEnableLoadMore(false);
        //是否在刷新的时候禁止内容的一切手势操作（默认false）
        mRefreshLayout.setDisableContentWhenRefresh(true);
        initRefreshLayout();
        observerRefreshTerminal();
        setupTcpConnect();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateViewStateByConnectState(tcpViewModel.getConnectStatus());
    }

    private void initRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull @NotNull RefreshLayout refreshLayout) {
                if (!tcpViewModel.getConnectStatus()) {
                    ToastUtils.show(getString(R.string.refresh_failed_while_device_disconnected));
                    mRefreshLayout.finishRefresh(false);
                    return;
                }
                //查询网关基本信息
                getGatewayBaseInfo();
                refreshLayout.getLayout().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (refreshLayout.isRefreshing()) {
                            refreshLayout.finishRefresh(false);
                            ToastUtils.show("刷新超时");
                        }
                    }
                }, WRITE_TIME_OUT_SECOND);
            }
        });
    }

    /**
     * 观察终端设备刷新<br>
     * 因为终端列表页面移除了设备，网关主页面需要刷新数据
     */
    private void observerRefreshTerminal() {
        vmsViewModel.getVmsRefreshTerminal().observeInFragment(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isRefresh) {
                if (isRefresh) {
                    //查询网关基本信息
                    getGatewayBaseInfo();
                }
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

    @OnClick({R.id.search_placeholder, R.id.tv_device_connect_operate})
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
        } else if (id == R.id.search_placeholder) {
            VmsTerminalSearchActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT);
        }
    }

    /**
     * 获取网关的基本信息
     */
    private void getGatewayBaseInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_MD_GET_GATEWAY_BASE);
        sendCommand(command);
    }

    /**
     * 获取网关不同通道下的控制参数
     */
    private void getGatewayAisleInfo(VmsAisleNumber vmsAisleNumber) {
        VmsAisleNumberEntity vmsAisleNumberEntity = new VmsAisleNumberEntity(vmsAisleNumber.toInt());
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_MD_GET_GATEWAY_PARAM, vmsAisleNumberEntity);
        sendCommand(command);
    }

    /**
     * 获取网关不同通道下，挂载终端的运行情况
     */
    private void getTerminalStatus(int channel, int index) {
        VmsTerminalStatusEntity entity = new VmsTerminalStatusEntity(channel, index);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_MD_GET_TERMINAL_STATUS, entity);
        sendCommand(command);
    }

    /**
     * 移除网关挂载的终端
     */
    public void removeTerminal(String sn) {
        TerminalSNEntity entity = new TerminalSNEntity(sn);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_MD_DELETE_TERMINAL, entity);
        showProgressDialog("处理中...");
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
                IOTCommandResult<VmsBasicInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    mRefreshLayout.finishRefresh(false);
                    String errMsg = String.format("%s %s", "查询网关基本信息出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                vmsBasicInfo = commandResult.getResult();
                updateHeadInfo();
                vmsAisleListFragment.clearAisleListInfo();
                //获取网关通道1的控制参数
                getGatewayAisleInfo(VmsAisleNumber.NUMBER_ONE);
            }
            break;

            case VMS_MD_GET_GATEWAY_PARAM: {//获取网关通道的控制参数
                IOTCommandResult<VmsAisleInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    mRefreshLayout.finishRefresh(false);
                    String errMsg = String.format("%s %s", "查询网关通道的控制参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                VmsAisleInfo vmsAisleInfo = commandResult.getResult();
                if (vmsAisleInfo == null) {
                    mRefreshLayout.finishRefresh(false);
                    return;
                }
                vmsAisleListFragment.updateAisleListInfo(vmsAisleInfo);
                if (vmsAisleInfo.getChannel() == 0) {
                    //获取网关通道2的控制参数
                    getGatewayAisleInfo(VmsAisleNumber.NUMBER_TWO);

                } else if (vmsAisleInfo.getChannel() == 1) {
                    vmsAisleInfo1 = vmsAisleInfo;
                    //获取网关通道3的控制参数
                    getGatewayAisleInfo(VmsAisleNumber.NUMBER_THREE);

                } else if (vmsAisleInfo.getChannel() == 2) {
                    vmsAisleInfo2 = vmsAisleInfo;

                    TextView textView = (TextView) tabLayout.getTabAt(1).getCustomView();
                    int totalCount = Integer.parseInt(vmsAisleInfo1.getTerminalnum()) + Integer.parseInt(vmsAisleInfo2.getTerminalnum());
                    textView.setText("设备(" + totalCount + ")");
                    tabLayout.getTabAt(1).select();

                    terminalIndex1 = 0;
                    vmsTerminalListFragmentTest.clearTerminalList();
                    vmsViewModel.clearCacheTerminalList();
                    getTerminalStatus(vmsAisleInfo1.getChannel(), terminalIndex1);
                }
            }
            break;

            case VMS_MD_GET_TERMINAL_STATUS: {//获取挂载终端的状态
                IOTCommandResult<VmsAisleTerminalInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    mRefreshLayout.finishRefresh(false);
                    String errMsg = String.format("%s %s", "查询网关通道下的挂载终端信息出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                VmsAisleTerminalInfo vmsAisleTerminalInfo = commandResult.getResult();
                modifyAisleTerminalInfo(vmsAisleTerminalInfo);
                vmsViewModel.addCacheTerminalList(vmsAisleTerminalInfo.getTerminal());
                vmsTerminalListFragmentTest.updateTerminalList(vmsAisleTerminalInfo.getTerminal());

                if (vmsAisleTerminalInfo.getChannel() == vmsAisleInfo1.getChannel()) {
                    terminalIndex1++;
                    if (terminalIndex1 < Integer.parseInt(vmsAisleInfo1.getTerminalnum())) {
                        getTerminalStatus(vmsAisleInfo1.getChannel(), terminalIndex1);
                    } else {
                        terminalIndex2 = 0;
                        getTerminalStatus(vmsAisleInfo2.getChannel(), terminalIndex2);
                    }
                } else if (vmsAisleTerminalInfo.getChannel() == vmsAisleInfo2.getChannel()) {
                    terminalIndex2++;
                    if (terminalIndex2 < Integer.parseInt(vmsAisleInfo2.getTerminalnum())) {
                        getTerminalStatus(vmsAisleInfo2.getChannel(), terminalIndex2);
                    } else {
                        mRefreshLayout.finishRefresh(true);
                    }
                }
            }
            break;

            case VMS_MD_DELETE_TERMINAL: {//删除终端设备
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    dismissProgressDialog();
                    String errMsg = String.format("%s %s", "删除终端出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                doAfterSetting();
            }
            break;

            default:
                mRefreshLayout.finishRefresh(false);
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

    /**
     * 修改某个通道下接入的的终端信息，设置终端的网络号、信道号与所属通道一致
     *
     * @param vmsAisleTerminalInfo
     */
    private void modifyAisleTerminalInfo(VmsAisleTerminalInfo vmsAisleTerminalInfo) {
        if (vmsAisleTerminalInfo == null || vmsAisleTerminalInfo.getTerminal() == null)
            return;

        for (VmsTerminalInfo vmsTerminalInfo : vmsAisleTerminalInfo.getTerminal()) {
            vmsTerminalInfo.setAsileNumber(vmsAisleTerminalInfo.getChannel());
            vmsTerminalInfo.setNetid(vmsAisleTerminalInfo.getNetid());
            vmsTerminalInfo.setChl(vmsAisleTerminalInfo.getChl());
        }
    }

    private void doAfterSetting() {
        ToastUtils.show("删除成功");
        dismissProgressDialog();
        updateTerminalTabText();
    }

    private void updateTerminalTabText() {
        TextView textView = (TextView) tabLayout.getTabAt(1).getCustomView();
        int totalCount = vmsViewModel.getCacheVmsTerminalList().size();
        textView.setText("设备(" + totalCount + ")");
        tabLayout.getTabAt(1).select();
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
    public void onStop() {
        super.onStop();
        mRefreshLayout.finishRefresh(false);
        dismissProgressDialog();
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