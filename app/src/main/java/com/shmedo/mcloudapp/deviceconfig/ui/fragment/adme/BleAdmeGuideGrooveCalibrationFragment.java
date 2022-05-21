package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;
import android.os.Message;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.ColorUtils;
import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeGuideGrooveCalibrationEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeGuideGrooveCalibrationInfo;
import com.shmedo.configlibrary.iot.model.adme.AdmeMotorMotionAngleInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.BaseUSRBleIotCommunicateFragment;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/8/21 <br/>
 * 描述：     ADME 导槽校准
 */
public class BleAdmeGuideGrooveCalibrationFragment extends BaseUSRBleIotCommunicateFragment {
    @BindView(R.id.tv_motion_way)
    TextView mTvMotionWay;

    @BindView(R.id.et_motor_movement_speed)
    ClearEditText mEtMovementSpeed;

    @BindView(R.id.motionPulseEt)
    ClearEditText mEtMotionPulse;

    @BindView(R.id.ll_clear_motion_data)
    ViewGroup clearMotionDataLayout;

    @BindView(R.id.ll_motion_data_clear_complete)
    ViewGroup motionDataClearCompleteLayout;

    @BindView(R.id.btn_run)
    Button mBtnRun;

    private int motionWayPos;

    private String motionWay;//运动方式
    private String movementSpeed;//电机运动速度(r/min)
    private String totalPulseGoal;//运动脉冲数

    private String lastPulse;//上次停止时脉冲数

    private AdmeGuideGrooveCalibrationInfo grooveCalibrationInfo;
    private BleAdmeMotorMotionAngleFragment motorMotionAngleFragment;

    public static BleAdmeGuideGrooveCalibrationFragment newInstance() {
        return new BleAdmeGuideGrooveCalibrationFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ble_adme_guide_groove_calibration_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setView();
        queryMotorMotionConfig();
    }

    private void setView() {
        mEtMovementSpeed.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtMovementSpeed.setHint("1-600");

        mEtMotionPulse.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});

        clearMotionDataLayout.setVisibility(View.VISIBLE);
        motionDataClearCompleteLayout.setVisibility(View.GONE);
    }

    /**
     * 获取导槽校准配置参数
     */
    private void queryMotorMotionConfig() {
        startDefaultProgress("加载中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_10000_MILLIS);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_GUIDE_GROOVE_CALIBRATION);
        sendCommand(command);
    }

    /**
     * 查询ADME导槽校准的脉冲数、运动角度
     */
    private void getMotorMotionData() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_GUIDE_GROOVE_CALIBRATION_PULSE);
        sendCommand(command);
    }

    /**
     * 清空电机运动数据记录指令
     */
    private void clearMotorMotionData() {
        startDefaultProgress("处理中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_10000_MILLIS);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_CLEAR_GUIDE_GROOVE_CALIBRATION_DATA);
        sendCommand(command);
    }

    @OnClick({R.id.ll_motion_type, R.id.btn_run, R.id.ll_clear_motion_data})
    public void onClick(View view) {
        int id = view.getId();
        if (isDoubleClick(view)) {
            return;
        }
        if (id == R.id.ll_motion_type) {//选择运动方式
            showMotionTypeDialog();

        } else if (id == R.id.btn_run) {//运行
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            if (!checkValueIsValid()) {
                Timber.w("配置参数错误!");
                return;
            }
            processSave();

        } else if (id == R.id.ll_clear_motion_data) {//清空数据
            com.blankj.utilcode.util.KeyboardUtils.hideSoftInput(view);
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            showClearWarnDialog("确认清除设备运动记录数据吗？");
        }
    }

    private boolean checkValueIsValid() {
        movementSpeed = mEtMovementSpeed.getText().toString().trim();
        totalPulseGoal = mEtMotionPulse.getText().toString().trim();
        if (TextUtils.isEmpty(movementSpeed)) {
            ToastUtils.show("请输入电机运动速度!");
            mEtMovementSpeed.requestFocus();
            return false;
        }
        try {
            int port = Integer.parseInt(movementSpeed);
            if (port < 1 || port > 600) {
                ToastUtils.show("请输入正确的电机运动速度!");
                mEtMovementSpeed.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的电机运动速度!");
            mEtMovementSpeed.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(totalPulseGoal)) {
            ToastUtils.show("请输入运动脉冲!");
            mEtMotionPulse.requestFocus();
            return false;
        }
        try {
            int value = Integer.parseInt(totalPulseGoal);
            if (value <= 0) {
                ToastUtils.show("请输入正确的运动脉冲!");
                mEtMotionPulse.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的运动脉冲!");
            mEtMotionPulse.requestFocus();
            return false;
        }

        return true;
    }

    private void processSave() {
        try {
            AdmeGuideGrooveCalibrationEntity entity = new AdmeGuideGrooveCalibrationEntity();
            entity.setMovementway(motionWay);
            entity.setMotorspeed(movementSpeed);
            entity.setMovepulse(totalPulseGoal);

            startDefaultProgress("处理中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_10000_MILLIS);
            String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_GUIDE_GROOVE_CALIBRATION, entity);
            sendCommand(command);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 选择运动方式
     */
    private void showMotionTypeDialog() {
        XPopup.setPrimaryColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", new String[]{"正转", "反转"},
                        null, motionWayPos, true,
                        new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                motionWayPos = position;
                                mTvMotionWay.setText(text);
                                if (position == 0) {
                                    motionWay = "0";
                                } else {
                                    motionWay = "1";
                                }
                            }
                        }, 0, R.layout.custom_xpopup_adapter_text_with_check)
                .show();
    }

    /**
     * 清空数据提醒
     *
     * @param content
     */
    protected void showClearWarnDialog(String content) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(requireContext())
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
                        clearMotorMotionData();
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    @Override
    protected void customHandleMessage(@NonNull @NotNull Message msg) {
        if (msg.what == AppContants.MsgWhat.MSG_DEFAULT) {
            ToastUtils.show("响应超时,请稍后尝试");
        }
    }

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case ADME_MD_GET_GUIDE_GROOVE_CALIBRATION: {//获取ADME的导槽校准配置参数
                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                IOTCommandResult<AdmeGuideGrooveCalibrationInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取导槽校准配置参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                grooveCalibrationInfo = commandResult.getResult();
                if (motorMotionAngleFragment != null && motorMotionAngleFragment.isVisible()) {
                    motorMotionAngleFragment.processMotorMotionState(grooveCalibrationInfo);
                } else {
                    initParamConfigInfo();
                    getMotorMotionData();
                }
            }
            break;

            case ADME_MD_SET_GUIDE_GROOVE_CALIBRATION: {//设置ADME的导槽校准配置参数
                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置导槽校准配置参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    mBtnRun.setEnabled(true);
                    return;
                }
                doAfterSetting();
            }
            break;

            case ADME_MD_GET_GUIDE_GROOVE_CALIBRATION_PULSE: {//查询电机实时运动数据
                IOTCommandResult<AdmeMotorMotionAngleInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取电机的实时运动数据出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                AdmeMotorMotionAngleInfo motorMotionAngleInfo = commandResult.getResult();
                if (motorMotionAngleInfo != null) {
                    lastPulse = motorMotionAngleInfo.getPulsenumber();
                }
                if (motorMotionAngleFragment != null && motorMotionAngleFragment.isVisible()) {
                    motorMotionAngleFragment.updateMotionData(motorMotionAngleInfo);
                    return;
                }
            }
            break;

            case ADME_MD_CLEAR_GUIDE_GROOVE_CALIBRATION_DATA: {//ADME导槽校准清空
                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "清空数据出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                clearMotionDataLayout.setVisibility(View.GONE);
                motionDataClearCompleteLayout.setVisibility(View.VISIBLE);
                getMotorMotionData();
            }
            break;

            default:
                super.parseResponseMessage(cmdStr);
                break;
        }
    }

    private void initParamConfigInfo() {
        if (grooveCalibrationInfo == null) {
            Timber.e("AdmeGuideGrooveCalibrationInfo is Null!");
            grooveCalibrationInfo = new AdmeGuideGrooveCalibrationInfo();
            return;
        }
        motionWay = grooveCalibrationInfo.getMovementway().trim();
        movementSpeed = grooveCalibrationInfo.getMotorspeed().trim();
        totalPulseGoal = grooveCalibrationInfo.getMovePulse().trim();
        if (motionWay.equals("0")) {
            motionWayPos = 0;
            mTvMotionWay.setText("正转");
        } else {
            motionWayPos = 1;
            mTvMotionWay.setText("反转");
        }

        DecimalFormat decimalFormat = new DecimalFormat();
        decimalFormat.applyPattern("#");
        try {
            mEtMovementSpeed.setText(movementSpeed);
            totalPulseGoal = decimalFormat.format(Double.parseDouble(totalPulseGoal));
            mEtMotionPulse.setText(totalPulseGoal);
        } catch (Exception ex) {
            ex.printStackTrace();
            totalPulseGoal = "0";
            mEtMotionPulse.setText(totalPulseGoal);
        }
        //电机处于运动状态，弹出底部运行数据展示框
        if (grooveCalibrationInfo.getMorunstate().trim().equals("1")) {
            showMotorMotionDialog();
        }
    }

    private void doAfterSetting() {
        mBtnRun.setEnabled(true);
        showMotorMotionDialog();
    }

    /**
     * 打开数据运行弹框
     */
    private void showMotorMotionDialog() {
        //数据运行弹框已经显示了
        if (motorMotionAngleFragment != null && motorMotionAngleFragment.isVisible()) {
            motorMotionAngleFragment.processContinueMotorMotion();
            return;
        }
        Timber.d("start Motion: lastPulse=%s,totalPulseGoal=%s", lastPulse, totalPulseGoal);
        motorMotionAngleFragment = BleAdmeMotorMotionAngleFragment.newInstance(motionWay, lastPulse, totalPulseGoal);
        motorMotionAngleFragment.show(getChildFragmentManager(), "dialog");

        clearMotionDataLayout.setVisibility(View.VISIBLE);
        motionDataClearCompleteLayout.setVisibility(View.GONE);
    }
}