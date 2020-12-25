package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.ble.utils.ValidateUtil;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeBasicConfigParamEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeBasicConfigParam;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

public class BleAdmeBasicParamConfigFragment extends BaseBleIotCommunicateFragment {
    @BindView(R.id.tv_inclinometer_type)
    TextView mTvInclinometerType;

    @BindView(R.id.et_collector_address)
    ClearEditText mEtCollectorAddress;//采集器地址

    @BindView(R.id.et_mac_address)
    ClearEditText mEtMacAddress;//Mac 地址

    @BindView(R.id.et_inclination_tube_hole_depth)
    ClearEditText mEtInclinometerTubeHoleDepth;//测斜管孔深(m)

    @BindView(R.id.et_decentralization_speed)
    ClearEditText mEtDecentralizationSpeed;//下放速度(r/min)

    @BindView(R.id.et_decentralization_waiting_time)
    ClearEditText mEtDecentralizationWaitingTime;//下放等待时间(min)

    @BindView(R.id.tv_data_reporting_method)
    TextView mTvDataReportingMethod;

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    @BindView(R.id.ll_collector_address)
    ViewGroup collectorAddressLayout;

    @BindView(R.id.ll_mac_address)
    ViewGroup macAddressLayout;

    private AdmeBasicConfigParam basicConfigParam;

    private int inclinometerTypePos;
    private int dataReportingMethodPos;

    private String inclinometerTypeOld;//测斜仪类型
    private String inclinometerType;// 测斜仪类型
    private String address;// 采集器地址/Mac 地址
    private String inclinometerTubeHoleDepth;// 测斜管孔深(m)
    private String decentralizationSpeed;// 下放速度(r/min)
    private String decentralizationWaitingTime;//下放等待时间(min)
    private String dataReportingMethodOld;//数据上报方式
    private String dataReportingMethod;// 数据上报方式


    public static BleAdmeBasicParamConfigFragment newInstance() {
        return new BleAdmeBasicParamConfigFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ble_adme_basic_param_config_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setView();
        queryBasicParamConfigInfo();
    }

    private void setView() {
        mEtCollectorAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtMacAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(17)});
        mEtInclinometerTubeHoleDepth.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
        mEtDecentralizationSpeed.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtDecentralizationWaitingTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
    }

    /**
     * 获取设备的基础配置参数
     */
    private void queryBasicParamConfigInfo() {
        errMsg = "查询数据超时,请稍后尝试";
        startProgressRunnable("加载中...", DELAY_MILLIS);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_BASIC_PARAMETERS);
        sendCommand(command);
    }

    @OnClick({R.id.ll_inclinometer_type, R.id.ll_data_reporting_method, R.id.btn_confirm})
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.ll_inclinometer_type) {
            showInclinometerTypeDialog();

        } else if (id == R.id.ll_data_reporting_method) {
            showDataReportingMethodDialog();

        } else if (id == R.id.btn_confirm) {
            KeyBordUtils.hideSoftKeyboard(view);
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            if (!checkValueIsValid()) {
                Timber.w("基础配置参数错误!");
                return;
            }

            processSave();
        }
    }

    /**
     * 选择测斜仪类型
     */
    private void showInclinometerTypeDialog() {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", new String[]{"433测斜仪", "蓝牙测斜仪"},
                        null, inclinometerTypePos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                inclinometerTypePos = position;
                                mTvInclinometerType.setText(text);
                                if (position == 0) {
                                    inclinometerType = "0";
                                    collectorAddressLayout.setVisibility(View.VISIBLE);
                                    macAddressLayout.setVisibility(View.GONE);
                                } else {
                                    inclinometerType = "1";
                                    collectorAddressLayout.setVisibility(View.GONE);
                                    macAddressLayout.setVisibility(View.VISIBLE);
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_match)
                .show();
    }

    private void showDataReportingMethodDialog() {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", new String[]{"顶固定法", "底固定法"},
                        null, dataReportingMethodPos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                dataReportingMethodPos = position;
                                mTvDataReportingMethod.setText(text);
                                if (position == 0) {
                                    dataReportingMethod = "0";
                                } else {
                                    dataReportingMethod = "1";
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_match)
                .show();
    }

    private boolean checkValueIsValid() {
        address = mEtCollectorAddress.getText().toString().trim();
        inclinometerTubeHoleDepth = mEtInclinometerTubeHoleDepth.getText().toString().trim();
        decentralizationSpeed = mEtDecentralizationSpeed.getText().toString().trim();
        decentralizationWaitingTime = mEtDecentralizationWaitingTime.getText().toString().trim();

        if (inclinometerType.equals("0")) {
            if (TextUtils.isEmpty(address)) {
                ToastUtils.show("采集器地址不能为空!");
                mEtCollectorAddress.requestFocus();
                return false;
            }
            if (Integer.parseInt(address) < 0) {
                ToastUtils.show("采集器地址不能小于0");
                mEtCollectorAddress.requestFocus();
                return false;
            }
        } else {
            if (TextUtils.isEmpty(address)) {
                ToastUtils.show("Mac地址不能为空!");
                mEtMacAddress.requestFocus();
                return false;
            }

            if (!address.contains(":")) {
                ToastUtils.show("请输入正确的Mac地址!");
                mEtMacAddress.requestFocus();
                return false;
            }
        }

        if (TextUtils.isEmpty(inclinometerTubeHoleDepth)) {
            ToastUtils.show("测斜管孔深不能为空!");
            mEtInclinometerTubeHoleDepth.requestFocus();
            return false;
        }

        try {
            double value = Double.parseDouble(inclinometerTubeHoleDepth);

        } catch (Exception ex) {
            ToastUtils.show("请输入正确的测斜管孔深!");
            mEtInclinometerTubeHoleDepth.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(decentralizationSpeed)) {
            ToastUtils.show("下放速度不能为空!");
            mEtDecentralizationSpeed.requestFocus();
            return false;
        }

        if (!ValidateUtil.isInteger(decentralizationSpeed)) {
            ToastUtils.show("请输入正确的下放速度!");
            mEtDecentralizationSpeed.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(decentralizationWaitingTime)) {
            ToastUtils.show("下放等待时间不能为空!");
            mEtDecentralizationWaitingTime.requestFocus();
            return false;
        }

        if (!ValidateUtil.isInteger(decentralizationWaitingTime)) {
            ToastUtils.show("请输入正确的下放等待时间!");
            mEtDecentralizationWaitingTime.requestFocus();
            return false;
        }

        return true;
    }

    private void processSave() {
        AdmeBasicConfigParamEntity entity = new AdmeBasicConfigParamEntity();
        entity.setInctype(inclinometerType);
        entity.setAddress(address);
        entity.setInterdeep(inclinometerTubeHoleDepth);
        entity.setDownspeed(decentralizationSpeed);
        entity.setDownwaitetime(decentralizationWaitingTime);
        entity.setDatatype(dataReportingMethod);

        errMsg = "发送指令超时,请稍后尝试";
        startProgressRunnable("正在发送配置指令...", DELAY_MILLIS);
        mBtnSave.setEnabled(false);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_BASIC_PARAMETERS, entity);
        sendCommand(command);
    }

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case ADME_MD_GET_BASIC_PARAMETERS: {//获取设备的基础配置参数
                stopProgressRunnable();
                IOTCommandResult<AdmeBasicConfigParam> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取设备的基础配置参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                basicConfigParam = commandResult.getResult();
                initBasicParamConfigInfo();
            }
            break;

            case ADME_MD_SET_BASIC_PARAMETERS: {//设置设备的基础配置参数
                stopProgressRunnable();
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "保存基础配置参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    mBtnSave.setEnabled(true);
                    return;
                }
                doAfterSetting();
            }
            break;

            default:
                stopProgressRunnable();
                super.parseResponseMessage(cmdStr);
                break;
        }
    }

    private void doAfterSetting() {
        mBtnSave.setEnabled(true);
        inclinometerTypeOld = inclinometerType;
        dataReportingMethodOld = dataReportingMethod;
        ToastUtils.show("保存成功");
    }

    private void initBasicParamConfigInfo() {
        if (basicConfigParam == null) {
            Timber.e("AdmeBasicConfigParam is Null!");
            basicConfigParam = new AdmeBasicConfigParam();
            return;
        }
        inclinometerTypeOld = basicConfigParam.getInctype().trim();
        inclinometerType = basicConfigParam.getInctype().trim();
        address = basicConfigParam.getAddress().trim();
        inclinometerTubeHoleDepth = basicConfigParam.getInterdeep().trim();
        decentralizationSpeed = basicConfigParam.getDownspeed().trim();
        decentralizationWaitingTime = basicConfigParam.getDownwaitetime().trim();
        dataReportingMethodOld = basicConfigParam.getDatatype().trim();
        dataReportingMethod = basicConfigParam.getDatatype().trim();

        mEtInclinometerTubeHoleDepth.setText(inclinometerTubeHoleDepth);
        mEtDecentralizationSpeed.setText(decentralizationSpeed);
        mEtDecentralizationWaitingTime.setText(decentralizationWaitingTime);
        mTvDataReportingMethod.setText(dataReportingMethodOld);

        if (inclinometerTypeOld.equals("0")) {
            inclinometerTypePos = 0;
            mTvInclinometerType.setText("433测斜仪");
            mEtCollectorAddress.setText(address);
            collectorAddressLayout.setVisibility(View.VISIBLE);
            macAddressLayout.setVisibility(View.GONE);

        } else {
            inclinometerTypePos = 1;
            mTvInclinometerType.setText("蓝牙测斜仪");
            mEtMacAddress.setText(address);
            collectorAddressLayout.setVisibility(View.GONE);
            macAddressLayout.setVisibility(View.VISIBLE);
        }

        if (dataReportingMethodOld.equals("0")) {
            dataReportingMethodPos = 0;
            mTvDataReportingMethod.setText("顶固定法");
        } else {
            dataReportingMethodPos = 1;
            mTvDataReportingMethod.setText("底固定法");
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
        if (inclinometerTypeOld != null && inclinometerType != null && !inclinometerTypeOld.equals(inclinometerType)) {
            return true;
        }

        if (address != null) {
            if (inclinometerTypeOld.equals("0") && !address.equals(mEtCollectorAddress.getText().toString().trim())) {
                return true;
            }
            if (inclinometerTypeOld.equals("1") && !address.equals(mEtMacAddress.getText().toString().trim())) {
                return true;
            }
        }

        if (inclinometerTubeHoleDepth != null && !inclinometerTubeHoleDepth.equals(mEtInclinometerTubeHoleDepth.getText().toString().trim())) {
            return true;
        }

        if (decentralizationSpeed != null && !decentralizationSpeed.equals(mEtDecentralizationSpeed.getText().toString().trim())) {
            return true;
        }

        if (decentralizationWaitingTime != null && !decentralizationWaitingTime.equals(mEtDecentralizationWaitingTime.getText().toString().trim())) {
            return true;
        }

        if (dataReportingMethodOld != null && dataReportingMethod != null && !dataReportingMethodOld.equals(dataReportingMethod)) {
            return true;
        }
        return false;
    }
}