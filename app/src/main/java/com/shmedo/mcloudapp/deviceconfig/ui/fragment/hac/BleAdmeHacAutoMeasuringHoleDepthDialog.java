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
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Observer;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.StringUtils;
import com.github.razir.progressbutton.DrawableButtonExtensionsKt;
import com.github.razir.progressbutton.ProgressButtonHolderKt;
import com.github.razir.progressbutton.ProgressParams;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
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
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/7/25 <br/>
 * 描述：     电机自动测量孔深模式时，实时脉冲数据展示底部弹窗
 */
public class BleAdmeHacAutoMeasuringHoleDepthDialog extends BaseDialogFragment {
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

    @BindView(R.id.btn_exit)
    TextView btnExit;

    private BleViewModel bleViewModel;

    private String curDistance;//当前距离
    private String curPulse;//脉冲数

    private int repeatNum = 0;//当查询电机脉冲数重复超过一定次数时，判定电机停止
    private boolean isStopQueryMotorState = false;

    private final BleAdmeHacAutoMeasuringHoleDepthDialog.DefaultHandler mDefaultHandler = new BleAdmeHacAutoMeasuringHoleDepthDialog.DefaultHandler(this);

    private static final class DefaultHandler extends WeakHandler<BleAdmeHacAutoMeasuringHoleDepthDialog> {
        private DefaultHandler(BleAdmeHacAutoMeasuringHoleDepthDialog fragment) {
            super(fragment);
        }

        @Override
        protected void handleMessage(Message msg, BleAdmeHacAutoMeasuringHoleDepthDialog fragment) {
            Lifecycle.State currentState = fragment.getLifecycle().getCurrentState();
            if (!currentState.isAtLeast(Lifecycle.State.STARTED)) {
                return;
            }
            fragment.getMotorMotionData();
        }
    }

    protected void startQueryMotorStateProgress(long delayMillis) {
        if (isStopQueryMotorState)
            return;

        mDefaultHandler.sendEmptyMessageDelayed(0, delayMillis);
    }

    protected void stopQueryMotorStateProgress() {
        isStopQueryMotorState = true;
        mDefaultHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopQueryMotorStateProgress();
    }

//    @Override
//    public void onStop() {
//        super.onStop();
//        stopAllProgress();
//    }

    public static BleAdmeHacAutoMeasuringHoleDepthDialog newInstance() {
        BleAdmeHacAutoMeasuringHoleDepthDialog fragment = new BleAdmeHacAutoMeasuringHoleDepthDialog();
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.ble_adme_hac_auto_measuring_hole_depth_dialog;
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
        isStopQueryMotorState = false;
        startQueryMotorStateProgress(0);
    }

    private void initView() {
        mIvClose.setVisibility(View.VISIBLE);
        btnStop.setVisibility(View.VISIBLE);
        btnExit.setVisibility(View.GONE);

        mTvMotionState.setText("正常");
        mTvMotionPulse.setText("0");
        mTvMotionDistance.setText("0");

        ProgressButtonHolderKt.bindProgressButton(this, btnStop);
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

    @OnClick({R.id.iv_close, R.id.btn_stop, R.id.btn_exit})
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.iv_close) {
            if (!bleViewModel.isConnected() || btnExit.getVisibility() == View.VISIBLE) {
                dismiss();
            } else {
                showExitWarnDialog("确认退出数据运行？");
            }
        } else if (id == R.id.btn_stop) {
            if (!bleViewModel.isConnected()) {
                ToastUtils.show(StringUtils.getString(R.string.ble_config_disconnect_warn));
                return;
            }
            stopQueryMotorStateProgress();
            stopMotorMotion();
            DrawableButtonExtensionsKt.showProgress(btnStop, new Function1<ProgressParams, Unit>() {
                @Override
                public Unit invoke(ProgressParams progressParams) {
                    progressParams.setButtonTextRes(R.string.processing);
                    progressParams.setProgressColor(ColorUtils.getColor(R.color.colorPrimary));
                    return Unit.INSTANCE;
                }
            });
            btnStop.setEnabled(false);

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
                btnStop.setEnabled(true);
                DrawableButtonExtensionsKt.hideProgress(btnStop, "停止");
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "停止电机出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                //电机停止,更新运动状态页面
                updateStopState();
            }
            break;

            default:
                break;
        }
    }

    /**
     * 实时刷新脉冲和运动距离
     */
    public void updateMotionData(HacMotorMotionDistanceInfo motorMotionDistanceInfo, boolean isMeasureOver) {
        if (motorMotionDistanceInfo == null) {
            Timber.e("HacMotorMotionDistanceInfo is Null!");
            return;
        }
        //异常码 99 表示上拉到管口，测量结束，停止轮询脉冲数并发送停止电机运动指令
        if (motorMotionDistanceInfo.getAbndiasis().contains("99")) {
            stopQueryMotorStateProgress();
            stopMotorMotion();
            return;
        }
        curPulse = motorMotionDistanceInfo.getPulsenumber();
        curDistance = motorMotionDistanceInfo.getRealmovedistance();
        mTvMotionPulse.setText(curPulse);
        mTvMotionDistance.setText(curDistance);

        //CTR 工作异常
        if (!motorMotionDistanceInfo.getAbndiasis().equals("0") && !motorMotionDistanceInfo.getAbndiasis().contains("99")) {
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
        } else {
            mTvMotionState.setText("正常");
            mTvMotionState.setTextColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_54DA99));
        }
        //继续轮询电机脉冲数据
        startQueryMotorStateProgress(1000);
    }

    /**
     * 更新电机停止运动状态页面
     */
    private void updateStopState() {
        stopQueryMotorStateProgress();
        repeatNum = 0;
        mIvClose.setVisibility(View.VISIBLE);
        btnStop.setVisibility(View.GONE);
        btnExit.setVisibility(View.VISIBLE);
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
                        if (bleViewModel.isConnected()) {
                            stopQueryMotorStateProgress();
                            //蓝牙未断开时先发送停止电机指令，再关闭运行页面
                            stopMotorMotion();
                        }
                        BleAdmeHacAutoMeasuringHoleDepthDialog.this.dismiss();
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }
}
