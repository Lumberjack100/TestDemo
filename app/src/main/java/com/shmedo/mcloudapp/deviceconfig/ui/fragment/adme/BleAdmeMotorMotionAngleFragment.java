package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeGuideGrooveCalibrationEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeGuideGrooveCalibrationInfo;
import com.shmedo.configlibrary.iot.model.adme.AdmeMotorMotionDataInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.BaseDialogFragment;
import com.shmedo.mcloudapp.profile.USRBleViewModel;

import java.util.UUID;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/7/21 <br/>
 * 描述：     ADME 电机导槽校准实时数据展示底部弹窗
 */
public class BleAdmeMotorMotionAngleFragment extends BaseDialogFragment {

    private static final String GUIDE_GROOVE_CALIBRATION_PARAM = "guide_groove_calibration_param";

    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.iv_close)
    ImageView mIvClose;

    @BindView(R.id.tv_clear_data)
    TextView mTvClearData;

    @BindView(R.id.tv_motion_pulse)
    TextView mTvMotionPulse;

    @BindView(R.id.tv_motion_angle)
    TextView mTvMotionAngle;

    @BindView(R.id.btn_stop)
    TextView btnStop;

    @BindView(R.id.btn_pause)
    TextView btnPause;

    @BindView(R.id.btn_exit)
    TextView btnExit;

    private USRBleViewModel usrBleViewModel;

    private AdmeGuideGrooveCalibrationInfo grooveCalibrationInfo;
    private AdmeMotorMotionDataInfo motorMotionDataInfo;

    private String pulseNumber;//脉冲数
    private String curAngle;

    private boolean isStopClick = false;
    private boolean isExit = false;

    private Handler motionDataHander = new Handler();
    private QueryMotorMotionDataRunnable queryMotorMotionDataRunnable;

    /**
     * 查询电机运动数据
     */
    private class QueryMotorMotionDataRunnable implements Runnable {
        @Override
        public void run() {
            getMotorMotionData();
        }
    }

    private void startQueryMotorMotionDataRunnable() {
        if (queryMotorMotionDataRunnable != null && isResumed()) {
            motionDataHander.postDelayed(queryMotorMotionDataRunnable, 500);
        }
    }

    private void stopQueryMotorMotionDataRunnable() {
        motionDataHander.removeCallbacksAndMessages(null);
        queryMotorMotionDataRunnable = null;
    }

    public static BleAdmeMotorMotionAngleFragment newInstance(AdmeGuideGrooveCalibrationInfo grooveCalibrationInfo) {
        BleAdmeMotorMotionAngleFragment fragment = new BleAdmeMotorMotionAngleFragment();
        Bundle args = new Bundle();
        args.putParcelable(GUIDE_GROOVE_CALIBRATION_PARAM, grooveCalibrationInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            grooveCalibrationInfo = (AdmeGuideGrooveCalibrationInfo) getArguments().getParcelable(GUIDE_GROOVE_CALIBRATION_PARAM);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ble_adme_motor_motion_angle_fragment;
    }

    @Override
    protected void setWindowStyle(int gravity) {
        super.setWindowStyle(gravity);
        this.setCancelable(false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        usrBleViewModel = getApplicationScopeViewModel(USRBleViewModel.class);
        usrBleViewModel.getResponseMsg().observeInFragment(this, new Observer<String>() {
            @Override
            public void onChanged(String result) {
                if (!result.startsWith("$cmd=") || !isResumed())
                    return;

                try {
                    parseResponseMessage(result);
                } catch (Exception ex) {
                    Timber.e(ex);
                }
            }
        });
        queryMotorMotionDataRunnable = new QueryMotorMotionDataRunnable();
    }

    @Override
    public void onResume() {
        super.onResume();
        startQueryMotorMotionDataRunnable();
    }

    private void initView() {
        mIvClose.setVisibility(View.VISIBLE);
        mTvClearData.setVisibility(View.GONE);
        btnStop.setVisibility(View.VISIBLE);
        btnPause.setVisibility(View.VISIBLE);
        btnExit.setVisibility(View.GONE);

        mTvMotionPulse.setText("0");
        mTvMotionAngle.setText("0");
    }

    /**
     * 查询ADME导槽校准的脉冲数、运动角度
     */
    private void getMotorMotionData() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_GUIDE_GROOVE_CALIBRATION_PULSE);
        sendCommand(command);
    }

    /**
     * 停止或者暂停电机运动指令
     */
    private void stopMotorMotion() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_STOP_GUIDE_GROOVE_CALIBRATION);
        sendCommand(command);
    }

    /**
     * 继续电机运动指令
     */
    private void continueMotorMotion() {
        if (grooveCalibrationInfo == null) {
            ToastUtils.show("无法继续操作!");
            return;
        }

        double motionAngle = 0;
        try {
            double totalAngle = Double.parseDouble(grooveCalibrationInfo.getMoveangle());
            double angle = Double.parseDouble(curAngle);
            motionAngle = totalAngle - angle;
            //运动角度无效
            if (motionAngle <= 0) {
                updateStopState();
                ToastUtils.show("无法继续操作!");
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        AdmeGuideGrooveCalibrationEntity entity = new AdmeGuideGrooveCalibrationEntity();
        entity.setMovementway(grooveCalibrationInfo.getMovementway());
        entity.setMoveangle(String.valueOf(motionAngle));

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_GUIDE_GROOVE_CALIBRATION_PARAMETERS, entity);
        sendCommand(command);
    }

    /**
     * 清空电机运动数据记录指令
     */
    private void clearMotorMotionData() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_CLEAR_GUIDE_GROOVE_CALIBRATION_DATA);
        sendCommand(command);
    }

    private void sendCommand(String cmdStr) {
        String apiKey = "b12aac6b-0bd2-4a01-80fd-97fe4f5d4ff9";
        if (!TextUtils.isEmpty(usrBleViewModel.getDeviceApiKey().getValue())) {
            apiKey = usrBleViewModel.getDeviceApiKey().getValue();
        }
        cmdStr += "&apikey=" + apiKey
                + "&msgid=" + UUID.randomUUID().toString().substring(30);
        usrBleViewModel.sendIOTProtocolCommand(cmdStr);
    }

    @OnClick({R.id.iv_close, R.id.tv_clear_data, R.id.btn_stop, R.id.btn_pause, R.id.btn_exit})
    public void onClick(View view) {
        int id = view.getId();
        if (isDoubleClick(view)) {
            return;
        }
        if (!usrBleViewModel.isConnected()) {
            ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
            return;
        }

        if (id == R.id.iv_close) {
            if (btnExit.getVisibility() == View.VISIBLE) {
                dismiss();
            } else {
                showExitWarnDialog("确认退出数据运行？");
            }
        } else if (id == R.id.tv_clear_data) {
            clearMotorMotionData();

        } else if (id == R.id.btn_stop) {
            isStopClick = true;
            stopQueryMotorMotionDataRunnable();
            stopMotorMotion();

        } else if (id == R.id.btn_pause) {
            isStopClick = false;
            btnPause.setEnabled(false);
            if (btnPause.getText().toString().equals("暂停")) {
                stopQueryMotorMotionDataRunnable();
                stopMotorMotion();

            } else {
                continueMotorMotion();
            }
        } else if (id == R.id.btn_exit) {
            dismiss();
        }
    }

    /**
     * 解析设备的参数指令
     */
    private void parseResponseMessage(String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case ADME_MD_GET_GUIDE_GROOVE_CALIBRATION_PULSE: {
                IOTCommandResult<AdmeMotorMotionDataInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取电机的实时运行状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                motorMotionDataInfo = commandResult.getResult();
                updateMotionData();
            }
            break;

            case ADME_MD_STOP_GUIDE_GROOVE_CALIBRATION: {
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "停止电机出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                //关闭页面
                if (isExit) {
                    BleAdmeMotorMotionAngleFragment.this.dismiss();
                    return;
                }

                if (isStopClick) {
                    updateStopState();
                } else {
                    btnPause.setEnabled(true);
                    if (btnPause.getText().toString().equals("暂停")) {
                        btnPause.setText("继续");
                        btnPause.setBackgroundResource(R.drawable.bg_btn_continue_motor_motion);
                    }
                }
            }
            break;

            case ADME_MD_SET_GUIDE_GROOVE_CALIBRATION_PARAMETERS: {//设置ADME的导槽校准配置参数
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置导槽校准配置参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                btnPause.setEnabled(true);
                if (btnPause.getText().toString().equals("继续")) {
                    btnPause.setText("暂停");
                    btnPause.setBackgroundResource(R.drawable.bg_btn_pause_motor_motion);
                }
                queryMotorMotionDataRunnable = new QueryMotorMotionDataRunnable();
                startQueryMotorMotionDataRunnable();
            }
            break;
        }
    }

    /**
     * 实时刷新脉冲和运动角度
     */
    private void updateMotionData() {
        if (motorMotionDataInfo == null) {
            Timber.e("AdmeMotorMotionDataInfo is Null!");
            return;
        }

        //电机已经停止，不用再轮询电机状态
        if (!TextUtils.isEmpty(pulseNumber) && motorMotionDataInfo.getPulsenumber().equals(pulseNumber)) {
            updateStopState();
            return;
        }
        pulseNumber = motorMotionDataInfo.getPulsenumber();
        curAngle = motorMotionDataInfo.getRealmoveangle();
        mTvMotionPulse.setText(pulseNumber);
        mTvMotionAngle.setText(curAngle);

        startQueryMotorMotionDataRunnable();
    }

    /**
     * 更新电机停止运动状态页面
     */
    private void updateStopState() {
        mIvClose.setVisibility(View.VISIBLE);
        mTvClearData.setVisibility(View.GONE);
        btnStop.setVisibility(View.GONE);
        btnPause.setVisibility(View.GONE);
        btnExit.setVisibility(View.VISIBLE);
        stopQueryMotorMotionDataRunnable();
    }

    @Override
    public void onStop() {
        stopQueryMotorMotionDataRunnable();
        super.onStop();
    }

    /**
     * 运动时退出提醒
     *
     * @param content
     */
    protected void showExitWarnDialog(String content) {
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
                        isExit = true;
                        stopMotorMotion();
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }
}