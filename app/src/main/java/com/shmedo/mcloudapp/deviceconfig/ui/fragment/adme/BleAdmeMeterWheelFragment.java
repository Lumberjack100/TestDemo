package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeMeterWheelEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeMeterWheelInfo;
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
 * 创建时间:  2020/12/27<br/>
 * 描述：     ADME 计米轮参数配置页面
 */
public class BleAdmeMeterWheelFragment extends BaseBleIotCommunicateFragment {
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

    private AdmeMeterWheelInfo admeMeterWheelInfo;

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


    public static BleAdmeMeterWheelFragment newInstance() {
        return new BleAdmeMeterWheelFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ble_adme_meter_wheel_fragment;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setView();
        queryParamConfigInfo();
    }

    private void setView() {
        mEtEncoderLineNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtOuterDiameter.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtUpCorrectionParametersOne.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        mEtUpCorrectionParametersTwo.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        mEtUpConstant.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        mEtUpFilterCoefficient.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtDownCorrectionParametersOne.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        mEtDownCorrectionParametersTwo.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        mEtDownConstant.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        mEtDownFilterCoefficient.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

        mEtUpFilterCoefficient.setHint("0-F");
        mEtDownFilterCoefficient.setHint("0-F");
    }

    /**
     * 获取ADME的计米轮参数
     */
    private void queryParamConfigInfo() {
        errMsg = "查询数据超时,请稍后尝试";
        startProgressRunnable("加载中...", WRITE_TIME_OUT_SECOND);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_METER_WHEEL_PARAMETERS);
        sendCommand(command);
    }

    @OnClick({R.id.btn_confirm})
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btn_confirm) {
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

    private boolean checkValueIsValid() {
        encoderLineNumber = mEtEncoderLineNumber.getText().toString().trim();
        outerDiameter = mEtOuterDiameter.getText().toString().trim();
        upCorrectionParametersOne = mEtUpCorrectionParametersOne.getText().toString().trim();
        upCorrectionParametersTwo = mEtUpCorrectionParametersTwo.getText().toString().trim();
        upConstant = mEtUpConstant.getText().toString().trim();
        upFilterCoefficient = mEtUpFilterCoefficient.getText().toString().trim();
        downCorrectionParametersOne = mEtDownCorrectionParametersOne.getText().toString().trim();
        downCorrectionParametersTwo = mEtDownCorrectionParametersTwo.getText().toString().trim();
        downConstant = mEtDownConstant.getText().toString().trim();
        downFilterCoefficient = mEtDownFilterCoefficient.getText().toString().trim();

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

        if (TextUtils.isEmpty(upFilterCoefficient)) {
            ToastUtils.show("请输入上拉滤波器系数!");
            mEtUpFilterCoefficient.requestFocus();
            return false;
        }
        String filterCoefficientRule = "[A-F0-9]";
        if (!upFilterCoefficient.matches(filterCoefficientRule)) {
            ToastUtils.show("请输入正确的上拉滤波器系数!");
            mEtUpFilterCoefficient.requestFocus();
            return false;
        }

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

        return true;
    }

    private void processSave() {
        try {
            AdmeMeterWheelEntity entity = new AdmeMeterWheelEntity();
            entity.setEnclinenum(encoderLineNumber);
            entity.setOutline(outerDiameter);

            decimalFormat.applyPattern("#.#####");
            entity.setUptiona(decimalFormat.format(Double.parseDouble(upCorrectionParametersOne)));
            entity.setUptionb(decimalFormat.format(Double.parseDouble(upCorrectionParametersTwo)));
            entity.setUpconstant(decimalFormat.format(Double.parseDouble(upConstant)));
            entity.setUpfilter(upFilterCoefficient);
            entity.setDowntiona(decimalFormat.format(Double.parseDouble(downCorrectionParametersOne)));
            entity.setDowntionb(decimalFormat.format(Double.parseDouble(downCorrectionParametersTwo)));
            entity.setDownconstant(decimalFormat.format(Double.parseDouble(downConstant)));
            entity.setDownfilter(downFilterCoefficient);

            errMsg = "发送指令超时,请稍后尝试";
            startProgressRunnable("处理中...", WRITE_TIME_OUT_SECOND);
            mBtnSave.setEnabled(false);
            String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_METER_WHEEL_PARAMETERS, entity);
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
            case ADME_MD_GET_METER_WHEEL_PARAMETERS: {//获取ADME的计米轮配置参数
                stopProgressRunnable();
                IOTCommandResult<AdmeMeterWheelInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取计米轮配置参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                admeMeterWheelInfo = commandResult.getResult();
                initParamConfigInfo();
            }
            break;

            case ADME_MD_SET_METER_WHEEL_PARAMETERS: {//设置ADME的计米轮配置参数
                stopProgressRunnable();
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "保存计米轮配置参数出错!", cmdResult.getReason());
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
        mBtnSave.setEnabled(true);
        ToastUtils.show("保存成功");
    }

    private void initParamConfigInfo() {
        if (admeMeterWheelInfo == null) {
            Timber.e("AdmeMeterWheelParam is Null!");
            admeMeterWheelInfo = new AdmeMeterWheelInfo();
            return;
        }
        try {
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

            mEtEncoderLineNumber.setText(encoderLineNumber);
            decimalFormat.applyPattern("#");
            outerDiameter = decimalFormat.format(Double.parseDouble(outerDiameter));
            mEtOuterDiameter.setText(outerDiameter);

            decimalFormat.applyPattern("#.#####");
            upCorrectionParametersOne = decimalFormat.format(Double.parseDouble(upCorrectionParametersOne));
            mEtUpCorrectionParametersOne.setText(upCorrectionParametersOne);

            upCorrectionParametersTwo = decimalFormat.format(Double.parseDouble(upCorrectionParametersTwo));
            mEtUpCorrectionParametersTwo.setText(upCorrectionParametersTwo);

            upConstant = decimalFormat.format(Double.parseDouble(upConstant));
            mEtUpConstant.setText(upConstant);

            mEtUpFilterCoefficient.setText(upFilterCoefficient);

            downCorrectionParametersOne = decimalFormat.format(Double.parseDouble(downCorrectionParametersOne));
            mEtDownCorrectionParametersOne.setText(downCorrectionParametersOne);

            downCorrectionParametersTwo = decimalFormat.format(Double.parseDouble(downCorrectionParametersTwo));
            mEtDownCorrectionParametersTwo.setText(downCorrectionParametersTwo);

            downConstant = decimalFormat.format(Double.parseDouble(downConstant));
            mEtDownConstant.setText(downConstant);

            mEtDownFilterCoefficient.setText(downFilterCoefficient);
        } catch (Exception ex) {
            ex.printStackTrace();
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
        if (encoderLineNumber != null && !encoderLineNumber.equals(mEtEncoderLineNumber.getText().toString().trim())) {
            return true;
        }
        if (outerDiameter != null && !outerDiameter.equals(mEtOuterDiameter.getText().toString().trim())) {
            return true;
        }
        if (upCorrectionParametersOne != null && !upCorrectionParametersOne.equals(mEtUpCorrectionParametersOne.getText().toString().trim())) {
            return true;
        }
        if (upCorrectionParametersTwo != null && !upCorrectionParametersTwo.equals(mEtUpCorrectionParametersTwo.getText().toString().trim())) {
            return true;
        }
        if (upConstant != null && !upConstant.equals(mEtUpConstant.getText().toString().trim())) {
            return true;
        }
        if (upFilterCoefficient != null && !upFilterCoefficient.equals(mEtUpFilterCoefficient.getText().toString().trim())) {
            return true;
        }
        if (downCorrectionParametersOne != null && !downCorrectionParametersOne.equals(mEtDownCorrectionParametersOne.getText().toString().trim())) {
            return true;
        }
        if (downCorrectionParametersTwo != null && !downCorrectionParametersTwo.equals(mEtDownCorrectionParametersTwo.getText().toString().trim())) {
            return true;
        }
        if (downConstant != null && !downConstant.equals(mEtDownConstant.getText().toString().trim())) {
            return true;
        }
        if (downFilterCoefficient != null && !downFilterCoefficient.equals(mEtDownFilterCoefficient.getText().toString().trim())) {
            return true;
        }

        return false;
    }
}