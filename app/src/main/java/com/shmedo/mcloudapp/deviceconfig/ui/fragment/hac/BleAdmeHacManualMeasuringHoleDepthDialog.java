package com.shmedo.mcloudapp.deviceconfig.ui.fragment.hac;

import android.graphics.Color;
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
import com.shmedo.configlibrary.iot.enums.AdmeModuleErrorType;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.hac.HacMotorMotionDistanceInfo;
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
 * 创建时间:  2022/7/25 <br/>
 * 描述：     HAC 手动测量孔深模式时，实时脉冲数据展示底部弹窗
 */
public class BleAdmeHacManualMeasuringHoleDepthDialog extends BaseDialogFragment {
    private static final String MOTION_WAY = "motion_way";
    private static final String LAST_DISTANCE = "last_distance";
    private static final String MOTION_DISTANCE = "motion_distance";

    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.iv_close)
    ImageView mIvClose;

    @BindView(R.id.tv_motion_state)
    TextView mTvMotionState;

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

    private static final class DefaultHandler extends WeakHandler<BleAdmeHacManualMeasuringHoleDepthDialog> {
        private DefaultHandler(BleAdmeHacManualMeasuringHoleDepthDialog fragment) {
            super(fragment);
        }

        @Override
        protected void handleMessage(Message msg, BleAdmeHacManualMeasuringHoleDepthDialog fragment) {
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

    public static BleAdmeHacManualMeasuringHoleDepthDialog newInstance(String motionWay, String lastDistance, String totalDistanceGoal) {
        BleAdmeHacManualMeasuringHoleDepthDialog fragment = new BleAdmeHacManualMeasuringHoleDepthDialog();
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
        return R.layout.ble_adme_hac_manual_measuring_hole_depth_dialog;
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

        mTvMotionState.setText("正常");
        mTvMotionPulse.setText("0");
        mTvMotionDistance.setText("0");
    }

    /**
     * 查询ADME测孔深运动的脉冲数、运动距离
     */
    private void getMotorMotionData() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_HAC_MD_GET_HOLE_MEASURE_PULSE);
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
                    BleAdmeHacManualMeasuringHoleDepthDialog.this.dismiss();
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
    public void updateMotionData(HacMotorMotionDistanceInfo motorMotionDistanceInfo) {
        if (motorMotionDistanceInfo == null) {
            Timber.e("HacMotorMotionDistanceInfo is Null!");
            return;
        }
        if (!TextUtils.isEmpty(curPulse)) {
            if (motorMotionDistanceInfo.getPulsenumber().equals(curPulse)) {
                repeatNum++;
                Timber.d("updateMotionData: lastDistance=%s,curDistance=%s,curPulse=%s,repeatNum=%s", lastDistance, curDistance, curPulse, repeatNum);
                //轮询 N 次电机脉冲数据不变化时，停留当前状态页面
                if (repeatNum >= 6) {
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

        //CTR 工作异常
        if (!motorMotionDistanceInfo.getAbndiasis().equals("0")) {
            //列出异常原因
            StringBuilder stringBuilder = new StringBuilder();
            String[] codes = motorMotionDistanceInfo.getAbndiasis().split("\\|");
            for (String code : codes) {
                AdmeModuleErrorType errorType = AdmeModuleErrorType.valueByCode(code);
                if (errorType != null) {
                    stringBuilder.append(errorType.getDescription());
                    stringBuilder.append(";");
                }
            }
            if (stringBuilder.toString().endsWith(";")) {
                stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
            }
            mTvMotionState.setText(stringBuilder.toString());
            mTvMotionState.setTextColor(Color.RED);
        }else{
            mTvMotionState.setText("正常");
            mTvMotionState.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_54DA99));
        }

        //继续轮询电机脉冲数据
        startQueryMotorMotionDataProgress();
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

    /**
     * 处理电机继续运动
     */
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
                            stopAllProgress();
                            //蓝牙未断开时先发送停止电机指令，再关闭运行页面
                            stopMotorMotion();
                            BleAdmeHacManualMeasuringHoleDepthDialog.this.dismiss();
                        } else {
                            //直接关闭运行页面
                            BleAdmeHacManualMeasuringHoleDepthDialog.this.dismiss();
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }
}
