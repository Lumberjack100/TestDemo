package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.TerminalSNEntity;
import com.shmedo.configlibrary.iot.cmd.entity.vms.VmsTerminalCollectorEntity;
import com.shmedo.configlibrary.iot.cmd.entity.vms.VmsTerminalCommEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.vms.VmsTerminalCollectorInfo;
import com.shmedo.configlibrary.iot.model.vms.VmsTerminalCommInfo;
import com.shmedo.configlibrary.iot.model.vms.VmsTerminalInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

public class TcpVmsTerminalParamSettingFragment extends BaseTcpConnectFragment {
    private static final String DEVICE_INFO = "device_info";

    @BindView(R.id.tv_reporting_method)
    TextView mTvReportingMethod;

    @BindView(R.id.et_reporting_interval)
    ClearEditText mEtReportingInterval;

    @BindView(R.id.et_network_number)
    ClearEditText mEtNetworkNumber;

    @BindView(R.id.et_channel_number)
    ClearEditText mEtChannelNumber;

    @BindView(R.id.et_air_baud_rate)
    ClearEditText mEtAirBaudRate;

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    private VmsTerminalInfo vmsTerminalInfo;
    private VmsTerminalCommInfo vmsTerminalCommInfo;
    private VmsTerminalCollectorInfo vmsTerminalCollectorInfo;

    private int reportingMethodPos;
    private String reportingMethodOld;//
    private String reportingMethod;//  上报方式
    private String reportingInterval;//上报间隔
    private String networkNumber;//网络号
    private String channelNumber;//通信信道
    private String airBaudRate;//空中波特率


    public static TcpVmsTerminalParamSettingFragment newInstance(VmsTerminalInfo vmsTerminalInfo) {
        TcpVmsTerminalParamSettingFragment fragment = new TcpVmsTerminalParamSettingFragment();
        Bundle args = new Bundle();
        args.putParcelable(DEVICE_INFO, vmsTerminalInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            vmsTerminalInfo = getArguments().getParcelable(DEVICE_INFO);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.tcp_vms_terminal_param_setting_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setView();
        queryTerminalCollecotrInfo();
    }

    private void setView() {
        mEtReportingInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtNetworkNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtChannelNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtAirBaudRate.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});

        mEtReportingInterval.setHint("≥60s");
        mEtNetworkNumber.setHint("1~65535");
        mEtChannelNumber.setHint("0~31");
        mEtAirBaudRate.setHint("1~6");
    }

    /**
     * 获取终端采集参数
     */
    private void queryTerminalCollecotrInfo() {
        TerminalSNEntity entity = new TerminalSNEntity(vmsTerminalInfo.getSn());
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_MD_GET_TERMINAL_COLLECTOR, entity);
        sendCommand(command);
    }

    /**
     * 获取终端通讯参数
     */
    private void queryTerminalCommunicationInfo() {
        TerminalSNEntity entity = new TerminalSNEntity(vmsTerminalInfo.getSn());
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_MD_GET_TERMINAL_COMMUNICATE, entity);
        sendCommand(command);
    }

    @OnClick({R.id.ll_reporting_method, R.id.btn_confirm})
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.ll_reporting_method) {
            showReportingMethodDialog();

        } else if (id == R.id.btn_confirm) {
            KeyBordUtils.hideSoftKeyboard(view);

            if (!tcpViewModel.getConnectStatus()) {
                ToastUtils.show(getString(R.string.tcp_config_disconnect_warn));
                return;
            }
            if (!checkValueIsValid()) {
                Timber.w("通道参数存在错误!");
                return;
            }

            processSaveTerminalCollector();
        }
    }

    private void showReportingMethodDialog() {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", new String[]{"网关召测", "主动上报"},
                        null, reportingMethodPos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                reportingMethodPos = position;
                                mTvReportingMethod.setText(text);
                                if (text.equals("网关召测")) {
                                    reportingMethod = "0";
                                } else {
                                    reportingMethod = "1";
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_match)
                .show();
    }

    private boolean checkValueIsValid() {
        reportingInterval = mEtReportingInterval.getText().toString().trim();
        networkNumber = mEtNetworkNumber.getText().toString().trim();
        channelNumber = mEtChannelNumber.getText().toString().trim();
        airBaudRate = mEtAirBaudRate.getText().toString().trim();

        if (!TextUtils.isEmpty(reportingInterval)) {
            try {
                int port = Integer.parseInt(reportingInterval);
                if (port < 60) {
                    ToastUtils.show("请输入正确的数据上报间隔!");
                    mEtReportingInterval.requestFocus();
                    return false;
                }

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的数据上报间隔!");
                mEtReportingInterval.requestFocus();
                return false;
            }
        }
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

        if (TextUtils.isEmpty(airBaudRate)) {
            ToastUtils.show("空中波特率不能为空!");
            mEtAirBaudRate.requestFocus();
            return false;
        }
        try {
            int port = Integer.parseInt(airBaudRate);
            if (port < 1 || port > 6) {
                ToastUtils.show("请输入正确的空中波特率!");
                mEtAirBaudRate.requestFocus();
                return false;
            }

        } catch (Exception ex) {
            ToastUtils.show("请输入正确的空中波特率!");
            mEtAirBaudRate.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * 保存终端采集参数
     */
    private void processSaveTerminalCollector() {
        VmsTerminalCollectorEntity entity = new VmsTerminalCollectorEntity();
        entity.setSn(vmsTerminalInfo.getSn());
        entity.setRepttype(reportingMethod);
        entity.setReptgap(reportingInterval);

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_MD_SET_TERMINAL_COLLECTOR, entity);
        sendCommand(command);
    }

    /**
     * 保存终端通信参数
     */
    private void processSaveTerminalComm() {
        VmsTerminalCommEntity entity = new VmsTerminalCommEntity();
        entity.setSn(vmsTerminalInfo.getSn());
        entity.setNetid(networkNumber);
        entity.setChannel(channelNumber);
        entity.setAirbaud(airBaudRate);

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.VMS_MD_SET_TERMINAL_COMMUNICATE, entity);
        sendCommand(command);
    }


    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case VMS_MD_GET_TERMINAL_COLLECTOR: {//获取Vms终端采集参数
                IOTCommandResult<VmsTerminalCollectorInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询终端采集参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                vmsTerminalCollectorInfo = commandResult.getResult();
                initCollectorData();
                queryTerminalCommunicationInfo();
            }
            break;

            case VMS_MD_GET_TERMINAL_COMMUNICATE: {//获取Vms终端通信参数
                IOTCommandResult<VmsTerminalCommInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询终端通信参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                vmsTerminalCommInfo = commandResult.getResult();
                initCommData();
            }
            break;

            case VMS_MD_SET_TERMINAL_COLLECTOR: {//设置Vms终端采集参数
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置参数失败!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                processSaveTerminalComm();
            }
            break;

            case VMS_MD_SET_TERMINAL_COMMUNICATE: {//设置Vms终端通信参数
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置参数失败!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
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
        reportingMethodOld = reportingMethod;
    }

    private void initCollectorData() {
        if (vmsTerminalCollectorInfo == null) {
            Timber.e("VmsTerminalCollectorInfo 为空!");
            vmsTerminalCollectorInfo = new VmsTerminalCollectorInfo();
            return;
        }
        reportingMethodOld = vmsTerminalCollectorInfo.getRepttype();
        reportingMethod = vmsTerminalCollectorInfo.getRepttype();
        if (reportingMethod.equals("0")) {
            reportingMethodPos = 0;
            mTvReportingMethod.setText("网关召测");
        } else if (reportingMethod.equals("1")) {
            reportingMethodPos = 1;
            mTvReportingMethod.setText("主动上报");
        }
        reportingInterval = vmsTerminalCollectorInfo.getReptgap();
        mEtReportingInterval.setText(reportingInterval);

    }

    private void initCommData() {
        if (vmsTerminalCommInfo == null) {
            Timber.e("VmsTerminalCommInfo 为空!");
            vmsTerminalCommInfo = new VmsTerminalCommInfo();
            return;
        }
        networkNumber = vmsTerminalCommInfo.getNetid();
        channelNumber = vmsTerminalCommInfo.getChannel();
        airBaudRate = vmsTerminalCommInfo.getAirbaud();

        mEtNetworkNumber.setText(networkNumber);
        mEtChannelNumber.setText(channelNumber);
        mEtAirBaudRate.setText(airBaudRate);
    }

    @Override
    public boolean onBackPressed() {
        if (tcpViewModel.getConnectStatus()) {
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
        if (reportingMethodOld != null && reportingMethodOld != null && !reportingMethod.equals(reportingMethod)) {
            return true;
        }

        if (networkNumber != null && !networkNumber.equals(mEtNetworkNumber.getText().toString().trim())) {
            return true;
        }
        if (channelNumber != null && !channelNumber.equals(mEtChannelNumber.getText().toString().trim())) {
            return true;
        }
        if (airBaudRate != null && !airBaudRate.equals(mEtAirBaudRate.getText().toString().trim())) {
            return true;
        }
        return false;
    }
}