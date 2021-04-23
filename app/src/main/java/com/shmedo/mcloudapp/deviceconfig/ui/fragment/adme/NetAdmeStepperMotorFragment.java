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
import com.shmedo.mcloudapp.deviceconfig.model.DispatchCmdItem;
import com.shmedo.mcloudapp.deviceconfig.model.QueryCmdResult;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.netcommon.BaseNetIotCommunicateFragment;
import com.shmedo.mcloudapp.projects.model.ProjectDeviceInfo;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/4/23 <br/>
 * 描述：      ADME 步进电机参数配置页面
 */
public class NetAdmeStepperMotorFragment extends BaseNetIotCommunicateFragment {

    @BindView(R.id.paramEnableSBtn)
    SwitchButton mSbParamEnable;

    @BindView(R.id.et_accuracy_correction_value)
    ClearEditText mEtAccuracyCorrectionValue;

    @BindView(R.id.et_motor_movement_speed)
    ClearEditText mEtMovementSpeed;

    @BindView(R.id.et_motor_torque)
    ClearEditText mEtMotorTorque;

    @BindView(R.id.btn_confirm)
    Button mBtnSave;

    @BindView(R.id.maskLayerChild)
    ViewGroup maskLayerChild;

    @BindView(R.id.maskLayerLayout)
    ViewGroup maskLayerLayout;

    private AdmeStepperMotorInfo admeStepperMotorInfo;

    private String accuracyCorrectionValue;//绝对精度修正值
    private String movementSpeed;//步进电机运动速度(r/min)
    private String motorTorque;//步进电机力矩

    private boolean paramEnableInitial;//开关初始状态，用于判断开关是否有打开后没有设置参数就返回
    private boolean isSaveParamOperation = false;//判断当前是保存参数操作，还是关闭开关操作

    private DecimalFormat decimalFormat = new DecimalFormat();


    public static NetAdmeStepperMotorFragment newInstance(ProjectDeviceInfo projectDeviceInfo) {
        NetAdmeStepperMotorFragment fragment = new NetAdmeStepperMotorFragment();
        Bundle args = new Bundle();
        args.putParcelable(PRO_DEVICE_INFO, projectDeviceInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.adme_stepper_motor_fragment;
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
        mEtAccuracyCorrectionValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
        mEtMovementSpeed.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtMotorTorque.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});

        mEtMovementSpeed.setHint("1-600");
    }

    private void setSwitchViewListener() {
        mSbParamEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked) {
                    showCloseSwitchButtonDialog("确定使参数不生效？");
                } else {
                    maskLayerChild.setVisibility(View.GONE);
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
                        maskLayerChild.setVisibility(View.VISIBLE);
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
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_STEPPER_MOTOR);
        showProgressDialog("加载中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    /**
     * 禁用步进电机参数</br>
     */
    private void disableStepperMotorParam() {
        AdmeStepperMotorEntity entity = new AdmeStepperMotorEntity();
        entity.setPosnegtest("0");

        isSaveParamOperation = false;
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_STEPPER_MOTOR, entity);
        showProgressDialog("处理中...");
        doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
    }

    @OnClick({R.id.btn_confirm})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.btn_confirm) {
            KeyBordUtils.hideSoftKeyboard(view);
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
        motorTorque = mEtMotorTorque.getText().toString().trim();

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
            ToastUtils.show("请输入步进电机运动速度!");
            mEtMovementSpeed.requestFocus();
            return false;
        }
        try {
            int port = Integer.parseInt(movementSpeed);
            if (port < 1 || port > 600) {
                ToastUtils.show("请输入正确的步进电机运动速度!");
                mEtMovementSpeed.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的步进电机运动速度!");
            mEtMovementSpeed.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(motorTorque)) {
            ToastUtils.show("请输入步进电机力矩!");
            mEtMotorTorque.requestFocus();
            return false;
        }
        try {
            int port = Integer.parseInt(motorTorque);
            if (port < 0) {
                ToastUtils.show("请输入正确的步进电机力矩!");
                mEtMotorTorque.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的步进电机力矩!");
            mEtMotorTorque.requestFocus();
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
            entity.setMovesm(motorTorque);

            paramEnableInitial = mSbParamEnable.isChecked();
            isSaveParamOperation = true;

            String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_STEPPER_MOTOR, entity);
            showProgressDialog("处理中...");
            doCommonDispatchRawCmd(command, Arrays.asList(projectDeviceInfo.getId()));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 调用指令下发/透传接口结果返回
     *
     * @param dispatchCmdItemList
     */
    @Override
    protected void onDispatchCmdResult(List<DispatchCmdItem> dispatchCmdItemList, String cmdStr) {
        if (dispatchCmdItemList == null || dispatchCmdItemList.size() == 0) {
            dismissProgressDialog();
            showDispatchFailedDialog(cmdStr);
            return;
        }
        msgIDList.clear();
        for (DispatchCmdItem cmdItem : dispatchCmdItemList) {
            msgIDList.add(cmdItem.getMsgID());
        }
        if (msgIDList != null && msgIDList.size() > 0) {
            startQueryCmdResponseRunnable(0);
        }
    }

    /**
     * 指令下发失败弹框
     */
    private void showDispatchFailedDialog(String cmdStr) {
        ToastUtils.show("下发指令失败");
    }

    /**
     * 查询指令响应结果出错
     *
     * @param errMsg
     */
    @Override
    protected void onQueryCmdResponseResultError(String errMsg) {
        super.onQueryCmdResponseResultError(errMsg);
        ToastUtils.show("指令响应错误");
    }

    /**
     * 查询指令响应结果超时
     *
     * @param queryCmdResult
     */
    @Override
    protected void onQueryCmdResponseResultTimeOut(QueryCmdResult queryCmdResult) {
        super.onQueryCmdResponseResultTimeOut(queryCmdResult);
        ToastUtils.show("指令响应超时");
    }

    /**
     * 查询指令响应结果成功
     *
     * @param queryCmdResult
     */
    @Override
    protected void onQueryCmdResponseResultSuccess(QueryCmdResult queryCmdResult) {
//        super.onQueryCmdResponseResultSuccess(queryCmdResult);
        setResultData(queryCmdResult);
    }

    private void setResultData(QueryCmdResult queryCmdResult) {
        String cmdStr = queryCmdResult.getResponseContent();
        IOTCommandType type = IOTStringUtil.extractCommandType(queryCmdResult.getCmdEngName());
        switch (type) {
            case ADME_MD_GET_STEPPER_MOTOR: {//获取ADME的步进电机配置参数
                dismissProgressDialog();
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

            case ADME_MD_SET_STEPPER_MOTOR: {//设置ADME的步进电机配置参数
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    dismissProgressDialog();
                    String errMsg = String.format("%s %s", "设置步进电机参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                doAfterSetting();
            }
            break;

            case MD_SAVE_CONFIG_PARAM: {
                dismissProgressDialog();
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "保存指令出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
            }
            if (isSaveParamOperation) {
                ToastUtils.show("保存成功");
            }
            break;

            default:
                break;
        }
    }

    private void initParamConfigInfo() {
        if (admeStepperMotorInfo == null) {
            Timber.e("AdmeStepperMotorInfo is Null!");
            admeStepperMotorInfo = new AdmeStepperMotorInfo();
            return;
        }
        if (admeStepperMotorInfo.getPosnegtest().trim().equals("0")) {
            paramEnableInitial = false;
            mSbParamEnable.setCheckedImmediatelyNoEvent(false);
            maskLayerChild.setVisibility(View.VISIBLE);
        } else {
            paramEnableInitial = true;
            mSbParamEnable.setCheckedImmediatelyNoEvent(true);
            maskLayerChild.setVisibility(View.GONE);
        }
        try {
            accuracyCorrectionValue = admeStepperMotorInfo.getAbsprsion().trim();
            movementSpeed = admeStepperMotorInfo.getMovspeed().trim();
            motorTorque = admeStepperMotorInfo.getMovesm().trim();

            decimalFormat.applyPattern("#.##");
            accuracyCorrectionValue = decimalFormat.format(Double.parseDouble(accuracyCorrectionValue));
            mEtAccuracyCorrectionValue.setText(accuracyCorrectionValue);
            mEtMovementSpeed.setText(movementSpeed);
            mEtMotorTorque.setText(motorTorque);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void doAfterSetting() {
        if (isSaveParamOperation) {
            if (admeStepperMotorInfo != null) {
                admeStepperMotorInfo.setAbsprsion(accuracyCorrectionValue);
                admeStepperMotorInfo.setMovspeed(movementSpeed);
                admeStepperMotorInfo.setMovesm(motorTorque);
            }
        }
        //TODO  打开注释，设置为浏览模式
//        configPageViewModel.configPageEditableChanged.setValue(false);
        saveConfigInfo();
    }

    @Override
    public boolean onBackPressed() {
        if (checkValueIsChange()) {
            warnNotYetSettingBeforeLeavePage();
            return true;
        } else {
            return false;
        }
    }

    private boolean checkValueIsChange() {
        if (!configPageViewModel.configPageEditableChanged.getValue())
            return false;

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
        if (motorTorque != null && !motorTorque.equals(mEtMotorTorque.getText().toString().trim())) {
            return true;
        }
        return false;
    }

    @Override
    protected void onEditableChanged(boolean isEditable) {
        if (isEditable) {
            mEtAccuracyCorrectionValue.setHint("请输入");
            mEtMovementSpeed.setHint("1-600");
        } else {
            mEtAccuracyCorrectionValue.setHint("");
            mEtMovementSpeed.setHint("");

            mEtAccuracyCorrectionValue.clearFocus();
            mEtMovementSpeed.clearFocus();

            initParamConfigInfo();
        }
        maskLayerLayout.setVisibility(isEditable ? View.GONE : View.VISIBLE);
        mBtnSave.setVisibility(isEditable ? View.VISIBLE : View.GONE);
    }
}
