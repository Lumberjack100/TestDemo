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
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeMeterWheelParamEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeMeterWheelParam;
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

    private AdmeMeterWheelParam admeMeterWheelParam;

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
        mEtEncoderLineNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        mEtOuterDiameter.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        mEtUpCorrectionParametersOne.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        mEtUpCorrectionParametersTwo.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        mEtUpConstant.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        mEtUpFilterCoefficient.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});
        mEtDownCorrectionParametersOne.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        mEtDownCorrectionParametersTwo.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        mEtDownConstant.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});
        mEtDownFilterCoefficient.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1)});

        mEtUpFilterCoefficient.setHint("0-F");
        mEtDownFilterCoefficient.setHint("0-F");
    }

    /**
     * 获取ADME的计米轮参数
     */
    private void queryParamConfigInfo() {
        errMsg = "查询数据超时,请稍后尝试";
        startProgressRunnable("加载中...", DELAY_MILLIS);
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
                Timber.w("基础配置参数错误!");
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
            ToastUtils.show("编码器线数不能为空!");
            mEtEncoderLineNumber.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(encoderLineNumber);

        } catch (Exception ex) {
            ToastUtils.show("请输入正确的编码器线数!");
            mEtEncoderLineNumber.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(outerDiameter)) {
            ToastUtils.show("外径不能为空!");
            mEtOuterDiameter.requestFocus();
            return false;
        }
        try {
            double value = Double.parseDouble(outerDiameter);

        } catch (Exception ex) {
            ToastUtils.show("请输入正确的外径!");
            mEtOuterDiameter.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(upCorrectionParametersOne)) {
            ToastUtils.show("上拉一次修正参数不能为空!");
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
            ToastUtils.show("上拉二次修正参数不能为空!");
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
            ToastUtils.show("上拉常数不能为空!");
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
            ToastUtils.show("上拉滤波器系数不能为空!");
            mEtUpFilterCoefficient.requestFocus();
            return false;
        }
        try {
            double value = Double.parseDouble(upFilterCoefficient);

        } catch (Exception ex) {
            ToastUtils.show("请输入正确的上拉滤波器系数!");
            mEtUpFilterCoefficient.requestFocus();
            return false;
        }


        if (TextUtils.isEmpty(downCorrectionParametersOne)) {
            ToastUtils.show("下放一次修正参数不能为空!");
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
            ToastUtils.show("下放二次修正参数不能为空!");
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
            ToastUtils.show("下放常数不能为空!");
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
            ToastUtils.show("下放滤波器系数不能为空!");
            mEtDownFilterCoefficient.requestFocus();
            return false;
        }
        try {
            double value = Double.parseDouble(downFilterCoefficient);

        } catch (Exception ex) {
            ToastUtils.show("请输入正确的下放滤波器系数!");
            mEtDownFilterCoefficient.requestFocus();
            return false;
        }

        return true;
    }

    private void processSave() {
        AdmeMeterWheelParamEntity entity =new AdmeMeterWheelParamEntity() ;
        entity.setEnclinenum(encoderLineNumber);
        entity.setOutline(outerDiameter);
        entity.setUptiona(upCorrectionParametersOne);
        entity.setUptionb(upCorrectionParametersTwo);
        entity.setUpconstant(upConstant);
        entity.setUpfilter(upFilterCoefficient);
        entity.setDowntiona(downCorrectionParametersOne);
        entity.setDowntionb(downCorrectionParametersTwo);
        entity.setDownconstant(downConstant);
        entity.setDownfilter(downFilterCoefficient);

        errMsg = "发送指令超时,请稍后尝试";
        startProgressRunnable("正在发送配置指令...", DELAY_MILLIS);
        mBtnSave.setEnabled(false);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_METER_WHEEL_PARAMETERS, entity);
        sendCommand(command);
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
                IOTCommandResult<AdmeMeterWheelParam> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取计米轮配置参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                admeMeterWheelParam=commandResult.getResult();
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
                stopProgressRunnable();
                super.parseResponseMessage(cmdStr);
                break;
        }
    }

    private void doAfterSetting() {
        mBtnSave.setEnabled(true);
        ToastUtils.show("保存成功");
    }

    private void initParamConfigInfo() {
        if (admeMeterWheelParam == null) {
            Timber.e("AdmeMeterWheelParam is Null!");
            admeMeterWheelParam = new AdmeMeterWheelParam();
            return;
        }

        encoderLineNumber = admeMeterWheelParam.getEnclinenum().trim();
        outerDiameter = admeMeterWheelParam.getOutline().trim();
        upCorrectionParametersOne = admeMeterWheelParam.getUptiona().trim();
        upCorrectionParametersTwo = admeMeterWheelParam.getUptionb().trim();
        upConstant = admeMeterWheelParam.getUpconstant().trim();
        upFilterCoefficient = admeMeterWheelParam.getUpfilter().trim();
        downCorrectionParametersOne = admeMeterWheelParam.getDowntiona().trim();
        downCorrectionParametersTwo = admeMeterWheelParam.getDowntionb().trim();
        downConstant = admeMeterWheelParam.getDownconstant().trim();
        downFilterCoefficient = admeMeterWheelParam.getDownfilter().trim();

        mEtEncoderLineNumber.setText(encoderLineNumber);
        mEtOuterDiameter.setText(outerDiameter);
        mEtUpCorrectionParametersOne.setText(upCorrectionParametersOne);
        mEtUpCorrectionParametersTwo.setText(upCorrectionParametersTwo);
        mEtUpConstant.setText(upConstant);
        mEtUpFilterCoefficient.setText(upFilterCoefficient);
        mEtDownCorrectionParametersOne.setText(downCorrectionParametersOne);
        mEtDownCorrectionParametersTwo.setText(downCorrectionParametersTwo);
        mEtDownConstant.setText(downConstant);
        mEtDownFilterCoefficient.setText(downFilterCoefficient);
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