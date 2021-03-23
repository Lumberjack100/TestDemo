package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.vms.VmsAisleNumberEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.enums.VmsAisleNumber;
import com.shmedo.configlibrary.iot.model.vms.VmsAisleInfo;
import com.shmedo.configlibrary.iot.model.vms.VmsBasicInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.VmsAisleAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.vms.VmsTerminalSearchActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.VmsViewModel;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

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
public class NetVmsHomeFragment extends BaseNetIotCommunicateFragment {
    public static final String EXTRA_DEVICE = "com.shmedo.mcloudapp.EXTRA_DEVICE";

    @BindView(R.id.swipeLayout)
    SwipeRefreshLayout swipeRefresh;

    @BindView(R.id.scrollView)
    NestedScrollView nestedScrollView;

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

    @BindView(R.id.tv_aisle_one_network_number)
    TextView mTvAisleOneNetworkNumber;//通道1网络号

    @BindView(R.id.tv_aisle_one_address)
    TextView mTvAisleOneAddress;//通道1地址

    @BindView(R.id.tv_aisle_one_communication_chl)
    TextView mTvAisleOneCommuChl;//通道1通信信道

    @BindView(R.id.tv_aisle_one_signal_strength)
    TextView mTvAisleOneSignalStrength;//通道1信号强度

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private VmsAisleAdapter vmsAisleAdapter;

    private List<VmsAisleInfo> vmsAisleInfoList = new ArrayList<>();

    public ProjectDeviceInfo projectDeviceInfo;
    private VmsViewModel vmsViewModel;
    private VmsBasicInfo vmsBasicInfo;

    private NetVmsTerminalListFragment vmsTerminalListFragment;
    private static Handler myHander = new Handler();
    private static RefreshRunnable refreshRunnable;


    public static NetVmsHomeFragment newInstance(ProjectDeviceInfo projectDeviceInfo) {
        NetVmsHomeFragment fragment = new NetVmsHomeFragment();
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_DEVICE, projectDeviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

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
            swipeRefresh.setRefreshing(true);
            refreshRunnable = new RefreshRunnable();
            myHander.postDelayed(refreshRunnable, delayMillis);
        }
    }

    private void stopRefreshRunnable() {
        myHander.removeCallbacksAndMessages(null);
        refreshRunnable = null;
        swipeRefresh.setRefreshing(false);
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initRefreshLayout();
        updateHeadInfo();
        initAdapter();
        vmsViewModel = getApplicationScopeViewModel(VmsViewModel.class);
        observerRefreshTerminal();

        //查询网关基本信息
        getGatewayBaseInfo();
    }

    private void initRefreshLayout() {
        swipeRefresh.setColorSchemeResources(android.R.color.holo_blue_light);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getGatewayBaseInfo();
            }
        });
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
                    getGatewayBaseInfo();
                }
            }
        });
    }

    private void initAdapter() {
        int spanCount = 1;//跟布局里面的spanCount属性是一致的
        int spacing = DensityUtil.Dp2Px(mActivity, 10);//每一个矩形的间距
        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, spanCount));
        //设置每个item间距
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
        vmsAisleAdapter = new VmsAisleAdapter(vmsAisleInfoList);
        vmsAisleAdapter.setAnimationEnable(true);
        vmsAisleAdapter.setAnimationFirstOnly(false);
        vmsAisleAdapter.setOnItemChildClickListener(new OnItemChildClickListener() {
            @Override
            public void onItemChildClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                if (isDoubleClick(view)) {
                    return;
                }
                VmsAisleInfo vmsAisleInfo = vmsAisleInfoList.get(position);
                if (vmsAisleInfo.getTerminalnum().trim().equals("0")) {
                    ToastUtils.show("此通道下没有接入终端设备");
                    return;
                }
                vmsTerminalListFragment = new NetVmsTerminalListFragment(projectDeviceInfo, vmsAisleInfo);
                vmsTerminalListFragment.show(getChildFragmentManager(), "dialog");
            }
        });
        mRecyclerView.setAdapter(vmsAisleAdapter);
    }

    @OnClick({R.id.search_placeholder})
    public void onClick(View v) {
        if (isDoubleClick(v)) {
            return;
        }
        //断开/重新连接
        int id = v.getId();
        if (id == R.id.search_placeholder) {
            VmsTerminalSearchActivity.startActivity(mActivity, projectDeviceInfo);
        }
    }

    /**
     * 获取网关的基本信息
     */
    private void getGatewayBaseInfo() {
        startRefreshRunnable(DELAY_MILLIS);
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
     * 调用指令下发/透传接口结果返回
     *
     * @param dispatchCmdItemList
     */
    @Override
    protected void onDispatchCmdResult(List<DispatchCmdItem> dispatchCmdItemList, String cmdStr) {
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            stopRefreshRunnable();
            ToastUtils.show("下发指令失败");
            return;
        }
        msgIDList.clear();
        for (DispatchCmdItem cmdItem : dispatchCmdItemList) {
            msgIDList.add(cmdItem.getMsgID());
        }
        if (msgIDList != null && msgIDList.size() > 0) {
            startQueryCmdResponseRunnable(2000);
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
        stopRefreshRunnable();
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
        stopRefreshRunnable();
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
                    stopRefreshRunnable();
                    String errMsg = String.format("%s %s", "查询网关基本信息出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                vmsBasicInfo = commandResult.getResult();
                updateHeadInfo();
                //获取网关通道1的控制参数
                getGatewayAisleInfo(VmsAisleNumber.NUMBER_ONE);
            }
            break;

            case VMS_MD_GET_GATEWAY_PARAM: {//获取网关通道的控制参数
                IOTCommandResult<VmsAisleInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    stopRefreshRunnable();
                    String errMsg = String.format("%s %s", "查询网关通道的控制参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                VmsAisleInfo vmsAisleInfo = commandResult.getResult();
                if (vmsAisleInfo.getChannel() == 0) {
                    updateAisleOneInfo(vmsAisleInfo);
                    //获取网关通道2的控制参数
                    getGatewayAisleInfo(VmsAisleNumber.NUMBER_TWO);

                } else if (vmsAisleInfo.getChannel() == 1) {
                    vmsAisleInfoList.clear();
                    vmsAisleInfoList.add(vmsAisleInfo);
                    //获取网关通道3的控制参数
                    getGatewayAisleInfo(VmsAisleNumber.NUMBER_THREE);

                } else if (vmsAisleInfo.getChannel() == 2) {
                    vmsAisleInfoList.add(vmsAisleInfo);
                    vmsAisleAdapter.notifyDataSetChanged();
                    scrollToEnd();
                    stopRefreshRunnable();
                }
            }
            break;

            default:
                break;
        }
    }

    /**
     * 更新注册通道的信息
     *
     * @param vmsAisleInfo
     */
    private void updateAisleOneInfo(VmsAisleInfo vmsAisleInfo) {
        mTvAisleOneNetworkNumber.setText(String.valueOf(vmsAisleInfo.getNetid()));
        mTvAisleOneAddress.setText(String.valueOf(vmsAisleInfo.getAddr()));
        mTvAisleOneCommuChl.setText(String.valueOf(vmsAisleInfo.getChl()));
        mTvAisleOneSignalStrength.setText(vmsAisleInfo.getRssi() + "dBm");
    }

    private void scrollToEnd() {
        nestedScrollView.post(new Runnable() {
            @Override
            public void run() {
                nestedScrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        stopRefreshRunnable();
    }
}
