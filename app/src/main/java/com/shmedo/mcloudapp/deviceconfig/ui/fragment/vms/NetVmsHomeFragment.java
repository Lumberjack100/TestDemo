package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;
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
import com.shmedo.configlibrary.iot.model.vms.VmsTerminalSn;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.vms.VmsTerminalSearchActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.VmsViewModel;
import com.shmedo.mcloudapp.deviceconfig.adapter.PageAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.ProjectDeviceInfo;
import com.shmedo.mcloudapp.util.permission.PermissionHelper;
import com.shmedo.mcloudapp.util.permission.XPermissionUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  3/10/21 <br/>
 * 描述：       Vms 网关 4g 模式配置主页面
 */
public class NetVmsHomeFragment extends BaseNetIotCommunicateFragment implements TabLayout.OnTabSelectedListener {
    public static final String EXTRA_DEVICE = "com.shmedo.mcloudapp.EXTRA_DEVICE";

    @NonNull
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

    private VmsAisleListFragment vmsAisleListFragment;
    private NetVmsTerminalListFragment vmsTerminalListFragment;
    public ProjectDeviceInfo projectDeviceInfo;
    private VmsViewModel vmsViewModel;
    private VmsBasicInfo vmsBasicInfo;
    private VmsAisleInfo vmsAisleInfo1, vmsAisleInfo2;
    private int terminalIndex1 = 0;//通道一终端索引号
    private int terminalIndex2 = 0;//通道二终端索引号


    public static NetVmsHomeFragment newInstance(ProjectDeviceInfo projectDeviceInfo) {
        NetVmsHomeFragment fragment = new NetVmsHomeFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_DEVICE, projectDeviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectDeviceInfo = getArguments().getParcelable(EXTRA_DEVICE);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.vms_home_fragment;
    }

    @Override
    protected void initView() {
        vmsAisleListFragment = new VmsAisleListFragment();
        vmsTerminalListFragment = NetVmsTerminalListFragment.newInstance(projectDeviceInfo);

        List<Fragment> mFragments = new ArrayList<>();
        mFragments.add(vmsAisleListFragment);
        mFragments.add(vmsTerminalListFragment);
        pagerAdapter = new PageAdapter((FragmentActivity) mActivity, mFragments);
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
        vmsViewModel = getApplicationScopeViewModel(VmsViewModel.class);
        observerRefreshTerminal();
        updateHeadInfo();
        initRefreshLayout();
        mRefreshLayout.setEnableLoadMore(false);
        //是否在刷新的时候禁止内容的一切手势操作（默认false）
        mRefreshLayout.setDisableContentWhenRefresh(true);
        mRefreshLayout.autoRefresh();
    }

    private void initRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull @NotNull RefreshLayout refreshLayout) {
                getGatewayBaseInfo();
            }
        });
    }

    /**
     * 更新头部信息
     */
    private void updateHeadInfo() {
        try {
            if (vmsBasicInfo != null) {
                mTvDeviceName.setText("VMS网关");
                mTvDeviceSn.setText(String.format("设备SN号：%s", vmsBasicInfo.getSn()));
                mTvProductModel.setText(String.format("版本信息：%s", vmsBasicInfo.getSwVersion()));
                mTvSubModel.setText(String.format("网关电压：%s", vmsBasicInfo.getVolt() + "V"));
                if (!TextUtils.isEmpty(vmsBasicInfo.getOnline()) && !vmsBasicInfo.getOnline().equals("0")) {
                    mTvDeviceState.setText("在线");
                    mTvDeviceState.setTextColor(ContextCompat.getColor(mActivity, R.color.text_color_50E9B9));
                    mTvDeviceState.setBackgroundResource(R.drawable.bg_device_online_state_flag);

                } else if (vmsBasicInfo.getOnline().equals("0")) {
                    mTvDeviceState.setText("离线");
                    mTvDeviceState.setTextColor(ContextCompat.getColor(mActivity, R.color.sub_title_text_color));
                    mTvDeviceState.setBackgroundResource(R.drawable.bg_device_offline_state_flag);
                }
            } else {
                mTvDeviceName.setText("VMS网关");
                mTvDeviceSn.setText(String.format("设备SN号：%s", projectDeviceInfo.getToken()));
                mTvProductModel.setText(String.format("版本信息：%s", projectDeviceInfo.getFirmwareVersion()));
                mTvSubModel.setText("网关电压：--");
                if (projectDeviceInfo.isOnline()) {
                    mTvDeviceState.setText("在线");
                    mTvDeviceState.setTextColor(ContextCompat.getColor(mActivity, R.color.text_color_50E9B9));
                    mTvDeviceState.setBackgroundResource(R.drawable.bg_device_online_state_flag);
                } else {
                    mTvDeviceState.setText("离线");
                    mTvDeviceState.setTextColor(ContextCompat.getColor(mActivity, R.color.sub_title_text_color));
                    mTvDeviceState.setBackgroundResource(R.drawable.bg_device_offline_state_flag);
                }
            }
            mTvPlatformCommunicationState.setVisibility(View.GONE);
            mTvDeviceConnectOperate.setVisibility(View.GONE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 观察终端设备刷新<br>
     * 因为终端列表页面移除了设备，网关主页面需要刷新数据
     */
    private void observerRefreshTerminal() {
        vmsViewModel.getVmsRefreshTerminal().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isRefresh) {
                if (isRefresh) {
                    getGatewayBaseInfo();
                }
            }
        });
    }

    @OnClick({R.id.search_placeholder, R.id.ll_scan_add_device})
    public void onClick(View v) {
        if (isDoubleClick(v)) {
            return;
        }
        //断开/重新连接
        int id = v.getId();
        if (id == R.id.search_placeholder) {
            VmsTerminalSearchActivity.startActivity(mActivity, projectDeviceInfo);
        } else if (id == R.id.ll_scan_add_device) {
            PermissionHelper.requestScanPermissions(NetVmsHomeFragment.this);
        }
    }

    /**
     * 获取网关的基本信息
     */
    private void getGatewayBaseInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_MD_GET_GATEWAY_BASE);
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    /**
     * 获取网关不同通道下的控制参数
     */
    private void getGatewayAisleInfo(VmsAisleNumber vmsAisleNumber) {
        VmsAisleNumberEntity vmsAisleNumberEntity = new VmsAisleNumberEntity(vmsAisleNumber.toInt());
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_MD_GET_GATEWAY_PARAM, vmsAisleNumberEntity);
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    /**
     * 获取网关不同通道下，挂载终端的运行情况
     */
    private void getTerminalStatus(int channel, int index) {
        VmsTerminalStatusEntity entity = new VmsTerminalStatusEntity(channel, index);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_MD_GET_TERMINAL_STATUS, entity);
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    /**
     * 扫描添加新的终端
     */
    private void scanAddTerminal(String sn) {
        TerminalSNEntity entity = new TerminalSNEntity(sn);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_MD_SCAN_ADD_TERMINAL, entity);
        showWaitDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    /**
     * 获取扫码添加的终端添列表
     */
    private void getTerminalSN() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_MD_GET_TERMINAL_SN) + "&type=2";
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    /**
     * 移除网关挂载的终端
     */
    public void removeTerminal(String sn) {
        TerminalSNEntity entity = new TerminalSNEntity(sn);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_MD_DELETE_TERMINAL, entity);
        showWaitDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    /**
     * 调用指令下发/透传接口结果返回
     *
     * @param dispatchCmdItemList
     */
    @Override
    protected void onDispatchCmdResult(List<DispatchCmdItem> dispatchCmdItemList, String cmdStr) {
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            if (mRefreshLayout.isRefreshing()) {
                mRefreshLayout.finishRefresh(false);
            }
            dismissWaitDialog();
            ToastUtils.show("下发指令失败");
            return;
        }
        msgIDList.clear();
        for (DispatchCmdItem cmdItem : dispatchCmdItemList) {
            msgIDList.add(cmdItem.getMsgID());
        }
        if (msgIDList != null && msgIDList.size() > 0) {
            startQueryCmdResponse();
        }
    }

    /**
     * 查询指令响应结果出错
     *
     * @param errMsg
     */
    @Override
    protected void onQueryCmdResponseResultError(String errMsg) {
        super.onQueryCmdResponseResultError(errMsg);
        if (mRefreshLayout.isRefreshing()) {
            mRefreshLayout.finishRefresh(false);
        }
        ToastUtils.show("查询数据响应错误");
    }

    /**
     * 查询指令响应结果超时
     *
     * @param queryCmdResult
     */
    @Override
    protected void onQueryCmdResponseResultTimeOut(QueryCmdResult queryCmdResult) {
        super.onQueryCmdResponseResultTimeOut(queryCmdResult);
        if (mRefreshLayout.isRefreshing()) {
            mRefreshLayout.finishRefresh(false);
        }
        ToastUtils.show("查询数据响应超时");
    }

    /**
     * 查询指令响应结果成功
     *
     * @param queryCmdResult
     */
    @Override
    protected void onQueryCmdResponseResultSuccess(QueryCmdResult queryCmdResult) {
        super.onQueryCmdResponseResultSuccess(queryCmdResult);
        setResultData(queryCmdResult);
    }

    private void setResultData(QueryCmdResult queryCmdResult) {
        String cmdStr = queryCmdResult.getResponseContent();
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case VMS_MD_GET_GATEWAY_BASE: {//获取网关的基本信息
                IOTCommandResult<VmsBasicInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(false);
                    }
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
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(false);
                    }
                    String errMsg = String.format("%s %s", "查询网关通道的控制参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                VmsAisleInfo vmsAisleInfo = commandResult.getResult();
                if (vmsAisleInfo == null) {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(false);
                    }
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

                    int totalCount = Integer.parseInt(vmsAisleInfo1.getTerminalnum()) + Integer.parseInt(vmsAisleInfo2.getTerminalnum());
                    updateTerminalTabText(totalCount);

                    terminalIndex1 = 0;
                    vmsTerminalListFragment.clearTerminalList();
                    vmsViewModel.clearCacheTerminalList();
                    getTerminalStatus(vmsAisleInfo1.getChannel(), terminalIndex1);
                }
            }
            break;

            case VMS_MD_GET_TERMINAL_STATUS: {//获取挂载终端的状态
                IOTCommandResult<VmsAisleTerminalInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    if (mRefreshLayout.isRefreshing()) {
                        mRefreshLayout.finishRefresh(false);
                    }
                    String errMsg = String.format("%s %s", "查询网关通道下的挂载终端信息出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                VmsAisleTerminalInfo vmsAisleTerminalInfo = commandResult.getResult();
                modifyAisleTerminalInfo(vmsAisleTerminalInfo);
                vmsViewModel.addCacheTerminalList(vmsAisleTerminalInfo.getTerminal());
                vmsTerminalListFragment.updateTerminalList(vmsAisleTerminalInfo.getTerminal());

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

            case VMS_MD_SCAN_ADD_TERMINAL: {//扫码添加终端
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    dismissWaitDialog();
                    String errMsg = String.format("%s %s", "扫码添加终端出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                getTerminalSN();
            }
            break;

            case VMS_MD_GET_TERMINAL_SN: {//获取扫码添加终端列表
                dismissWaitDialog();
                IOTCommandResult<VmsTerminalSn> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取扫码添加终端列表出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                VmsTerminalSn vmsTerminalSn = commandResult.getResult();
                if (!TextUtils.isEmpty(vmsTerminalSn.getSn())) {
                    List<VmsTerminalInfo> dataList = new ArrayList<>();
                    String[] values = vmsTerminalSn.getSn().split(",");
                    for (String sn : values) {
                        VmsTerminalInfo terminalInfo = new VmsTerminalInfo(VmsTerminalInfo.SCAN_ADD_DEVICE);
                        terminalInfo.setSn(sn);
                        dataList.add(terminalInfo);
                    }
                    vmsTerminalListFragment.updateTerminalList(dataList);
                    updateTerminalTabText(vmsTerminalListFragment.getTerminalSize());
                }
            }
            break;

            case VMS_MD_DELETE_TERMINAL: {//删除终端设备
                dismissWaitDialog();
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "删除终端出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                doAfterSetting();
            }
            break;

            default:
                break;
        }
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
        int totalCount = vmsViewModel.getCacheVmsTerminalList().size();
        updateTerminalTabText(totalCount);
    }

    private void updateTerminalTabText(int totalCount) {
        TextView textView = (TextView) tabLayout.getTabAt(1).getCustomView();
        textView.setText("设备(" + totalCount + ")");
        tabLayout.requestLayout();
        tabLayout.getTabAt(1).select();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mRefreshLayout.isRefreshing()) {
            mRefreshLayout.finishRefresh(false);
        }
        dismissWaitDialog();
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
        }
    }

    /**
     * 处理老设备条码规则，例如：MEDO,189150L,DAS
     */
    private void parseOldDeviceCode(String barCode) {
        if (!barCode.startsWith("MEDO")) {
            showTipDialog("请扫描正确的设备二维码");
            return;
        }
        String[] localData = barCode.split(",");
        if (localData.length != 3) {
            showTipDialog("请扫描正确的设备二维码");
            return;
        }
        if (TextUtils.isEmpty(localData[0]) || TextUtils.isEmpty(localData[1]) || TextUtils.isEmpty(localData[2])) {
            showTipDialog("请扫描正确的设备二维码");
            return;
        }
        if (localData[1].length() != 7) {
            showTipDialog("设备标识有误,请扫描正确的设备二维码");
            return;
        }

        String sn = localData[1].replace("MD-", "");
        scanAddTerminal(sn);
    }

    /**
     * 处理新设备条码规则，例如：https://cloud.shmedo.cn/mcloudapp/device?sn=189150L
     */
    private void parseNewDeviceCode(String barCode) {
        if (!barCode.startsWith("https://cloud.shmedo.cn/mcloudapp/device?sn=")) {
            showTipDialog("请扫描正确的设备二维码");
            return;
        }
        String[] localData = barCode.split("=");
        if (localData.length != 2) {
            showTipDialog("请扫描正确的设备二维码");
            return;
        }
        if (TextUtils.isEmpty(localData[1])) {
            showTipDialog("请扫描正确的设备二维码");
            return;
        }
        if (localData[1].length() != 7) {
            showTipDialog("设备标识有误,请扫描正确的设备二维码");
            return;
        }
        String sn = localData[1].replace("MD-", "");
        scanAddTerminal(sn);
    }
}
