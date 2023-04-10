package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.StringUtils;
import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.shmedo.configlibrary.ble.cmd.CommandManager;
import com.shmedo.configlibrary.ble.cmd.CommandResult;
import com.shmedo.configlibrary.ble.cmd.entity.DataCommunicateModeEntity;
import com.shmedo.configlibrary.ble.cmd.entity.DataReportIntervalEntity;
import com.shmedo.configlibrary.ble.cmd.entity.SixTargerBDNumberEntity;
import com.shmedo.configlibrary.ble.cmd.parser.ParseManager;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.model.BaseConfigInfo;
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.configlibrary.ble.utils.ValidateUtil;
import com.shmedo.configlibrary.iot.enums.ProductType;
import com.shmedo.configlibrary.iot.enums.ServerNumber;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.DataCenterConfigActivity;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 蓝牙配置数据中心
 */
public class BleDasDataCenterHomeFragment extends BaseBleCommunicateFragment {
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout mRefreshLayout;

    @BindView(R.id.tv_communication_method)
    TextView mTvCommunicationMethod;

    @BindView(R.id.reportingIntervalET)
    EditText mEtReportingInterval;

    @BindView(R.id.bdCardNumberET)
    EditText mEtBdCardNumber;

    @BindView(R.id.ll_bd_card_number)
    View bdCardNumberLayout;

    private String dataCommunicationMode;
    private String reportingInterval;
    private String bdCardNumber;
    private final String[] communicatModes = new String[]{"4G", "SMS", "BD", "BD+4G"};

    private ActivityResultLauncher<Intent> resultLauncher;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {

                        }
                    }
                });
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ble_das_data_center_home_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setFilter();
        initRefreshLayout();
        mRefreshLayout.setEnableLoadMore(false);
        //是否在刷新的时候禁止内容的一切手势操作（默认false）
        mRefreshLayout.setDisableContentWhenRefresh(true);
        mRefreshLayout.autoRefresh();
    }

    private void setFilter() {
        mEtReportingInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtBdCardNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});

        mTvCommunicationMethod.setText(communicatModes[0]);
        dataCommunicationMode = "1";
    }

    private void initRefreshLayout() {
        mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull @NotNull RefreshLayout refreshLayout) {
                if (!isConnected()) {
                    ToastUtils.show(StringUtils.getString(R.string.refresh_failed_while_device_disconnected));
                    mRefreshLayout.finishRefresh(false);
                    return;
                }
                queryData();
                startDefaultProgress(null, AppContants.MsgWhat.MSG_SMART_REFRESH, DELAY_10000_MILLIS);
            }
        });
    }

    private void queryData() {
        String baseInfoCommand = CommandManager.getInstance().getCommand(CommandType.BASE_CONFIG);
        sendCommand(baseInfoCommand);
        Timber.d("查询基础配置信息指令===%s", baseInfoCommand);
    }

    @OnClick({R.id.communicationMethodLayout, R.id.dataCenterOneLayout, R.id.dataCenterTwoLayout, R.id.dataCenterThreeLayout, R.id.btn_confirm})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.communicationMethodLayout:
                showCommunicateModeDialog();
                break;

            case R.id.dataCenterOneLayout:
                if (!isConnected()) {
                    ToastUtils.show(StringUtils.getString(R.string.ble_config_disconnect_warn));
                    return;
                }
                DataCenterConfigActivity.startActivity(mActivity, resultLauncher, ProductType.DAS, AppContants.CommunicationWay.BLE_CONNECT, ServerNumber.NUMBER_ONE, "");
                break;

            case R.id.dataCenterTwoLayout:
                if (!isConnected()) {
                    ToastUtils.show(StringUtils.getString(R.string.ble_config_disconnect_warn));
                    return;
                }
                DataCenterConfigActivity.startActivity(mActivity, resultLauncher, ProductType.DAS, AppContants.CommunicationWay.BLE_CONNECT, ServerNumber.NUMBER_TWO, "");
                break;

            case R.id.dataCenterThreeLayout:
                if (!isConnected()) {
                    ToastUtils.show(StringUtils.getString(R.string.ble_config_disconnect_warn));
                    return;
                }
                DataCenterConfigActivity.startActivity(mActivity, resultLauncher, ProductType.DAS, AppContants.CommunicationWay.BLE_CONNECT, ServerNumber.NUMBER_THREE, "");
                break;

            case R.id.btn_confirm:
                if (!isConnected()) {
                    ToastUtils.show(StringUtils.getString(R.string.ble_config_disconnect_warn));
                    return;
                }
                processSave();
                break;
            default:
                break;
        }
    }

    /**
     * 选择 通讯方式
     */
    private void showCommunicateModeDialog() {
        int pos = Arrays.asList(communicatModes).indexOf(String.valueOf(mTvCommunicationMethod.getText()));
        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", communicatModes,
                        null, pos,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                mTvCommunicationMethod.setText(text);
                                dataCommunicationMode = String.valueOf(position + 1);
                                if (position == 0 || position == 1) {
                                    bdCardNumberLayout.setVisibility(View.GONE);
                                } else {
                                    bdCardNumberLayout.setVisibility(View.VISIBLE);
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();

    }

    private void processSave() {
        commandItems.clear();
        DataCommunicateModeEntity communicateModeEntity = new DataCommunicateModeEntity(Integer.parseInt(dataCommunicationMode));
        String cmd = CommandManager.getInstance().getCommand(CommandType.DATA_MASSAGE_MODEL, communicateModeEntity);
        Timber.d("设置数据通讯模式===%s", cmd);
        commandItems.add(cmd);

        reportingInterval = mEtReportingInterval.getText().toString().trim();
        if (TextUtils.isEmpty(reportingInterval)) {
            ToastUtils.show("数据上报间隔参数不能为空");
            mEtReportingInterval.requestFocus();
            return;
        }
        if (Integer.parseInt(reportingInterval) < 1) {
            ToastUtils.show("数据上报间隔必须为正整数");
            mEtReportingInterval.requestFocus();
            return;
        }
        if (Integer.parseInt(reportingInterval) > 9999) {
            ToastUtils.show("数据上报间隔参数错误");
            mEtReportingInterval.requestFocus();
            return;
        }
        DataReportIntervalEntity intervalEntity = new DataReportIntervalEntity(Integer.parseInt(reportingInterval));
        cmd = CommandManager.getInstance().getCommand(CommandType.DATA_REPORT_INTERVAL, intervalEntity);
        Timber.d("设置数据上报间隔===%s", cmd);
        commandItems.add(cmd);

        if (dataCommunicationMode.equals("3") || dataCommunicationMode.equals("4")) {
            bdCardNumber = mEtBdCardNumber.getText().toString().trim();
            if (TextUtils.isEmpty(bdCardNumber)) {
                ToastUtils.show("北斗卡号不能为空");
                mEtBdCardNumber.requestFocus();
                return;
            }
            if (!ValidateUtil.isNumberSix(bdCardNumber)) {
                ToastUtils.show("北斗卡号参数错误");
                mEtBdCardNumber.requestFocus();
                return;
            }
            //设置六位目标北斗卡号
            SixTargerBDNumberEntity bdNumberEntity = new SixTargerBDNumberEntity(bdCardNumber);
            cmd = CommandManager.getInstance().getCommand(CommandType.SIX_TARGER_BD_NUMBER, bdNumberEntity);
            Timber.d("北斗配置参数===%s", cmd);
            commandItems.add(cmd);
        }

        startDefaultProgress("处理中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_20000_MILLIS);
        sendCommandFromCmdList(this::saveConfigInfoNoReboot);
    }

    @Override
    protected void parseResponseMessage(String cmdStr) {
        // TODO #gh# 屏蔽从其他页面返回到当前页面时，接收到其他页面的最后接收到的指令数据(LiveData事件)
        if (!isResumed()) {
            return;
        }
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        String tempStr = cmdStr.replace("$$", "").replace("\r\n", "");
        CommandType type = StringUtil.extractCommandType(cmdStr);
        switch (type) {
            case BASE_CONFIG://获取基础配置信息
                if (mRefreshLayout.isRefreshing()) {
                    mRefreshLayout.finishRefresh(true);
                }
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    Timber.e("查询基础配置信息指令出错!");
                    ToastUtils.show("查询基础配置信息指令出错!");
                    return;
                }
                CommandResult<BaseConfigInfo> bean = ParseManager.getInstance().parse(cmdStr);
                if (!bean.isSuccess()) {
                    return;
                }
                BaseConfigInfo baseConfigInfo = bean.getResult();
                int communicateMode = baseConfigInfo.getDataCommunicateMode().toInt();
                mTvCommunicationMethod.setText(communicatModes[communicateMode - 1]);
                dataCommunicationMode = String.valueOf(communicateMode);
                if (communicateMode == 3 || communicateMode == 4) {
                    bdCardNumberLayout.setVisibility(View.VISIBLE);
                } else {
                    bdCardNumberLayout.setVisibility(View.GONE);
                }
                int reportInterval = baseConfigInfo.getDataReportInterval();
                reportingInterval = String.valueOf(reportInterval);
                bdCardNumber = baseConfigInfo.getTargetBGNum();
                mEtReportingInterval.setText(reportingInterval);
                mEtBdCardNumber.setText(bdCardNumber);
                break;

            case DATA_MASSAGE_MODEL://设置数据通讯方式
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("数据通讯方式配置错误!");
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                sendCommandFromCmdList(this::saveConfigInfoNoReboot);
                break;

            case DATA_REPORT_INTERVAL://设置数据上报间隔
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("数据上报配置错误!");
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                sendCommandFromCmdList(this::saveConfigInfoNoReboot);
                break;

            case SIX_TARGER_BD_NUMBER://北斗配置
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("北斗配置错误!");
                    stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                    return;
                }
                sendCommandFromCmdList(this::saveConfigInfoNoReboot);
                break;

            case SAVE_CONFIG_INFO:
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("保存参数指令错误!");
                    return;
                }
                ToastUtils.show("保存成功");
                break;

            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
    }

    @Override
    protected void customHandleMessage(@NonNull @NotNull Message msg) {
        switch (msg.what) {
            case AppContants.MsgWhat.MSG_SMART_REFRESH:
                if (mRefreshLayout.isRefreshing()) {
                    mRefreshLayout.finishRefresh(false);
                    ToastUtils.show("刷新超时");
                }
                break;

            case AppContants.MsgWhat.MSG_DEFAULT:
                ToastUtils.show("响应超时,请稍后尝试");
                break;
        }
    }

    @Override
    public boolean onBackPressed() {
        if (isConnected()) {
            if (checkValueIsChange()) {
                warnNotYetSettingBeforeLeavePage();
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    private boolean checkValueIsChange() {
//        if (dataCommunicationModeOld != null && dataCommunicationMode != null && !dataCommunicationModeOld.equals(dataCommunicationMode)) {
//            return true;
//        }
//        if (reportingInterval != null && !reportingInterval.equals(mEtReportingInterval.getText().toString().trim())) {
//            return true;
//        }
//        if (bdCardNumber != null && !bdCardNumber.equals(mEtBdCardNumber.getText().toString().trim())) {
//            return true;
//        }
        return false;
    }
}
