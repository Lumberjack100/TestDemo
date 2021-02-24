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
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeGuideGrooveCalibrationEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeMotorMotionAngleInfo;
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
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

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

    private USRBleViewModel usrBleViewModel;

    private String motionWay;//运动方式
    private String totalPulse;//总脉冲数
    private String curPulse;// 当前脉冲数
    private String motionPulse;//  运动脉冲数


    private static int repeatNum = 0;//当查询电机脉冲数重复超过一定次数时，判定电机停止
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

    public static BleAdmeMotorMotionAngleFragment newInstance(String motionWay, String totalPulse) {
        BleAdmeMotorMotionAngleFragment fragment = new BleAdmeMotorMotionAngleFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, motionWay);
        args.putString(ARG_PARAM2, totalPulse);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            motionWay = getArguments().getString(ARG_PARAM1);
            totalPulse = getArguments().getString(ARG_PARAM2);
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
        double pulseGoal = 0;
        try {
            double pulseTotal = Math.abs(Double.parseDouble(totalPulse));
            double pulseCurrent = Math.abs(Double.parseDouble(curPulse));
            pulseGoal = pulseTotal - pulseCurrent;
            //运动脉冲数无效
            if (pulseGoal <= 0) {
                updateStopState();
                ToastUtils.show("无法继续电机运动操作!");
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        motionPulse = String.valueOf(pulseGoal);
        AdmeGuideGrooveCalibrationEntity entity = new AdmeGuideGrooveCalibrationEntity();
        entity.setMovementway(motionWay);
        entity.setMovepulse(motionPulse);

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_GUIDE_GROOVE_CALIBRATION_PARAMETERS, entity);
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

    @OnClick({R.id.iv_close, R.id.btn_stop, R.id.btn_pause, R.id.btn_exit})
    public void onClick(View view) {
        int id = view.getId();
        if (isDoubleClick(view)) {
            return;
        }

        if (id == R.id.iv_close) {
            if (!usrBleViewModel.isConnected() || btnExit.getVisibility() == View.VISIBLE) {
                dismiss();
            } else {
                showExitWarnDialog("确认退出数据运行？");
            }
        } else if (id == R.id.btn_stop) {
            isStopClick = true;
            if (!usrBleViewModel.isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            stopQueryMotorMotionDataRunnable();
            stopMotorMotion();

        } else if (id == R.id.btn_pause) {
            if (!usrBleViewModel.isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            isStopClick = false;
            if (btnPause.getText().toString().equals("暂停")) {
//                stopQueryMotorMotionDataRunnable();
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
//            case ADME_MD_GET_GUIDE_GROOVE_CALIBRATION_PULSE: {//查询电机运动状态
//                IOTCommandResult<AdmeMotorMotionAngleInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
//                if (!commandResult.isSuccess()) {
//                    String errMsg = String.format("%s %s", "获取电机的实时运行状态出错!", commandResult.getMessage());
//                    Timber.e(errMsg);
//                    ToastUtils.show(errMsg);
//                    return;
//                }
//                AdmeMotorMotionAngleInfo  motorMotionAngleInfo = commandResult.getResult();
//                updateMotionData(motorMotionAngleInfo);
//            }
//            break;

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

//            case ADME_MD_SET_GUIDE_GROOVE_CALIBRATION_PARAMETERS: {//设置ADME的导槽校准配置参数
//                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
//                if (!cmdResult.isSucceed()) {
//                    String errMsg = String.format("%s %s", "设置导槽校准配置参数出错!", cmdResult.getReason());
//                    Timber.e(errMsg);
//                    ToastUtils.show(errMsg);
//                    return;
//                }
//                if (btnPause.getText().toString().equals("继续")) {
//                    btnPause.setText("暂停");
//                    btnPause.setBackgroundResource(R.drawable.bg_btn_pause_motor_motion);
//                }
//                queryMotorMotionDataRunnable = new QueryMotorMotionDataRunnable();
//                startQueryMotorMotionDataRunnable();
//            }
//            break;

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
        //电机已经停止，不用再轮询电机脉冲数据
        if (!TextUtils.isEmpty(curPulse) && motorMotionAngleInfo.getPulsenumber().equals(curPulse)) {
            repeatNum++;
            Timber.d("updateMotionData: totalPulse=%s,curPulse=%s,repeatNum=%s", totalPulse, curPulse, repeatNum);
            if (repeatNum >= 5) {
                try {
                    double pulseCurrent = Math.abs(Double.parseDouble(curPulse));
                    double pulseTotal = Double.parseDouble(totalPulse);
                    if (pulseCurrent >= pulseTotal) {
                        updateStopState();
                    } else {
                        //暂停状态处理
                        repeatNum = 0;
                        btnPause.setText("继续");
                        btnPause.setBackgroundResource(R.drawable.bg_btn_continue_motor_motion);
                        stopQueryMotorMotionDataRunnable();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    updateStopState();
                }
                return;
            }
        }

        if (!TextUtils.isEmpty(curPulse) && !motorMotionAngleInfo.getPulsenumber().equals(curPulse) && repeatNum != 0) {
            repeatNum = 0;
        }
        curPulse = motorMotionAngleInfo.getPulsenumber();
        mTvMotionPulse.setText(curPulse);
        mTvMotionAngle.setText(motorMotionAngleInfo.getRealmoveangle());

        startQueryMotorMotionDataRunnable();
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
        stopQueryMotorMotionDataRunnable();
    }

    public void processContinueMotorMotion() {
        Timber.d("start Motion: totalPulse=%s,curPulse=%s,motionPulse=%s", totalPulse, curPulse, motionPulse);

        if (btnPause.getText().toString().equals("继续")) {
            btnPause.setText("暂停");
            btnPause.setBackgroundResource(R.drawable.bg_btn_pause_motor_motion);
        }
        queryMotorMotionDataRunnable = new QueryMotorMotionDataRunnable();
        startQueryMotorMotionDataRunnable();
    }

    @Override
    public void onStop() {
        //TODO #gh# 屏幕熄灭时触发此回调，会造成未正确刷新电机运行状态,stopQueryMotorMotionDataRunnable()放在onDestroy()中调用
//        stopQueryMotorMotionDataRunnable();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        stopQueryMotorMotionDataRunnable();
        super.onDestroy();
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
                        if (usrBleViewModel.isConnected()) {
                            //蓝牙未断开时先发送停止电机指令，再关闭运行页面
                            stopMotorMotion();
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