package com.shmedo.mcloudapp.deviceconfig.view.adme;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ColorUtils;
import com.lxj.xpopup.XPopup;
import com.shmedo.configlibrary.iot.enums.AdmeModuleErrorType;
import com.shmedo.configlibrary.iot.model.adme.AdmeCurrentStateInfo;
import com.shmedo.configlibrary.iot.model.adme.AdmeMotionState;
import com.shmedo.mcloudapp.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/3/10 <br/>
 * 描述：     ADME  运行状态页面
 */
public class AdmeCurrentStateView extends LinearLayout {
    @BindView(R.id.ll_ctr_motion_info)
    ViewGroup ctrMotionLayout;

    @BindView(R.id.tv_measure_mode)
    TextView mTvMeasureMode;//测量模式(0：正测1：反测)

    @BindView(R.id.tv_motor_info)
    TextView mTvMotorInfo;//电机运动信息(1：磁开关触发,测斜仪配对,设置参数2：测斜仪下放 3：管底等待4：测点测量5：磁开关触发，测量结束6：测斜仪配对,读取数据7：数据上传8：周期等待)

    @BindView(R.id.tv_measure_point)
    TextView mTvMeasurePoint;

    /**
     * 基本信息
     */
    @BindView(R.id.tv_device_sn)
    TextView mTvDeviceSn;

    @BindView(R.id.tv_product_number)
    TextView mTvProductNumber;

    @BindView(R.id.tv_sim_card_number)
    TextView mTVSimCardNumber;

    @BindView(R.id.tv_imei_number)
    TextView mTvImeiNumber;

    @BindView(R.id.tv_firmware_version)
    TextView mTvFirmwareVersion;

    /**
     * 数据中心
     */
    @BindView(R.id.tv_signal_strength)
    TextView mTvSignalStrength;

    @BindView(R.id.tv_link_one_status)
    TextView mTvLinkOneStatus;

    @BindView(R.id.tv_link_one_send_data)
    TextView mTvLinkOneSendData;

    @BindView(R.id.tv_link_one_unsend_data)
    TextView mTvLinkOneUnsendData;

    @BindView(R.id.tv_link_one_online_rate)
    TextView mTvLinkOneOnlineRate;


    @BindView(R.id.tv_link_two_status)
    TextView mTvLinkTwoStatus;

    @BindView(R.id.tv_link_two_send_data)
    TextView mTvLinkTwoSendData;

    @BindView(R.id.tv_link_two_unsend_data)
    TextView mTvLinkTwoUnsendData;

    @BindView(R.id.tv_link_two_online_rate)
    TextView mTvLinkTwoOnlineRate;

    /**
     * 设备工作信息
     */
    @BindView(R.id.ll_device_abnormal_diagnosis)
    ViewGroup deviceAbnormalDiagnosisLayout;

    @BindView(R.id.tv_device_abnormal_diagnosis)
    TextView mTvDeviceAbnormalDiagnosis;

    @BindView(R.id.tv_work_mode)
    TextView mTvWorkMode;

    @BindView(R.id.tv_ctr_input_voltage)
    TextView mTvCTRInputVoltage;

    @BindView(R.id.tv_driver_input_voltage)
    TextView mTvDriverInputVoltage;

    @BindView(R.id.tv_device_temperature)
    TextView mTvDeviceTemperature;

    @BindView(R.id.tv_device_humidity)
    TextView mTvDeviceHumidity;

    @BindView(R.id.tv_device_drop_number)
    TextView mTvDeviceDropNumber;

    /**
     * 测斜仪信息
     */
    @BindView(R.id.tv_inclinometer_type)
    TextView mTvInclinometerType;

    @BindView(R.id.tv_inclinometer_channel_number)
    TextView mTvInclinometerChannelNumber;

    @BindView(R.id.tv_inclinometer_location_information)
    TextView mTvInclinometerLocationInfo;

    @BindView(R.id.tv_inclinometer_voltage)
    TextView mTvInclinometerVoltage;

    @BindView(R.id.tv_inclinometer_temperature)
    TextView mTvInclinometerTemperature;

    private DecimalFormat decimalFormat = new DecimalFormat();

    public AdmeCurrentStateView(Context context) {
        this(context, null);
    }

    public AdmeCurrentStateView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdmeCurrentStateView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关联布局文件
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.adme_current_state_view, this, true);
        ButterKnife.bind(this);
    }

    public void updateMotionState(AdmeMotionState admeMotionState) {
        if (admeMotionState == null) {
            Timber.e("AdmeMotionState is Null!");
            return;
        }
        if (admeMotionState.getMeasmode().equals("NullKey")) {
            ctrMotionLayout.setVisibility(View.GONE);
            return;
        }
        ctrMotionLayout.setVisibility(View.VISIBLE);

        mTvMeasureMode.setText(admeMotionState.getMeasmode().equals("0") ? "正向测量" : "反向测量");
        String measurePoint = admeMotionState.getMeaspoint();
        String msg;
        switch (admeMotionState.getMotorinfo()) {
            case "1":
                mTvMotorInfo.setText("磁开关触发,测斜仪配对,设置参数");
                break;

            case "2": {//测斜仪下放
                msg = (!TextUtils.isEmpty(measurePoint) && !measurePoint.contains("|")) ? String.format("测斜仪下放: %s 米", measurePoint) : "测斜仪下放";
                mTvMotorInfo.setText(msg);
            }
            break;

            case "3": {//管底等待
                msg = (!TextUtils.isEmpty(measurePoint) && !measurePoint.contains("|")) ? String.format("管底等待-位置(%s m)-剩余时间(%s s)", measurePoint, admeMotionState.getWaittime()) : "管底等待";
                mTvMotorInfo.setText(msg);
            }
            break;

            case "4": {//测点测量
                if (!TextUtils.isEmpty(measurePoint) && measurePoint.contains("|")) {
                    String[] values = measurePoint.split("\\|");
                    msg = (!TextUtils.isEmpty(values[0]) && !TextUtils.isEmpty(values[1])) ? String.format("测点测量-测斜仪位置(%s m)-测点序列(%s)", values[1], values[0]) : "测点测量";
                    mTvMotorInfo.setText(msg);
                }
            }
            break;

            case "5":
                mTvMotorInfo.setText("磁开关触发，测量结束");
                break;

            case "6":
                mTvMotorInfo.setText("测斜仪配对,读取数据");
                break;

            case "7":
                mTvMotorInfo.setText("数据上传");
                break;

            case "8":
                mTvMotorInfo.setText("周期等待");
                break;

            default:
                break;
        }
    }

    public void initStatusInfo(AdmeCurrentStateInfo currentStateInfo) {
        if (currentStateInfo == null) {
            Timber.e("AdmeCurrentStateInfo is Null!");
            return;
        }
        try {
            mTvDeviceSn.setText(currentStateInfo.getSn());
            mTvProductNumber.setText(currentStateInfo.getProductid());
            mTVSimCardNumber.setText(currentStateInfo.getSimid());
            mTvImeiNumber.setText(currentStateInfo.getImeid());
            mTvFirmwareVersion.setText(currentStateInfo.getFirversion());

            if (currentStateInfo.getTestway().equals("0")) {
                mTvWorkMode.setText("常规测量模式");
            } else if (currentStateInfo.getTestway().equals("1")) {
                mTvWorkMode.setText("特定点位模式");
            } else if (currentStateInfo.getTestway().equals("2")) {
                mTvWorkMode.setText("静态测量模式");
            } else if (currentStateInfo.getTestway().equals("3")) {
                mTvWorkMode.setText("设备停用模式");
            }
            decimalFormat.applyPattern("#.###");
            mTvCTRInputVoltage.setText(String.format("%sV", decimalFormat.format(Double.parseDouble(currentStateInfo.getCtrinputv()))));
            mTvDriverInputVoltage.setText(String.format("%sV", decimalFormat.format(Double.parseDouble(currentStateInfo.getDriveinputv()))));
            mTvDeviceTemperature.setText(String.format("%s℃", decimalFormat.format(Double.parseDouble(currentStateInfo.getTemperature()))));
            mTvDeviceHumidity.setText(String.format("%s%%", decimalFormat.format(Double.parseDouble(currentStateInfo.getHumidity()))));
            mTvDeviceDropNumber.setText(currentStateInfo.getDownnum());

            mTvInclinometerType.setText(currentStateInfo.getInctype().equals("0") ? "433测斜仪" : "蓝牙测斜仪");
            mTvInclinometerChannelNumber.setText(currentStateInfo.getIncnum());
            mTvInclinometerLocationInfo.setText(currentStateInfo.getIncloc());
            mTvInclinometerVoltage.setText(String.format("%sV", decimalFormat.format(Double.parseDouble(currentStateInfo.getIncvoltage()))));
            mTvInclinometerTemperature.setText(String.format("%s℃", decimalFormat.format(Double.parseDouble(currentStateInfo.getIntertempe()))));

            //处理设备异常诊断信息
            if (currentStateInfo.getAbndiasis().equals("0")) {
                deviceAbnormalDiagnosisLayout.setEnabled(false);
                mTvDeviceAbnormalDiagnosis.setText("正常");
                mTvDeviceAbnormalDiagnosis.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_3AD094));
                mTvDeviceAbnormalDiagnosis.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            } else {
                deviceAbnormalDiagnosisLayout.setEnabled(true);
                mTvDeviceAbnormalDiagnosis.setText("异常");
                mTvDeviceAbnormalDiagnosis.setTextColor(Color.RED);
                mTvDeviceAbnormalDiagnosis.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.icon_arrow_right, 0);
            }
        } catch (Exception ex) {
            mTvDeviceAbnormalDiagnosis.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            ex.printStackTrace();
        }
    }

    /**
     * 展示异常模块信息
     */
    public void showErrorModulesInfoDialog(AdmeCurrentStateInfo currentStateInfo) {
        if (currentStateInfo == null || TextUtils.isEmpty(currentStateInfo.getAbndiasis()))
            return;

        List<String> descList = new ArrayList<>();
        String errinfo = currentStateInfo.getAbndiasis();
        String[] codes = errinfo.split("\\|");
        for (String code : codes) {
            AdmeModuleErrorType errorType = AdmeModuleErrorType.valueByCode(code);
            if (errorType != null) {
                descList.add(errorType.getDescription());
            }
        }
        if (descList.isEmpty())
            return;

        new XPopup.Builder(getContext())
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asCenterList("异常信息", descList.toArray(new String[0]),
                        null, -1, null, 0, R.layout.custom_xpopup_adapter_text)
                .show();
    }
}
