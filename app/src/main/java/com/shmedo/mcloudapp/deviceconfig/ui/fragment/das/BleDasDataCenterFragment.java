package com.shmedo.mcloudapp.deviceconfig.ui.fragment.das;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.ble.cmd.CommandManager;
import com.shmedo.configlibrary.ble.cmd.CommandResult;
import com.shmedo.configlibrary.ble.cmd.entity.DataCommunicateModeEntity;
import com.shmedo.configlibrary.ble.cmd.entity.DataReportIntervalEntity;
import com.shmedo.configlibrary.ble.cmd.entity.SixTargerBDNumberEntity;
import com.shmedo.configlibrary.ble.cmd.parser.ParseManager;
import com.shmedo.configlibrary.ble.enums.CommandType;
import com.shmedo.configlibrary.ble.enums.ServerNumber;
import com.shmedo.configlibrary.ble.model.BaseConfigInfo;
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.configlibrary.ble.utils.ValidateUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.das.DataCenterServerConfigActivity;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 蓝牙配置数据中心
 */
public class BleDasDataCenterFragment extends TestBaseBleCommunicateFragment {
    @BindView(R.id.tv_communication_method)
    TextView mTvCommunicationMethod;

    @BindView(R.id.reportingIntervalET)
    EditText mEtReportingInterval;

    @BindView(R.id.bdCardNumberET)
    EditText mEtBdCardNumber;

    @BindView(R.id.ll_bd_card_number)
    View bdCardNumberLayout;

    private int pos = 1;
    private String dataCommunicationModeOld;
    private String dataCommunicationMode;
    private String reportingInterval;
    private String bdCardNumber;

    private String cmdDataReport;//数据上报间隔
    private String cmdBDCardNumber;//北斗卡号

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_ble_das_data_center;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setFilter();
        queryData();
    }

    private void setFilter() {
        mEtReportingInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtBdCardNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
    }

    private void queryData() {
        errMsg = "查询数据超时,请稍后尝试";
        startProgressRunnable("正在获取参数...", 25000);

        String baseInfoCommand = CommandManager.getInstance().getCommand(CommandType.BASE_CONFIG);
        sendCommand(baseInfoCommand);
        Timber.d("查询基础配置信息指令===%s", baseInfoCommand);
    }


    @OnClick({R.id.communicationMethodLayout, R.id.centerOneLayout, R.id.centerTwoLayout, R.id.centerThreeLayout, R.id.btn_confirm})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.communicationMethodLayout:
                XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
                new XPopup.Builder(mActivity)
                        .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                        .asBottomList("", new String[]{"4G", "SMS", "BD", "BD+4G"},
                                null, pos, true,
                                new OnSelectListener() {
                                    @Override
                                    public void onSelect(int position, String text) {
                                        pos = position;
                                        mTvCommunicationMethod.setText(text);

                                        switch (text) {
                                            case "4G":
                                                dataCommunicationMode = "1";
                                                bdCardNumberLayout.setVisibility(View.GONE);
                                                break;

                                            case "SMS":
                                                dataCommunicationMode = "2";
                                                bdCardNumberLayout.setVisibility(View.GONE);
                                                break;

                                            case "BD":
                                                dataCommunicationMode = "3";
                                                bdCardNumberLayout.setVisibility(View.VISIBLE);
                                                break;

                                            case "BD+4G":
                                                dataCommunicationMode = "4";
                                                bdCardNumberLayout.setVisibility(View.VISIBLE);
                                                break;
                                        }
                                    }
                                }, 0, R.layout.custom_xpopup_adapter_text_match)
                        .show();
                break;

            case R.id.centerOneLayout:
                if (!isConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    return;
                }
                DataCenterServerConfigActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, ServerNumber.NUMBER_ONE);
                break;

            case R.id.centerTwoLayout:
                if (!isConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    return;
                }
                DataCenterServerConfigActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, ServerNumber.NUMBER_TWO);
                break;

            case R.id.centerThreeLayout:
                if (!isConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    return;
                }
                DataCenterServerConfigActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT, ServerNumber.NUMBER_THREE);
                break;

            case R.id.btn_confirm:
                if (!isConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    return;
                }
                processSave();
                break;
            default:
                break;
        }
    }

    private void processSave() {
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
            cmdBDCardNumber = CommandManager.getInstance().getCommand(CommandType.SIX_TARGER_BD_NUMBER, bdNumberEntity);
        }

        DataReportIntervalEntity intervalEntity = new DataReportIntervalEntity(Integer.parseInt(reportingInterval));
        cmdDataReport = CommandManager.getInstance().getCommand(CommandType.DATA_REPORT_INTERVAL, intervalEntity);

        errMsg = "发送指令超时,请稍后尝试";
        startProgressRunnable("正在发送配置指令...", CONFIG_PARAMS_DELAY_MILLIS);
        DataCommunicateModeEntity communicateModeEntity = new DataCommunicateModeEntity(Integer.parseInt(dataCommunicationMode));
        String cmd = CommandManager.getInstance().getCommand(CommandType.DATA_MASSAGE_MODEL, communicateModeEntity);
        sendCommand(cmd);
        Timber.d("设置数据通讯模式===%s", cmd);
    }

    @Override
    protected void parseResponseMessage(String cmdStr) {
        if (!isActive) {
            return;
        }
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        String tempStr = cmdStr.replace("$$", "").replace("\r\n", "");
        CommandType type = StringUtil.extractCommandType(cmdStr);
        switch (type) {
            case BASE_CONFIG://获取基础配置信息
                stopProgressRunnable();
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
                pos = communicateMode - 1;
                switch (communicateMode) {
                    case 1:
                        mTvCommunicationMethod.setText("4G");
                        dataCommunicationModeOld = "1";
                        dataCommunicationMode = "1";
                        bdCardNumberLayout.setVisibility(View.GONE);
                        break;
                    case 2:
                        mTvCommunicationMethod.setText("SMS");
                        dataCommunicationModeOld = "2";
                        dataCommunicationMode = "2";
                        bdCardNumberLayout.setVisibility(View.GONE);
                        break;
                    case 3:
                        mTvCommunicationMethod.setText("BD");
                        dataCommunicationModeOld = "3";
                        dataCommunicationMode = "3";
                        bdCardNumberLayout.setVisibility(View.VISIBLE);
                        break;
                    case 4:
                        mTvCommunicationMethod.setText("BD+4G");
                        dataCommunicationModeOld = "4";
                        dataCommunicationMode = "4";
                        bdCardNumberLayout.setVisibility(View.VISIBLE);
                        break;
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
                    stopProgressRunnable();
                    return;
                }
                sendCommand(cmdDataReport);
                Timber.d("设置数据上报间隔===%s", cmdDataReport);
                break;

            case DATA_REPORT_INTERVAL://设置数据上报间隔
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("数据上报配置错误!");
                    stopProgressRunnable();
                    return;
                }
                if (dataCommunicationMode.equals("3") || dataCommunicationMode.equals("4")) {
                    sendCommand(cmdBDCardNumber);
                    Timber.d("北斗配置参数===%s", cmdBDCardNumber);
                    return;
                }
                doAfterSetting();
                break;

            case SIX_TARGER_BD_NUMBER://北斗配置
                stopProgressRunnable();
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("北斗配置错误!");
                    return;
                }
                doAfterSetting();
                break;

            case SAVE_CONFIG_INFO:
                if (tempStr.endsWith(CommandResult.ERROR_END)) {
                    ToastUtils.show("保存参数指令错误!");
                    return;
                }
                Toast.makeText(getActivity(), "已保存", Toast.LENGTH_LONG).show();
                dataCommunicationModeOld = dataCommunicationMode;
                break;

            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
    }

    private void doAfterSetting() {
        stopProgressRunnable();
        saveConfigInfoNoReboot();
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
        if (dataCommunicationModeOld != null && dataCommunicationMode != null && !dataCommunicationModeOld.equals(dataCommunicationMode)) {
            return true;
        }

        if (reportingInterval != null && !reportingInterval.equals(mEtReportingInterval.getText().toString().trim())) {
            return true;
        }

        if (bdCardNumber != null && !bdCardNumber.equals(mEtBdCardNumber.getText().toString().trim())) {
            return true;
        }

        return false;
    }
}
