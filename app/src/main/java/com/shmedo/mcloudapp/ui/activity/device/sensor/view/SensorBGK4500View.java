package com.shmedo.mcloudapp.ui.activity.device.sensor.view;

import android.content.Context;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.shmedo.core.model.CollectorSensorParamsInfo;
import com.shmedo.core.model.SensorKangPercolateInfo;
import com.shmedo.core.utils.StringUtil;
import com.shmedo.core.utils.ValidateUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/6/6 <br/>
 * 描述：   基康渗压计(BGK-4500)配置项视图
 */
public class SensorBGK4500View extends FrameLayout {
    @BindView(R.id.et_trigger_threshold)
    EditText mEtTriggerThreshold;//触发阀值
    @BindView(R.id.polynomialRatioA)
    EditText mEtCoefficientA;//多项式系数A
    @BindView(R.id.polynomialRatioB)
    EditText mEtCoefficientB;//多项式系数B
    @BindView(R.id.polynomialRatioC)
    EditText mEtCoefficientC;//多项式系数C
    @BindView(R.id.temperatureCoefficientK)
    EditText mEtCoefficientK;//温度系数K
    @BindView(R.id.et_initialtemperature)
    EditText mEtInitialTemperature;//初始温度
    @BindView(R.id.et_correct_value)
    EditText mEtCorrectValue;//手动纠偏
    @BindView(R.id.et_cord_length)
    EditText mEtCordLength;//绳长
    @BindView(R.id.et_install_elevation)
    EditText mEtInstallElevation;//安装高程

    private String triggerThreshold, coefficientA, coefficientB, coefficientC, coefficientK, initialTemperature, correctValue, cordLength, installElevation;


    public SensorBGK4500View(@NonNull Context context) {
        this(context, null);
    }

    public SensorBGK4500View(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SensorBGK4500View(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关联布局文件
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.bgk_osmometer_config, this, true);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        mEtTriggerThreshold.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtCoefficientA.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        mEtCoefficientB.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        mEtCoefficientC.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        mEtCoefficientK.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        mEtInitialTemperature.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtCorrectValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtCordLength.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
        mEtInstallElevation.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

        mEtInitialTemperature.setText("0");
        mEtCorrectValue.setText("0");
    }

    public void bindSensorData(CollectorSensorParamsInfo infoSub) {
        SensorKangPercolateInfo sensorInfo = (SensorKangPercolateInfo) infoSub.getSensorData();
        mEtTriggerThreshold.setText((int) Double.parseDouble(sensorInfo.getTriggerThreshold()) + "");
        mEtCoefficientA.setText(sensorInfo.getPolynomialRatioA());
        mEtCoefficientB.setText(sensorInfo.getPolynomialRatioB());
        mEtCoefficientC.setText(sensorInfo.getPolynomialRatioC());
        mEtCoefficientK.setText(sensorInfo.getTemperatureCoefficientK());
        mEtInitialTemperature.setText(StringUtil.getDouble3AccuracyString(sensorInfo.getCreateTemperature()));
        mEtCorrectValue.setText(StringUtil.getDouble3AccuracyString(sensorInfo.getManualCorrection()));
        mEtCordLength.setText(StringUtil.getDouble3AccuracyString(sensorInfo.getCordLenght()));
        mEtInstallElevation.setText(StringUtil.getDouble3AccuracyString(sensorInfo.getInstallElevation()));
    }

    /**
     * 通过扫描二维码填充多项式参数
     */
    public void initDataByScan(SensorKangPercolateInfo sensorInfo) {
        mEtCoefficientA.setText(sensorInfo.getPolynomialRatioA());
        mEtCoefficientB.setText(sensorInfo.getPolynomialRatioB());
        mEtCoefficientC.setText(sensorInfo.getPolynomialRatioC());
        mEtCoefficientK.setText(sensorInfo.getTemperatureCoefficientK());
    }

    public boolean updateSensorData(CollectorSensorParamsInfo infoSub) {
        if (!checkValue()) {
            return false;
        }

        SensorKangPercolateInfo sensorInfo = new SensorKangPercolateInfo();
        sensorInfo.setTriggerThreshold(triggerThreshold);
        sensorInfo.setPolynomialRatioA(coefficientA);
        sensorInfo.setPolynomialRatioB(coefficientB);
        sensorInfo.setPolynomialRatioC(coefficientC);
        sensorInfo.setTemperatureCoefficientK(coefficientK);
        sensorInfo.setCreateTemperature(TextUtils.isEmpty(initialTemperature) ? "0" : initialTemperature);
        sensorInfo.setManualCorrection(TextUtils.isEmpty(correctValue) ? "0" : correctValue);
        sensorInfo.setCordLenght(cordLength);
        sensorInfo.setInstallElevation(installElevation);
        infoSub.setSensorData(sensorInfo);

        return true;
    }

    private boolean checkValue() {
        triggerThreshold = mEtTriggerThreshold.getText().toString().trim();
        coefficientA = mEtCoefficientA.getText().toString().trim();
        coefficientB = mEtCoefficientB.getText().toString().trim();
        coefficientC = mEtCoefficientC.getText().toString().trim();
        coefficientK = mEtCoefficientK.getText().toString().trim();
        initialTemperature = mEtInitialTemperature.getText().toString().trim();
        correctValue = mEtCorrectValue.getText().toString().trim();
        cordLength = mEtCordLength.getText().toString().trim();
        installElevation = mEtInstallElevation.getText().toString().trim();

        if (TextUtils.isEmpty(triggerThreshold)) {
            ToastUtils.show("触发阀值不能为空!");
            return false;
        }

        if (!ValidateUtil.isInteger(triggerThreshold)) {
            ToastUtils.show("请输入正确的触发阀值!");
            return false;
        }

        if (TextUtils.isEmpty(coefficientA)) {
            ToastUtils.show("多项式系数A不能为空!");
            return false;
        }

        if (TextUtils.isEmpty(coefficientB)) {
            ToastUtils.show("多项式系数B不能为空!");
            return false;
        }

        if (TextUtils.isEmpty(coefficientC)) {
            ToastUtils.show("多项式系数C不能为空!");
            return false;
        }

        if (TextUtils.isEmpty(coefficientK)) {
            ToastUtils.show("温度系数K不能为空!");
            return false;
        }

        if (!TextUtils.isEmpty(initialTemperature) && !ValidateUtil.isDouble(initialTemperature)) {
            ToastUtils.show("请输入正确的初始温度!");
            return false;
        }

        if (!TextUtils.isEmpty(correctValue) && !ValidateUtil.isDouble(correctValue)) {
            ToastUtils.show("请输入正确的手动纠偏!");
            return false;
        }

        if (TextUtils.isEmpty(cordLength)) {
            ToastUtils.show("绳长不能为空!");
            return false;
        }

        if (!ValidateUtil.isDouble(cordLength)) {
            ToastUtils.show("请输入正确的绳长!");
            return false;
        }

        if (TextUtils.isEmpty(installElevation)) {
            ToastUtils.show("安装高程不能为空!");
            return false;
        }

        if (!ValidateUtil.isDouble(installElevation)) {
            ToastUtils.show("请输入正确的安装高程!");
            return false;
        }
        return true;
    }


    /**
     * 点击空白区域隐藏键盘.
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {  //把操作放在用户点击的时候
            View v = this.getFocusedChild();      //得到当前页面的焦点,ps:有输入框的页面焦点一般会被输入框占据
            if (isShouldHideKeyboard(v, motionEvent)) { //判断用户点击的是否是输入框以外的区域
                KeyBordUtils.hideSoftKeyboard(v);
            }
        }
        return super.dispatchTouchEvent(motionEvent);
    }

    /**
     * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时则不能隐藏
     *
     * @param v
     * @param event
     * @return
     */
    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if ((v instanceof EditText)) {  //判断得到的焦点控件是否包含EditText
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],    //得到输入框在屏幕中上下左右的位置
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击位置如果是EditText的区域，忽略它，不收起键盘。
                return false;
            } else {
                return true;
            }
        }
        // 如果焦点不是EditText则忽略
        return false;
    }

}
