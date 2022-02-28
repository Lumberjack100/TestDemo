package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.StringUtils;
import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.IotLogOutputEntity;
import com.shmedo.configlibrary.iot.cmd.entity.ServerNumberEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.enums.ProductType;
import com.shmedo.configlibrary.iot.enums.ServerNumber;
import com.shmedo.configlibrary.iot.enums.VmsAisleNumber;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.DataCenterStatus;
import com.shmedo.configlibrary.iot.model.IotLogOutputInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DataCenterConfigActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.vms.VmsAisleSettingActivity;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/20 <br/>
 * 描述：     Vms 网关高级设置页面
 */
public class TcpVmsAdvancedSettingsFragment extends BaseVmsTcpCommunicateFragment {
    private static final int VMS_REBOOT = 0x1000;
    private static final int VMS_RESET = 0x1001;

    @BindView(R.id.tv_data_center_one)
    TextView mTvDataCenterOne;

    @BindView(R.id.tv_data_center_two)
    TextView mTvDataCenterTwo;

    @BindView(R.id.tv_data_center_three)
    TextView mTvDataCenterThree;

    @BindView(R.id.tv_data_center_four)
    TextView mTvDataCenterFour;

    @BindView(R.id.tv_log_output)
    TextView mTvLogOutput;

    private static final int SERVER_NUMBER_ONE = 0x1001;
    private static final int SERVER_NUMBER_TWO = 0x1002;
    private static final int SERVER_NUMBER_THREE = 0x1003;
    private static final int SERVER_NUMBER_FOUR = 0x1004;
    private int serverNumber = -1;
    private ActivityResultLauncher<Intent> resultLauncher;

    private String logLevel;


    public static TcpVmsAdvancedSettingsFragment newInstance() {
        return new TcpVmsAdvancedSettingsFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.vms_advanced_settings_fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            refreshSpecifiedServerStatus();
                        }
                    }
                });
    }

    /**
     * 刷新指定的数据中心状态
     */
    private void refreshSpecifiedServerStatus() {
        startDefaultProgress("加载中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_10000_MILLIS);
        switch (serverNumber) {
            case SERVER_NUMBER_ONE:
                getDataCenterStatus(ServerNumber.NUMBER_ONE);
                break;

            case SERVER_NUMBER_TWO:
                getDataCenterStatus(ServerNumber.NUMBER_TWO);
                break;

            case SERVER_NUMBER_THREE:
                getDataCenterStatus(ServerNumber.NUMBER_THREE);
                break;

            case SERVER_NUMBER_FOUR:
                getDataCenterStatus(ServerNumber.NUMBER_FOUR);
                break;
        }
    }

   @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        serverNumber = -1;
        startDefaultProgress("加载中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_10000_MILLIS);
        getDataCenterStatus(ServerNumber.NUMBER_ONE);
    }

    @OnClick({R.id.dataCenterOneLayout, R.id.dataCenterTwoLayout, R.id.dataCenterThreeLayout, R.id.dataCenterFourLayout, R.id.vmsAisleOneLayout, R.id.vmsAisleTwoLayout, R.id.vmsAisleThreeLayout, R.id.vmsRebootLayout, R.id.vmsResetLayout, R.id.logOutputLayout})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        if (!tcpViewModel.getConnectStatus()) {
            ToastUtils.show(getString(R.string.tcp_config_disconnect_warn));
            return;
        }
        int id = view.getId();
        if (id == R.id.dataCenterOneLayout) {
            serverNumber = SERVER_NUMBER_ONE;
            DataCenterConfigActivity.startActivity(mActivity, resultLauncher, ProductType.VMS, AppContants.CommunicationWay.TCP_CONNECT, AppContants.DataCenterConfigMethod.ADVANCED_CONFIG, ServerNumber.NUMBER_ONE, mTvDataCenterOne.getText().toString());

        } else if (id == R.id.dataCenterTwoLayout) {
            serverNumber = SERVER_NUMBER_TWO;
            DataCenterConfigActivity.startActivity(mActivity, resultLauncher, ProductType.VMS, AppContants.CommunicationWay.TCP_CONNECT, AppContants.DataCenterConfigMethod.ADVANCED_CONFIG, ServerNumber.NUMBER_TWO, mTvDataCenterTwo.getText().toString());

        } else if (id == R.id.dataCenterThreeLayout) {
            serverNumber = SERVER_NUMBER_THREE;
            DataCenterConfigActivity.startActivity(mActivity, resultLauncher, ProductType.VMS, AppContants.CommunicationWay.TCP_CONNECT, AppContants.DataCenterConfigMethod.ADVANCED_CONFIG, ServerNumber.NUMBER_THREE, mTvDataCenterThree.getText().toString());

        } else if (id == R.id.dataCenterFourLayout) {
            serverNumber = SERVER_NUMBER_FOUR;
            DataCenterConfigActivity.startActivity(mActivity, resultLauncher, ProductType.VMS, AppContants.CommunicationWay.TCP_CONNECT, AppContants.DataCenterConfigMethod.ADVANCED_CONFIG, ServerNumber.NUMBER_FOUR, mTvDataCenterFour.getText().toString());

        } else if (id == R.id.vmsAisleOneLayout) {
            VmsAisleSettingActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT, VmsAisleNumber.NUMBER_ONE);

        } else if (id == R.id.vmsAisleTwoLayout) {
            VmsAisleSettingActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT, VmsAisleNumber.NUMBER_TWO);

        } else if (id == R.id.vmsAisleThreeLayout) {
            VmsAisleSettingActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT, VmsAisleNumber.NUMBER_THREE);

        } else if (id == R.id.vmsRebootLayout) {
            showWarnDialog("确定重启网关吗？", VMS_REBOOT);
        } else if (id == R.id.vmsResetLayout) {
            showWarnDialog("确定恢复出厂设置吗？", VMS_RESET);
        }else if (id == R.id.logOutputLayout) {
            showLogLevelDialog();
        }
    }

    /**
     * 获取网关数据中心状态
     */
    private void getDataCenterStatus(ServerNumber serverNumber) {
        ServerNumberEntity serverNumberEntity = new ServerNumberEntity(serverNumber.toInt());
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_GET_DATA_CENTER_STATUS, serverNumberEntity);
        sendCommand(command);
    }

    /**
     * 网关重启指令
     */
    private void rebootGateWay() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.REBOOT);
        sendCommand(command);
    }

    /**
     * 网关恢复出厂设置指令
     */
    private void resetGateWay() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.RESET);
        sendCommand(command);
    }

    /**
     * 查询日志输出等级
     */
    private void queryLogOutput() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.GET_LOG_OUTPUT_MODE_LEVEL);
        sendCommand(command);
    }

    /**
     * 设置日志输出等级
     */
    private void setLogOutput(IotLogOutputEntity entity) {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.SET_LOG_OUTPUT_MODE_LEVEL, entity);
        sendCommand(command);
    }

    /**
     * 危险操作前弹框提醒
     */
    private void showWarnDialog(String content, int operateType) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(mActivity)
                .title("温馨提示")
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
                            case VMS_REBOOT:
                                rebootGateWay();
                                break;

                            case VMS_RESET:
                                resetGateWay();
                                break;
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    /**
     * 选择日志输出等级
     */
    private void showLogLevelDialog() {
        final String[] logLevels = new String[]{"off", "info", "debug"};
        int pos = Arrays.asList(logLevels).indexOf(String.valueOf(logLevel));
        pos = pos == -1 ? 0 : pos;
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", logLevels,
                        null, pos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                logLevel = logLevels[position];
                                IotLogOutputEntity entity = new IotLogOutputEntity();
                                entity.setLevel(logLevel);
                                setLogOutput(entity);
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    @Override
    protected void customHandleMessage(@NonNull @NotNull Message msg) {
        switch (msg.what) {
            case AppContants.MsgWhat.MSG_DEFAULT:
                ToastUtils.show("响应超时,请稍后尝试");
                break;
        }
    }

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case MD_GET_DATA_CENTER_STATUS: {//获取Vms数据中心状态
                IOTCommandResult<DataCenterStatus> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    String errMsg = String.format("%s %s", "查询网关数据中心状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                DataCenterStatus centerStatus = commandResult.getResult();
                if (centerStatus.getCenterid() == 1) {
                    mTvDataCenterOne.setText(getStatusTextById(centerStatus.getStatus()));
                    mTvDataCenterOne.setTextColor(ColorUtils.getColor(getStatusColorResId(centerStatus.getStatus())));
                    //表示首次进入页面，需要逐个刷新所有的数据中心
                    if (serverNumber == -1) {
                        getDataCenterStatus(ServerNumber.NUMBER_TWO);
                    } else {
                        //表示刷新指定的数据中心
                        stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    }
                } else if (centerStatus.getCenterid() == 2) {
                    mTvDataCenterTwo.setText(getStatusTextById(centerStatus.getStatus()));
                    mTvDataCenterTwo.setTextColor(ColorUtils.getColor(getStatusColorResId(centerStatus.getStatus())));
                    //表示首次进入页面，需要逐个刷新所有的数据中心
                    if (serverNumber == -1) {
                        getDataCenterStatus(ServerNumber.NUMBER_THREE);
                    } else {
                        //表示刷新指定的数据中心
                        stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    }
                } else if (centerStatus.getCenterid() == 3) {
                    mTvDataCenterThree.setText(getStatusTextById(centerStatus.getStatus()));
                    mTvDataCenterThree.setTextColor(ColorUtils.getColor(getStatusColorResId(centerStatus.getStatus())));
                    //表示首次进入页面，需要逐个刷新所有的数据中心
                    if (serverNumber == -1) {
                        getDataCenterStatus(ServerNumber.NUMBER_FOUR);
                    } else {
                        //表示刷新指定的数据中心
                        stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    }
                } else if (centerStatus.getCenterid() == 4) {
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    mTvDataCenterFour.setText(getStatusTextById(centerStatus.getStatus()));
                    mTvDataCenterFour.setTextColor(ColorUtils.getColor(getStatusColorResId(centerStatus.getStatus())));
                    queryLogOutput();
                }
            }
            break;

            case REBOOT: {//重启网关
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", StringUtils.getString(R.string.reboot_failed), cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                ToastUtils.show(StringUtils.getString(R.string.device_reboot_tip));
                MCloudApp.getMainHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tcpViewModel.disconnect();
                    }
                }, 3000);
            }
            break;

            case RESET: {//恢复出厂设置
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", StringUtils.getString(R.string.reset_failed), cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                ToastUtils.show(StringUtils.getString(R.string.device_reset_tip));
                MCloudApp.getMainHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        tcpViewModel.disconnect();
                    }
                }, 3000);
            }
            break;

            case GET_LOG_OUTPUT_MODE_LEVEL: {//获取日志输出方式和等级
                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                IOTCommandResult<IotLogOutputInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询日志输出方式出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                IotLogOutputInfo logOutputInfo = commandResult.getResult();
                mTvLogOutput.setText(logOutputInfo.getLevel());
                logLevel = logOutputInfo.getLevel();
            }
            break;

            case SET_LOG_OUTPUT_MODE_LEVEL: {//设置日志输出方式和等级
                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置日志输出方式失败!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                ToastUtils.show("设置成功");
            }
            break;

            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
    }

    private String getStatusTextById(String statusId) {
        String status = "未知状态";
        if (statusId.equals("0")) {
            status = "未开启";
        } else if (statusId.equals("1")) {
            status = "已连接";
        } else if (statusId.equals("2")) {
            status = "未连接";
        }
        return status;
    }

    private int getStatusColorResId(String statusId) {
        int resId = R.color.device_unopened_platform;
        if (statusId.equals("0")) {
            resId = R.color.device_unopened_platform;
        } else if (statusId.equals("1")) {
            resId = R.color.text_color_3AD094;
        } else if (statusId.equals("2")) {
            resId = R.color.device_not_connected_platform;
        }
        return resId;
    }

}