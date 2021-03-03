package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeMeasuringHoleDepthEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeMeasuringHoleDepthInfo;
import com.shmedo.configlibrary.iot.model.adme.AdmeMotorMotionDistanceInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.util.GlobalUtil;
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
 * 创建时间:  1/6/21 <br/>
 * 描述：     ADME 测量孔深
 */
public class BleAdmeMeasuringHoleDepthFragment extends BaseUSRBleIotCommunicateFragment {
    @BindView(R.id.tv_motion_way)
    TextView mTvMotionWay;

    @BindView(R.id.et_motor_movement_speed)
    ClearEditText mEtMovementSpeed;

    @BindView(R.id.motionDistanceEt)
    ClearEditText mEtMotionDistance;

    @BindView(R.id.radio_auto_control)
    RadioButton rbAutoControl;

    @BindView(R.id.radio_manual_control)
    RadioButton rbManualControl;

    @BindView(R.id.ll_clear_motion_data)
    ViewGroup clearMotionDataLayout;

    @BindView(R.id.ll_motion_data_clear_complete)
    ViewGroup motionDataClearCompleteLayout;

    @BindView(R.id.btn_run)
    Button mBtnRun;

    private int motionWayPos;

    private String motionWay;//  运动方式
    private String movementSpeed;// 电机运动速度(r/min)
    private String totalDistanceGoal;//  运动距离

    private String lastDistance;//当前距离

    private AdmeMeasuringHoleDepthInfo measuringHoleDepthInfo;
    private BleAdmeMotorMotionDistanceFragment motorMotionDistanceFragment;

    private DecimalFormat decimalFormat = new DecimalFormat();

    public static BleAdmeMeasuringHoleDepthFragment newInstance() {
        return new BleAdmeMeasuringHoleDepthFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ble_adme_measuring_hole_depth_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setView();
        setRadioButtonListener();
        queryMotorMotionConfig();
    }

    private void setView() {
        mEtMovementSpeed.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});
        mEtMovementSpeed.setHint("1-180");

        mEtMotionDistance.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});

        rbAutoControl.setChecked(true);
        rbManualControl.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));

        clearMotionDataLayout.setVisibility(View.VISIBLE);
        motionDataClearCompleteLayout.setVisibility(View.GONE);
    }

    /**
     * 单选框事件
     */
    private void setRadioButtonListener() {
        //自动单选按钮
        rbAutoControl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    rbAutoControl.setTextColor(GlobalUtil.getColor(R.color.text_color_343434));
                    mEtMotionDistance.setEnabled(true);
                    rbManualControl.setChecked(false);
                } else {
                    rbAutoControl.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                    mEtMotionDistance.setEnabled(false);
                }
            }
        });

        //手动单选按钮
        rbManualControl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    rbManualControl.setTextColor(GlobalUtil.getColor(R.color.text_color_343434));
                    rbAutoControl.setChecked(false);
                } else {
                    rbManualControl.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
                }
            }
        });
    }

    /**
     * 获取电机运动配置参数
     */
    private void queryMotorMotionConfig() {
        errMsg = "查询数据超时,请稍后尝试";
        startProgressRunnable("加载中...", WRITE_TIME_OUT_SECOND);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_MEASURING_HOLEDEPTH);
        sendCommand(command);
    }

    /**
     * 查询ADME测孔深运动的脉冲数、运动距离指令
     */
    private void getMotorMotionData() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_MEASURING_HOLEDEPTH_PULSE);
        sendCommand(command);
    }

    /**
     * 清空电机运动数据记录指令
     */
    private void clearMotorMotionData() {
        errMsg = "发送指令超时,请稍后尝试";
        startProgressRunnable("处理中...", WRITE_TIME_OUT_SECOND);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_CLEAR_MEASURING_HOLEDEPTH_DATA);
        sendCommand(command);
    }

    @OnClick({R.id.ll_motion_type, R.id.btn_run, R.id.ll_clear_motion_data})
    public void onClick(View view) {
        int id = view.getId();
        if (isDoubleClick(view)) {
            return;
        }

        if (id == R.id.ll_motion_type) {
            showMotionTypeDialog();

        } else if (id == R.id.btn_run) {
            KeyBordUtils.hideSoftKeyboard(view);
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            if (!checkValueIsValid()) {
                Timber.w("配置参数错误!");
                return;
            }
            processSave();

        } else if (id == R.id.ll_clear_motion_data) {
            KeyBordUtils.hideSoftKeyboard(view);
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            showClearWarnDialog("确认清除设备运动记录数据吗？");
        }
    }

    private boolean checkValueIsValid() {
        movementSpeed = mEtMovementSpeed.getText().toString().trim();
        totalDistanceGoal = mEtMotionDistance.getText().toString().trim();
        if (TextUtils.isEmpty(movementSpeed)) {
            ToastUtils.show("请输入电机运动速度!");
            mEtMovementSpeed.requestFocus();
            return false;
        }
        try {
            int port = Integer.parseInt(movementSpeed);
            if (port < 1 || port > 180) {
                ToastUtils.show("请输入正确的电机运动速度!");
                mEtMovementSpeed.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入正确的电机运动速度!");
            mEtMovementSpeed.requestFocus();
            return false;
        }

        if (rbAutoControl.isChecked()) {
            if (TextUtils.isEmpty(totalDistanceGoal)) {
                ToastUtils.show("请输入运动距离!");
                mEtMotionDistance.requestFocus();
                return false;
            }
            try {
                double value = Double.parseDouble(totalDistanceGoal);
                if (value <= 0) {
                    ToastUtils.show("请输入正确的运动距离!");
                    mEtMotionDistance.requestFocus();
                    return false;
                }
            } catch (Exception ex) {
                ToastUtils.show("请输入正确的运动距离!");
                mEtMotionDistance.requestFocus();
                return false;
            }
        } else {
            totalDistanceGoal = "99999";
        }

        return true;
    }

    private void processSave() {
        try {
            AdmeMeasuringHoleDepthEntity entity = new AdmeMeasuringHoleDepthEntity();
            entity.setMovementway(motionWay);
            entity.setMotorspeed(movementSpeed);
            decimalFormat.applyPattern("#.###");
            totalDistanceGoal = decimalFormat.format(Double.parseDouble(totalDistanceGoal));
            entity.setMovedistance(totalDistanceGoal);

            mBtnRun.setEnabled(false);
            errMsg = "发送指令超时,请稍后尝试";
            startProgressRunnable("处理中...", WRITE_TIME_OUT_SECOND);
            String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_MEASURING_HOLEDEPTH, entity);
            sendCommand(command);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void doProgressRun() {
        super.doProgressRun();
        mBtnRun.setEnabled(true);
    }

    /**
     * 选择运动方式
     */
    private void showMotionTypeDialog() {
        XPopup.setPrimaryColor(getResources().getColor(R.color.blue_52B4F8));
        new XPopup.Builder(mActivity)
                .isDestroyOnDismiss(true) //对于只使用一次的弹窗，推荐设置这个
                .asBottomList("", new String[]{"上拉", "下放"},
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
                        }, 0, R.layout.custom_xpopup_adapter_text_match)
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
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case ADME_MD_GET_MEASURING_HOLEDEPTH: {//获取ADME的测量孔深配置参数
                stopProgressRunnable();
                IOTCommandResult<AdmeMeasuringHoleDepthInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取测量孔深配置参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                measuringHoleDepthInfo = commandResult.getResult();
                if (motorMotionDistanceFragment != null && motorMotionDistanceFragment.isVisible()) {
                    motorMotionDistanceFragment.processMotorMotionState(measuringHoleDepthInfo);
                } else {
                    initParamConfigInfo();
                    getMotorMotionData();
                }
            }
            break;

            case ADME_MD_GET_MEASURING_HOLEDEPTH_PULSE: {//查询电机实时运动数据
                IOTCommandResult<AdmeMotorMotionDistanceInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取电机的实时运动数据出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                AdmeMotorMotionDistanceInfo motorMotionDistanceInfo = commandResult.getResult();
                if (motorMotionDistanceInfo != null) {
                    lastDistance = motorMotionDistanceInfo.getRealmovedistance();
                }
                if (motorMotionDistanceFragment != null && motorMotionDistanceFragment.isVisible()) {
                    motorMotionDistanceFragment.updateMotionData(motorMotionDistanceInfo);
                    return;
                }
            }
            break;

            case ADME_MD_SET_MEASURING_HOLEDEPTH: {//设置ADME的测量孔深配置参数
                stopProgressRunnable();
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置测量孔深配置参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    mBtnRun.setEnabled(true);
                    return;
                }
                doAfterSetting();
            }
            break;

            case ADME_MD_CLEAR_MEASURING_HOLEDEPTH_DATA: {//ADME测量孔深清空
                stopProgressRunnable();
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
        if (measuringHoleDepthInfo == null) {
            Timber.e("AdmeMeasuringHoleDepthInfo is Null!");
            measuringHoleDepthInfo = new AdmeMeasuringHoleDepthInfo();
            return;
        }
        motionWay = measuringHoleDepthInfo.getMovementway().trim();
        movementSpeed = measuringHoleDepthInfo.getMotorspeed().trim();
        totalDistanceGoal = measuringHoleDepthInfo.getMovedistance().trim();
        if (motionWay.equals("0")) {
            motionWayPos = 0;
            mTvMotionWay.setText("上拉");
        } else {
            motionWayPos = 1;
            mTvMotionWay.setText("下放");
        }

        try {
            mEtMovementSpeed.setText(movementSpeed);
            decimalFormat.applyPattern("#.###");
            totalDistanceGoal = decimalFormat.format(Double.parseDouble(totalDistanceGoal));
            mEtMotionDistance.setText(totalDistanceGoal);
        } catch (Exception ex) {
            ex.printStackTrace();
            totalDistanceGoal = "0";
            mEtMotionDistance.setText(totalDistanceGoal);
        }
        //电机处于运动状态，弹出底部运行数据展示框
        if (measuringHoleDepthInfo.getMorunstate().trim().equals("1")) {
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
        if (motorMotionDistanceFragment != null && motorMotionDistanceFragment.isVisible()) {
            motorMotionDistanceFragment.processContinueMotorMotion();
            return;
        }
        Timber.d("start Motion: lastDistance=%s,totalDistanceGoal=%s", lastDistance, totalDistanceGoal);
        motorMotionDistanceFragment = BleAdmeMotorMotionDistanceFragment.newInstance(motionWay, lastDistance, totalDistanceGoal);
        motorMotionDistanceFragment.show(getChildFragmentManager(), "dialog");

        clearMotionDataLayout.setVisibility(View.VISIBLE);
        motionDataClearCompleteLayout.setVisibility(View.GONE);
    }
}