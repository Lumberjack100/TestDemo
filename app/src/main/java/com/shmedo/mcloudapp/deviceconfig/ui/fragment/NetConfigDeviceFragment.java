package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.toast.ToastUtils;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.event.DeviceModuleSwitchTabEvent;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.core.util.GsonFactory;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.model.PageResult;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.ConfigModuleAdapter;
import com.shmedo.mcloudapp.deviceconfig.helper.DispatchCmdHelper;
import com.shmedo.mcloudapp.deviceconfig.model.ConfigModule;
import com.shmedo.mcloudapp.deviceconfig.model.DevcieCurrentState;
import com.shmedo.mcloudapp.deviceconfig.model.DevcieHistoryState;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.FirmWareInfo;
import com.shmedo.mcloudapp.deviceconfig.model.params.DispatchCmdParam;
import com.shmedo.mcloudapp.deviceconfig.model.params.DispatchRawCmdParam;
import com.shmedo.mcloudapp.deviceconfig.model.params.QueryCmdStateParam;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.AdvancedSettingActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceCurrentStateActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceHistoryDataAnalysisActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.das.DasCollectorSettingActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.BaseDialogFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.FirmWareSelectDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.BaseDispatchCmdDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.CommonCmdDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.DispatchCmdFailedDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.QueryCurrentStateDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.QueryTerminalTimeDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.TelemetryDialog;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrCode;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.NetworkConst;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;
import com.shmedo.mcloudapp.util.DateUtil;
import com.shmedo.mcloudapp.util.ResponseHandler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.RequestBody;

/**
 * 网络配置设备主页面
 */
public class NetConfigDeviceFragment extends BaseFragment {
    private static final String PRO_DEVICE_INFO = "com.shmedo.mcloudapp.PRO_DEVICE_INFO";

    private static final int REBOOT = 0x0002;
    private static final int SWITCH_TO_BLE = 0x0003;

    @BindView(R.id.tv_device_name)
    TextView mTvDeviceName;

    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSn;

    @BindView(R.id.tv_product_model)
    TextView mTvProductModel;

    @BindView(R.id.tv_time_or_sub_model)
    TextView mTvTime;

    @BindView(R.id.tv_device_state_flag)
    TextView mTvDeviceState;//通信状态(在线、离线)

    @BindView(R.id.tv_device_connect_operate)
    TextView mTvDeviceConnectOperate;//蓝牙连接状态(断开连接、重新连接)

    @BindView(R.id.tv_device_communication_way_switch)
    TextView mTvDeviceCommunicationWaySwitch;//通信方式(网络、蓝牙)

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private ConfigModuleAdapter moduleAdapter;

    private List<ConfigModule> configModuleList = new ArrayList<>();
    private ProjectDeviceInfo projectDeviceInfo;

    private ConfigModule selectedConfigModule;
    private List<String> msgIDList = new ArrayList<>();


    public static NetConfigDeviceFragment newInstance(ProjectDeviceInfo projectDeviceInfo) {
        NetConfigDeviceFragment fragment = new NetConfigDeviceFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, projectDeviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectDeviceInfo = getArguments().getParcelable(PRO_DEVICE_INFO);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(List<DispatchCmdItem> dispatchCmdItemList) {
        dismissProgressDialog();
        //判断此页面是否处于前台
        if (!isActive) {
            return;
        }

        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            showDispatchFailedDialog();
            return;
        }

        msgIDList.clear();
        for (DispatchCmdItem cmdItem : dispatchCmdItemList) {
            msgIDList.add(cmdItem.getMsgID());
        }
        showDispatchSuccessDialog();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_net_work_config_device;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHeadInfo();
        initAdapter();
        initConfigModuleData();
        queryCmdState();
    }

    private void setHeadInfo() {
        if (projectDeviceInfo != null) {
            mTvDeviceName.setText("物联网数据采集器");
            mTvDeviceSn.setText(String.format("设备编号：%s", TextUtils.isEmpty(projectDeviceInfo.getToken()) ? "" : projectDeviceInfo.getToken()));
            mTvProductModel.setText(String.format("产品型号：%s", TextUtils.isEmpty(projectDeviceInfo.getDeviceTypeName()) ? "" : projectDeviceInfo.getDeviceTypeName()));
            mTvTime.setText(String.format("更新时间：%s", TextUtils.isEmpty(projectDeviceInfo.getLastActiveTime()) ? "" : projectDeviceInfo.getLastActiveTime()));
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
        mTvDeviceConnectOperate.setVisibility(View.INVISIBLE);
        mTvDeviceCommunicationWaySwitch.setText("蓝牙");
    }

    private void initAdapter() {
        int spanCount = 2;//跟布局里面的spanCount属性是一致的
        int spacing = DensityUtil.Dp2Px(mActivity, 15);//每一个矩形的间距
        mRecyclerView.setLayoutManager(new GridLayoutManager(mActivity, spanCount));
        //设置每个item间距
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));
        moduleAdapter = new ConfigModuleAdapter(configModuleList);
        moduleAdapter.setAnimationEnable(true);
        moduleAdapter.setAnimationFirstOnly(false);
        moduleAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                if (isDoubleClick(view)) {
                    return;
                }

                selectedConfigModule = configModuleList.get(position);
                processItemClick();
            }
        });
        mRecyclerView.setAdapter(moduleAdapter);
    }

    @OnClick({R.id.tv_device_communication_way_switch, R.id.rl_run_state_analysis})
    public void onClick(View v) {
        if (isDoubleClick(v)) {
            return;
        }

        switch (v.getId()) {
            case R.id.tv_device_communication_way_switch://切换连接方式
                showWarnDialog("连接方式", "确定切换至蓝牙连接？", SWITCH_TO_BLE);
                break;

            case R.id.rl_run_state_analysis:
                DeviceHistoryDataAnalysisActivity.startActivity(mActivity, projectDeviceInfo);
                break;
        }
    }

    private void processItemClick() {
        switch (selectedConfigModule.getName()) {
            case "状态":
            case "时间":
            case "遥测":
                processDispatchCommonCmd();
                break;

            case "重启":
                showWarnDialog("温馨提示", "确定重启设备吗？", REBOOT);
                break;

            case "固件升级":
                FirmWareSelectDialog newFragment = new FirmWareSelectDialog(MCloudApp.getCompanyID(), projectDeviceInfo.getDeviceTypeID());
                newFragment.setDialogFragmentClickListener(listener);
                newFragment.show(getChildFragmentManager(), "dialog");
                break;

            case "采集器配置":
                DasCollectorSettingActivity.startActivity(mActivity, projectDeviceInfo.getId());
                break;

            case "设置":
                AdvancedSettingActivity.startActivity(mActivity, projectDeviceInfo, AppContants.DeviceType.DAS);
                break;

        }
    }

    private void processDispatchCommonCmd() {
        DispatchCmdParam dispatchCmdParam = new DispatchCmdParam();
        dispatchCmdParam.setCmdID(selectedConfigModule.getCmdID());
        dispatchCmdParam.setCompanyID(MCloudApp.getCompanyID());
        dispatchCmdParam.setDeviceIDList(Arrays.asList(projectDeviceInfo.getId()));

        showProgressDialog("指令下发中...");
        DispatchCmdHelper.getInstance().processDispatchCmd(dispatchCmdParam);
    }

    private BaseDialogFragment.DialogFragmentClickListener listener = new BaseDialogFragment.DialogFragmentClickListener<FirmWareInfo>() {
        @Override
        public boolean onPositiveClick(View view, FirmWareInfo firmWareInfo) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("$cmd=md_upgrade");
            stringBuilder.append("&url=");
            stringBuilder.append(firmWareInfo.getFwPath());
            stringBuilder.append("&md5=");
            stringBuilder.append(firmWareInfo.getFwMd5());
            stringBuilder.append("&size=");
            stringBuilder.append(firmWareInfo.getFwSize());

            DispatchRawCmdParam param = new DispatchRawCmdParam();
            param.setContent(stringBuilder.toString());
            param.setCompanyID(MCloudApp.getCompanyID());
            param.setDeviceIDList(Arrays.asList(projectDeviceInfo.getId()));

            showProgressDialog("指令下发中...");
            DispatchCmdHelper.getInstance().processDispatchRawCmd(param);
            return true;
        }


        @Override
        public void onNegativeClick(View view) {

        }
    };

    private void initConfigModuleData() {
        configModuleList.clear();
        ConfigModule configModule = new ConfigModule(R.drawable.ic_device_current_state, 5, GlobalUtil.getString(R.string.device_config_module_current_state), "获取当前设备状态");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_current_time, 1, "时间", "获取当前设备时间");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_telemetry, 6, "遥测", "远距离测量");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_reboot, 7, "重启", "重新启动当前设备");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_firmware_upgrade, 24, "固件升级", "版本:--");
        configModuleList.add(configModule);

        // TODO(设备暂不支持网络配置)
        //DAS具有采集器配置项
//        if (projectDeviceInfo.getDeviceTypeID() == 4) {
//            configModule = new ConfigModule(R.drawable.ic_device_collector_config, "采集器配置", "采集器参数配置");
//            configModuleList.add(configModule);
//        }

        // TODO(下次版本迭代再开发)
//        configModule = new ConfigModule(R.drawable.ic_device_instruction_send, "指令下发", "服务端代码指令下发");
//        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_setting, "设置", "高级设置");
        configModuleList.add(configModule);
    }

    /**
     * 查询设备状态历史
     */
    private void queryCmdState() {
        showProgressDialog("加载中...");

        String end = TextUtils.isEmpty(projectDeviceInfo.getLastActiveTime()) ? DateUtil.getNowDateString() : projectDeviceInfo.getLastActiveTime();
        Date beginDate = DateUtil.getBackOrAddDate2(DateUtil.stringToDate(end, "yyyy-MM-dd HH:mm:ss"), -10);
        String begin = DateUtil.DateToStrFormat(beginDate, "yyyy-MM-dd HH:mm:ss");

        QueryCmdStateParam parameter = new QueryCmdStateParam();
        parameter.setCompanyID(MCloudApp.getCompanyID());
        parameter.setDeviceID(projectDeviceInfo.getId());
        parameter.setBegin(begin);
        parameter.setEnd(end);
        parameter.setPageSize(5);
        parameter.setCurrentPage(1);

        String json = GsonFactory.getGson().toJson(parameter);
        RequestBody body = RequestBody.create(NetworkConst.JSON_TYPE, json);
        MDRetrofit.getInstance()
                .createService()
                .QueryCmdState(MCloudApp.getAccessToken(), body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<PageResult<DevcieHistoryState>>() {
                    @Override
                    protected void onResponse(PageResult<DevcieHistoryState> data, ErrCode errCode) {
                        dismissProgressDialog();
                        if (!ResponseHandler.getInstance().handleResponse(errCode)) {
                            if (errCode.getCode() == 0) {
                                if (data == null || data.getCurrentPageData() == null || data.getCurrentPageData().size() == 0) {
                                    return;
                                }
                                updateConfigModuleData(data.getCurrentPageData().get(0));
                            } else {
                                if (!TextUtils.isEmpty(errCode.getErrMessage())) {
                                    ToastUtils.show(errCode.getErrMessage());
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissProgressDialog();
                        ResponseHandler.getInstance().handleFailure((Exception) e);
                    }
                });
    }

    /**
     * 更新设备功能模块显示的信息
     *
     * @param devcieHistoryState
     */
    private void updateConfigModuleData(DevcieHistoryState devcieHistoryState) {
        if (devcieHistoryState == null) {
            return;
        }

        String firmwareVersion = TextUtils.isEmpty(devcieHistoryState.getSwVersion()) ? "--" : devcieHistoryState.getSwVersion();
        for (ConfigModule configModule : configModuleList) {
            if (configModule.getName().equals("固件升级")) {
                configModule.setDesc("版本:" + firmwareVersion);
                break;
            }
        }
        moduleAdapter.notifyDataSetChanged();
    }

    /**
     * 指令下发失败弹框
     */
    private void showDispatchFailedDialog() {
        String title = selectedConfigModule.getName();
        switch (selectedConfigModule.getName()) {
            case "状态":
                title = "运行状态";
                break;

            case "时间":
                title = "终端时间";
                break;

            case "遥测":
                title = "遥测";
                break;

            case "重启":
                title = "重新启动";
                break;
        }
        BaseDispatchCmdDialog newFragment = new DispatchCmdFailedDialog(title);
        newFragment.show(getChildFragmentManager(), "dialog");
    }

    /**
     * 指令下发成功弹框
     */
    private void showDispatchSuccessDialog() {
        BaseDispatchCmdDialog newFragment = null;
        //下发指令成功，弹出对话框开始轮询查询指令响应
        switch (selectedConfigModule.getName()) {
            case "状态":
                newFragment = new QueryCurrentStateDialog("运行状态", msgIDList);
                ((QueryCurrentStateDialog) newFragment).setOnSeeDetailClickListener(new QueryCurrentStateDialog.OnSeeDetailClickListener() {
                    @Override
                    public void onSeeDetailClick(DevcieCurrentState devcieCurrentState) {
                        DeviceCurrentStateActivity.startActivity(mActivity, projectDeviceInfo, devcieCurrentState, AppContants.DeviceType.DAS);
                    }
                });
                break;

            case "时间":
                newFragment = new QueryTerminalTimeDialog("终端时间", msgIDList);
                break;

            case "遥测":
                newFragment = new TelemetryDialog("遥测", msgIDList);
                break;

            case "重启":
                newFragment = new CommonCmdDialog("重新启动", "正在重启中...", "预计耗时三分钟,请耐心等待", msgIDList);
                break;

            case "固件升级":
                newFragment = new CommonCmdDialog("固件升级", "固件升级中...", "此过程耗时较长,请耐心等待", msgIDList);
                break;
        }
        if (newFragment != null)
            newFragment.show(getChildFragmentManager(), "dialog");
    }

    /**
     * 危险操作前弹框提醒
     */
    private void showWarnDialog(String title, String content, int operateType) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(mActivity)
                .title(title)
                .content(content)
                .contentColorRes(R.color.title_text_color)
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .positiveColorRes(R.color.blue_52B4F8)
                .negativeColorRes(R.color.sub_title_text_color)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        switch (operateType) {
                            case REBOOT:
                                processDispatchCommonCmd();
                                break;

                            case SWITCH_TO_BLE:
                                DeviceModuleSwitchTabEvent switchTabEvent = new DeviceModuleSwitchTabEvent(1);
                                EventBus.getDefault().post(switchTabEvent);
                                mActivity.finish();
                                break;
                        }
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        switch (operateType) {
                            case REBOOT:
                                break;

                            case SWITCH_TO_BLE:
                                break;
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }
}
