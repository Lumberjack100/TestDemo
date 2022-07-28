package com.shmedo.mcloudapp.deviceconfig.view.hac;

import android.content.Context;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.constant.RegexConstants;
import com.blankj.utilcode.util.RegexUtils;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeExecutiveAgencyEntity;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.adme.AdmeExecutiveAgencyInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;

import java.text.DecimalFormat;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/7/12 <br/>
 * 描述：     ADME 执行结构参数配置项视图
 */
public class AdmeHacExecutiveAgencyView extends LinearLayout {
    @BindView(R.id.tv_data_settlement_method)
    TextView mTvDataSettlementMethod;

    @BindView(R.id.dataResponseEnableSBtn)
    public SwitchButton dataResponseEnableSBtn;

    @BindView(R.id.et_data_reading_interval)
    ClearEditText mEtDataReadingInterval;//数据读取间隔

    @BindView(R.id.et_measurement_compensation_time)
    ClearEditText mEtMeasurementCompensationTime;//测量补偿时间

    @BindView(R.id.et_motor_drive_address)
    ClearEditText mEtMotorDriveAddress;//电机驱动器地址

    @BindView(R.id.et_decentralization_speed)
    ClearEditText mEtDecentralizationSpeed;//下放速度(r/min)

    @BindView(R.id.et_decentralization_waiting_time)
    ClearEditText mEtDecentralizationWaitingTime;//下放等待时间(min)

    @BindView(R.id.et_pull_up_speed)
    ClearEditText mEtPullUpSpeed;//电机上拉速度

    @BindView(R.id.et_measuring_distance)
    ClearEditText mEtMeasuringDistance;//测量间距

    @BindView(R.id.et_measurement_interval_time)
    ClearEditText mEtMeasurementIntervalTime;//测量间隔时间

    @BindView(R.id.et_interval_compensation)
    ClearEditText mEtIntervalCompensation;//管口安全距离h1

    @BindView(R.id.et_interval_fitting)
    ClearEditText mEtIntervalFitting;//数据拟合区间h2

    @BindView(R.id.et_point_offset)
    ClearEditText mEtPointOffset;//测点偏移距离h3

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    @BindView(R.id.ll_data_settlement_method)
    ViewGroup dataSettlementMethodLayout;

    @BindView(R.id.ll_data_response)
    ViewGroup dataResponseLayout;

    @BindView(R.id.ll_data_reading_interval)
    ViewGroup dataReadingIntervalLayout;

    @BindView(R.id.ll_measurement_compensation_time)
    ViewGroup measurementCompensationTimeLayout;

    @BindView(R.id.ll_motor_drive_address)
    ViewGroup motorDriveAddressLayout;

    @BindView(R.id.ll_decentralization_speed)
    ViewGroup decentralizationSpeedLayout;

    @BindView(R.id.ll_decentralization_waiting_time)
    ViewGroup decentralizationWaitingTimeLayout;

    @BindView(R.id.ll_pull_up_speed)
    ViewGroup pullUpSpeedLayout;

    @BindView(R.id.ll_measuring_distance)
    ViewGroup measuringDistanceLayout;

    @BindView(R.id.ll_measurement_interval_time)
    ViewGroup measurementIntervalTimeLayout;

    @BindView(R.id.ll_interval_compensation)
    ViewGroup intervalCompensationLayout;

    @BindView(R.id.ll_interval_fitting)
    ViewGroup intervalFittingLayout;

    @BindView(R.id.ll_point_offset)
    ViewGroup pointOffsetLayout;

    private String dataSettlementMethodOld;//数据解算方式
    private String dataSettlementMethod;// 数据解算方式
    private String dataReadingInterval;// 数据读取间隔
    private String measurementCompensationTime;// 测量补偿时间
    private String motorDriveAddress;// 电机驱动器地址
    private String decentralizationSpeed;// 下放速度(r/min)
    private String decentralizationWaitingTime;//下放等待时间(min)
    private String pullUpSpeed;// 电机上拉速度
    private String measuringDistance;// 测量间距
    private String measurementIntervalTime;// 测量间隔时间
    private String intervalCompensation;// 管口安全距离h1
    private String intervalFitting;// 数据拟合区间h2
    private String pointOffset;// 测点偏移距离h3

    private final String[] settlementMethods = new String[]{"顶部固定法", "底部固定法"};

    private DecimalFormat decimalFormat = new DecimalFormat();
    public AdmeExecutiveAgencyInfo admeExecutiveAgencyInfo;


    public AdmeHacExecutiveAgencyView(Context context) {
        this(context, null);
    }

    public AdmeHacExecutiveAgencyView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdmeHacExecutiveAgencyView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关联布局文件
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.adme_hac_executive_agency_view, this, true);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        mEtDataReadingInterval.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtMeasurementCompensationTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtMotorDriveAddress.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        mEtDecentralizationSpeed.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtDecentralizationSpeed.setHint("1-180");
        mEtDecentralizationWaitingTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        mEtDecentralizationWaitingTime.setHint("1-32");
        mEtPullUpSpeed.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtPullUpSpeed.setHint("1-180");
        mEtMeasuringDistance.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtMeasurementIntervalTime.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtIntervalCompensation.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtIntervalFitting.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtPointOffset.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});

        mTvDataSettlementMethod.setText("顶部固定法");
        dataSettlementMethodOld = "0";
    }

    /**
     * 选择数据解算方式
     */
    public void showDataSettlementMethodDialog(Context context) {
        int pos = Arrays.asList(settlementMethods).indexOf(String.valueOf(mTvDataSettlementMethod.getText()));
        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(context)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", settlementMethods,
                        null, pos,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                mTvDataSettlementMethod.setText(text);
                                if (position == 0) {
                                    dataSettlementMethod = "0";
                                } else {
                                    dataSettlementMethod = "1";
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    public boolean checkValueIsValid() {
        dataReadingInterval = mEtDataReadingInterval.getText().toString();
        measurementCompensationTime = mEtMeasurementCompensationTime.getText().toString();
        motorDriveAddress = mEtMotorDriveAddress.getText().toString();
        decentralizationSpeed = mEtDecentralizationSpeed.getText().toString();
        decentralizationWaitingTime = mEtDecentralizationWaitingTime.getText().toString();
        pullUpSpeed = mEtPullUpSpeed.getText().toString();
        measuringDistance = mEtMeasuringDistance.getText().toString();
        measurementIntervalTime = mEtMeasurementIntervalTime.getText().toString();
        intervalCompensation = mEtIntervalCompensation.getText().toString();
        intervalFitting = mEtIntervalFitting.getText().toString();
        pointOffset = mEtPointOffset.getText().toString();

        if (!admeExecutiveAgencyInfo.getDatainval().equals("NullKey") && !admeExecutiveAgencyInfo.getDatainval().equals(dataReadingInterval)) {
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
        } else {
//            dataReadingInterval = null;
        }

        if (!admeExecutiveAgencyInfo.getCompensatetime().equals("NullKey") && !admeExecutiveAgencyInfo.getCompensatetime().equals(measurementCompensationTime)) {
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
        } else {
//            measurementCompensationTime = null;
        }

        if (!admeExecutiveAgencyInfo.getDriveaddress().equals("NullKey") && !admeExecutiveAgencyInfo.getDriveaddress().equals(motorDriveAddress)) {
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
        } else {
//            motorDriveAddress = null;
        }

        if (!admeExecutiveAgencyInfo.getDownspeed().equals("NullKey") && !admeExecutiveAgencyInfo.getDownspeed().equals(decentralizationSpeed)) {
            if (TextUtils.isEmpty(decentralizationSpeed)) {
                ToastUtils.show("请输入电机下放速度!");
                mEtDecentralizationSpeed.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(decentralizationSpeed);
                if (value < 1 || value > 180) {
                    ToastUtils.show("请输入正确的电机下放速度!");
                    mEtDecentralizationSpeed.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的电机下放速度!");
                mEtDecentralizationSpeed.requestFocus();
                return false;
            }
        } else {
//            decentralizationSpeed = null;
        }

        if (!admeExecutiveAgencyInfo.getDownwaitetime().equals("NullKey") && !admeExecutiveAgencyInfo.getDownwaitetime().equals(decentralizationWaitingTime)) {
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
        } else {
//            decentralizationWaitingTime = null;
        }

        if (!admeExecutiveAgencyInfo.getUpspeed().equals("NullKey") && !admeExecutiveAgencyInfo.getUpspeed().equals(pullUpSpeed)) {
            if (TextUtils.isEmpty(pullUpSpeed)) {
                ToastUtils.show("请输入电机上拉速度!");
                mEtPullUpSpeed.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(pullUpSpeed);
                if (value < 1 || value > 180) {
                    ToastUtils.show("请输入正确的电机上拉速度!");
                    mEtPullUpSpeed.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的电机上拉速度!");
                mEtPullUpSpeed.requestFocus();
                return false;
            }
        } else {
//            pullUpSpeed = null;
        }

        if (!admeExecutiveAgencyInfo.getMeaspacing().equals("NullKey") && !admeExecutiveAgencyInfo.getMeaspacing().equals(measuringDistance)) {
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
        } else {
//            measuringDistance = null;
        }

        if (!admeExecutiveAgencyInfo.getMeaintertime().equals("NullKey") && !admeExecutiveAgencyInfo.getMeaintertime().equals(measurementIntervalTime)) {
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
        } else {
//            measurementIntervalTime = null;
        }

        decimalFormat.applyPattern("#.###");
        if (!admeExecutiveAgencyInfo.getInterval_compensation().equals("NullKey")
                && RegexUtils.isMatch(RegexConstants.REGEX_FLOAT, admeExecutiveAgencyInfo.getInterval_compensation())
                && !decimalFormat.format(Double.parseDouble(admeExecutiveAgencyInfo.getInterval_compensation())).equals(intervalCompensation)) {

            if (TextUtils.isEmpty(intervalCompensation)) {
                ToastUtils.show("请输入管口安全距离!");
                mEtIntervalCompensation.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(intervalCompensation);
                if (value <= -10 || value >= 10) {
                    ToastUtils.show("请输入正确的管口安全距离!");
                    mEtIntervalCompensation.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的管口安全距离!");
                mEtIntervalCompensation.requestFocus();
                return false;
            }
        } else {
//            intervalCompensation = null;
        }

        decimalFormat.applyPattern("#.#");
        if (!admeExecutiveAgencyInfo.getInterval_fitting().equals("NullKey")
                && RegexUtils.isMatch(RegexConstants.REGEX_FLOAT, admeExecutiveAgencyInfo.getInterval_fitting())
                && !decimalFormat.format(Double.parseDouble(admeExecutiveAgencyInfo.getInterval_fitting())).equals(intervalFitting)) {

            if (TextUtils.isEmpty(intervalFitting)) {
                ToastUtils.show("请输入数据拟合区间!");
                mEtIntervalFitting.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(intervalFitting);
                if (value < 0 || value >= 10) {
                    ToastUtils.show("请输入正确的数据拟合区间!");
                    mEtIntervalFitting.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的数据拟合区间!");
                mEtIntervalFitting.requestFocus();
                return false;
            }
        } else {
//            intervalFitting = null;
        }

        decimalFormat.applyPattern("#.###");
        if (!admeExecutiveAgencyInfo.getPoint_offset().equals("NullKey")
                && RegexUtils.isMatch(RegexConstants.REGEX_FLOAT, admeExecutiveAgencyInfo.getPoint_offset())
                && !decimalFormat.format(Double.parseDouble(admeExecutiveAgencyInfo.getPoint_offset())).equals(pointOffset)) {
            if (TextUtils.isEmpty(pointOffset)) {
                ToastUtils.show("请输入测点偏移距离!");
                mEtPointOffset.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(pointOffset);
                if (value < 0 || value >= 0.5) {
                    ToastUtils.show("请输入正确的测点偏移距离!");
                    mEtPointOffset.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的测点偏移距离!");
                mEtPointOffset.requestFocus();
                return false;
            }
        } else {
//            pointOffset = null;
        }

        return true;
    }

    public String getSetCommand() {
        String command = "";
        try {
            AdmeExecutiveAgencyEntity entity = new AdmeExecutiveAgencyEntity();
            entity.setDatatype(dataSettlementMethod);
            entity.setDatareply(dataResponseEnableSBtn.isChecked() ? "1" : "0");
            entity.setDatainval(dataReadingInterval);
            entity.setCompensatetime(measurementCompensationTime);
            entity.setDriveaddress(motorDriveAddress);
            entity.setDownspeed(decentralizationSpeed);
            entity.setDownwaitetime(decentralizationWaitingTime);
            entity.setUpspeed(pullUpSpeed);
            entity.setMeaspacing(measuringDistance);
            entity.setMeaintertime(measurementIntervalTime);
            entity.setInterval_compensation(intervalCompensation);
            entity.setInterval_fitting(intervalFitting);
            entity.setPoint_offset(pointOffset);

            command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_EXECUTIVE_AGENCY, entity);
        } catch (Exception ex) {
            command = "";
            ex.printStackTrace();
        }
        return command;
    }

    public void initParamConfigInfo() {
        if (admeExecutiveAgencyInfo == null) {
            Timber.e("AdmeExecutiveAgencyInfo is Null!");
            admeExecutiveAgencyInfo = new AdmeExecutiveAgencyInfo();
            return;
        }
        dataSettlementMethodOld = admeExecutiveAgencyInfo.getDatatype();
        dataSettlementMethod = dataSettlementMethodOld;

        dataReadingInterval = admeExecutiveAgencyInfo.getDatainval();
        measurementCompensationTime = admeExecutiveAgencyInfo.getCompensatetime();
        motorDriveAddress = admeExecutiveAgencyInfo.getDriveaddress();
        decentralizationSpeed = admeExecutiveAgencyInfo.getDownspeed();
        decentralizationWaitingTime = admeExecutiveAgencyInfo.getDownwaitetime();
        pullUpSpeed = admeExecutiveAgencyInfo.getUpspeed();
        measuringDistance = admeExecutiveAgencyInfo.getMeaspacing();
        measurementIntervalTime = admeExecutiveAgencyInfo.getMeaintertime();
        intervalCompensation = admeExecutiveAgencyInfo.getInterval_compensation();
        intervalFitting = admeExecutiveAgencyInfo.getInterval_fitting();
        pointOffset = admeExecutiveAgencyInfo.getPoint_offset();

        if (dataSettlementMethodOld.equals("NullKey")) {
            dataSettlementMethodLayout.setVisibility(View.GONE);
        } else {
            if (dataSettlementMethodOld.equals("0")) {
                mTvDataSettlementMethod.setText(settlementMethods[0]);
            } else {
                mTvDataSettlementMethod.setText(settlementMethods[1]);
            }
        }

        if (admeExecutiveAgencyInfo.getDatareply().equals("NullKey")) {
            dataResponseLayout.setVisibility(View.GONE);
        } else {
            if (admeExecutiveAgencyInfo.getDatareply().equals("0")) {
                dataResponseEnableSBtn.setCheckedImmediatelyNoEvent(false);
            } else {
                dataResponseEnableSBtn.setCheckedImmediatelyNoEvent(true);
            }
        }
        try {
            if (dataReadingInterval.equals("NullKey")) {
                dataReadingIntervalLayout.setVisibility(View.GONE);
            } else {
                mEtDataReadingInterval.setText(dataReadingInterval);
            }

            if (measurementCompensationTime.equals("NullKey")) {
                measurementCompensationTimeLayout.setVisibility(View.GONE);
            } else {
                mEtMeasurementCompensationTime.setText(measurementCompensationTime);
            }

            if (motorDriveAddress.equals("NullKey")) {
                motorDriveAddressLayout.setVisibility(View.GONE);
            } else {
                mEtMotorDriveAddress.setText(motorDriveAddress);
            }

            if (decentralizationSpeed.equals("NullKey")) {
                decentralizationSpeedLayout.setVisibility(View.GONE);
            } else {
                mEtDecentralizationSpeed.setText(decentralizationSpeed);
            }

            if (decentralizationWaitingTime.equals("NullKey")) {
                decentralizationWaitingTimeLayout.setVisibility(View.GONE);
            } else {
                mEtDecentralizationWaitingTime.setText(decentralizationWaitingTime);
            }

            if (pullUpSpeed.equals("NullKey")) {
                pullUpSpeedLayout.setVisibility(View.GONE);
            } else {
                mEtPullUpSpeed.setText(pullUpSpeed);
            }

            decimalFormat.applyPattern("#.##");
            if (measuringDistance.equals("NullKey")) {
                measuringDistanceLayout.setVisibility(View.GONE);
            } else {
                measuringDistance = decimalFormat.format(Double.parseDouble(measuringDistance));
                mEtMeasuringDistance.setText(measuringDistance);
            }

            if (measurementIntervalTime.equals("NullKey")) {
                measurementIntervalTimeLayout.setVisibility(View.GONE);
            } else {
                mEtMeasurementIntervalTime.setText(measurementIntervalTime);
            }

            decimalFormat.applyPattern("#.###");
            if (intervalCompensation.equals("NullKey")) {
                intervalCompensationLayout.setVisibility(View.GONE);
            } else {
                intervalCompensation = decimalFormat.format(Double.parseDouble(intervalCompensation));
                mEtIntervalCompensation.setText(intervalCompensation);
            }

            decimalFormat.applyPattern("#.#");
            if (intervalFitting.equals("NullKey")) {
                intervalFittingLayout.setVisibility(View.GONE);
            } else {
                intervalFitting = decimalFormat.format(Double.parseDouble(intervalFitting));
                mEtIntervalFitting.setText(intervalFitting);
            }

            decimalFormat.applyPattern("#.###");
            if (pointOffset.equals("NullKey")) {
                pointOffsetLayout.setVisibility(View.GONE);
            } else {
                pointOffset = decimalFormat.format(Double.parseDouble(pointOffset));
                mEtPointOffset.setText(pointOffset);
            }
        } catch (
                Exception ex) {
            ex.printStackTrace();
        }
    }

    public void doAfterSetting() {
        dataSettlementMethodOld = dataSettlementMethod;
    }

    public void onEditableChanged(boolean isEditable) {
        dataSettlementMethodLayout.setEnabled(isEditable);
        dataResponseEnableSBtn.setEnabled(isEditable);
        mEtDataReadingInterval.setEnabled(isEditable);
        mEtMeasurementCompensationTime.setEnabled(isEditable);
        mEtMotorDriveAddress.setEnabled(isEditable);
        mEtDecentralizationSpeed.setEnabled(isEditable);
        mEtDecentralizationWaitingTime.setEnabled(isEditable);
        mEtPullUpSpeed.setEnabled(isEditable);
        mEtMeasuringDistance.setEnabled(isEditable);
        mEtMeasurementIntervalTime.setEnabled(isEditable);
        mEtIntervalCompensation.setEnabled(isEditable);
        mEtIntervalFitting.setEnabled(isEditable);
        mEtPointOffset.setEnabled(isEditable);

        if (isEditable) {
            mTvDataSettlementMethod.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_arrow_right, 0);
            mEtDataReadingInterval.setHint("请输入");
            mEtMeasurementCompensationTime.setHint("请输入");
            mEtMotorDriveAddress.setHint("请输入");
            mEtDecentralizationSpeed.setHint("1-180");
            mEtDecentralizationWaitingTime.setHint("1-32");
            mEtPullUpSpeed.setHint("1-180");
            mEtMeasuringDistance.setHint("请输入");
            mEtMeasurementIntervalTime.setHint("请输入");
            mEtIntervalCompensation.setHint("请输入");
            mEtIntervalFitting.setHint("请输入");
            mEtPointOffset.setHint("请输入");
        } else {
            mTvDataSettlementMethod.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            mEtDataReadingInterval.setHint("");
            mEtMeasurementCompensationTime.setHint("");
            mEtMotorDriveAddress.setHint("");
            mEtDecentralizationSpeed.setHint("");
            mEtDecentralizationWaitingTime.setHint("");
            mEtPullUpSpeed.setHint("");
            mEtMeasuringDistance.setHint("");
            mEtMeasurementIntervalTime.setHint("");
            mEtIntervalCompensation.setHint("");
            mEtIntervalFitting.setHint("");
            mEtPointOffset.setHint("");
//            initParamConfigInfo();
        }
        mBtnSave.setVisibility(isEditable ? View.VISIBLE : View.GONE);
    }
}
