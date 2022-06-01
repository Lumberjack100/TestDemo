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
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeMeasuringHoleDepthEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeMeasuringHoleDepthInfo;
import com.shmedo.configlibrary.iot.model.adme.AdmeMotorMotionDistanceInfo;
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
 * 描述：     ADME 电机手动测量孔深模式时，实时数据展示底部弹窗
 */
public class BleAdmeManualMeasuringHoleDepthBottomDialog extends BaseDialogFragment {
    private static final String MOTION_WAY = "motion_way";
    private static final String LAST_DISTANCE = "last_distance";
    private static final String MOTION_DISTANCE = "motion_distance";

    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.iv_close)
    ImageView mIvClose;

    @BindView(R.id.tv_motion_pulse)
    TextView mTvMotionPulse;

    @BindView(R.id.tv_motion_distance)
    TextView mTvMotionDistance;

    @BindView(R.id.btn_stop)
    TextView btnStop;

    @BindView(R.id.btn_pause)
    TextView btnPause;

    @BindView(R.id.btn_exit)
    TextView btnExit;

    private BleViewModel bleViewModel;

    private String motionWay;//运动方式
    private String lastDistance;//上次停止时运动距离
    private String curDistance;//当前距离
    private String totalDistanceGoal;//总运动距离目标数
    private String continueDistanceGoal;//继续运动距离目标数
    private String curPulse;//脉冲数

    private static int repeatNum = 0;//当查询电机脉冲数重复超过一定次数时，判定电机停止
    private boolean isStopClick = false;//是否是点击停止按钮操作
    private boolean isExit = false;

    private final DefaultHandler mDefaultHandler = new DefaultHandler(this);

    private static final class DefaultHandler extends WeakHandler<BleAdmeManualMeasuringHoleDepthBottomDialog> {
        private DefaultHandler(BleAdmeManualMeasuringHoleDepthBottomDialog fragment) {
            super(fragment);
        }

        @Override
        protected void handleMessage(Message msg, BleAdmeManualMeasuringHoleDepthBottomDialog fragment) {
            fragment.getMotorMotionData();
        }
    }

    protected void startQueryMotorMotionDataProgress() {
        if (!isResumed())
            return;

        mDefaultHandler.sendEmptyMessageDelayed(0, 800);
    }

    protected void stopAllProgress() {
        mDefaultHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onStop() {
        super.onStop();
        stopAllProgress();
    }

    public static BleAdmeManualMeasuringHoleDepthBottomDialog newInstance(String motionWay, String lastDistance, String totalDistanceGoal) {
        BleAdmeManualMeasuringHoleDepthBottomDialog fragment = new BleAdmeManualMeasuringHoleDepthBottomDialog();
        Bundle args = new Bundle();
        args.putString(MOTION_WAY, motionWay);
        args.putString(LAST_DISTANCE, lastDistance);
        args.putString(MOTION_DISTANCE, totalDistanceGoal);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            motionWay = getArguments().getString(MOTION_WAY);
            lastDistance = getArguments().getString(LAST_DISTANCE);
            totalDistanceGoal = getArguments().getString(MOTION_DISTANCE);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ble_adme_manual_measuring_hole_depth_bottom_dialog;
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
        mTvMotionDistance.setText("0");
    }

    /**
     * 查询ADME测孔深运动的脉冲数、运动距离
     */
    private void getMotorMotionData() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_MEASURING_HOLEDEPTH_PULSE);
        sendCommand(command);
    }

    /**
     * 获取电机运动配置参数
     */
    private void queryMotorMotionConfig() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_MEASURING_HOLEDEPTH);
        sendCommand(command);
    }

    /**
     * 停止或者暂停电机运动
     */
    private void stopMotorMotion() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_STOP_MEASURING_HOLEDEPTH);
        sendCommand(command);
    }

    /**
     * 继续电机运动
     */
    private void continueMotorMotion() {
        double distanceGoal;
        try {
            double distanceTotalGoal = Math.abs(Double.parseDouble(totalDistanceGoal));
            double distanceDiff = Math.abs(Double.parseDouble(curDistance) - Double.parseDouble(lastDistance));
            distanceGoal = distanceTotalGoal - distanceDiff;
            //已达到设定运动目标
            if (distanceGoal <= 0) {
                updateStopState();
                ToastUtils.show("无法继续电机运动操作!");
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }
        continueDistanceGoal = String.valueOf(distanceGoal);
        AdmeMeasuringHoleDepthEntity entity = new AdmeMeasuringHoleDepthEntity();
        entity.setMovementway(motionWay);
        entity.setMovedistance(continueDistanceGoal);

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_MEASURING_HOLEDEPTH, entity);
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
            if (!bleViewModel.isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            stopAllProgress();
            isStopClick = true;
            stopMotorMotion();

        } else if (id == R.id.btn_pause) {
            if (!bleViewModel.isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            isStopClick = false;
            if (btnPause.getText().toString().equals("暂停")) {
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
            case ADME_MD_STOP_MEASURING_HOLEDEPTH: {//停止电机运动
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "停止电机出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                //关闭页面
                if (isExit) {
                    BleAdmeManualMeasuringHoleDepthBottomDialog.this.dismiss();
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
     * 实时刷新脉冲和运动距离
     */
    public void updateMotionData(AdmeMotorMotionDistanceInfo motorMotionDistanceInfo) {
        if (motorMotionDistanceInfo == null) {
            Timber.e("AdmeMotorMotionDistanceInfo is Null!");
            return;
        }
        if (!TextUtils.isEmpty(curPulse)) {
            if (motorMotionDistanceInfo.getPulsenumber().equals(curPulse)) {
                repeatNum++;
                Timber.d("updateMotionData: lastDistance=%s,curDistance=%s,curPulse=%s,repeatNum=%s", lastDistance, curDistance, curPulse, repeatNum);
                //轮询十次电机脉冲数据不变化时，查询电机运动状态，判断电机是否停止运动
                if (repeatNum >= 6) {
                    queryMotorMotionConfig();
                    return;
                }
            } else {
                repeatNum = 0;
            }
        }
        curPulse = motorMotionDistanceInfo.getPulsenumber();
        curDistance = motorMotionDistanceInfo.getRealmovedistance();
        mTvMotionPulse.setText(curPulse);
        mTvMotionDistance.setText(curDistance);
        //继续轮询电机脉冲数据
        startQueryMotorMotionDataProgress();
    }

    /**
     * 处理电机运动状态变化<br>
     * 在轮询十次电机脉冲数据不变化后，根据查询的电机运动状态更新底部弹框按钮状态
     */
    public void processMotorMotionState(AdmeMeasuringHoleDepthInfo measuringHoleDepthInfo) {
        if (measuringHoleDepthInfo == null) {
            return;
        }
        //轮询十次电机脉冲数据不变化，但是电机状态为"1",表示还在运动，则清空计数，继续轮询电机脉冲数据
        if (measuringHoleDepthInfo.getMorunstate().trim().equals("1")) {
            repeatNum = 0;
            startQueryMotorMotionDataProgress();
        } else {//电机状态表示停止运动
            try {
                double distanceTotalGoal = Math.abs(Double.parseDouble(totalDistanceGoal));
                double distanceDiff = Math.abs(Double.parseDouble(curDistance) - Double.parseDouble(lastDistance));
                //设定的运动距离目标值小于等于运动距离变化量时,电机停止
                if (distanceTotalGoal - distanceDiff <= 0) {
                    updateStopState();
                } else {
                    //暂停状态处理
                    stopAllProgress();
                    repeatNum = 0;
                    btnPause.setText("继续");
                    btnPause.setBackgroundResource(R.drawable.bg_btn_continue_motor_motion);
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
        stopAllProgress();
        repeatNum = 0;
        mIvClose.setVisibility(View.VISIBLE);
        btnStop.setVisibility(View.GONE);
        btnPause.setVisibility(View.GONE);
        btnExit.setVisibility(View.VISIBLE);
    }

    public void processContinueMotorMotion() {
        Timber.d("Continue Motion: lastDistance=%s,curDistance=%s,continueDistanceGoal=%s,curPulse=%s", lastDistance, curDistance, continueDistanceGoal, curPulse);
        if (btnPause.getText().toString().equals("继续")) {
            btnPause.setText("暂停");
            btnPause.setBackgroundResource(R.drawable.bg_btn_pause_motor_motion);
        }
        startQueryMotorMotionDataProgress();
    }

    private void sendCommand(String cmdStr) {
        String apiKey = "b12aac6b-0bd2-4a01-80fd-97fe4f5d4ff9";
        if (bleViewModel.deviceApiKeyRequest.getDeviceApiKeyLiveData().getValue() != null && !TextUtils.isEmpty(bleViewModel.deviceApiKeyRequest.getDeviceApiKeyLiveData().getValue().getApikey())) {
            apiKey = bleViewModel.deviceApiKeyRequest.getDeviceApiKeyLiveData().getValue().getApikey();
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
                            BleAdmeManualMeasuringHoleDepthBottomDialog.this.dismiss();
                        } else {
                            //直接关闭运行页面
                            BleAdmeManualMeasuringHoleDepthBottomDialog.this.dismiss();
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }
}