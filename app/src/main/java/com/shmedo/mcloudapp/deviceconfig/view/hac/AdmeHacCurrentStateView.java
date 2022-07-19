package com.shmedo.mcloudapp.deviceconfig.view.hac;

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

import com.shmedo.configlibrary.iot.enums.AdmeCTRMotionState;
import com.shmedo.configlibrary.iot.enums.AdmeModuleErrorType;
import com.shmedo.configlibrary.iot.model.adme.AdmeCurrentStateInfo;
import com.shmedo.configlibrary.iot.model.hac.HacMotionState;
import com.shmedo.mcloudapp.R;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/3/10 <br/>
 * 描述：     ADME  运行状态页面
 */
public class AdmeHacCurrentStateView extends LinearLayout {
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

    @BindView(R.id.tv_sim_card_number)
    TextView mTVSimCardNumber;

    @BindView(R.id.tv_imei_number)
    TextView mTvImeiNumber;

    @BindView(R.id.tv_firmware_version)
    TextView mTvFirmwareVersion;

    /**
     * 设备工作信息
     */
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
    @BindView(R.id.tv_inclinometer_channel_number)
    TextView mTvInclinometerChannelNumber;

    @BindView(R.id.tv_inclinometer_location_information)
    TextView mTvInclinometerLocationInfo;

    @BindView(R.id.tv_inclinometer_voltage)
    TextView mTvInclinometerVoltage;

    @BindView(R.id.tv_inclinometer_temperature)
    TextView mTvInclinometerTemperature;

    private DecimalFormat decimalFormat = new DecimalFormat();

    public AdmeHacCurrentStateView(Context context) {
        this(context, null);
    }

    public AdmeHacCurrentStateView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdmeHacCurrentStateView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关联布局文件
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.adme_hac_current_state_view, this, true);
        ButterKnife.bind(this);
    }

    public void updateMotionState(HacMotionState hacMotionState) {
        if (hacMotionState == null) {
            Timber.e("AdmeMotionState is Null!");
            ctrMotionLayout.setVisibility(View.GONE);
            return;
        }
        if (hacMotionState.getMeasmode().equals("NullKey")) {
            ctrMotionLayout.setVisibility(View.GONE);
            return;
        }
        ctrMotionLayout.setVisibility(View.VISIBLE);

        //CTR 工作异常
        if (!hacMotionState.getAbndiasis().equals("0")) {
            mTvMeasureMode.setText("异常保护");
            //列出异常原因
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("异常原因: ");
            String[] codes = hacMotionState.getAbndiasis().split("\\|");
            for (String code : codes) {
                AdmeModuleErrorType errorType = AdmeModuleErrorType.valueByCode(code);
                if (errorType != null) {
                    stringBuilder.append(errorType.getDescription());
                    stringBuilder.append(";");
                }
            }
            if (stringBuilder.toString().endsWith(";")) {
                stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
            }
            mTvMotorInfo.setText(stringBuilder.toString());
            mTvMotorInfo.setTextColor(Color.RED);
            return;
        }
        mTvMeasureMode.setText(hacMotionState.getMeasmode().equals("0") ? "正向测量" : "反向测量");
        mTvMotorInfo.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.title_text_color));
        String measurePoint = hacMotionState.getMeaspoint();
        String msg;
        AdmeCTRMotionState ctrMotionState = AdmeCTRMotionState.valueByCode(hacMotionState.getMotorinfo());
        if (ctrMotionState == null)
            return;

        switch (ctrMotionState) {
            case BOTTOM_WAITING://管底等待
                msg = (!TextUtils.isEmpty(measurePoint) && !measurePoint.contains("|")) ? String.format("管底等待-剩余时间(%s s)", hacMotionState.getWaittime()) : "管底等待";
                mTvMotorInfo.setText(msg);
                break;

            case POINT_MEASUREMENT://测点测量
                if (!TextUtils.isEmpty(measurePoint) && measurePoint.contains("|")) {
                    String[] values = measurePoint.split("\\|");
                    msg = (!TextUtils.isEmpty(values[0]) && !TextUtils.isEmpty(values[1])) ? String.format("测点测量-测斜仪位置(%s m)-测点序列(%s)", values[1], values[0]) : "测点测量";
                    mTvMotorInfo.setText(msg);
                }
                break;

            default:
                mTvMotorInfo.setText(ctrMotionState.getDescription());
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

            mTvInclinometerChannelNumber.setText(currentStateInfo.getIncnum());
            mTvInclinometerLocationInfo.setText(currentStateInfo.getIncloc());
            mTvInclinometerVoltage.setText(String.format("%sV", decimalFormat.format(Double.parseDouble(currentStateInfo.getIncvoltage()))));
            mTvInclinometerTemperature.setText(String.format("%s℃", decimalFormat.format(Double.parseDouble(currentStateInfo.getIntertempe()))));

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
