package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.VmsAisleNumberEntity;
import com.shmedo.configlibrary.iot.cmd.entity.VmsAisleParamEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.enums.VmsAisleNumber;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.VmsAisleInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.BaseTcpConnectFragment;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;


/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/20 <br/>
 * 描述：     Vms 网关通道控制参数配置
 */
public class TcpVmsAisleSettingFragment extends BaseTcpConnectFragment {
    @BindView(R.id.et_network_number)
    ClearEditText mEtNetworkNumber;

    @BindView(R.id.et_aisle_address)
    ClearEditText mEtAisleAddress;

    @BindView(R.id.et_channel_number)
    ClearEditText mEtChannelNumber;

    @BindView(R.id.et_air_speed)
    ClearEditText mEtAirSpeed;

    @BindView(R.id.et_air_wake_time)
    ClearEditText mEtAirWakeTime;

    @BindView(R.id.et_terminal_working_mode)
    ClearEditText mEtTerminalWorkingMode;

    @BindView(R.id.et_data_request_interval)
    ClearEditText mEtDataRequestInterval;

    @BindView(R.id.et_offline_interval)
    ClearEditText mEtOfflineInterval;

    @BindView(R.id.et_terminal_sleep_time)
    ClearEditText mEtTerminalSleepTime;

    @BindView(R.id.et_terminal_wake_time)
    ClearEditText mEtTerminalWakeTime;

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    private static final String VMS_AISLE_NUMBER = "vms_aisle_number";
    private VmsAisleNumber vmsAisleNumber;
    private VmsAisleInfo vmsAisleInfo;

    private String networkNumber;//网络号
    private String aisleAddress;//通道的地址
    private String channelNumber;//通信信道
    private String airSpeed;//空中速率
    private String airWakeTime;//空中唤醒时间
    private String terminalWorkingMode;//终端工作模式
    private String dataRequestInterval;//数据请求间隔
    private String offlineInterval;//离线间隔
    private String terminalSleepTime;//终端休眠时间
    private String terminalWakeTime;//终端唤醒时间

    public static TcpVmsAisleSettingFragment newInstance(VmsAisleNumber vmsAisleNumber) {
        TcpVmsAisleSettingFragment fragment = new TcpVmsAisleSettingFragment();
        Bundle args = new Bundle();
        args.putSerializable(VMS_AISLE_NUMBER, vmsAisleNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            vmsAisleNumber = (VmsAisleNumber) getArguments().getSerializable(VMS_AISLE_NUMBER);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.tcp_vms_aisle_setting_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setView();
        queryVmsAisleInfo();
    }

    private void setView() {
        mEtNetworkNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtAisleAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtChannelNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtAirSpeed.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtAirWakeTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtTerminalWorkingMode.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
        mEtDataRequestInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtOfflineInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtTerminalSleepTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtTerminalWakeTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});

        mEtNetworkNumber.setHint("1~65535");
        mEtAisleAddress.setHint("0~63");
        mEtChannelNumber.setHint("0~31");
        mEtAirSpeed.setHint("1~6");
        mEtAirWakeTime.setHint("0~5");
        mEtTerminalWorkingMode.setHint("0低功耗模式，1正常模式");
        mEtDataRequestInterval.setHint("≥3s");
        mEtOfflineInterval.setHint("≥7200s");
        mEtTerminalSleepTime.setHint("0~5");
        mEtTerminalSleepTime.setHint("0~65535");
    }

    /**
     * 获取网关不同通道下的控制参数
     */
    private void queryVmsAisleInfo() {
//        startProgressRunnable("加载数据...", QUERY_CMD_DELAY_MILLIS);

        VmsAisleNumberEntity vmsAisleNumberEntity = new VmsAisleNumberEntity(vmsAisleNumber.toInt());
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_MD_GET_GATEWAY_PARAM, vmsAisleNumberEntity);
        sendCommand(command);
    }

    @OnClick({R.id.btn_confirm})
    public void onClick(View view) {
        if (view.getId() == R.id.btn_confirm) {
            KeyBordUtils.hideSoftKeyboard(view);

            if (!tcpShareViewModel.getConnectStatus()) {
                ToastUtils.show(getString(R.string.tcp_config_disconnect_warn));
                return;
            }

            if (!checkValueIsValid()) {
                Timber.w("通道参数存在错误!");
                return;
            }

            processSave();
        }
    }

    private boolean checkValueIsValid() {
        networkNumber = mEtNetworkNumber.getText().toString().trim();
        aisleAddress = mEtAisleAddress.getText().toString().trim();
        channelNumber = mEtChannelNumber.getText().toString().trim();
        airSpeed = mEtAirSpeed.getText().toString().trim();
        airWakeTime = mEtAirWakeTime.getText().toString().trim();
        terminalWorkingMode = mEtTerminalWorkingMode.getText().toString().trim();
        dataRequestInterval = mEtDataRequestInterval.getText().toString().trim();
        offlineInterval = mEtOfflineInterval.getText().toString().trim();
        terminalSleepTime = mEtTerminalSleepTime.getText().toString().trim();
        terminalWakeTime = mEtTerminalWakeTime.getText().toString().trim();

        if (!TextUtils.isEmpty(networkNumber)) {
            try {
                int port = Integer.parseInt(networkNumber);
                if (port < 1 || port > 65535) {
                    ToastUtils.show("请输入有效的网络号!");
                    mEtNetworkNumber.requestFocus();
                    return false;
                }

            } catch (Exception ex) {
                ToastUtils.show("请输入有效的网络号!");
                mEtNetworkNumber.requestFocus();
                return false;
            }
        }
        if (!TextUtils.isEmpty(aisleAddress)) {
            try {
                int port = Integer.parseInt(aisleAddress);
                if (port < 0 || port > 63) {
                    ToastUtils.show("请输入有效的地址!");
                    mEtAisleAddress.requestFocus();
                    return false;
                }

            } catch (Exception ex) {
                ToastUtils.show("请输入有效的地址!");
                mEtAisleAddress.requestFocus();
                return false;
            }
        }
        if (!TextUtils.isEmpty(channelNumber)) {
            try {
                int port = Integer.parseInt(channelNumber);
                if (port < 0 || port > 31) {
                    ToastUtils.show("请输入有效的信道号!");
                    mEtAisleAddress.requestFocus();
                    return false;
                }

            } catch (Exception ex) {
                ToastUtils.show("请输入有效的信道号!");
                mEtAisleAddress.requestFocus();
                return false;
            }
        }
        if (TextUtils.isEmpty(airSpeed)) {
            ToastUtils.show("空中速率不能为空!");
            mEtAirSpeed.requestFocus();
            return false;
        }
        try {
            int port = Integer.parseInt(airSpeed);
            if (port < 1 || port > 6) {
                ToastUtils.show("请输入有效的空中速率!");
                mEtAirSpeed.requestFocus();
                return false;
            }

        } catch (Exception ex) {
            ToastUtils.show("请输入有效的空中速率!");
            mEtAirSpeed.requestFocus();
            return false;
        }
        if (!TextUtils.isEmpty(airWakeTime)) {
            try {
                int port = Integer.parseInt(airWakeTime);
                if (port < 0 || port > 5) {
                    ToastUtils.show("请输入有效的空中唤醒时间!");
                    mEtAirWakeTime.requestFocus();
                    return false;
                }

            } catch (Exception ex) {
                ToastUtils.show("请输入有效的空中唤醒时间!");
                mEtAirWakeTime.requestFocus();
                return false;
            }
        }
        if (TextUtils.isEmpty(terminalWorkingMode)) {
            ToastUtils.show("终端工作模式不能为空!");
            mEtTerminalWorkingMode.requestFocus();
            return false;
        }
        try {
            int port = Integer.parseInt(terminalWorkingMode);
            if (port != 0 && port != 1) {
                ToastUtils.show("请输入有效的终端工作模式!");
                mEtTerminalWorkingMode.requestFocus();
                return false;
            }

        } catch (Exception ex) {
            ToastUtils.show("请输入有效的终端工作模式!");
            mEtTerminalWorkingMode.requestFocus();
            return false;
        }
        if (!TextUtils.isEmpty(dataRequestInterval)) {
            try {
                int port = Integer.parseInt(dataRequestInterval);
                if (port < 3) {
                    ToastUtils.show("请输入有效的数据请求间隔!");
                    mEtAirWakeTime.requestFocus();
                    return false;
                }

            } catch (Exception ex) {
                ToastUtils.show("请输入有效的数据请求间隔!");
                mEtAirWakeTime.requestFocus();
                return false;
            }
        }
        if (!TextUtils.isEmpty(offlineInterval)) {
            try {
                int port = Integer.parseInt(offlineInterval);
                if (port < 7200) {
                    ToastUtils.show("请输入有效的离线间隔!");
                    mEtOfflineInterval.requestFocus();
                    return false;
                }

            } catch (Exception ex) {
                ToastUtils.show("请输入有效的离线间隔!");
                mEtOfflineInterval.requestFocus();
                return false;
            }
        }
        if (!TextUtils.isEmpty(terminalSleepTime)) {
            try {
                int port = Integer.parseInt(terminalSleepTime);
                if (port < 0 || port > 5) {
                    ToastUtils.show("请输入有效的终端休眠时间!");
                    mEtTerminalSleepTime.requestFocus();
                    return false;
                }

            } catch (Exception ex) {
                ToastUtils.show("请输入有效的终端休眠时间!");
                mEtTerminalSleepTime.requestFocus();
                return false;
            }
        }
        if (!TextUtils.isEmpty(terminalWakeTime)) {
            try {
                int port = Integer.parseInt(terminalWakeTime);
                if (port < 0 || port > 65535) {
                    ToastUtils.show("请输入有效的终端唤醒时间!");
                    mEtTerminalWakeTime.requestFocus();
                    return false;
                }

            } catch (Exception ex) {
                ToastUtils.show("请输入有效的终端唤醒时间!");
                mEtTerminalWakeTime.requestFocus();
                return false;
            }
        }

        return true;
    }

    private void processSave() {
        VmsAisleParamEntity vmsAisleParamEntity = new VmsAisleParamEntity();
        vmsAisleParamEntity.setVmsAisleNumber(vmsAisleNumber);
        vmsAisleParamEntity.setNetid(networkNumber);
        vmsAisleParamEntity.setPpt(airWakeTime);
        vmsAisleParamEntity.setAddr(aisleAddress);
        vmsAisleParamEntity.setChl(channelNumber);
        vmsAisleParamEntity.setTerminalmode(terminalWorkingMode);
        vmsAisleParamEntity.setSendgap(dataRequestInterval);
        vmsAisleParamEntity.setOffline(offlineInterval);
        vmsAisleParamEntity.setSleepgap(terminalSleepTime);
        vmsAisleParamEntity.setWakeupgap(terminalWakeTime);
        vmsAisleParamEntity.setAirbaud(airSpeed);

        mBtnSave.setEnabled(false);
//        startProgressRunnable("正在发送配置指令...", SEND_CMD_DELAY_MILLIS);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_MD_SET_GATEWAY_PARAM, vmsAisleParamEntity);
        sendCommand(command);
    }

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case VMS_MD_GET_GATEWAY_PARAM: {//获取网关通道的控制参数
//                stopProgressRunnable();
                IOTCommandResult<VmsAisleInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = "查询网关通道的控制参数出错!";
                    Timber.e("%s%s", errMsg, commandResult.getMessage());
                    ToastUtils.show(errMsg);
                    return;
                }
                vmsAisleInfo = commandResult.getResult();
                initViewData();
            }
            break;

            case VMS_MD_SET_GATEWAY_PARAM: {//设置网关通道的控制参数
//                stopProgressRunnable();
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = "设置参数失败!";
                    Timber.e("%s%s", errMsg, cmdResult.getReason());
                    ToastUtils.show(errMsg);
                    mBtnSave.setEnabled(true);
                    return;
                }
                doAfterSetting();
            }
            break;

            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
    }


    private void doAfterSetting() {
        ToastUtils.show("设置成功");
        mBtnSave.setEnabled(true);
    }

    private void initViewData() {
        if (vmsAisleInfo == null) {
            Timber.e("VmsAisleParamInfo 为空!");
            vmsAisleInfo = new VmsAisleInfo();
            return;
        }

        networkNumber = vmsAisleInfo.getNetid().trim();
        aisleAddress = vmsAisleInfo.getAddr().trim();
        channelNumber = vmsAisleInfo.getChl().trim();
        airSpeed = vmsAisleInfo.getAirbaud().trim();
        airWakeTime = vmsAisleInfo.getPpt().trim();
        terminalWorkingMode = vmsAisleInfo.getTerminalmode().trim();
        dataRequestInterval = vmsAisleInfo.getSendgap().trim();
        offlineInterval = vmsAisleInfo.getOffline().trim();
        terminalSleepTime = vmsAisleInfo.getSleepgap().trim();
        terminalWakeTime = vmsAisleInfo.getWakeupgap().trim();

        mEtNetworkNumber.setText(networkNumber);
        mEtAisleAddress.setText(aisleAddress);
        mEtChannelNumber.setText(channelNumber);
        mEtAirSpeed.setText(airSpeed);
        mEtAirWakeTime.setText(airWakeTime);
        mEtTerminalWorkingMode.setText(terminalWorkingMode);
        mEtDataRequestInterval.setText(dataRequestInterval);
        mEtOfflineInterval.setText(offlineInterval);
        mEtTerminalSleepTime.setText(terminalSleepTime);
        mEtTerminalWakeTime.setText(terminalWakeTime);
    }

    @Override
    public boolean onBackPressed() {
        if (tcpShareViewModel.getConnectStatus()) {
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
        if (networkNumber != null && !networkNumber.equals(mEtNetworkNumber.getText().toString().trim())) {
            return true;
        }
        if (aisleAddress != null && !aisleAddress.equals(mEtAisleAddress.getText().toString().trim())) {
            return true;
        }
        if (channelNumber != null && !channelNumber.equals(mEtChannelNumber.getText().toString().trim())) {
            return true;
        }
        if (airSpeed != null && !airSpeed.equals(mEtAirSpeed.getText().toString().trim())) {
            return true;
        }
        if (airWakeTime != null && !airWakeTime.equals(mEtAirWakeTime.getText().toString().trim())) {
            return true;
        }
        if (terminalWorkingMode != null && !terminalWorkingMode.equals(mEtTerminalWorkingMode.getText().toString().trim())) {
            return true;
        }
        if (dataRequestInterval != null && !dataRequestInterval.equals(mEtDataRequestInterval.getText().toString().trim())) {
            return true;
        }
        if (offlineInterval != null && !offlineInterval.equals(mEtOfflineInterval.getText().toString().trim())) {
            return true;
        }
        if (terminalSleepTime != null && !terminalSleepTime.equals(mEtTerminalSleepTime.getText().toString().trim())) {
            return true;
        }
        if (terminalWakeTime != null && !terminalWakeTime.equals(mEtTerminalWakeTime.getText().toString().trim())) {
            return true;
        }

        return false;
    }


}