package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

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
import com.blankj.utilcode.util.ConvertUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.TerminalSNEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.vms.VmsTerminalInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.recycleviewitemdivider.GridSpacingItemDecoration;
import com.shmedo.mcloudapp.deviceconfig.adapter.ConfigModuleAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.ConfigModule;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.vms.VmsTerminalExternalSensorHomeActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.vms.VmsTerminalParamSettingActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.BaseDialogFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.BaseDispatchCmdDialog;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.netcmd.TelemetryDialog;
import com.shmedo.mcloudapp.util.DateUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/19 <br/>
 * 描述：     Vms 网关挂载的终端设备主页面
 */
public class TcpVmsTerminalHomeFragment extends BaseVmsTcpCommunicateFragment {
    private static final String TERMINAL_INFO = "terminal_info";
    private static final int REBOOT = 0x0002;

    @BindView(R.id.tv_device_name)
    TextView mTvDeviceName;//设备名称

    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSn;//设备SN号

    @BindView(R.id.tv_product_name)
    TextView mTvProductName;//所属产品

    @BindView(R.id.tv_firmware_version)
    TextView mTvFirmwareVersion;//固件版本

    @BindView(R.id.tv_extended_field3)
    TextView mTvTime;//接入网关时间

    @BindView(R.id.tv_platform_communication_state)
    TextView mTvPlatformCommunicationState;//与米度平台连接状态

    @BindView(R.id.tv_device_state_flag)
    TextView mTvDeviceState;//通信状态(在线、离线)

    @BindView(R.id.tv_device_connect_operate)
    TextView mTvDeviceConnectOperate;

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    private ConfigModuleAdapter moduleAdapter;
    private List<ConfigModule> configModuleList = new ArrayList<>();
    private ConfigModule selectedConfigModule;

    private VmsTerminalInfo vmsTerminalInfo;

    public static TcpVmsTerminalHomeFragment newInstance(VmsTerminalInfo vmsTerminalInfo) {
        TcpVmsTerminalHomeFragment fragment = new TcpVmsTerminalHomeFragment();
        Bundle args = new Bundle();
        args.putParcelable(TERMINAL_INFO, vmsTerminalInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            vmsTerminalInfo = getArguments().getParcelable(TERMINAL_INFO);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.universal_config_home_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initHeadInfo();
        initAdapter();
        initConfigModuleData();
    }

    private void initHeadInfo() {
        if (vmsTerminalInfo != null) {
            if (vmsTerminalInfo.getSn().toUpperCase().endsWith("D")) {
                mTvDeviceName.setText("振弦式采集器");
            } else if (vmsTerminalInfo.getSn().toUpperCase().endsWith("F")) {
                mTvDeviceName.setText("数字式采集器");
            } else if (vmsTerminalInfo.getSn().toUpperCase().endsWith("Y")) {
                mTvDeviceName.setText("雨量计");
            } else if (vmsTerminalInfo.getSn().toUpperCase().endsWith("W")) {
                mTvDeviceName.setText("崩滑仪");
            }
            mTvDeviceSn.setText(String.format("设备SN号：%s", vmsTerminalInfo.getSn()));
            mTvFirmwareVersion.setText(String.format("固件版本：%s", ""));
            mTvTime.setText(String.format("接入时间：%s", vmsTerminalInfo.getLogintime()));
            mTvDeviceState.setText(processTerminalState(vmsTerminalInfo.getLastpackagetime(), vmsTerminalInfo.getLogintime()));
            if (vmsTerminalInfo.getStatus() != 0) {
                mTvDeviceState.setTextColor(ContextCompat.getColor(mActivity, R.color.text_color_50E9B9));
                mTvDeviceState.setBackgroundResource(R.drawable.bg_device_online_state_flag);
            } else {
                mTvDeviceState.setTextColor(ContextCompat.getColor(mActivity, R.color.sub_title_text_color));
                mTvDeviceState.setBackgroundResource(R.drawable.bg_device_offline_state_flag);
            }
        }
        mTvProductName.setVisibility(View.GONE);
        mTvPlatformCommunicationState.setVisibility(View.GONE);
        mTvDeviceConnectOperate.setVisibility(View.GONE);
    }

    private String processTerminalState(String lastTime, String logintime) {
        if (TextUtils.isEmpty(lastTime)) {
            return "离线";
        }
        long lastPackageTime = DateUtil.stringToLong(lastTime, "yyyy/MM/dd HH:mm:ss");
        long loginTime = DateUtil.stringToLong(logintime, "yyyy/MM/dd HH:mm:ss");
        long phoneTime = new Date().getTime();
        if (((phoneTime - lastPackageTime) > 24 * 3600 * 1000) || (Math.abs(loginTime - lastPackageTime) < 2 * 1000)) {
            return "离线";
        } else {
            return "待机";
        }
    }

    private void initAdapter() {
        int spanCount = 2;//跟布局里面的spanCount属性是一致的
        int spacing = ConvertUtils.dp2px(15);//每一个矩形的间距
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
                    ToastUtils.show(StringUtils.getString(R.string.tcp_config_disconnect_warn));
                    return;
                }
                selectedConfigModule = configModuleList.get(position);
                processItemClick();
            }
        });
        mRecyclerView.setAdapter(moduleAdapter);
    }

    private void processItemClick() {
        BaseDialogFragment newFragment = null;
        switch (selectedConfigModule.getName()) {
            case "状态":
                newFragment = VmsTerminalCurrentStateDialog.newInstance(vmsTerminalInfo);
                newFragment.show(getChildFragmentManager(), "dialog");
                break;

            case "遥测":
                sampleTerminal();
                break;

            case "重启":
                showWarnDialog("温馨提示", "确定重启终端设备吗？", REBOOT);
                break;

            case "传感器配置":
                VmsTerminalExternalSensorHomeActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT, vmsTerminalInfo);
                break;

            case "终端配置":
                VmsTerminalParamSettingActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT, vmsTerminalInfo);
                break;
        }
    }

    /**
     * 遥测终端
     */
    private void sampleTerminal() {
        TerminalSNEntity entity = new TerminalSNEntity(vmsTerminalInfo.getSn());
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_TERMINAL_QUERY_SAMPLE, entity);
        sendCommand(command);
    }

    /**
     * 重启终端指令
     */
    private void rebootTerminal() {
        TerminalSNEntity entity = new TerminalSNEntity(vmsTerminalInfo.getSn());
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_MD_REBOOT_TERMINAL, entity);
        sendCommand(command);
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
                                rebootTerminal();
                                break;
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    private void initConfigModuleData() {
        configModuleList.clear();
        ConfigModule configModule = new ConfigModule(R.drawable.ic_device_current_state, "状态", "获取当前设备状态");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_telemetry, "遥测", "远距离测量");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_reboot, "重启", "重新启动当前设备");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_sensor_config, "传感器配置", "传感器参数配置");
        configModuleList.add(configModule);

        configModule = new ConfigModule(R.drawable.ic_device_sensor_config, "终端配置", "终端参数配置");
        configModuleList.add(configModule);
    }

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case VMS_TERMINAL_QUERY_SAMPLE: {//Vms终端遥测
                IOTCommandResult<String> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "遥测出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                BaseDispatchCmdDialog newFragment = new TelemetryDialog("遥测", commandResult.getResult());
                newFragment.show(getChildFragmentManager(), "dialog");
            }
            break;

            case VMS_MD_REBOOT_TERMINAL: {//重启Vms终端
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "发送重启指令失败!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                ToastUtils.show("终端设备稍后将重启");
            }
            break;

            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
    }
}