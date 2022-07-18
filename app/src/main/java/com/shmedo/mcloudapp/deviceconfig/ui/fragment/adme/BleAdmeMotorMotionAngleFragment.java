package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;
import android.os.Message;
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
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeGuideGrooveCalibrationEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeGuideGrooveCalibrationInfo;
import com.shmedo.configlibrary.iot.model.adme.AdmeMotorMotionAngleInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.callback.WeakHandler;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.BaseDialogFragment;
import com.shmedo.mcloudapp.profile.BleViewModel;

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
    private static final String MOTION_WAY = "motion_way";
    private static final String LAST_PULSE = "last_pulse";
    private static final String MOTION_PULSE = "motion_pulse";

    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.iv_close)
    ImageView mIvClose;

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

    private BleViewModel bleViewModel;

    private String motionWay;//运动方式
    private String lastPulse;//上次停止时脉冲数
    private String curPulse;// 当前脉冲数
    private String totalPulseGoal;//总运动脉冲目标数
    private String continuePulseGoal;//继续运动脉冲目标数

    private static int repeatNum = 0;//当查询电机脉冲数重复超过一定次数时，判定电机停止
    private boolean isStopClick = false;//是否是点击停止按钮操作
    private boolean isExit = false;

    private final DefaultHandler mDefaultHandler = new DefaultHandler(this);

    private static final class DefaultHandler extends WeakHandler<BleAdmeMotorMotionAngleFragment> {
        private DefaultHandler(BleAdmeMotorMotionAngleFragment fragment) {
            super(fragment);
        }

        @Override
        protected void handleMessage(Message msg, BleAdmeMotorMotionAngleFragment fragment) {
            fragment.getMotorMotionData();
        }
    }

    protected void startQueryMotorMotionDataProgress() {
        if (!isResumed())
            return;

        mDefaultHandler.sendEmptyMessageDelayed(0, 500);
    }

    protected void stopAllProgress() {
        mDefaultHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onStop() {
        super.onStop();
        stopAllProgress();
    }

    public static BleAdmeMotorMotionAngleFragment newInstance(String motionWay, String lastPulse, String totalPulseGoal) {
        BleAdmeMotorMotionAngleFragment fragment = new BleAdmeMotorMotionAngleFragment();
        Bundle args = new Bundle();
        args.putString(MOTION_WAY, motionWay);
        args.putString(LAST_PULSE, lastPulse);
        args.putString(MOTION_PULSE, totalPulseGoal);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            motionWay = getArguments().getString(MOTION_WAY);
            lastPulse = getArguments().getString(LAST_PULSE);
            totalPulseGoal = getArguments().getString(MOTION_PULSE);
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        bleViewModel = getApplicationScopeViewModel(BleViewModel.class);
        bleViewModel.getResponseMsg().observe(getViewLifecycleOwner(), new Observer<String>() {
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
    }

    @Override
    public void onResume() {
        super.onResume();
        startQueryMotorMotionDataProgress();
    }

    private void initView() {
        mIvClose.setVisibility(View.VISIBLE);
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
     * 获取电机运动配置参数
     */
    private void queryMotorMotionConfig() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_GUIDE_GROOVE_CALIBRATION);
        sendCommand(command);
    }

    /**
     * 停止或者暂停电机运动
     */
    private void stopMotorMotion() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_STOP_GUIDE_GROOVE_CALIBRATION);
        sendCommand(command);
    }

    /**
     * 继续电机运动
     */
    private void continueMotorMotion() {
        int pulseGoal;
        try {
            int pulseTotalGoal = Integer.parseInt(totalPulseGoal);
            int pulseDiff = Math.abs(Integer.parseInt(curPulse) - Integer.parseInt(lastPulse));
            pulseGoal = pulseTotalGoal - pulseDiff;
            //已达到设定运动目标
            if (pulseGoal <= 0) {
                updateStopState();
                ToastUtils.show("无法继续电机运动操作!");
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        continuePulseGoal = String.valueOf(pulseGoal);
        AdmeGuideGrooveCalibrationEntity entity = new AdmeGuideGrooveCalibrationEntity();
        entity.setMovementway(motionWay);
        entity.setMovepulse(continuePulseGoal);

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_GUIDE_GROOVE_CALIBRATION, entity);
        sendCommand(command);
    }

    @OnClick({R.id.iv_close, R.id.btn_stop, R.id.btn_pause, R.id.btn_exit})
    public void onClick(View view) {
        int id = view.getId();
        if (isDoubleClick(view)) {
            return;
        }

        if (id == R.id.iv_close) {
            if (!bleViewModel.isConnected() || btnExit.getVisibility() == View.VISIBLE) {
                dismiss();
            } else {
                showExitWarnDialog("确认退出数据运行？");
            }
        } else if (id == R.id.btn_stop) {
            isStopClick = true;
            if (!bleViewModel.isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            stopAllProgress();
            stopMotorMotion();

        } else if (id == R.id.btn_pause) {
            if (!bleViewModel.isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            isStopClick = false;
            if (btnPause.getText().toString().equals("暂停")) {
//                stopAllProgress();
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
            case ADME_MD_STOP_GUIDE_GROOVE_CALIBRATION: {//停止电机运动
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

                if (isStopClick) {//停止按钮操作
                    updateStopState();
                } else {//暂停按钮操作
                    if (btnPause.getText().toString().equals("暂停")) {
                        btnPause.setText("继续");
                        btnPause.setBackgroundResource(R.drawable.bg_btn_continue_motor_motion);
                    }
                }
            }
            break;

            default:
                break;
        }
    }

    /**
     * 实时刷新脉冲和运动角度
     */
    public void updateMotionData(AdmeMotorMotionAngleInfo motorMotionAngleInfo) {
        if (motorMotionAngleInfo == null) {
            Timber.e("AdmeMotorMotionAngleInfo is Null!");
            return;
        }
        if (!TextUtils.isEmpty(curPulse) && motorMotionAngleInfo.getPulsenumber().equals(curPulse)) {
            repeatNum++;
            Timber.d("updateMotionData: lastPulse=%s,curPulse=%s,repeatNum=%s", lastPulse, curPulse, repeatNum);
            //轮询五次电机脉冲数据不变化时，查询电机运动状态，判断电机是否停止运动
            if (repeatNum >= 5) {
//                stopAllProgress();
                queryMotorMotionConfig();
                return;
            }
        }
        if (!TextUtils.isEmpty(curPulse) && !motorMotionAngleInfo.getPulsenumber().equals(curPulse) && repeatNum != 0) {
            repeatNum = 0;
        }
        curPulse = motorMotionAngleInfo.getPulsenumber();
        mTvMotionPulse.setText(curPulse);
        mTvMotionAngle.setText(motorMotionAngleInfo.getRealmoveangle());
        //继续轮询电机脉冲数据
        startQueryMotorMotionDataProgress();
    }

    /**
     * 处理电机运动状态变化
     */
    public void processMotorMotionState(AdmeGuideGrooveCalibrationInfo grooveCalibrationInfo) {
        if (grooveCalibrationInfo == null) {
            return;
        }
        //轮询五次电机脉冲数据不变化，但是电机状态表示还在运动，清空计数，继续轮询电机脉冲数据
        if (grooveCalibrationInfo.getMorunstate().trim().equals("1")) {
            repeatNum = 0;
            startQueryMotorMotionDataProgress();
        } else {//电机状态表示停止运动
            try {
                int pulseTotalGoal = Integer.parseInt(totalPulseGoal);
                int pulseDiff = Math.abs(Integer.parseInt(curPulse) - Integer.parseInt(lastPulse));
                if (pulseTotalGoal - pulseDiff <= 0) {
                    updateStopState();
                } else {
                    //暂停状态处理
                    repeatNum = 0;
                    btnPause.setText("继续");
                    btnPause.setBackgroundResource(R.drawable.bg_btn_continue_motor_motion);
                    stopAllProgress();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                updateStopState();
            }
        }
    }

    /**
     * 更新电机停止运动状态页面
     */
    private void updateStopState() {
        repeatNum = 0;
        mIvClose.setVisibility(View.VISIBLE);
        btnStop.setVisibility(View.GONE);
        btnPause.setVisibility(View.GONE);
        btnExit.setVisibility(View.VISIBLE);
        stopAllProgress();
    }

    public void processContinueMotorMotion() {
        Timber.d("start Motion: lastPulse=%s,curPulse=%s,continuePulseGoal=%s", lastPulse, curPulse, continuePulseGoal);
        if (btnPause.getText().toString().equals("继续")) {
            btnPause.setText("暂停");
            btnPause.setBackgroundResource(R.drawable.bg_btn_pause_motor_motion);
        }
        startQueryMotorMotionDataProgress();
    }

    private void sendCommand(String cmdStr) {
        String apiKey = "b12aac6b-0bd2-4a01-80fd-97fe4f5d4ff9";
        if (bleViewModel.deviceRequest.getDeviceApiKeyLiveData().getValue() != null && !TextUtils.isEmpty(bleViewModel.deviceRequest.getDeviceApiKeyLiveData().getValue().getApikey())) {
            apiKey = bleViewModel.deviceRequest.getDeviceApiKeyLiveData().getValue().getApikey();
        }
        cmdStr += "&apikey=" + apiKey
                + "&msgid=" + UUID.randomUUID().toString().substring(30);
        bleViewModel.sendIOTProtocolCommand(cmdStr);
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
                        if (bleViewModel.isConnected()) {
                            //蓝牙未断开时先发送停止电机指令，再关闭运行页面
                            stopMotorMotion();
                            BleAdmeMotorMotionAngleFragment.this.dismiss();
                        } else {
                            //直接关闭运行页面
                            BleAdmeMotorMotionAngleFragment.this.dismiss();
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }
}