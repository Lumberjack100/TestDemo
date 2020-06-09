package com.shmedo.mcloudapp.ui.activity.device.sensor.view;

import android.content.Context;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.shmedo.core.model.SensorJunXingZljInfo;
import com.shmedo.core.utils.StringUtil;
import com.shmedo.core.utils.ValidateUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.entity.ble.collector.CollectorSensorParamsInfoSub;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/7 <br/>
 * 描述：  军星轴力计(ZLJ-300T)配置项视图
 */
public class SensorZLJ300tView extends FrameLayout {
    @BindView(R.id.et_trigger_threshold)
    EditText mEtTriggerThreshold;//触发阀值
    @BindView(R.id.sensitivityCoefficient)
    EditText mEtSensitivityCoefficient;//灵敏度k
    @BindView(R.id.et_ReferenceValue)
    EditText mEtReferenceValue;//基准值F0
    @BindView(R.id.et_correct_value)
    EditText mEtCorrectValue;//手动纠偏
    @BindView(R.id.et_initialtemperature)
    EditText mEtInitialTemperature;//初始温度

    private String triggerThreshold, coefficientK, referenceValue, correctValue, initialTemperature;

    public SensorZLJ300tView(@NonNull Context context) {
        this(context, null);
    }

    public SensorZLJ300tView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SensorZLJ300tView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关联布局文件
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.axialforcegauge_config, this, true);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        mEtTriggerThreshold.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtSensitivityCoefficient.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        mEtReferenceValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        mEtCorrectValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
    }

    public void bindSensorData(CollectorSensorParamsInfoSub infoSub) {
        SensorJunXingZljInfo sensorInfo = (SensorJunXingZljInfo) infoSub.getSensorData();
        mEtTriggerThreshold.setText((int) Double.parseDouble(sensorInfo.getTriggerThreshold()) + "");
        mEtSensitivityCoefficient.setText(sensorInfo.getSensitivityK());
        mEtReferenceValue.setText(sensorInfo.getReferenceValue());
        mEtCorrectValue.setText(StringUtil.getDouble3AccuracyString(sensorInfo.getManualCorrection()));
        mEtInitialTemperature.setText("");
    }

    /**
     * 通过扫描二维码填充多项式参数
     *
     * @param sensorInfo
     */
    public void initDataByScan(SensorJunXingZljInfo sensorInfo) {
        mEtSensitivityCoefficient.setText(sensorInfo.getSensitivityK());
        mEtReferenceValue.setText(sensorInfo.getReferenceValue());
    }

    public boolean updateSensorData(CollectorSensorParamsInfoSub infoSub) {
        if (!checkValue()) {
            return false;
        }

        SensorJunXingZljInfo sensorInfo = new SensorJunXingZljInfo();
        sensorInfo.setTriggerThreshold(triggerThreshold);
        sensorInfo.setSensitivityK(coefficientK);
        sensorInfo.setReferenceValue(referenceValue);
        sensorInfo.setManualCorrection(correctValue);
        infoSub.setSensorData(sensorInfo);

        return true;
    }

    private boolean checkValue() {
        triggerThreshold = mEtTriggerThreshold.getText().toString().trim();
        coefficientK = mEtSensitivityCoefficient.getText().toString().trim();
        referenceValue = mEtReferenceValue.getText().toString().trim();
        correctValue = mEtCorrectValue.getText().toString().trim();
//        initialTemperature = mEtInitialTemperature.getText().toString().trim();

        if (TextUtils.isEmpty(triggerThreshold)) {
            ToastUtils.show("触发阀值不能为空!");
            return false;
        }

        if (!ValidateUtil.isDouble(triggerThreshold)) {
            ToastUtils.show("请输入正确的触发阀值!");
            return false;
        }

        if (TextUtils.isEmpty(coefficientK)) {
            ToastUtils.show("灵敏度不能为空!");
            return false;
        }

        if (!TextUtils.isEmpty(correctValue) && !ValidateUtil.isDouble(correctValue)) {
            ToastUtils.show("请输入正确的修正值!");
            return false;
        }

        return true;
    }
}
