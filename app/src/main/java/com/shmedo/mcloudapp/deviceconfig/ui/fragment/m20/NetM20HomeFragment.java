package com.shmedo.mcloudapp.deviceconfig.ui.fragment.m20;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.m20.M20BaseInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.ConfigModuleAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.ConfigModule;
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.model.params.DispatchRawCmdParam;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.AdvancedSettingActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DataCenterHomeActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DeviceCurrentStateActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.BaseDispatchCmdDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.DispatchCmdFailedDialog;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import timber.log.Timber;

public class NetM20HomeFragment extends BaseNetIotCommunicateFragment {
    public static final String EXTRA_DEVICE = "com.shmedo.mcloudapp.EXTRA_DEVICE";

    @BindView(R.id.progress_overlay)
    View progressOverlay;

    @BindView(R.id.tv_progress_text)
    TextView mTvProgressText;

    @BindView(R.id.tv_device_name)
    TextView mTvDeviceName;//设备名称

    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSn;//设备SN号

    @BindView(R.id.tv_product_model)
    TextView mTvProductModel;//产品型号

    @BindView(R.id.tv_time_or_sub_model)
    TextView mTvFirmwareVersion;//固件版本

    @BindView(R.id.tv_platform_communication_state)
    TextView mTvPlatformCommunicationState;//与平台连接状态

    @BindView(R.id.tv_device_state_flag)
    TextView mTvDeviceState;//(在线、离线)

    @BindView(R.id.tv_device_connect_operate)
    TextView mTvDeviceConnectOperate;//蓝牙连接操作(断开连接、重新连接)

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private ConfigModuleAdapter moduleAdapter;
    private List<ConfigModule> configModuleList = new ArrayList<>();
    private ConfigModule selectedConfigModule;

    public ProjectDeviceInfo projectDeviceInfo;
    private M20BaseInfo m20BaseInfo;

    private NetM20SetupWizardDialogFragment setupWizardDialogFragment;


    public static NetM20HomeFragment newInstance(ProjectDeviceInfo projectDeviceInfo) {
        NetM20HomeFragment fragment = new NetM20HomeFragment();
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
        return R.layout.net_m20_home_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHeadInfo();
        initAdapter();
        initConfigModuleData();
        showProgressDialog("处理中...");
        queryBaseInfo();
    }

    private void setHeadInfo() {
        if (projectDeviceInfo != null) {
            mTvDeviceName.setText("普适型GNSS一体机");
            mTvDeviceSn.setText(String.format("设备编号：%s", TextUtils.isEmpty(projectDeviceInfo.getToken()) ? "" : projectDeviceInfo.getToken()));
            mTvProductModel.setText(String.format("产品型号：%s", TextUtils.isEmpty(projectDeviceInfo.getDeviceTypeName()) ? "M20" : projectDeviceInfo.getDeviceTypeName()));
            mTvFirmwareVersion.setText(String.format("固件版本：%s", TextUtils.isEmpty(projectDeviceInfo.getFirmwareVersion()) ? "" : projectDeviceInfo.getFirmwareVersion()));
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
                selectedConfigModule = configModuleList.get(position);
                processItemClick();
            }
        });
        mRecyclerView.setAdapter(moduleAdapter);
    }

    private void processItemClick() {
        switch (selectedConfigModule.getName()) {
            case "设置向导":
                setupWizardDialogFragment = NetM20SetupWizardDialogFragment.newInstance();
                setupWizardDialogFragment.show(getChildFragmentManager(), "dialog");
                break;

            case "状态":
                showProgressDialog("处理中...");
                String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.QUERY_DEVICE_STATUS);
                doCommonDispatchRawCmd(command);
                break;

            case "数据中心":
                DataCenterHomeActivity.startActivity(mActivity, AppContants.DeviceType.M20, projectDeviceInfo, AppContants.DataCenterConfigMethod.BASIC_CONFIG);
                break;

            case "设置":
                AdvancedSettingActivity.startActivity(mActivity, projectDeviceInfo, AppContants.DeviceType.M20);
                break;
        }
    }

    private void initConfigModuleData() {
        configModuleList.clear();

        ConfigModule configModule = new ConfigModule(R.drawable.ic_setup_wizard, "设置向导", "一键配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_current_state, 5, "状态", "获取当前设备状态");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_data_center, "数据中心", "基础参数配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_setting, "设置", "高级设置");
        configModuleList.add(configModule);
    }

    /**
     * 获取设备的基本信息
     */
    private void queryBaseInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.M20_MD_GET_BASE_INFO);
        doCommonDispatchRawCmd(command);
    }

    /**
     * 水平初始化
     */
    public void setLevelInitial() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.M20_MD_LEVEL_INITIAL);
        doCommonDispatchRawCmd(command);
    }

    /**
     * 调用指令透传接口
     *
     * @param content
     */
    private void doCommonDispatchRawCmd(String content) {
        DispatchRawCmdParam rawCmdParam = new DispatchRawCmdParam();
        rawCmdParam.setContent(content);
        rawCmdParam.setCompanyID(MCloudApp.getCompanyID());
        rawCmdParam.setDeviceIDList(Arrays.asList(projectDeviceInfo.getId()));

        processDispatchRawCmd(rawCmdParam);
    }

    @Override
    protected void onDispatchCmdResult(List<DispatchCmdItem> dispatchCmdItemList, String cmdStr) {
//        dismissProgressDialog();
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            dismissProgressDialog();
            showDispatchFailedDialog(cmdStr);
            return;
        }

        msgIDList.clear();
        for (DispatchCmdItem cmdItem : dispatchCmdItemList) {
            msgIDList.add(cmdItem.getMsgID());
        }
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case QUERY_DEVICE_STATUS:
            case M20_MD_LEVEL_INITIAL:
                dismissProgressDialog();
                showDispatchSuccessDialog();
                break;

            case M20_MD_GET_BASE_INFO: {
                if (msgIDList != null && msgIDList.size() > 0) {
                    startQueryCmdResponseRunnable(2000);
                }
            }
            break;

            default:
                break;
        }
    }

    /**
     * 指令下发失败弹框
     */
    private void showDispatchFailedDialog(String cmdStr) {
        String title;
        BaseDispatchCmdDialog newFragment = null;
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case QUERY_DEVICE_STATUS:
                title = "运行状态";
                newFragment = new DispatchCmdFailedDialog(title);
                break;

            case M20_MD_LEVEL_INITIAL:
                if (setupWizardDialogFragment != null && setupWizardDialogFragment.isVisible()) {
                    setupWizardDialogFragment.updateDispatchCmdResult(false, null);
                }
                break;

            case M20_MD_GET_BASE_INFO:
                break;

            default:
                break;
        }
        if (newFragment != null)
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
//                newFragment = new QueryCurrentStateDialog("运行状态", msgIDList);
//                ((QueryCurrentStateDialog) newFragment).setOnSeeDetailClickListener(new QueryCurrentStateDialog.OnSeeDetailClickListener() {
//                    @Override
//                    public void onSeeDetailClick(DevcieCurrentState devcieCurrentState) {
//                        DeviceCurrentStateActivity.startActivity(mActivity, projectDeviceInfo, devcieCurrentState, AppContants.DeviceType.M20);
//                    }
//                });
                DeviceCurrentStateActivity.startActivity(mActivity, projectDeviceInfo, null, AppContants.DeviceType.M20);
                break;

            case "设置向导":
                if (setupWizardDialogFragment != null && setupWizardDialogFragment.isVisible()) {
                    setupWizardDialogFragment.updateDispatchCmdResult(true, msgIDList);
                }
                break;
        }
        if (newFragment != null)
            newFragment.show(getChildFragmentManager(), "dialog");
    }

    /**
     * 查询指令响应结果出错
     *
     * @param errMsg
     */
    @Override
    protected void onQueryCmdResponseResultError(String errMsg) {
        super.onQueryCmdResponseResultError(errMsg);
        ToastUtils.show("查询设备响应错误");
    }

    /**
     * 查询指令响应结果超时
     *
     * @param queryCmdResult
     */
    @Override
    protected void onQueryCmdResponseResultTimeOut(QueryCmdResult queryCmdResult) {
        super.onQueryCmdResponseResultTimeOut(queryCmdResult);
        ToastUtils.show("查询设备响应超时");
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
            case M20_MD_GET_BASE_INFO: {//获取设备的基本信息
                IOTCommandResult<M20BaseInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取设备的基本信息出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                m20BaseInfo = commandResult.getResult();
                updateHeadInfo();
            }
            break;

            default:
                break;
        }
    }

    /**
     * 更新头部信息
     */
    private void updateHeadInfo() {
        try {
            if (m20BaseInfo != null) {
                mTvFirmwareVersion.setText(String.format("固件版本：%s", m20BaseInfo.getFirversion()));
            } else {
                mTvFirmwareVersion.setText("固件版本：--");
            }
            mTvPlatformCommunicationState.setText("平台连接状态：--");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}