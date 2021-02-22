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
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeMeasuringHoleDepthEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeMeasuringHoleDepthInfo;
import com.shmedo.configlibrary.iot.model.adme.AdmeMotorMotionDistanceInfo;
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
 * 描述：     ADME 电机测量孔深实时数据展示底部弹窗
 */
public class BleAdmeMotorMotionDistanceFragment extends BaseDialogFragment {
    private static final String MEASURING_HOLE_DEPTH_PARAM = "measuring_hole_depth_param";

    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.iv_close)
    ImageView mIvClose;

    @BindView(R.id.tv_clear_data)
    TextView mTvClearData;

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

    private USRBleViewModel usrBleViewModel;

    private AdmeMeasuringHoleDepthInfo measuringHoleDepthInfo;

    private AdmeMotorMotionDistanceInfo motorMotionDistanceInfo;

    private String pulseNumber;//脉冲数
    private String curDistance;

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

    public static BleAdmeMotorMotionDistanceFragment newInstance(AdmeMeasuringHoleDepthInfo measuringHoleDepthInfo) {
        BleAdmeMotorMotionDistanceFragment fragment = new BleAdmeMotorMotionDistanceFragment();
        Bundle args = new Bundle();
        args.putParcelable(MEASURING_HOLE_DEPTH_PARAM, measuringHoleDepthInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            measuringHoleDepthInfo = (AdmeMeasuringHoleDepthInfo) getArguments().getParcelable(MEASURING_HOLE_DEPTH_PARAM);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ble_adme_motor_motion_distance_fragment;
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
        mTvMotionDistance.setText("0");
    }

    /**
     * 查询ADME测孔深运动的脉冲数、运动距离指令
     */
    private void getMotorMotionData() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_MEASURING_HOLEDEPTH_PULSE);
        sendCommand(command);
    }

    /**
     * 停止或者暂停电机运动指令
     */
    private void stopMotorMotion() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_STOP_MEASURING_HOLEDEPTH);
        sendCommand(command);
    }

    /**
     * 继续电机运动指令
     */
    private void continueMotorMotion() {
        if (measuringHoleDepthInfo == null) {
            ToastUtils.show("无法继续操作!");
            return;
        }

        double motionDistance = 0;
        try {
            double totalDistance = Double.parseDouble(measuringHoleDepthInfo.getMovedistance());
            double distance = Double.parseDouble(curDistance);
            motionDistance = totalDistance - distance;
            //运动距离无效
            if (motionDistance <= 0) {
                updateStopState();
                ToastUtils.show("无法继续操作!");
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        AdmeMeasuringHoleDepthEntity entity = new AdmeMeasuringHoleDepthEntity();
        entity.setMovementway(measuringHoleDepthInfo.getMovementway());
        entity.setMovedistance(String.valueOf(motionDistance));

        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_MEASURING_HOLEDEPTH_PARAMETERS, entity);
        sendCommand(command);
    }

    /**
     * 清空电机运动数据记录指令
     */
    private void clearMotorMotionData() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_CLEAR_MEASURING_HOLEDEPTH_DATA);
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
        if (id == R.id.iv_close) {
            if (btnExit.getVisibility() == View.VISIBLE) {
                dismiss();
            } else {
                showExitWarnDialog("确认退出数据运行？");
            }
        } else if (id == R.id.tv_clear_data) {
            if (!usrBleViewModel.isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            clearMotorMotionData();

        } else if (id == R.id.btn_stop) {
            if (!usrBleViewModel.isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            isStopClick = true;
            stopQueryMotorMotionDataRunnable();
            stopMotorMotion();

        } else if (id == R.id.btn_pause) {
            if (!usrBleViewModel.isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
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
            case ADME_MD_GET_MEASURING_HOLEDEPTH_PULSE: {
                IOTCommandResult<AdmeMotorMotionDistanceInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取电机的实时运行状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                motorMotionDistanceInfo = commandResult.getResult();
                updateMotionData();
            }
            break;

            case ADME_MD_STOP_MEASURING_HOLEDEPTH: {
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "停止电机出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                //关闭页面
                if (isExit) {
                    BleAdmeMotorMotionDistanceFragment.this.dismiss();
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

            case ADME_MD_SET_MEASURING_HOLEDEPTH_PARAMETERS: {//设置ADME的测量孔深配置参数
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "设置测量孔深配置参数出错!", cmdResult.getReason());
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

            default:
                break;
        }
    }

    /**
     * 实时刷新脉冲和运动距离
     */
    private void updateMotionData() {
        if (motorMotionDistanceInfo == null) {
            Timber.e("AdmeMotorMotionDistanceInfo is Null!");
            return;
        }

        //电机已经停止，不用再轮询电机状态
        if (!TextUtils.isEmpty(pulseNumber) && motorMotionDistanceInfo.getPulsenumber().equals(pulseNumber)) {
            updateStopState();
            return;
        }
        pulseNumber = motorMotionDistanceInfo.getPulsenumber();
        curDistance = motorMotionDistanceInfo.getRealmovedistance();
        mTvMotionPulse.setText(pulseNumber);
        mTvMotionDistance.setText(curDistance);

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