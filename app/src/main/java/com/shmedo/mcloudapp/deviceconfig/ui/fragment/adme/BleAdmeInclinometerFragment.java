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
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeInclinometerEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeInclinometerInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/12/28<br/>
 * 描述：     ADME 测斜仪参数配置页面
 */
public class BleAdmeInclinometerFragment extends BaseBleIotCommunicateFragment {
    @BindView(R.id.tv_inclinometer_type)
    TextView mTvInclinometerType;

    @BindView(R.id.tv_low_power_mode)
    TextView mTvLowPowerMode;

    @BindView(R.id.et_collector_address)
    ClearEditText mEtCollectorAddress;//采集器地址

    @BindView(R.id.et_mac_address)
    ClearEditText mEtMacAddress;//Mac 地址

    @BindView(R.id.et_collection_interval)
    ClearEditText mEtCollectionInterval;

    @BindView(R.id.et_solving_interval)
    ClearEditText mEtSolvingInterval;

    @BindView(R.id.et_sleep_time)
    ClearEditText mEtSleepTime;

    @BindView(R.id.et_correction_value)
    ClearEditText mEtCorrectionValue;

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    @BindView(R.id.ll_collector_address)
    ViewGroup collectorAddressLayout;

    @BindView(R.id.ll_mac_address)
    ViewGroup macAddressLayout;

    private AdmeInclinometerInfo admeInclinometerInfo;

    private int inclinometerTypePos;
    private int lowPowerModePos;

    private String inclinometerTypeOld;//测斜仪类型
    private String inclinometerType;// 测斜仪类型
    private String lowPowerModeOld;//低功耗模式
    private String lowPowerMode;// 低功耗模式
    private String address;// 采集器地址/Mac 地址
    private String collectionInterval;//采集器采集间隔
    private String solvingInterval;//采集器解算间隔
    private String sleepTime;//休眠时间
    private String correctionValue;//测斜仪修正值

    public static BleAdmeInclinometerFragment newInstance() {
        return new BleAdmeInclinometerFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ble_adme_inclinometer_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setView();
        queryParamConfigInfo();
    }

    private void setView() {
        mEtCollectorAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtMacAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(17)});
        mEtCollectionInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtSolvingInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtSleepTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtCorrectionValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
    }

    /**
     * 获取ADME的测斜仪参数
     */
    private void queryParamConfigInfo() {
        errMsg = "查询数据超时,请稍后尝试";
        startProgressRunnable("加载中...", WRITE_TIME_OUT_SECOND);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_INCLINOMETER_PARAMETERS);
        sendCommand(command);
    }

    @OnClick({R.id.ll_inclinometer_type, R.id.ll_low_power_mode, R.id.btn_confirm})
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.ll_inclinometer_type) {
            showInclinometerTypeDialog();

        } else if (id == R.id.ll_low_power_mode) {
            showLowPowerModeDialog();

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

    /**
     * 选择低功耗模式
     */
    private void showLowPowerModeDialog() {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", new String[]{"关闭", "开启"},
                        null, lowPowerModePos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                lowPowerModePos = position;
                                mTvLowPowerMode.setText(text);
                                if (position == 0) {
                                    lowPowerMode = "0";
                                } else {
                                    lowPowerMode = "1";
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_match)
                .show();
    }

    private boolean checkValueIsValid() {
        address = mEtCollectorAddress.getText().toString().trim();
        collectionInterval = mEtCollectionInterval.getText().toString().trim();
        solvingInterval = mEtSolvingInterval.getText().toString().trim();
        sleepTime = mEtSleepTime.getText().toString().trim();
        correctionValue = mEtCorrectionValue.getText().toString().trim();

        if (inclinometerType.equals("0")) {
            if (TextUtils.isEmpty(address)) {
                ToastUtils.show("采集器地址不能为空!");
                mEtCollectorAddress.requestFocus();
                return false;
            }

            if (!ValidateUtil.isInteger(address)) {
                ToastUtils.show("请输入正确的采集器地址!");
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

        if (TextUtils.isEmpty(collectionInterval)) {
            ToastUtils.show("采集器采集间隔不能为空!");
            mEtCollectionInterval.requestFocus();
            return false;
        }
        if (!ValidateUtil.isInteger(collectionInterval)) {
            ToastUtils.show("请输入正确的采集器采集间隔!");
            mEtCollectionInterval.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(solvingInterval)) {
            ToastUtils.show("采集器解算间隔不能为空!");
            mEtSolvingInterval.requestFocus();
            return false;
        }
        if (!ValidateUtil.isInteger(solvingInterval)) {
            ToastUtils.show("请输入正确的采集器解算间隔!");
            mEtSolvingInterval.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(sleepTime)) {
            ToastUtils.show("休眠时间不能为空!");
            mEtSleepTime.requestFocus();
            return false;
        }
        if (!ValidateUtil.isInteger(sleepTime)) {
            ToastUtils.show("请输入正确的休眠时间!");
            mEtSleepTime.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(correctionValue)) {
            ToastUtils.show("测斜仪修正值不能为空!");
            mEtCorrectionValue.requestFocus();
            return false;
        }
        try {
            double value = Double.parseDouble(correctionValue);

        } catch (Exception ex) {
            ToastUtils.show("请输入正确的测斜仪修正值!");
            mEtCorrectionValue.requestFocus();
            return false;
        }
        return true;
    }

    private void processSave() {
        AdmeInclinometerEntity entity = new AdmeInclinometerEntity();
        entity.setInctype(inclinometerType);
        entity.setLowpower(lowPowerMode);
        entity.setAddress(address);
        entity.setCollinval(collectionInterval);
        entity.setCalcinval(solvingInterval);
        entity.setDormancytime(sleepTime);
        entity.setInterupdate(correctionValue);

        errMsg = "发送指令超时,请稍后尝试";
        startProgressRunnable("正在发送配置指令...", WRITE_TIME_OUT_SECOND);
        mBtnSave.setEnabled(false);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_INCLINOMETER_PARAMETERS, entity);
        sendCommand(command);
    }

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case ADME_MD_GET_INCLINOMETER_PARAMETERS: {//获取ADME的测斜仪配置参数
                stopProgressRunnable();
                IOTCommandResult<AdmeInclinometerInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取设备的测斜仪参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                admeInclinometerInfo = commandResult.getResult();
                initParamConfigInfo();
            }
            break;

            case ADME_MD_SET_INCLINOMETER_PARAMETERS: {//设置ADME的测斜仪配置参数
                stopProgressRunnable();
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "保存测斜仪参数出错!", cmdResult.getReason());
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
        lowPowerModeOld = lowPowerMode;
        ToastUtils.show("保存成功");
    }

    private void initParamConfigInfo() {
        if (admeInclinometerInfo == null) {
            Timber.e("AdmeInclinometerInfo is Null!");
            admeInclinometerInfo = new AdmeInclinometerInfo();
            return;
        }

        inclinometerTypeOld = admeInclinometerInfo.getInctype().trim();
        inclinometerType = admeInclinometerInfo.getInctype().trim();
        lowPowerModeOld = admeInclinometerInfo.getLowpower().trim();
        lowPowerMode = admeInclinometerInfo.getLowpower().trim();
        address = admeInclinometerInfo.getAddress().trim();
        collectionInterval = admeInclinometerInfo.getCollinval().trim();
        solvingInterval = admeInclinometerInfo.getCalcinval().trim();
        sleepTime = admeInclinometerInfo.getDormancytime().trim();
        correctionValue = admeInclinometerInfo.getInterupdate().trim();

        mEtCollectionInterval.setText(collectionInterval);
        mEtSolvingInterval.setText(solvingInterval);
        mEtSleepTime.setText(sleepTime);
        mEtCorrectionValue.setText(correctionValue);

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

        if (lowPowerModeOld.equals("0")) {
            lowPowerModePos = 0;
            mTvLowPowerMode.setText("关闭");
        } else {
            lowPowerModePos = 1;
            mTvLowPowerMode.setText("开启");
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

        if (lowPowerModeOld != null && lowPowerMode != null && !lowPowerModeOld.equals(lowPowerMode)) {
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

        if (collectionInterval != null && !collectionInterval.equals(mEtCollectionInterval.getText().toString().trim())) {
            return true;
        }

        if (solvingInterval != null && !solvingInterval.equals(mEtSolvingInterval.getText().toString().trim())) {
            return true;
        }

        if (sleepTime != null && !sleepTime.equals(mEtSleepTime.getText().toString().trim())) {
            return true;
        }

        if (correctionValue != null && !correctionValue.equals(mEtCorrectionValue.getText().toString().trim())) {
            return true;
        }
        return false;
    }
}