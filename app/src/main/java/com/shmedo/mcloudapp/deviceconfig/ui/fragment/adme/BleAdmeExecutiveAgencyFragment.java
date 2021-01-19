package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeExecutiveAgencyEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeExecutiveAgencyInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/12/28<br/>
 * 描述：     ADME 执行机构配置页面
 */
public class BleAdmeExecutiveAgencyFragment extends BaseBleIotCommunicateFragment {

    @BindView(R.id.tv_data_settlement_method)
    TextView mTvDataSettlementMethod;

    @BindView(R.id.tv_data_response)
    TextView mTvDataResponse;

    @BindView(R.id.et_waiting_interval_per_round)
    ClearEditText mEtWaitingIntervalPerRound;

    @BindView(R.id.et_data_reading_interval)
    ClearEditText mEtDataReadingInterval;

    @BindView(R.id.et_measurement_compensation_time)
    ClearEditText mEtMeasurementCompensationTime;

    @BindView(R.id.et_motor_drive_address)
    ClearEditText mEtMotorDriveAddress;

    @BindView(R.id.et_decentralization_speed)
    ClearEditText mEtDecentralizationSpeed;//下放速度(r/min)

    @BindView(R.id.et_inclination_tube_hole_depth)
    ClearEditText mEtInclinometerTubeHoleDepth;//测斜管孔深(m)

    @BindView(R.id.et_decentralization_waiting_time)
    ClearEditText mEtDecentralizationWaitingTime;//下放等待时间(min)

    @BindView(R.id.et_pull_up_speed)
    ClearEditText mEtPullUpSpeed;

    @BindView(R.id.et_measuring_distance)
    ClearEditText mEtMeasuringDistance;

    @BindView(R.id.et_measurement_interval_time)
    ClearEditText mEtMeasurementIntervalTime;

    @BindView(R.id.et_measuring_reference_depth)
    ClearEditText mEtMeasuringReferenceDepth;

    @BindView(R.id.decentralizedPredictionEnableSBtn)
    SwitchButton mSbDecentralizedPredictionEnable;

    @BindView(R.id.et_pulses_per_unit_time)
    ClearEditText mEtPulsesPerUnitTime;

    @BindView(R.id.et_detection_time)
    ClearEditText mEtDetectionTime;

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    @BindView(R.id.decentralizedPredictionLayout)
    ViewGroup decentralizedPredictionLayout;

    @BindView(R.id.maskLayerLayout)
    ViewGroup maskLayerLayout;

    private AdmeExecutiveAgencyInfo admeExecutiveAgencyInfo;

    private int dataSettlementMethodPos;
    private int dataResponsePos;

    private String dataSettlementMethodOld;//数据结算方式
    private String dataSettlementMethod;// 数据结算方式
    private String dataResponseOld;//数据应答（0:关闭，1:启用）
    private String dataResponse;// 数据应答（0:关闭，1:启用）
    private String waitingIntervalPerRound;// 每轮等待时间
    private String dataReadingInterval;// 数据读取间隔
    private String measurementCompensationTime;// 测量补偿时间
    private String motorDriveAddress;// 电机驱动器地址
    private String decentralizationSpeed;// 下放速度(r/min)
    private String inclinometerTubeHoleDepth;// 测斜管孔深(m)
    private String decentralizationWaitingTime;//下放等待时间(min)
    private String pullUpSpeed;// 电机上拉速度
    private String measuringDistance;// 测量间距
    private String measurementIntervalTime;// 测量间隔时间
    private String measuringReferenceDepth;// 测量基准深度
    private String pulsesPerUnitTime;// 堵转单位时间脉冲数
    private String detectionTime;// 堵转检测判断时间

    private DecimalFormat decimalFormat = new DecimalFormat();


    public static BleAdmeExecutiveAgencyFragment newInstance() {
        return new BleAdmeExecutiveAgencyFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ble_adme_executive_agency_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setView();
        setSwitchViewListener();
        queryParamInfo();
        //TODO 设备处于自动监测模式时，不可编辑参数(后期还要考虑点击编辑按钮时的页面状态切换)
        if (admeViewModel.deviceMode == 0) {
            configPageViewModel.configPageEditableChanged.setValue(true);
        } else {
            configPageViewModel.configPageEditableChanged.setValue(false);
        }
    }

    private void setView() {
        mEtWaitingIntervalPerRound.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        mEtDataReadingInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtMeasurementCompensationTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtMotorDriveAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        mEtDecentralizationSpeed.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtDecentralizationSpeed.setHint("1-100");

        mEtInclinometerTubeHoleDepth.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtDecentralizationWaitingTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        mEtDecentralizationWaitingTime.setHint("1-32");

        mEtPullUpSpeed.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtPullUpSpeed.setHint("1-100");

        mEtMeasuringDistance.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtMeasurementIntervalTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtMeasuringReferenceDepth.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
        mEtPulsesPerUnitTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtDetectionTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
    }

    private void setSwitchViewListener() {
        mSbDecentralizedPredictionEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    mSbDecentralizedPredictionEnable.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }

                if (!isChecked) {
                    decentralizedPredictionLayout.setVisibility(View.GONE);
                } else {
                    decentralizedPredictionLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * 获取执行机构参数
     */
    private void queryParamInfo() {
        errMsg = "查询数据超时,请稍后尝试";
        startProgressRunnable("加载中...", WRITE_TIME_OUT_SECOND);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_EXECUTIVE_AGENCY_PARAMETERS);
        sendCommand(command);
    }

    @OnClick({R.id.ll_data_settlement_method, R.id.ll_data_response, R.id.btn_confirm})
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.ll_data_settlement_method) {
            showDataSettlementMethodDialog();

        } else if (id == R.id.ll_data_response) {
            showDataResponseDialog();

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

    /**
     * 选择数据应答方式
     */
    private void showDataResponseDialog() {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", new String[]{"关闭", "启用"},
                        null, dataResponsePos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                dataResponsePos = position;
                                mTvDataResponse.setText(text);
                                if (position == 0) {
                                    dataResponse = "0";
                                } else {
                                    dataResponse = "1";
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_match)
                .show();
    }

    private boolean checkValueIsValid() {
        waitingIntervalPerRound = mEtWaitingIntervalPerRound.getText().toString().trim();
        dataReadingInterval = mEtDataReadingInterval.getText().toString().trim();
        measurementCompensationTime = mEtMeasurementCompensationTime.getText().toString().trim();
        motorDriveAddress = mEtMotorDriveAddress.getText().toString().trim();
        decentralizationSpeed = mEtDecentralizationSpeed.getText().toString().trim();
        inclinometerTubeHoleDepth = mEtInclinometerTubeHoleDepth.getText().toString().trim();
        decentralizationWaitingTime = mEtDecentralizationWaitingTime.getText().toString().trim();
        pullUpSpeed = mEtPullUpSpeed.getText().toString().trim();
        measuringDistance = mEtMeasuringDistance.getText().toString().trim();
        measurementIntervalTime = mEtMeasurementIntervalTime.getText().toString().trim();
        measuringReferenceDepth = mEtMeasuringReferenceDepth.getText().toString().trim();
        pulsesPerUnitTime = mEtPulsesPerUnitTime.getText().toString().trim();
        detectionTime = mEtDetectionTime.getText().toString().trim();

        if (TextUtils.isEmpty(waitingIntervalPerRound)) {
            ToastUtils.show("请输入每轮等待时间!");
            mEtWaitingIntervalPerRound.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(waitingIntervalPerRound);
            if (value < 1) {
                ToastUtils.show("请输入正确的每轮等待时间!");
                mEtWaitingIntervalPerRound.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的每轮等待时间!");
            mEtWaitingIntervalPerRound.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(dataReadingInterval)) {
            ToastUtils.show("请输入数据读取间隔!");
            mEtDataReadingInterval.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(dataReadingInterval);
            if (value < 1) {
                ToastUtils.show("请输入正确的数据读取间隔!");
                mEtDataReadingInterval.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的数据读取间隔!");
            mEtDataReadingInterval.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(measurementCompensationTime)) {
            ToastUtils.show("请输入测量补偿时间!");
            mEtMeasurementCompensationTime.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(measurementCompensationTime);
            if (value < 1) {
                ToastUtils.show("请输入正确的测量补偿时间!");
                mEtMeasurementCompensationTime.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的测量补偿时间!");
            mEtMeasurementCompensationTime.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(motorDriveAddress)) {
            ToastUtils.show("请输入电机驱动器地址!");
            mEtMotorDriveAddress.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(motorDriveAddress);
            if (value < 0 || value > 99) {
                ToastUtils.show("请输入正确的电机驱动器地址!");
                mEtMotorDriveAddress.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的电机驱动器地址!");
            mEtMotorDriveAddress.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(decentralizationSpeed)) {
            ToastUtils.show("请输入电机下放速度!");
            mEtDecentralizationSpeed.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(decentralizationSpeed);
            if (value < 1 || value > 100) {
                ToastUtils.show("请输入正确的电机下放速度!");
                mEtDecentralizationSpeed.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的电机下放速度!");
            mEtDecentralizationSpeed.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(inclinometerTubeHoleDepth)) {
            ToastUtils.show("请输入测斜管孔深!");
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

        if (TextUtils.isEmpty(decentralizationWaitingTime)) {
            ToastUtils.show("请输入下放等待时间!");
            mEtDecentralizationWaitingTime.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(decentralizationWaitingTime);
            if (value < 1 || value > 32) {
                ToastUtils.show("请输入正确的下放等待时间!");
                mEtDecentralizationWaitingTime.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的下放等待时间!");
            mEtDecentralizationWaitingTime.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(pullUpSpeed)) {
            ToastUtils.show("请输入电机上拉速度!");
            mEtPullUpSpeed.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(pullUpSpeed);
            if (value < 1 || value > 100) {
                ToastUtils.show("请输入正确的电机上拉速度!");
                mEtPullUpSpeed.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的电机上拉速度!");
            mEtPullUpSpeed.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(measuringDistance)) {
            ToastUtils.show("请输入测量间距!");
            mEtMeasuringDistance.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(measuringDistance);
            if (value < 1) {
                ToastUtils.show("请输入正确的测量间距!");
                mEtMeasuringDistance.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的测量间距!");
            mEtMeasuringDistance.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(measurementIntervalTime)) {
            ToastUtils.show("请输入测量间隔时间!");
            mEtMeasurementIntervalTime.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(measurementIntervalTime);
            if (value < 1) {
                ToastUtils.show("请输入正确的测量间隔时间!");
                mEtMeasurementIntervalTime.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的测量间隔时间!");
            mEtMeasurementIntervalTime.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(measuringReferenceDepth)) {
            ToastUtils.show("请输入测量基准深度!");
            mEtMeasuringReferenceDepth.requestFocus();
            return false;
        }
        try {
            double value = Double.parseDouble(measuringReferenceDepth);

        } catch (Exception ex) {
            ToastUtils.show("请输入正确的测量基准深度!");
            mEtMeasuringReferenceDepth.requestFocus();
            return false;
        }

        if (mSbDecentralizedPredictionEnable.isChecked()) {
            if (TextUtils.isEmpty(pulsesPerUnitTime)) {
                ToastUtils.show("请输入堵转单位时间脉冲数!");
                mEtPulsesPerUnitTime.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(pulsesPerUnitTime);
                if (value < 1) {
                    ToastUtils.show("请输入正确的堵转单位时间脉冲数!");
                    mEtPulsesPerUnitTime.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的堵转单位时间脉冲数!");
                mEtPulsesPerUnitTime.requestFocus();
                return false;
            }

            if (TextUtils.isEmpty(detectionTime)) {
                ToastUtils.show("请输入堵转检测判断时间!");
                mEtDetectionTime.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(detectionTime);

            } catch (Exception ex) {
                ToastUtils.show("请输入正确的堵转检测判断时间!");
                mEtDetectionTime.requestFocus();
                return false;
            }
        }
        return true;
    }

    private void processSave() {
        try {
            AdmeExecutiveAgencyEntity entity = new AdmeExecutiveAgencyEntity();
            entity.setDatatype(dataSettlementMethod);
            entity.setDatareply(dataResponse);
            entity.setRoundwaitetime(waitingIntervalPerRound);
            entity.setDatainval(dataReadingInterval);
            entity.setCompensatetime(measurementCompensationTime);
            entity.setDriveaddress(motorDriveAddress);
            entity.setDownspeed(decentralizationSpeed);
            decimalFormat.applyPattern("#.##");
            entity.setInterdeep(decimalFormat.format(Double.parseDouble(inclinometerTubeHoleDepth)));
            entity.setDownwaitetime(decentralizationWaitingTime);
            entity.setUpspeed(pullUpSpeed);
            entity.setMeaspacing(measuringDistance);
            entity.setMeaintertime(measurementIntervalTime);
            decimalFormat.applyPattern("#.##");
            entity.setMeabaseth(decimalFormat.format(Double.parseDouble(measuringReferenceDepth)));

            if (mSbDecentralizedPredictionEnable.isChecked()) {
                entity.setUntimenum(pulsesPerUnitTime);
                decimalFormat.applyPattern("#.##");
                entity.setDetectiontime(decimalFormat.format(Double.parseDouble(detectionTime)));
            }

            errMsg = "发送指令超时,请稍后尝试";
            startProgressRunnable("正在发送配置指令...", WRITE_TIME_OUT_SECOND);
            mBtnSave.setEnabled(false);
            String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_EXECUTIVE_AGENCY_PARAMETERS, entity);
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
            case ADME_MD_GET_EXECUTIVE_AGENCY_PARAMETERS: {//获取ADME的执行机构配置参数
                stopProgressRunnable();
                IOTCommandResult<AdmeExecutiveAgencyInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询步进电机参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                admeExecutiveAgencyInfo = commandResult.getResult();
                initParamConfigInfo();
            }
            break;

            case ADME_MD_SET_EXECUTIVE_AGENCY_PARAMETERS: {//设置ADME的执行机构配置参数
                stopProgressRunnable();
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置步进电机参数出错!", cmdResult.getReason());
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
        if (admeExecutiveAgencyInfo != null) {
            admeExecutiveAgencyInfo.setDatatype(dataSettlementMethod);
            admeExecutiveAgencyInfo.setDatareply(dataResponse);
            admeExecutiveAgencyInfo.setRoundwaitetime(waitingIntervalPerRound);
            admeExecutiveAgencyInfo.setDatainval(dataReadingInterval);
            admeExecutiveAgencyInfo.setCompensatetime(measurementCompensationTime);
            admeExecutiveAgencyInfo.setDriveaddress(motorDriveAddress);
            admeExecutiveAgencyInfo.setDownspeed(decentralizationSpeed);
            admeExecutiveAgencyInfo.setInterdeep(inclinometerTubeHoleDepth);
            admeExecutiveAgencyInfo.setDownwaitetime(decentralizationWaitingTime);
            admeExecutiveAgencyInfo.setUpspeed(pullUpSpeed);
            admeExecutiveAgencyInfo.setMeaspacing(measuringDistance);
            admeExecutiveAgencyInfo.setMeaintertime(measurementIntervalTime);
            admeExecutiveAgencyInfo.setMeabaseth(measuringReferenceDepth);
            admeExecutiveAgencyInfo.setUntimenum(pulsesPerUnitTime);
            admeExecutiveAgencyInfo.setDetectiontime(detectionTime);
        }
        //TODO  打开注释，设置为浏览模式
//        configPageViewModel.configPageEditableChanged.setValue(false);
        dataSettlementMethodOld = dataSettlementMethod;
        dataResponseOld = dataResponse;
        mBtnSave.setEnabled(true);
        ToastUtils.show("保存成功");
    }

    private void initParamConfigInfo() {
        if (admeExecutiveAgencyInfo == null) {
            Timber.e("AdmeExecutiveAgencyInfo is Null!");
            admeExecutiveAgencyInfo = new AdmeExecutiveAgencyInfo();
            return;
        }
        dataSettlementMethodOld = admeExecutiveAgencyInfo.getDatatype().trim();
        dataSettlementMethod = admeExecutiveAgencyInfo.getDatatype().trim();
        dataResponseOld = admeExecutiveAgencyInfo.getDatareply().trim();
        dataResponse = admeExecutiveAgencyInfo.getDatareply().trim();
        waitingIntervalPerRound = admeExecutiveAgencyInfo.getRoundwaitetime().trim();
        dataReadingInterval = admeExecutiveAgencyInfo.getDatainval().trim();
        measurementCompensationTime = admeExecutiveAgencyInfo.getCompensatetime().trim();
        motorDriveAddress = admeExecutiveAgencyInfo.getDriveaddress().trim();
        decentralizationSpeed = admeExecutiveAgencyInfo.getDownspeed().trim();
        inclinometerTubeHoleDepth = admeExecutiveAgencyInfo.getInterdeep().trim();
        decentralizationWaitingTime = admeExecutiveAgencyInfo.getDownwaitetime().trim();
        pullUpSpeed = admeExecutiveAgencyInfo.getUpspeed().trim();
        measuringDistance = admeExecutiveAgencyInfo.getMeaspacing().trim();
        measurementIntervalTime = admeExecutiveAgencyInfo.getMeaintertime().trim();
        measuringReferenceDepth = admeExecutiveAgencyInfo.getMeabaseth().trim();
        pulsesPerUnitTime = admeExecutiveAgencyInfo.getUntimenum().trim();
        detectionTime = admeExecutiveAgencyInfo.getDetectiontime().trim();

        if (dataSettlementMethodOld.equals("0")) {
            dataSettlementMethodPos = 0;
            mTvDataSettlementMethod.setText("顶固定法");
        } else {
            dataSettlementMethodPos = 1;
            mTvDataSettlementMethod.setText("底固定法");
        }

        if (dataResponseOld.equals("0")) {
            dataResponsePos = 0;
            mTvDataResponse.setText("关闭");
        } else {
            dataResponsePos = 1;
            mTvDataResponse.setText("启用");
        }
        try {
            mEtWaitingIntervalPerRound.setText(waitingIntervalPerRound);
            mEtDataReadingInterval.setText(dataReadingInterval);
            mEtMeasurementCompensationTime.setText(measurementCompensationTime);
            mEtMotorDriveAddress.setText(motorDriveAddress);

            decimalFormat.applyPattern("#.##");
            inclinometerTubeHoleDepth = decimalFormat.format(Double.parseDouble(inclinometerTubeHoleDepth));
            mEtInclinometerTubeHoleDepth.setText(inclinometerTubeHoleDepth);
            mEtDecentralizationSpeed.setText(decentralizationSpeed);
            mEtDecentralizationWaitingTime.setText(decentralizationWaitingTime);

            mEtPullUpSpeed.setText(pullUpSpeed);
            mEtMeasuringDistance.setText(measuringDistance);
            mEtMeasurementIntervalTime.setText(measurementIntervalTime);

            decimalFormat.applyPattern("#.##");
            measuringReferenceDepth = decimalFormat.format(Double.parseDouble(measuringReferenceDepth));
            mEtMeasuringReferenceDepth.setText(measuringReferenceDepth);
            mEtPulsesPerUnitTime.setText(pulsesPerUnitTime);

            decimalFormat.applyPattern("#.##");
            detectionTime = decimalFormat.format(Double.parseDouble(detectionTime));
            mEtDetectionTime.setText(detectionTime);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (admeExecutiveAgencyInfo.getDwonblocked().equals("0")) {
            decentralizedPredictionLayout.setVisibility(View.GONE);
        } else if (admeExecutiveAgencyInfo.getDwonblocked().equals("1")) {
            decentralizedPredictionLayout.setVisibility(View.VISIBLE);
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

        if (dataSettlementMethodOld != null && dataSettlementMethod != null && !dataSettlementMethodOld.equals(dataSettlementMethod)) {
            return true;
        }
        if (dataResponseOld != null && dataResponse != null && !dataResponseOld.equals(dataResponse)) {
            return true;
        }
        if (waitingIntervalPerRound != null && !waitingIntervalPerRound.equals(mEtWaitingIntervalPerRound.getText().toString().trim())) {
            return true;
        }
        if (dataReadingInterval != null && !dataReadingInterval.equals(mEtDataReadingInterval.getText().toString().trim())) {
            return true;
        }
        if (measurementCompensationTime != null && !measurementCompensationTime.equals(mEtMeasurementCompensationTime.getText().toString().trim())) {
            return true;
        }
        if (motorDriveAddress != null && !motorDriveAddress.equals(mEtMotorDriveAddress.getText().toString().trim())) {
            return true;
        }
        if (decentralizationSpeed != null && !decentralizationSpeed.equals(mEtDecentralizationSpeed.getText().toString().trim())) {
            return true;
        }
        if (inclinometerTubeHoleDepth != null && !inclinometerTubeHoleDepth.equals(mEtInclinometerTubeHoleDepth.getText().toString().trim())) {
            return true;
        }
        if (decentralizationWaitingTime != null && !decentralizationWaitingTime.equals(mEtDecentralizationWaitingTime.getText().toString().trim())) {
            return true;
        }
        if (pullUpSpeed != null && !pullUpSpeed.equals(mEtPullUpSpeed.getText().toString().trim())) {
            return true;
        }
        if (measuringDistance != null && !measuringDistance.equals(mEtMeasuringDistance.getText().toString().trim())) {
            return true;
        }
        if (measurementIntervalTime != null && !measurementIntervalTime.equals(mEtMeasurementIntervalTime.getText().toString().trim())) {
            return true;
        }
        if (measuringReferenceDepth != null && !measuringReferenceDepth.equals(mEtMeasuringReferenceDepth.getText().toString().trim())) {
            return true;
        }

        if (mSbDecentralizedPredictionEnable.isChecked()) {
            if (pulsesPerUnitTime != null && !pulsesPerUnitTime.equals(mEtPulsesPerUnitTime.getText().toString().trim())) {
                return true;
            }
            if (detectionTime != null && !detectionTime.equals(mEtDetectionTime.getText().toString().trim())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onEditableChanged(boolean isEditable) {
        if (isEditable) {
            mTvDataSettlementMethod.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_right, 0);
            mTvDataResponse.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_right, 0);

            mEtWaitingIntervalPerRound.setHint("请输入");
            mEtDataReadingInterval.setHint("请输入");
            mEtMeasurementCompensationTime.setHint("请输入");
            mEtMotorDriveAddress.setHint("请输入");
            mEtDecentralizationSpeed.setHint("1-100");
            mEtInclinometerTubeHoleDepth.setHint("请输入");
            mEtDecentralizationWaitingTime.setHint("1-32");
            mEtPullUpSpeed.setHint("1-100");
            mEtMeasuringDistance.setHint("请输入");
            mEtMeasurementIntervalTime.setHint("请输入");
            mEtMeasuringReferenceDepth.setHint("请输入");
            mEtPulsesPerUnitTime.setHint("请输入");
            mEtDetectionTime.setHint("请输入");

        } else {
            mTvDataSettlementMethod.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            mTvDataResponse.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            mEtWaitingIntervalPerRound.setHint("");
            mEtDataReadingInterval.setHint("");
            mEtMeasurementCompensationTime.setHint("");
            mEtMotorDriveAddress.setHint("");
            mEtDecentralizationSpeed.setHint("");
            mEtInclinometerTubeHoleDepth.setHint("");
            mEtDecentralizationWaitingTime.setHint("");
            mEtPullUpSpeed.setHint("");
            mEtMeasuringDistance.setHint("");
            mEtMeasurementIntervalTime.setHint("");
            mEtMeasuringReferenceDepth.setHint("");
            mEtPulsesPerUnitTime.setHint("");
            mEtDetectionTime.setHint("");

            mEtWaitingIntervalPerRound.clearFocus();
            mEtDataReadingInterval.clearFocus();
            mEtMeasurementCompensationTime.clearFocus();
            mEtMotorDriveAddress.clearFocus();
            mEtDecentralizationSpeed.clearFocus();
            mEtInclinometerTubeHoleDepth.clearFocus();
            mEtDecentralizationWaitingTime.clearFocus();
            mEtPullUpSpeed.clearFocus();
            mEtMeasuringDistance.clearFocus();
            mEtMeasurementIntervalTime.clearFocus();
            mEtMeasuringReferenceDepth.clearFocus();
            mEtPulsesPerUnitTime.clearFocus();
            mEtDetectionTime.clearFocus();

            initParamConfigInfo();
        }
        maskLayerLayout.setVisibility(isEditable ? View.GONE : View.VISIBLE);
        mBtnSave.setVisibility(isEditable ? View.VISIBLE : View.GONE);
    }
}