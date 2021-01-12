package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.kyleduo.switchbutton.SwitchButton;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeStepperMotorEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeStepperMotorInfo;
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
 * 描述：     ADME 步进电机参数配置页面
 */
public class BleAdmeStepperMotorFragment extends BaseBleIotCommunicateFragment {
    @BindView(R.id.contentLayout)
    ViewGroup contentLayout;

    @BindView(R.id.maskLayer)
    ViewGroup maskLayerLayout;

    @BindView(R.id.paramEnableSBtn)
    SwitchButton mSbParamEnable;

    @BindView(R.id.et_accuracy_correction_value)
    ClearEditText mEtAccuracyCorrectionValue;

    @BindView(R.id.et_movement_speed)
    ClearEditText mEtMovementSpeed;

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    private AdmeStepperMotorInfo admeStepperMotorInfo;

    private String accuracyCorrectionValue;//绝对精度修正值
    private String movementSpeed;//电机运动速度(r/min)

    private boolean paramEnableInitial;//开关初始状态，用于判断开关是否有打开后没有设置参数就返回
    private boolean isSaveParamOperation = false;//判断当前是保存参数操作，还是关闭开关操作

    private DecimalFormat decimalFormat = new DecimalFormat();


    public static BleAdmeStepperMotorFragment newInstance() {
        return new BleAdmeStepperMotorFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ble_adme_stepper_motor_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setView();
        setSwitchViewListener();
        queryParamInfo();
    }

    private void setView() {
        mEtAccuracyCorrectionValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
        mEtMovementSpeed.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

        mEtMovementSpeed.setHint("1-100");
    }

    private void setSwitchViewListener() {
        mSbParamEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isConnected()) {
                    ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                    mSbParamEnable.setCheckedImmediatelyNoEvent(!isChecked);
                    return;
                }

                if (!isChecked) {
                    showCloseSwitchButtonDialog("确定使参数不生效？");
                } else {
                    maskLayerLayout.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * 关闭SwitchButton
     */
    private void showCloseSwitchButtonDialog(String content) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(mActivity)
                .title("温馨提示：")
                .content(content)
                .contentColorRes(R.color.title_text_color)
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .positiveColorRes(R.color.blue_52B4F8)
                .negativeColorRes(R.color.sub_title_text_color)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        disableStepperMotorParam();
                        maskLayerLayout.setVisibility(View.VISIBLE);
                        paramEnableInitial = mSbParamEnable.isChecked();
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        mSbParamEnable.setCheckedImmediatelyNoEvent(true);
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    /**
     * 获取设备的步进电机参数
     */
    private void queryParamInfo() {
        errMsg = "查询数据超时,请稍后尝试";
        startProgressRunnable("加载中...", WRITE_TIME_OUT_SECOND);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_STEPPER_MOTOR_PARAMETERS);
        sendCommand(command);
    }

    /**
     * 禁用步进电机参数</br>
     */
    private void disableStepperMotorParam() {
        AdmeStepperMotorEntity entity = new AdmeStepperMotorEntity();
        entity.setPosnegtest("0");

        isSaveParamOperation = false;
        mBtnSave.setEnabled(false);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_STEPPER_MOTOR_PARAMETERS, entity);
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
        accuracyCorrectionValue = mEtAccuracyCorrectionValue.getText().toString().trim();
        movementSpeed = mEtMovementSpeed.getText().toString().trim();

        if (TextUtils.isEmpty(accuracyCorrectionValue)) {
            ToastUtils.show("请输入绝对精度修正值!");
            mEtAccuracyCorrectionValue.requestFocus();
            return false;
        }
        try {
            double value = Double.parseDouble(accuracyCorrectionValue);
            if (value < 0) {
                ToastUtils.show("请输入正确的绝对精度修正值!");
                mEtAccuracyCorrectionValue.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的绝对精度修正值!");
            mEtAccuracyCorrectionValue.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(movementSpeed)) {
            ToastUtils.show("请输入电机运动速度!");
            mEtMovementSpeed.requestFocus();
            return false;
        }
        try {
            int port = Integer.parseInt(movementSpeed);
            if (port < 1 || port > 100) {
                ToastUtils.show("请输入正确的电机运动速度!");
                mEtMovementSpeed.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的电机运动速度!");
            mEtMovementSpeed.requestFocus();
            return false;
        }
        return true;
    }

    private void processSave() {
        try {
            AdmeStepperMotorEntity entity = new AdmeStepperMotorEntity();
            entity.setPosnegtest("1");
            decimalFormat.applyPattern("#.##");
            entity.setAbsprsion(decimalFormat.format(Double.parseDouble(accuracyCorrectionValue)));
            entity.setMovspeed(movementSpeed);

            paramEnableInitial = mSbParamEnable.isChecked();
            isSaveParamOperation = true;
            mBtnSave.setEnabled(false);

            errMsg = "发送指令超时,请稍后尝试";
            startProgressRunnable("正在发送配置指令...", WRITE_TIME_OUT_SECOND);
            String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_STEPPER_MOTOR_PARAMETERS, entity);
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
            case ADME_MD_GET_STEPPER_MOTOR_PARAMETERS: {//获取ADME的步进电机配置参数
                stopProgressRunnable();
                IOTCommandResult<AdmeStepperMotorInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "查询步进电机参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                admeStepperMotorInfo = commandResult.getResult();
                initParamConfigInfo();
            }
            break;

            case ADME_MD_SET_STEPPER_MOTOR_PARAMETERS: {//设置ADME的步进电机配置参数
                stopProgressRunnable();
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置步进电机参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    mBtnSave.setEnabled(true);
                    if (isSaveParamOperation)
                        isSaveParamOperation = false;
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
        if (isSaveParamOperation) {
            isSaveParamOperation = false;
            ToastUtils.show("设置成功");
        }
        mBtnSave.setEnabled(true);
    }

    private void initParamConfigInfo() {
        if (admeStepperMotorInfo == null) {
            Timber.e("AdmeStepperMotorInfo is Null!");
            admeStepperMotorInfo = new AdmeStepperMotorInfo();
            return;
        }

        try {
            accuracyCorrectionValue = admeStepperMotorInfo.getAbsprsion().trim();
            movementSpeed = admeStepperMotorInfo.getMovspeed().trim();

            decimalFormat.applyPattern("#.##");
            accuracyCorrectionValue = decimalFormat.format(Double.parseDouble(accuracyCorrectionValue));
            mEtAccuracyCorrectionValue.setText(accuracyCorrectionValue);
            mEtMovementSpeed.setText(movementSpeed);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (admeStepperMotorInfo.getPosnegtest().trim().equals("0")) {
            paramEnableInitial = false;
            mSbParamEnable.setCheckedImmediatelyNoEvent(false);
            maskLayerLayout.setVisibility(View.VISIBLE);
        } else {
            paramEnableInitial = true;
            mSbParamEnable.setCheckedImmediatelyNoEvent(true);
            maskLayerLayout.setVisibility(View.GONE);
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
        if (!mSbParamEnable.isChecked()) {
            return false;
        }

        if (paramEnableInitial != mSbParamEnable.isChecked()) {
            return true;
        }

        if (accuracyCorrectionValue != null && !accuracyCorrectionValue.equals(mEtAccuracyCorrectionValue.getText().toString().trim())) {
            return true;
        }

        if (movementSpeed != null && !movementSpeed.equals(mEtMovementSpeed.getText().toString().trim())) {
            return true;
        }

        return false;
    }

}