package com.shmedo.mcloudapp.deviceconfig.view.vms;

import android.content.Context;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.entity.vms.VmsAisleParamEntity;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.enums.VmsAisleNumber;
import com.shmedo.configlibrary.iot.model.vms.VmsAisleInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/7/13 <br/>
 * 描述：    网关通道控制参数配置
 */
public class VmsAisleSettingView extends LinearLayout {
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

    @BindView(R.id.ll_network_number)
    ViewGroup networkNumberLayout;

    @BindView(R.id.ll_aisle_address)
    ViewGroup aisleAddressLayout;

    @BindView(R.id.ll_channel_number)
    ViewGroup channelNumberLayout;

    @BindView(R.id.ll_air_speed)
    ViewGroup airSpeedLayout;

    @BindView(R.id.ll_air_wake_time)
    ViewGroup airWakeTimeLayout;

    @BindView(R.id.ll_terminal_working_mode)
    ViewGroup terminalWorkingModeLayout;

    @BindView(R.id.ll_data_request_interval)
    ViewGroup dataRequestIntervalLayout;

    @BindView(R.id.ll_offline_interval)
    ViewGroup offlineIntervalLayout;

    @BindView(R.id.ll_terminal_sleep_time)
    ViewGroup terminalSleepTimeLayout;

    @BindView(R.id.ll_terminal_wakeup_time)
    ViewGroup terminalWakeupTimeLayout;

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

    public VmsAisleNumber vmsAisleNumber;
    public VmsAisleInfo vmsAisleInfo = new VmsAisleInfo();

    public VmsAisleSettingView(Context context) {
        this(context, null);
    }

    public VmsAisleSettingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VmsAisleSettingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关联布局文件
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.vms_aisle_setting_view, this, true);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
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
        mEtAisleAddress.setHint("0~65535");
        mEtChannelNumber.setHint("0~31");
        mEtAirSpeed.setHint("1~6");
        mEtAirWakeTime.setHint("0~5");
        mEtTerminalWorkingMode.setHint("0低功耗模式，1正常模式");
        mEtDataRequestInterval.setHint("≥3s");
        mEtOfflineInterval.setHint("≥1800s");
        mEtTerminalSleepTime.setHint("0~5");
        mEtTerminalWakeTime.setHint("0~65535");
    }

    public boolean checkValueIsValid() {
        if (!networkNumber.equals("NullKey")) {
            networkNumber = mEtNetworkNumber.getText().toString().trim();
            if (!TextUtils.isEmpty(networkNumber)) {
                try {
                    int port = Integer.parseInt(networkNumber);
                    if (port < 1 || port > 65535) {
                        ToastUtils.show("请输入正确的网络号!");
                        mEtNetworkNumber.requestFocus();
                        return false;
                    }

                } catch (Exception ex) {
                    ToastUtils.show("请输入正确的网络号!");
                    mEtNetworkNumber.requestFocus();
                    return false;
                }
            }
        }
        if (!aisleAddress.equals("NullKey")) {
            aisleAddress = mEtAisleAddress.getText().toString().trim();
            if (!TextUtils.isEmpty(aisleAddress)) {
                try {
                    int port = Integer.parseInt(aisleAddress);
                    if (port < 0 || port > 65535) {
                        ToastUtils.show("请输入正确的地址!");
                        mEtAisleAddress.requestFocus();
                        return false;
                    }

                } catch (Exception ex) {
                    ToastUtils.show("请输入正确的地址!");
                    mEtAisleAddress.requestFocus();
                    return false;
                }
            }
        }
        if (!channelNumber.equals("NullKey")) {
            channelNumber = mEtChannelNumber.getText().toString().trim();
            if (!TextUtils.isEmpty(channelNumber)) {
                try {
                    int port = Integer.parseInt(channelNumber);
                    if (port < 0 || port > 31) {
                        ToastUtils.show("请输入正确的信道号!");
                        mEtChannelNumber.requestFocus();
                        return false;
                    }

                } catch (Exception ex) {
                    ToastUtils.show("请输入正确的信道号!");
                    mEtChannelNumber.requestFocus();
                    return false;
                }
            }
        }
        if (!airSpeed.equals("NullKey")) {
            airSpeed = mEtAirSpeed.getText().toString().trim();
            if (TextUtils.isEmpty(airSpeed)) {
                ToastUtils.show("空中速率不能为空!");
                mEtAirSpeed.requestFocus();
                return false;
            }
            try {
                int port = Integer.parseInt(airSpeed);
                if (port < 1 || port > 6) {
                    ToastUtils.show("请输入正确的空中速率!");
                    mEtAirSpeed.requestFocus();
                    return false;
                }

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的空中速率!");
                mEtAirSpeed.requestFocus();
                return false;
            }
        }
        if (!airWakeTime.equals("NullKey")) {
            airWakeTime = mEtAirWakeTime.getText().toString().trim();
            if (!TextUtils.isEmpty(airWakeTime)) {
                try {
                    int port = Integer.parseInt(airWakeTime);
                    if (port < 0 || port > 5) {
                        ToastUtils.show("请输入正确的空中唤醒时间!");
                        mEtAirWakeTime.requestFocus();
                        return false;
                    }

                } catch (Exception ex) {
                    ToastUtils.show("请输入正确的空中唤醒时间!");
                    mEtAirWakeTime.requestFocus();
                    return false;
                }
            }
        }
        if (!terminalWorkingMode.equals("NullKey")) {
            terminalWorkingMode = mEtTerminalWorkingMode.getText().toString().trim();
            if (TextUtils.isEmpty(terminalWorkingMode)) {
                ToastUtils.show("终端工作模式不能为空!");
                mEtTerminalWorkingMode.requestFocus();
                return false;
            }
            try {
                int port = Integer.parseInt(terminalWorkingMode);
                if (port != 0 && port != 1) {
                    ToastUtils.show("请输入正确的终端工作模式!");
                    mEtTerminalWorkingMode.requestFocus();
                    return false;
                }

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的终端工作模式!");
                mEtTerminalWorkingMode.requestFocus();
                return false;
            }
        }
        if (!dataRequestInterval.equals("NullKey")) {
            dataRequestInterval = mEtDataRequestInterval.getText().toString().trim();
            if (!TextUtils.isEmpty(dataRequestInterval)) {
                try {
                    int port = Integer.parseInt(dataRequestInterval);
                    if (port < 3) {
                        ToastUtils.show("请输入正确的数据请求间隔!");
                        mEtDataRequestInterval.requestFocus();
                        return false;
                    }

                } catch (Exception ex) {
                    ToastUtils.show("请输入正确的数据请求间隔!");
                    mEtDataRequestInterval.requestFocus();
                    return false;
                }
            }
        }
        if (!offlineInterval.equals("NullKey")) {
            offlineInterval = mEtOfflineInterval.getText().toString().trim();
            if (!TextUtils.isEmpty(offlineInterval)) {
                try {
                    int port = Integer.parseInt(offlineInterval);
                    if (port < 1800) {
                        ToastUtils.show("请输入正确的离线间隔!");
                        mEtOfflineInterval.requestFocus();
                        return false;
                    }

                } catch (Exception ex) {
                    ToastUtils.show("请输入正确的离线间隔!");
                    mEtOfflineInterval.requestFocus();
                    return false;
                }
            }
        }
        if (!terminalSleepTime.equals("NullKey")) {
            terminalSleepTime = mEtTerminalSleepTime.getText().toString().trim();
            if (!TextUtils.isEmpty(terminalSleepTime)) {
                try {
                    int port = Integer.parseInt(terminalSleepTime);
                    if (port < 0 || port > 5) {
                        ToastUtils.show("请输入正确的终端休眠时间!");
                        mEtTerminalSleepTime.requestFocus();
                        return false;
                    }

                } catch (Exception ex) {
                    ToastUtils.show("请输入正确的终端休眠时间!");
                    mEtTerminalSleepTime.requestFocus();
                    return false;
                }
            }
        }
        if (!terminalWakeTime.equals("NullKey")) {
            terminalWakeTime = mEtTerminalWakeTime.getText().toString().trim();
            if (!TextUtils.isEmpty(terminalWakeTime)) {
                try {
                    int port = Integer.parseInt(terminalWakeTime);
                    if (port < 0 || port > 65535) {
                        ToastUtils.show("请输入正确的终端唤醒时间!");
                        mEtTerminalWakeTime.requestFocus();
                        return false;
                    }

                } catch (Exception ex) {
                    ToastUtils.show("请输入正确的终端唤醒时间!");
                    mEtTerminalWakeTime.requestFocus();
                    return false;
                }
            }
        }

        return true;
    }

    public String getSetCommand() {
        VmsAisleParamEntity vmsAisleParamEntity = new VmsAisleParamEntity();
        vmsAisleParamEntity.setVmsAisleNumber(vmsAisleNumber);
        vmsAisleParamEntity.setNetid(vmsAisleInfo.getNetid().equals("NullKey") ? "NullKey" : networkNumber);
        vmsAisleParamEntity.setAddr(vmsAisleInfo.getAddr().equals("NullKey") ? "NullKey" : aisleAddress);
        vmsAisleParamEntity.setChl(vmsAisleInfo.getChl().equals("NullKey") ? "NullKey" : channelNumber);
        vmsAisleParamEntity.setAirbaud(vmsAisleInfo.getAirbaud().equals("NullKey") ? "NullKey" : airSpeed);
        vmsAisleParamEntity.setPpt(vmsAisleInfo.getPpt().equals("NullKey") ? "NullKey" : airWakeTime);
        vmsAisleParamEntity.setTerminalmode(vmsAisleInfo.getTerminalmode().equals("NullKey") ? "NullKey" : terminalWorkingMode);
        vmsAisleParamEntity.setSendgap(vmsAisleInfo.getSendgap().equals("NullKey") ? "NullKey" : dataRequestInterval);
        vmsAisleParamEntity.setOffline(vmsAisleInfo.getOffline().equals("NullKey") ? "NullKey" : offlineInterval);
        vmsAisleParamEntity.setSleepgap(vmsAisleInfo.getSleepgap().equals("NullKey") ? "NullKey" : terminalSleepTime);
        vmsAisleParamEntity.setWakeupgap(vmsAisleInfo.getWakeupgap().equals("NullKey") ? "NullKey" : terminalWakeTime);

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_MD_SET_GATEWAY_PARAM, vmsAisleParamEntity);
        return command;
    }

    public void initParamConfigInfo() {
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

        if (networkNumber.equals("NullKey")) {
            networkNumberLayout.setVisibility(View.GONE);
        } else {
            mEtNetworkNumber.setText(networkNumber);
        }
        if (aisleAddress.equals("NullKey")) {
            aisleAddressLayout.setVisibility(View.GONE);
        } else {
            mEtAisleAddress.setText(aisleAddress);
        }
        if (channelNumber.equals("NullKey")) {
            channelNumberLayout.setVisibility(View.GONE);
        } else {
            mEtChannelNumber.setText(channelNumber);
        }
        if (airSpeed.equals("NullKey")) {
            airSpeedLayout.setVisibility(View.GONE);
        } else {
            mEtAirSpeed.setText(airSpeed);
        }
        if (airWakeTime.equals("NullKey")) {
            airWakeTimeLayout.setVisibility(View.GONE);
        } else {
            mEtAirWakeTime.setText(airWakeTime);
        }
        if (terminalWorkingMode.equals("NullKey")) {
            terminalWorkingModeLayout.setVisibility(View.GONE);
        } else {
            mEtTerminalWorkingMode.setText(terminalWorkingMode);
        }
        if (dataRequestInterval.equals("NullKey")) {
            dataRequestIntervalLayout.setVisibility(View.GONE);
        } else {
            mEtDataRequestInterval.setText(dataRequestInterval);
        }
        if (offlineInterval.equals("NullKey")) {
            offlineIntervalLayout.setVisibility(View.GONE);
        } else {
            mEtOfflineInterval.setText(offlineInterval);
        }
        if (terminalSleepTime.equals("NullKey")) {
            terminalSleepTimeLayout.setVisibility(View.GONE);
        } else {
            mEtTerminalSleepTime.setText(terminalSleepTime);
        }
        if (terminalWakeTime.equals("NullKey")) {
            terminalWakeupTimeLayout.setVisibility(View.GONE);
        } else {
            mEtTerminalWakeTime.setText(terminalWakeTime);
        }
    }

    public boolean checkValueIsChange() {
        if (networkNumber != null && !networkNumber.equals("NullKey") && !networkNumber.equals(mEtNetworkNumber.getText().toString().trim())) {
            return true;
        }
        if (aisleAddress != null && !aisleAddress.equals("NullKey") && !aisleAddress.equals(mEtAisleAddress.getText().toString().trim())) {
            return true;
        }
        if (channelNumber != null && !channelNumber.equals("NullKey") && !channelNumber.equals(mEtChannelNumber.getText().toString().trim())) {
            return true;
        }
        if (airSpeed != null && !airSpeed.equals("NullKey") && !airSpeed.equals(mEtAirSpeed.getText().toString().trim())) {
            return true;
        }
        if (airWakeTime != null && !airWakeTime.equals("NullKey") && !airWakeTime.equals(mEtAirWakeTime.getText().toString().trim())) {
            return true;
        }
        if (terminalWorkingMode != null && !terminalWorkingMode.equals("NullKey") && !terminalWorkingMode.equals(mEtTerminalWorkingMode.getText().toString().trim())) {
            return true;
        }
        if (dataRequestInterval != null && !dataRequestInterval.equals("NullKey") && !dataRequestInterval.equals(mEtDataRequestInterval.getText().toString().trim())) {
            return true;
        }
        if (offlineInterval != null && !offlineInterval.equals("NullKey") && !offlineInterval.equals(mEtOfflineInterval.getText().toString().trim())) {
            return true;
        }
        if (terminalSleepTime != null && !terminalSleepTime.equals("NullKey") && !terminalSleepTime.equals(mEtTerminalSleepTime.getText().toString().trim())) {
            return true;
        }
        if (terminalWakeTime != null && !terminalWakeTime.equals("NullKey") && !terminalWakeTime.equals(mEtTerminalWakeTime.getText().toString().trim())) {
            return true;
        }
        return false;
    }

}
