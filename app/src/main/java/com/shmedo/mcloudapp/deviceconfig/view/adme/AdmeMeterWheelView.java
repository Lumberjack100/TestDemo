package com.shmedo.mcloudapp.deviceconfig.view.adme;

import android.content.Context;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeMeterWheelEntity;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.adme.AdmeMeterWheelInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/7/13 <br/>
 * 描述：     TODO
 */
public class AdmeMeterWheelView extends LinearLayout {
    @BindView(R.id.et_encoder_line_number)
    ClearEditText mEtEncoderLineNumber;

    @BindView(R.id.et_outer_diameter)
    ClearEditText mEtOuterDiameter;

    @BindView(R.id.et_up_correction_parameters_one)
    ClearEditText mEtUpCorrectionParametersOne;

    @BindView(R.id.et_up_correction_parameters_two)
    ClearEditText mEtUpCorrectionParametersTwo;

    @BindView(R.id.et_up_constant)
    ClearEditText mEtUpConstant;

    @BindView(R.id.et_up_filter_coefficient)
    ClearEditText mEtUpFilterCoefficient;

    @BindView(R.id.et_down_correction_parameters_one)
    ClearEditText mEtDownCorrectionParametersOne;

    @BindView(R.id.et_down_correction_parameters_two)
    ClearEditText mEtDownCorrectionParametersTwo;

    @BindView(R.id.et_down_constant)
    ClearEditText mEtDownConstant;

    @BindView(R.id.et_down_filter_coefficient)
    ClearEditText mEtDownFilterCoefficient;

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    @BindView(R.id.ll_encoder_line_number)
    ViewGroup encoderLineNumberLayout;

    @BindView(R.id.ll_outer_diameter)
    ViewGroup outerDiameterLayout;

    @BindView(R.id.ll_up_correction_parameters_one)
    ViewGroup upCorrectionParametersOneLayout;

    @BindView(R.id.ll_up_correction_parameters_two)
    ViewGroup upCorrectionParametersTwoLayout;

    @BindView(R.id.ll_up_constant)
    ViewGroup upConstantLayout;

    @BindView(R.id.ll_up_filter_coefficient)
    ViewGroup upFilterCoefficientLayout;

    @BindView(R.id.ll_down_correction_parameters_one)
    ViewGroup downCorrectionParametersOneLayout;

    @BindView(R.id.ll_down_correction_parameters_two)
    ViewGroup downCorrectionParametersTwoLayout;

    @BindView(R.id.ll_down_constant)
    ViewGroup downConstantLayout;

    @BindView(R.id.ll_down_filter_coefficient)
    ViewGroup downFilterCoefficientLayout;

    private String encoderLineNumber;//编码器线数
    private String outerDiameter;//外径
    private String upCorrectionParametersOne;//上拉一次修正参数
    private String upCorrectionParametersTwo;//上拉二次修正参数
    private String upConstant;//上拉常数
    private String upFilterCoefficient;//上拉滤波器系数
    private String downCorrectionParametersOne;//下放一次修正参数
    private String downCorrectionParametersTwo;//下放二次修正参数
    private String downConstant;//下放常数
    private String downFilterCoefficient;//下放滤波器系数

    private DecimalFormat decimalFormat = new DecimalFormat();
    public AdmeMeterWheelInfo admeMeterWheelInfo=new AdmeMeterWheelInfo();


    public AdmeMeterWheelView(Context context) {
        this(context, null);
    }

    public AdmeMeterWheelView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdmeMeterWheelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //关联布局文件
        ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.adme_meter_wheel_view, this, true);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        mEtEncoderLineNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtOuterDiameter.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtUpCorrectionParametersOne.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
        mEtUpCorrectionParametersTwo.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
        mEtUpConstant.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
        mEtUpFilterCoefficient.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
        mEtDownCorrectionParametersOne.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
        mEtDownCorrectionParametersTwo.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
        mEtDownConstant.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
        mEtDownFilterCoefficient.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});

        mEtUpFilterCoefficient.setHint("0-F");
        mEtDownFilterCoefficient.setHint("0-F");
    }

    public boolean checkValueIsValid() {
        String filterCoefficientRule = "[A-F0-9]";

        if (!encoderLineNumber.equals("NullKey")) {
            encoderLineNumber = mEtEncoderLineNumber.getText().toString().trim();
            if (TextUtils.isEmpty(encoderLineNumber)) {
                ToastUtils.show("请输入编码器线数!");
                mEtEncoderLineNumber.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(encoderLineNumber);
                if (value < 1) {
                    ToastUtils.show("请输入正确的编码器线数!");
                    mEtEncoderLineNumber.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的编码器线数!");
                mEtEncoderLineNumber.requestFocus();
                return false;
            }
        }
        if (!outerDiameter.equals("NullKey")) {
            outerDiameter = mEtOuterDiameter.getText().toString().trim();
            if (TextUtils.isEmpty(outerDiameter)) {
                ToastUtils.show("请输入外径!");
                mEtOuterDiameter.requestFocus();
                return false;
            }
            try {
                int value = Integer.parseInt(outerDiameter);
                if (value < 1) {
                    ToastUtils.show("请输入正确的外径!");
                    mEtOuterDiameter.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的外径!");
                mEtOuterDiameter.requestFocus();
                return false;
            }
        }
        if (!upCorrectionParametersOne.equals("NullKey")) {
            upCorrectionParametersOne = mEtUpCorrectionParametersOne.getText().toString().trim();
            if (TextUtils.isEmpty(upCorrectionParametersOne)) {
                ToastUtils.show("请输入上拉一次修正参数!");
                mEtUpCorrectionParametersOne.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(upCorrectionParametersOne);
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的上拉一次修正参数!");
                mEtUpCorrectionParametersOne.requestFocus();
                return false;
            }
        }
        if (!upCorrectionParametersTwo.equals("NullKey")) {
            upCorrectionParametersTwo = mEtUpCorrectionParametersTwo.getText().toString().trim();
            if (TextUtils.isEmpty(upCorrectionParametersTwo)) {
                ToastUtils.show("请输入上拉二次修正参数");
                mEtUpCorrectionParametersTwo.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(upCorrectionParametersTwo);
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的上拉二次修正参数!");
                mEtUpCorrectionParametersTwo.requestFocus();
                return false;
            }
        }
        if (!upConstant.equals("NullKey")) {
            upConstant = mEtUpConstant.getText().toString().trim();
            if (TextUtils.isEmpty(upConstant)) {
                ToastUtils.show("请输入上拉常数!");
                mEtUpConstant.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(upConstant);
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的上拉常数!");
                mEtUpConstant.requestFocus();
                return false;
            }
        }
        if (!upFilterCoefficient.equals("NullKey")) {
            upFilterCoefficient = mEtUpFilterCoefficient.getText().toString().trim();
            if (TextUtils.isEmpty(upFilterCoefficient)) {
                ToastUtils.show("请输入上拉滤波器系数!");
                mEtUpFilterCoefficient.requestFocus();
                return false;
            }
            if (!upFilterCoefficient.matches(filterCoefficientRule)) {
                ToastUtils.show("请输入正确的上拉滤波器系数!");
                mEtUpFilterCoefficient.requestFocus();
                return false;
            }
        }
        if (!downCorrectionParametersOne.equals("NullKey")) {
            downCorrectionParametersOne = mEtDownCorrectionParametersOne.getText().toString().trim();
            if (TextUtils.isEmpty(downCorrectionParametersOne)) {
                ToastUtils.show("请输入下放一次修正参数!");
                mEtDownCorrectionParametersOne.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(downCorrectionParametersOne);
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的下放一次修正参数!");
                mEtDownCorrectionParametersOne.requestFocus();
                return false;
            }
        }
        if (!downCorrectionParametersTwo.equals("NullKey")) {
            downCorrectionParametersTwo = mEtDownCorrectionParametersTwo.getText().toString().trim();
            if (TextUtils.isEmpty(downCorrectionParametersTwo)) {
                ToastUtils.show("请输入下放二次修正参数!");
                mEtDownCorrectionParametersTwo.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(downCorrectionParametersTwo);
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的下放二次修正参数!");
                mEtDownCorrectionParametersTwo.requestFocus();
                return false;
            }
        }
        if (!downConstant.equals("NullKey")) {
            downConstant = mEtDownConstant.getText().toString().trim();
            if (TextUtils.isEmpty(downConstant)) {
                ToastUtils.show("请输入下放常数!");
                mEtDownConstant.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(downConstant);
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的下放常数!");
                mEtDownConstant.requestFocus();
                return false;
            }
        }
        if (!downFilterCoefficient.equals("NullKey")) {
            downFilterCoefficient = mEtDownFilterCoefficient.getText().toString().trim();
            if (TextUtils.isEmpty(downFilterCoefficient)) {
                ToastUtils.show("请输入下放滤波器系数!");
                mEtDownFilterCoefficient.requestFocus();
                return false;
            }
            if (!downFilterCoefficient.matches(filterCoefficientRule)) {
                ToastUtils.show("请输入正确的下放滤波器系数!");
                mEtDownFilterCoefficient.requestFocus();
                return false;
            }
        }

        return true;
    }

    public String getSetCommand() {
        String command = "";
        try {
            AdmeMeterWheelEntity entity = new AdmeMeterWheelEntity();
            entity.setEnclinenum(admeMeterWheelInfo.getEnclinenum().equals("NullKey") ? "NullKey" : encoderLineNumber);
            entity.setOutline(admeMeterWheelInfo.getEnclinenum().equals("NullKey") ? "NullKey" : outerDiameter);

            decimalFormat.applyPattern("#.######");
            entity.setUptiona(admeMeterWheelInfo.getUptiona().equals("NullKey") ? "NullKey" : decimalFormat.format(Double.parseDouble(upCorrectionParametersOne)));
            entity.setUptionb(admeMeterWheelInfo.getUptionb().equals("NullKey") ? "NullKey" : decimalFormat.format(Double.parseDouble(upCorrectionParametersTwo)));
            entity.setUpconstant(admeMeterWheelInfo.getUpconstant().equals("NullKey") ? "NullKey" : decimalFormat.format(Double.parseDouble(upConstant)));
            entity.setUpfilter(admeMeterWheelInfo.getUpfilter().equals("NullKey") ? "NullKey" : upFilterCoefficient);
            entity.setDowntiona(admeMeterWheelInfo.getDowntiona().equals("NullKey") ? "NullKey" : decimalFormat.format(Double.parseDouble(downCorrectionParametersOne)));
            entity.setDowntionb(admeMeterWheelInfo.getDowntionb().equals("NullKey") ? "NullKey" : decimalFormat.format(Double.parseDouble(downCorrectionParametersTwo)));
            entity.setDownconstant(admeMeterWheelInfo.getDownconstant().equals("NullKey") ? "NullKey" : decimalFormat.format(Double.parseDouble(downConstant)));
            entity.setDownfilter(admeMeterWheelInfo.getDownfilter().equals("NullKey") ? "NullKey" : downFilterCoefficient);

            command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_METER_WHEEL, entity);
        } catch (Exception ex) {
            command = "";
            ex.printStackTrace();
        }
        return command;
    }

    public void initParamConfigInfo() {
        if (admeMeterWheelInfo == null) {
            Timber.e("AdmeMeterWheelParam is Null!");
            admeMeterWheelInfo = new AdmeMeterWheelInfo();
            return;
        }
        encoderLineNumber = admeMeterWheelInfo.getEnclinenum().trim();
        outerDiameter = admeMeterWheelInfo.getOutline().trim();
        upCorrectionParametersOne = admeMeterWheelInfo.getUptiona().trim();
        upCorrectionParametersTwo = admeMeterWheelInfo.getUptionb().trim();
        upConstant = admeMeterWheelInfo.getUpconstant().trim();
        upFilterCoefficient = admeMeterWheelInfo.getUpfilter().trim();
        downCorrectionParametersOne = admeMeterWheelInfo.getDowntiona().trim();
        downCorrectionParametersTwo = admeMeterWheelInfo.getDowntionb().trim();
        downConstant = admeMeterWheelInfo.getDownconstant().trim();
        downFilterCoefficient = admeMeterWheelInfo.getDownfilter().trim();

        try {
            if (encoderLineNumber.equals("NullKey")) {
                encoderLineNumberLayout.setVisibility(View.GONE);
            } else {
                mEtEncoderLineNumber.setText(encoderLineNumber);
            }
            if (outerDiameter.equals("NullKey")) {
                outerDiameterLayout.setVisibility(View.GONE);
            } else {
                decimalFormat.applyPattern("#");
                outerDiameter = decimalFormat.format(Double.parseDouble(outerDiameter));
                mEtOuterDiameter.setText(outerDiameter);
            }

            decimalFormat.applyPattern("#.######");
            if (upCorrectionParametersOne.equals("NullKey")) {
                upCorrectionParametersOneLayout.setVisibility(View.GONE);
            } else {
                upCorrectionParametersOne = decimalFormat.format(Double.parseDouble(upCorrectionParametersOne));
                mEtUpCorrectionParametersOne.setText(upCorrectionParametersOne);
            }
            if (upCorrectionParametersTwo.equals("NullKey")) {
                upCorrectionParametersTwoLayout.setVisibility(View.GONE);
            } else {
                upCorrectionParametersTwo = decimalFormat.format(Double.parseDouble(upCorrectionParametersTwo));
                mEtUpCorrectionParametersTwo.setText(upCorrectionParametersTwo);
            }
            if (upConstant.equals("NullKey")) {
                upConstantLayout.setVisibility(View.GONE);
            } else {
                upConstant = decimalFormat.format(Double.parseDouble(upConstant));
                mEtUpConstant.setText(upConstant);
            }
            if (upFilterCoefficient.equals("NullKey")) {
                upFilterCoefficientLayout.setVisibility(View.GONE);
            } else {
                mEtUpFilterCoefficient.setText(upFilterCoefficient);
            }
            if (downCorrectionParametersOne.equals("NullKey")) {
                downCorrectionParametersOneLayout.setVisibility(View.GONE);
            } else {
                downCorrectionParametersOne = decimalFormat.format(Double.parseDouble(downCorrectionParametersOne));
                mEtDownCorrectionParametersOne.setText(downCorrectionParametersOne);
            }
            if (downCorrectionParametersTwo.equals("NullKey")) {
                downCorrectionParametersTwoLayout.setVisibility(View.GONE);
            } else {
                downCorrectionParametersTwo = decimalFormat.format(Double.parseDouble(downCorrectionParametersTwo));
                mEtDownCorrectionParametersTwo.setText(downCorrectionParametersTwo);
            }
            if (downConstant.equals("NullKey")) {
                downConstantLayout.setVisibility(View.GONE);
            } else {
                downConstant = decimalFormat.format(Double.parseDouble(downConstant));
                mEtDownConstant.setText(downConstant);
            }
            if (downFilterCoefficient.equals("NullKey")) {
                downFilterCoefficientLayout.setVisibility(View.GONE);
            } else {
                mEtDownFilterCoefficient.setText(downFilterCoefficient);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void doAfterSetting() {
//        if (admeMeterWheelInfo != null) {
//            admeMeterWheelInfo.setEnclinenum(encoderLineNumber);
//            admeMeterWheelInfo.setOutline(outerDiameter);
//            admeMeterWheelInfo.setUptiona(upCorrectionParametersOne);
//            admeMeterWheelInfo.setUptionb(upCorrectionParametersTwo);
//            admeMeterWheelInfo.setUpconstant(upConstant);
//            admeMeterWheelInfo.setUpfilter(upFilterCoefficient);
//            admeMeterWheelInfo.setDowntiona(downCorrectionParametersOne);
//            admeMeterWheelInfo.setDowntionb(downCorrectionParametersTwo);
//            admeMeterWheelInfo.setDownconstant(downConstant);
//            admeMeterWheelInfo.setDownfilter(downFilterCoefficient);
//        }
        //TODO  打开注释，设置为浏览模式
//        configPageViewModel.configPageEditableChanged.setValue(false);
    }

    public boolean checkValueIsChange(boolean configPageEditableChanged) {
        if (!configPageEditableChanged)
            return false;

        if (encoderLineNumber != null && !encoderLineNumber.equals("NullKey") && !encoderLineNumber.equals(mEtEncoderLineNumber.getText().toString().trim())) {
            return true;
        }
        if (outerDiameter != null && !outerDiameter.equals("NullKey") && !outerDiameter.equals(mEtOuterDiameter.getText().toString().trim())) {
            return true;
        }
        if (upCorrectionParametersOne != null && !upCorrectionParametersOne.equals("NullKey") && !upCorrectionParametersOne.equals(mEtUpCorrectionParametersOne.getText().toString().trim())) {
            return true;
        }
        if (upCorrectionParametersTwo != null && !upCorrectionParametersTwo.equals("NullKey") && !upCorrectionParametersTwo.equals(mEtUpCorrectionParametersTwo.getText().toString().trim())) {
            return true;
        }
        if (upConstant != null && !upConstant.equals("NullKey") && !upConstant.equals(mEtUpConstant.getText().toString().trim())) {
            return true;
        }
        if (upFilterCoefficient != null && !upFilterCoefficient.equals("NullKey") && !upFilterCoefficient.equals(mEtUpFilterCoefficient.getText().toString().trim())) {
            return true;
        }
        if (downCorrectionParametersOne != null && !downCorrectionParametersOne.equals("NullKey") && !downCorrectionParametersOne.equals(mEtDownCorrectionParametersOne.getText().toString().trim())) {
            return true;
        }
        if (downCorrectionParametersTwo != null && !downCorrectionParametersTwo.equals("NullKey") && !downCorrectionParametersTwo.equals(mEtDownCorrectionParametersTwo.getText().toString().trim())) {
            return true;
        }
        if (downConstant != null && !downConstant.equals("NullKey") && !downConstant.equals(mEtDownConstant.getText().toString().trim())) {
            return true;
        }
        if (downFilterCoefficient != null && !downFilterCoefficient.equals("NullKey") && !downFilterCoefficient.equals(mEtDownFilterCoefficient.getText().toString().trim())) {
            return true;
        }
        return false;
    }

    public void onEditableChanged(boolean isEditable) {
        mEtEncoderLineNumber.setEnabled(isEditable);
        mEtOuterDiameter.setEnabled(isEditable);
        mEtUpCorrectionParametersOne.setEnabled(isEditable);
        mEtUpCorrectionParametersTwo.setEnabled(isEditable);
        mEtUpConstant.setEnabled(isEditable);
        mEtUpFilterCoefficient.setEnabled(isEditable);
        mEtDownCorrectionParametersOne.setEnabled(isEditable);
        mEtDownCorrectionParametersTwo.setEnabled(isEditable);
        mEtDownConstant.setEnabled(isEditable);
        mEtDownFilterCoefficient.setEnabled(isEditable);

        if (isEditable) {
            mEtEncoderLineNumber.setHint("请输入");
            mEtOuterDiameter.setHint("请输入");
            mEtUpCorrectionParametersOne.setHint("请输入");
            mEtUpCorrectionParametersTwo.setHint("请输入");
            mEtUpConstant.setHint("请输入");
            mEtUpFilterCoefficient.setHint("0-F");
            mEtDownCorrectionParametersOne.setHint("请输入");
            mEtDownCorrectionParametersTwo.setHint("请输入");
            mEtDownConstant.setHint("请输入");
            mEtDownFilterCoefficient.setHint("0-F");
        } else {
            mEtEncoderLineNumber.setHint("");
            mEtOuterDiameter.setHint("");
            mEtUpCorrectionParametersOne.setHint("");
            mEtUpCorrectionParametersTwo.setHint("");
            mEtUpConstant.setHint("");
            mEtUpFilterCoefficient.setHint("");
            mEtDownCorrectionParametersOne.setHint("");
            mEtDownCorrectionParametersTwo.setHint("");
            mEtDownConstant.setHint("");
            mEtDownFilterCoefficient.setHint("");
//            initParamConfigInfo();
        }
        mBtnSave.setVisibility(isEditable ? View.VISIBLE : View.GONE);
    }
}
