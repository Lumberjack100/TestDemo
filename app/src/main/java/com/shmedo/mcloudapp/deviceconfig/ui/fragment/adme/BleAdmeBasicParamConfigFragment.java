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
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeBasicConfigEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeBasicConfigInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

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

    @BindView(R.id.tv_data_settlement_method)
    TextView mTvDataSettlementMethod;

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    @BindView(R.id.ll_collector_address)
    ViewGroup collectorAddressLayout;

    @BindView(R.id.ll_mac_address)
    ViewGroup macAddressLayout;

    private AdmeBasicConfigInfo basicConfigParam;

    private int inclinometerTypePos;
    private int dataSettlementMethodPos;

    private String inclinometerTypeOld;//测斜仪类型
    private String inclinometerType;// 测斜仪类型
    private String address;// 采集器地址/Mac 地址
    private String inclinometerTubeHoleDepth;// 测斜管孔深(m)
    private String decentralizationSpeed;// 下放速度(r/min)
    private String decentralizationWaitingTime;//下放等待时间(min)
    private String dataSettlementMethodOld;//数据结算方式
    private String dataSettlementMethod;// 数据结算方式

    private DecimalFormat decimalFormat = new DecimalFormat();


    public static BleAdmeBasicParamConfigFragment newInstance() {
        return new BleAdmeBasicParamConfigFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ble_adme_basic_param_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setView();
        queryBasicParamConfigInfo();
        //TODO 设备处于自动监测模式时，不可编辑参数(后期还要考虑点击编辑按钮时的页面状态切换)
        if (admeViewModel.deviceMode == 0) {
            configPageViewModel.configPageEditableChanged.setValue(true);
        } else {
            configPageViewModel.configPageEditableChanged.setValue(false);
        }
    }

    private void setView() {
        mEtCollectorAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        mEtCollectorAddress.setHint("0-32");

        mEtMacAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(17)});
        mEtMacAddress.setHint("xx:xx:xx:xx:xx:xx");

        mEtInclinometerTubeHoleDepth.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});

        mEtDecentralizationSpeed.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtDecentralizationSpeed.setHint("1-100");

        mEtDecentralizationWaitingTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        mEtDecentralizationWaitingTime.setHint("1-32");
    }

    /**
     * 获取设备的基础配置参数
     */
    private void queryBasicParamConfigInfo() {
        errMsg = "查询数据超时,请稍后尝试";
        startProgressRunnable("加载中...", WRITE_TIME_OUT_SECOND);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_BASIC_PARAMETERS);
        sendCommand(command);
    }

    @OnClick({R.id.ll_inclinometer_type, R.id.ll_data_settlement_method, R.id.btn_confirm})
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.ll_inclinometer_type) {
            showInclinometerTypeDialog();

        } else if (id == R.id.ll_data_settlement_method) {
            showDataSettlementMethodDialog();

        } else if (id == R.id.btn_confirm) {
            KeyBordUtils.hideSoftKeyboard(view);
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            if (!checkValueIsValid()) {
                Timber.w("参数存在错误!");
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

    /**
     * 选择数据结算方式
     */
    private void showDataSettlementMethodDialog() {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", new String[]{"顶固定法", "底固定法"},
                        null, dataSettlementMethodPos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                dataSettlementMethodPos = position;
                                mTvDataSettlementMethod.setText(text);
                                if (position == 0) {
                                    dataSettlementMethod = "0";
                                } else {
                                    dataSettlementMethod = "1";
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
                ToastUtils.show("请输入采集器地址!");
                mEtMacAddress.requestFocus();
                return false;
            }
            if (Integer.parseInt(address) < 0 || Integer.parseInt(address) > 32) {
                ToastUtils.show("请输入正确的采集器地址!");
                mEtCollectorAddress.requestFocus();
                return false;
            }
        } else {
            if (TextUtils.isEmpty(address)) {
                ToastUtils.show("请输入Mac地址!");
                mEtMacAddress.requestFocus();
                return false;
            }
            if (!ValidateUtil.isValidMacAddress(address)) {
                ToastUtils.show("请输入正确的Mac地址!");
                mEtMacAddress.requestFocus();
                return false;
            }
        }

        if (TextUtils.isEmpty(inclinometerTubeHoleDepth)) {
            ToastUtils.show("请输入测斜管孔深!");
            mEtInclinometerTubeHoleDepth.requestFocus();
            return false;
        }
        try {
            double value = Double.parseDouble(inclinometerTubeHoleDepth);
            if (value < 1) {
                ToastUtils.show("请输入正确的测斜管孔深!");
                mEtInclinometerTubeHoleDepth.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的测斜管孔深!");
            mEtInclinometerTubeHoleDepth.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(decentralizationSpeed)) {
            ToastUtils.show("请输入下放速度!");
            mEtDecentralizationSpeed.requestFocus();
            return false;
        }
        try {
            int port = Integer.parseInt(decentralizationSpeed);
            if (port < 1 || port > 100) {
                ToastUtils.show("请输入正确的下放速度!");
                mEtDecentralizationSpeed.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的下放速度!");
            mEtDecentralizationSpeed.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(decentralizationWaitingTime)) {
            ToastUtils.show("请输入下放等待时间!");
            mEtDecentralizationWaitingTime.requestFocus();
            return false;
        }
        try {
            int port = Integer.parseInt(decentralizationWaitingTime);
            if (port < 1 || port > 32) {
                ToastUtils.show("请输入正确的下放等待时间!");
                mEtDecentralizationWaitingTime.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的下放等待时间!");
            mEtDecentralizationWaitingTime.requestFocus();
            return false;
        }

        return true;
    }

    private void processSave() {
        try {
            AdmeBasicConfigEntity entity = new AdmeBasicConfigEntity();
            entity.setInctype(inclinometerType);
            entity.setAddress(address);

            decimalFormat.applyPattern("#.##");
            entity.setInterdeep(decimalFormat.format(Double.parseDouble(inclinometerTubeHoleDepth)));
            entity.setDownspeed(decentralizationSpeed);
            entity.setDownwaitetime(decentralizationWaitingTime);
            entity.setDatatype(dataSettlementMethod);

            errMsg = "发送指令超时,请稍后尝试";
            startProgressRunnable("处理中...", WRITE_TIME_OUT_SECOND);
            mBtnSave.setEnabled(false);
            String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_BASIC_PARAMETERS, entity);
            sendCommand(command);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void doProgressRun() {
        super.doProgressRun();
        mBtnSave.setEnabled(true);
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
                IOTCommandResult<AdmeBasicConfigInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取设备的基础配置参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                basicConfigParam = commandResult.getResult();
                initParamConfigInfo();
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
                super.parseResponseMessage(cmdStr);
                break;
        }
    }

    private void doAfterSetting() {
        if (basicConfigParam != null) {
            basicConfigParam.setInctype(inclinometerType);
            basicConfigParam.setAddress(address);
            basicConfigParam.setInterdeep(inclinometerTubeHoleDepth);
            basicConfigParam.setDownspeed(decentralizationSpeed);
            basicConfigParam.setDownwaitetime(decentralizationWaitingTime);
            basicConfigParam.setDatatype(dataSettlementMethod);
        }
        //TODO  打开注释，设置为浏览模式
//        configPageViewModel.configPageEditableChanged.setValue(false);
        mBtnSave.setEnabled(true);
        ToastUtils.show("保存成功");
    }

    private void initParamConfigInfo() {
        if (basicConfigParam == null) {
            Timber.e("AdmeBasicConfigParam is Null!");
            basicConfigParam = new AdmeBasicConfigInfo();
            return;
        }
        try {
            inclinometerTypeOld = basicConfigParam.getInctype().trim();
            inclinometerType = basicConfigParam.getInctype().trim();
            address = basicConfigParam.getAddress().trim();
            inclinometerTubeHoleDepth = basicConfigParam.getInterdeep().trim();
            decentralizationSpeed = basicConfigParam.getDownspeed().trim();
            decentralizationWaitingTime = basicConfigParam.getDownwaitetime().trim();
            dataSettlementMethodOld = basicConfigParam.getDatatype().trim();
            dataSettlementMethod = basicConfigParam.getDatatype().trim();

            decimalFormat.applyPattern("#.##");
            inclinometerTubeHoleDepth = decimalFormat.format(Double.parseDouble(inclinometerTubeHoleDepth));
            mEtInclinometerTubeHoleDepth.setText(inclinometerTubeHoleDepth);
            mEtDecentralizationSpeed.setText(decentralizationSpeed);
            mEtDecentralizationWaitingTime.setText(decentralizationWaitingTime);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

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

        if (dataSettlementMethodOld.equals("0")) {
            dataSettlementMethodPos = 0;
            mTvDataSettlementMethod.setText("顶固定法");
        } else {
            dataSettlementMethodPos = 1;
            mTvDataSettlementMethod.setText("底固定法");
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
        if (!configPageViewModel.configPageEditableChanged.getValue())
            return false;

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

        if (dataSettlementMethodOld != null && dataSettlementMethod != null && !dataSettlementMethodOld.equals(dataSettlementMethod)) {
            return true;
        }
        return false;
    }

    @Override
    protected void onEditableChanged(boolean isEditable) {
        if (isEditable) {
            mTvInclinometerType.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_right, 0);
            mEtCollectorAddress.setHint("请输入");
            mEtMacAddress.setHint("请输入");
            mEtInclinometerTubeHoleDepth.setHint("请输入");
            mEtDecentralizationSpeed.setHint("请输入");
            mEtDecentralizationWaitingTime.setHint("请输入");
            mTvDataSettlementMethod.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_right, 0);
        } else {
            mTvInclinometerType.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            mEtCollectorAddress.setHint("");
            mEtMacAddress.setHint("");
            mEtInclinometerTubeHoleDepth.setHint("");
            mEtDecentralizationSpeed.setHint("");
            mEtDecentralizationWaitingTime.setHint("");
            mTvDataSettlementMethod.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

            mEtCollectorAddress.clearFocus();
            mEtMacAddress.clearFocus();
            mEtInclinometerTubeHoleDepth.clearFocus();
            mEtDecentralizationSpeed.clearFocus();
            mEtDecentralizationWaitingTime.clearFocus();

            initParamConfigInfo();
        }
        mBtnSave.setVisibility(isEditable ? View.VISIBLE : View.GONE);
    }
}