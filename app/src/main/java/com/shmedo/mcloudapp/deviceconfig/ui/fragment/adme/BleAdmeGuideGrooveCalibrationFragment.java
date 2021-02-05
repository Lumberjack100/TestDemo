package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;
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
 * 创建时间:  1/8/21 <br/>
 * 描述：     ADME 导槽校准
 */
public class BleAdmeGuideGrooveCalibrationFragment extends BaseBleIotCommunicateFragment {
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

    private String motionWay;//  运动方式
    private String movementSpeed;// 电机运动速度(r/min)
    private String motionPulse;//  运动脉冲数

    private AdmeGuideGrooveCalibrationInfo grooveCalibrationInfo;
    private BleAdmeMotorMotionAngleFragment motorMotionAngleFragment;

    private DecimalFormat decimalFormat = new DecimalFormat();


    public static BleAdmeGuideGrooveCalibrationFragment newInstance() {
        return new BleAdmeGuideGrooveCalibrationFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ble_adme_guide_groove_calibration_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setView();
        queryParamInfo();
    }

    private void setView() {
        mEtMovementSpeed.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        mEtMovementSpeed.setHint("1-100");

        mEtMotionPulse.setFilters(new InputFilter[]{new InputFilter.LengthFilter(11)});

        clearMotionDataLayout.setVisibility(View.VISIBLE);
        motionDataClearCompleteLayout.setVisibility(View.GONE);
    }

    /**
     * 获取运动状态参数
     */
    private void queryParamInfo() {
        errMsg = "查询数据超时,请稍后尝试";
        startProgressRunnable("加载中...", WRITE_TIME_OUT_SECOND);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_GUIDE_GROOVE_CALIBRATION_PARAMETERS);
        sendCommand(command);
    }

    /**
     * 清空电机运动数据记录指令
     */
    private void clearMotorMotionData() {
        errMsg = "发送指令超时,请稍后尝试";
        startProgressRunnable("处理中...", WRITE_TIME_OUT_SECOND);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_CLEAR_GUIDE_GROOVE_CALIBRATION_DATA);
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
            showClearWarnDialog("确认清除设备运动监测数据吗？");
        }
    }

    private boolean checkValueIsValid() {
        movementSpeed = mEtMovementSpeed.getText().toString().trim();
        motionPulse = mEtMotionPulse.getText().toString().trim();
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

        if (TextUtils.isEmpty(motionPulse)) {
            ToastUtils.show("请输入运动脉冲!");
            mEtMotionPulse.requestFocus();
            return false;
        }
        try {
            double value = Double.parseDouble(motionPulse);
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
            decimalFormat.applyPattern("#.#");
            motionPulse = decimalFormat.format(Double.parseDouble(motionPulse));
            entity.setMovepulse(motionPulse);

            mBtnRun.setEnabled(false);
            errMsg = "发送指令超时,请稍后尝试";
            startProgressRunnable("处理中...", WRITE_TIME_OUT_SECOND);
            String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_GUIDE_GROOVE_CALIBRATION_PARAMETERS, entity);
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
                        clearMotionDataLayout.setEnabled(false);
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
            case ADME_MD_GET_GUIDE_GROOVE_CALIBRATION_PARAMETERS: {//获取ADME的导槽校准配置参数
                stopProgressRunnable();
                IOTCommandResult<AdmeGuideGrooveCalibrationInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取设备的导槽校准配置参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                grooveCalibrationInfo = commandResult.getResult();
                initParamConfigInfo();
            }
            break;

            case ADME_MD_SET_GUIDE_GROOVE_CALIBRATION_PARAMETERS: {//设置ADME的导槽校准配置参数
                stopProgressRunnable();
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

            case ADME_MD_CLEAR_GUIDE_GROOVE_CALIBRATION_DATA: {//ADME导槽校准清空
                stopProgressRunnable();
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "清空数据出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    clearMotionDataLayout.setEnabled(true);
                    return;
                }
                clearMotionDataLayout.setVisibility(View.GONE);
                motionDataClearCompleteLayout.setVisibility(View.VISIBLE);
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
        motionPulse = grooveCalibrationInfo.getMovePulse().trim();
        if (motionWay.equals("0")) {
            motionWayPos = 0;
            mTvMotionWay.setText("正转");
        } else {
            motionWayPos = 1;
            mTvMotionWay.setText("反转");
        }

        try {
            mEtMovementSpeed.setText(movementSpeed);
            decimalFormat.applyPattern("#.#");
            motionPulse = decimalFormat.format(Double.parseDouble(motionPulse));
            mEtMotionPulse.setText(motionPulse);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //电机处于运动状态，弹出底部运行数据展示框
        if (grooveCalibrationInfo.getMorunstate().trim().equals('1')) {
            showMotorMotionDialog();
        }
    }

    private void doAfterSetting() {
        if (motorMotionAngleFragment != null && motorMotionAngleFragment.isVisible()) {
            return;
        }
        mBtnRun.setEnabled(true);
        if (grooveCalibrationInfo != null) {
            grooveCalibrationInfo.setMovementway(motionWay);
            grooveCalibrationInfo.setMotorspeed(movementSpeed);
            grooveCalibrationInfo.setMovePulse(motionPulse);
        }
        showMotorMotionDialog();
    }

    private void showMotorMotionDialog() {
        if (motorMotionAngleFragment != null && motorMotionAngleFragment.isVisible())
            return;

        if (grooveCalibrationInfo != null) {
            motorMotionAngleFragment = BleAdmeMotorMotionAngleFragment.newInstance(grooveCalibrationInfo);
            motorMotionAngleFragment.show(getChildFragmentManager(), "dialog");

            clearMotionDataLayout.setVisibility(View.VISIBLE);
            motionDataClearCompleteLayout.setVisibility(View.GONE);
        }
    }
}