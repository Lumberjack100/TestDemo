package com.shmedo.mcloudapp.deviceconfig.ui.fragment.hac;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.kongzue.dialogx.dialogs.CustomDialog;
import com.kongzue.dialogx.interfaces.OnBindView;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.hac.HacMeasuringDataInfoEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.AdmeCTRMotionState;
import com.shmedo.configlibrary.iot.enums.AdmeModuleErrorType;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.hac.HacMotionState;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.callback.WeakHandler;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.hac.AdmeHacMeasuringDataResultsActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon.BaseUSRBleIotCommunicateFragment;
import com.shmedo.mcloudapp.deviceconfig.view.hac.HacMeasuringDataHorizontalProgressBarView;
import com.shmedo.mcloudapp.deviceconfig.view.hac.HacMeasuringDataVerticalProgressBarView;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.MessageFormat;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2022/7/27 <br/>
 * 描述：     测量数据动态过程展示页面
 */
public class BleAdmeHacMeasuringDataProcedureFragment extends BaseUSRBleIotCommunicateFragment {
    protected static final String HOLE_DEPTH = "hole_depth";

    @BindView(R.id.tv_measure_mode)
    TextView mTvMeasureMode; //测量模式

    @BindView(R.id.verticalProgressBarView)
    HacMeasuringDataVerticalProgressBarView verticalProgressBarView;

    @BindView(R.id.horizontalProgressBarView)
    HacMeasuringDataHorizontalProgressBarView horizontalProgressBarView;

    @BindView(R.id.tv_kind_tips)
    TextView mTvKindTips; // 温馨提示

    @BindView(R.id.tv_inclinometer_battery)
    TextView mTvInclinometerBattery; // 测斜仪电量

    @BindView(R.id.tv_device_battery)
    TextView mTvDeviceBattery; //设备电量

    @BindView(R.id.tv_waiting_time)
    TextView mTvWaitingTime; // 预计等待时间

    @BindView(R.id.tv_waiting_time_title)
    TextView mTvWaitingTimeTitle; //测孔深模式

    @BindView(R.id.tv_motorinfo)
    TextView mTvMotorInfo; // 设备运行状态信息

    @BindView(R.id.btn_action)
    TextView btnAction; // 动作按钮

    @BindView(R.id.ll_waiting_time)
    View waitingTimeLayout; //


    private CustomDialog customDialog;

    private HacMotionState motionState;
    private String holeDepth;

    private IOTCommandType curCommandType = IOTCommandType.UNKNOWN_TYPE;

    private final QueryMotorStateHandler queryMotorStateHandler = new QueryMotorStateHandler(this);
    private boolean isStopQuery = false;
    private boolean isFirstShowError = true;//是否第一次弹出异常信息框，当前页面生命周期内只谈出一次

    private static final class QueryMotorStateHandler extends WeakHandler<BleAdmeHacMeasuringDataProcedureFragment> {
        private QueryMotorStateHandler(BleAdmeHacMeasuringDataProcedureFragment fragment) {
            super(fragment);
        }

        @Override
        protected void handleMessage(Message msg, BleAdmeHacMeasuringDataProcedureFragment fragment) {
            if (fragment.isConnected()) {
                fragment.queryMotorState();
                //实现查询电机状态指令响应超时，重新发送查询
//                fragment.startDefaultProgress(null, AppContants.MsgWhat.MSG_POLLING, DELAY_10000_MILLIS);
            }
        }
    }

    protected void startQueryMotorStateProgress(long delayMillis) {
        if (isStopQuery)
            return;

        queryMotorStateHandler.sendEmptyMessageDelayed(-1, delayMillis);
    }

    protected void stopQueryMotorStateProgress() {
        Timber.d("call stopQueryMotorStateProgress");
        isStopQuery = true;
        queryMotorStateHandler.removeCallbacksAndMessages(null);
    }

    /**
     * 查询电机状态指令响应超时回调
     *
     * @param msg
     */
    @Override
    protected void customHandleMessage(@NonNull @NotNull Message msg) {
        switch (msg.what) {
            case AppContants.MsgWhat.MSG_POLLING:
                Timber.d("queryMotorState timeout");
                //查询电机状态指令响应超时，重新发送查询
                queryMotorState();
                break;
        }
    }

    public static BleAdmeHacMeasuringDataProcedureFragment newInstance(String holeDepth) {
        BleAdmeHacMeasuringDataProcedureFragment fragment = new BleAdmeHacMeasuringDataProcedureFragment();
        Bundle args = new Bundle();
        args.putString(HOLE_DEPTH, holeDepth);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey(HOLE_DEPTH)) {
            holeDepth = getArguments().getString(HOLE_DEPTH);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_ble_adme_hac_measuring_data_procedure;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        isStopQuery = false;
        startQueryMotorStateProgress(0);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopQueryMotorStateProgress();
    }

    /**
     * 获取电机的运行状态
     */
    private void queryMotorState() {
        curCommandType = IOTCommandType.ADME_HAC_MD_GET_MOTION_STATE;
        String command = IOTCommandManager.getInstance().getCommand(curCommandType);
        sendCommand(command);
    }

    /**
     * 数据测量配置参数
     */
    private String setMeasuringDataParamCommand() {
        try {
            HacMeasuringDataInfoEntity entity = new HacMeasuringDataInfoEntity();
            entity.setEquipmodel("0");

            curCommandType = IOTCommandType.ADME_HAC_MD_SET_DATA_MEASURE_PARAM;
            String command = IOTCommandManager.getInstance().getCommand(curCommandType, entity);
            return command;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * 停止测量操作下发指令集
     */
    private void initStopActionCommands() {
        commandItems.clear();

        //停止电机运动指令
        String command = setMeasuringDataParamCommand();
        if (!TextUtils.isEmpty(command))
            commandItems.add(command);

        if (commandItems.size() > 0) {
            startDefaultProgress("处理中...", AppContants.MsgWhat.MSG_DEFAULT, DELAY_15000_MILLIS);
            sendCommandFromCmdList();
        }
    }

    @OnClick({R.id.btn_action})
    public void onClick(View view) {
        int id = view.getId();
        if (isDoubleClick(view)) {
            return;
        }
        if (id == R.id.btn_action) {
            if (!bleViewModel.isConnected()) {
                ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
                return;
            }
            if (btnAction.getText().toString().equals("停止")) {
                showStopWarnDialog();

            } else if (btnAction.getText().toString().equals("下一步")) {
                setResult();
                if (motionState.getMotorinfo().equals("8")) {//等待下次测量,进入测量结果展示页面
                    AdmeHacMeasuringDataResultsActivity.startActivity(mActivity, AppContants.CommunicationWay.BLE_CONNECT);
                    mActivity.finish();
                } else if (motionState.getMotorinfo().equals("9")) {//等待反测,回到测量参数配置页面
                    mActivity.finish();
                }
            }
        }
    }

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case ADME_HAC_MD_GET_MOTION_STATE: {//查询电机当前运动状态
                stopDefaultProgress(AppContants.MsgWhat.MSG_POLLING);
                IOTCommandResult<HacMotionState> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取电机当前运动状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(commandResult.getMessage().contains("unsupported") ? "设备版本不支持!" : errMsg);
                    return;
                }
                motionState = commandResult.getResult();
                refreshMotionState();
                startQueryMotorStateProgress(5000);
            }
            break;

            case ADME_HAC_MD_SET_DATA_MEASURE_PARAM: {//设置HAC数据测量参数
                stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "停止电机出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                mTvKindTips.setText(MessageFormat.format("本轮测量已停止,预计 {0} 分钟后可重新测量", getMinTime()));
                btnAction.setVisibility(View.INVISIBLE);
            }
            break;

            case LENGTH_INVALID://接收的数据格式不符合物联网指令协议，进入此逻辑处理
                if (curCommandType == IOTCommandType.ADME_HAC_MD_GET_MOTION_STATE) {
                    startQueryMotorStateProgress(5000);
                }
                break;
        }
    }

    /**
     * 实时刷新电机运动状态
     */
    private void refreshMotionState() {
        if (motionState == null) {
            return;
        }
        mTvMeasureMode.setText(motionState.getMeasmode().equals("0") ? "正向测量" : "反向测量");

        //异常时，停止轮询电机运动状态，展示异常原因
        if (!motionState.getAbndiasis().equals("0") && isFirstShowError) {//表示异常
            isFirstShowError = false;
            showErrorProtectionTip(motionState.getAbndiasis());
        }
        try {
            String batteryStr = motionState.getIncvoltage();
            SpannableStringBuilder builder = new SpannableStringBuilder(batteryStr + "%");
            double battery = Double.parseDouble(batteryStr);
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(battery <= 20 ? com.blankj.utilcode.util.ColorUtils.getColor(R.color.red) : com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_3AD094));
            builder.setSpan(colorSpan, 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mTvInclinometerBattery.setText(builder);

            batteryStr = motionState.getDriveinputv();
            builder = new SpannableStringBuilder(batteryStr + "%");
            battery = Double.parseDouble(batteryStr);
            colorSpan = new ForegroundColorSpan(battery <= 20 ? com.blankj.utilcode.util.ColorUtils.getColor(R.color.red) : com.blankj.utilcode.util.ColorUtils.getColor(R.color.text_color_3AD094));
            builder.setSpan(colorSpan, 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            mTvDeviceBattery.setText(builder);

            verticalProgressBarView.setVisibility(View.VISIBLE);
            horizontalProgressBarView.setVisibility(View.GONE);
            waitingTimeLayout.setVisibility(View.INVISIBLE);
            if (motionState.getMotorinfo().equals("0")) {//上拉至管口
                verticalProgressBarView.init(motionState.getMeaspoint());

            } else if (motionState.getMotorinfo().equals("1")) {//测斜仪配对,设置参数
                verticalProgressBarView.init(motionState.getMeaspoint());
                mTvKindTips.setText("测斜仪配对中，请耐心等待...");

            } else if (motionState.getMotorinfo().equals("2") || motionState.getMotorinfo().equals("3")) {//测斜仪下放、管底等待
                verticalProgressBarView.init(motionState.getMeaspoint());
                waitingTimeLayout.setVisibility(View.VISIBLE);
                mTvWaitingTimeTitle.setText(motionState.getMotorinfo().equals("2") ? "下放结束预计" : "距离开始测量预计");
                mTvWaitingTime.setText(String.format("%s分钟", getMinTime()));
                mTvKindTips.setText(motionState.getMotorinfo().equals("2") ? "测斜仪下放中，请耐心等待..." : "管底等待中，请耐心等待...");

            } else if (motionState.getMotorinfo().equals("4")) {//测点测量
                verticalProgressBarView.updateProgress(motionState.getMeaspoint());
                waitingTimeLayout.setVisibility(View.VISIBLE);
                mTvWaitingTimeTitle.setText("测量结束预计");
                mTvWaitingTime.setText(String.format("%s分钟", getMinTime()));
                mTvKindTips.setText(String.format("%s测量中，请耐心等待...", motionState.getMeasmode().equals("0") ? "正向" : "反向"));

            } else if (motionState.getMotorinfo().equals("5")) {//磁开关触发,测量结束
                verticalProgressBarView.setLastProgress();
                mTvKindTips.setText("测量结束，等待读取数据...");

            } else if (motionState.getMotorinfo().equals("6")) {//测斜仪配对,读取数据
                horizontalProgressBarView.setVisibility(View.VISIBLE);
                verticalProgressBarView.setVisibility(View.GONE);
                horizontalProgressBarView.setProgressDrawable(true);
                horizontalProgressBarView.updateProgress(motionState.getMeaspoint());
                waitingTimeLayout.setVisibility(View.VISIBLE);
                mTvWaitingTimeTitle.setText("数据读取结束预计");
                mTvWaitingTime.setText(String.format("%s分钟", getMinTime()));
                mTvKindTips.setText("数据读取中，请耐心等待...");
                btnAction.setVisibility(View.INVISIBLE);

            } else if (motionState.getMotorinfo().equals("7")) {//数据上传
                horizontalProgressBarView.setVisibility(View.VISIBLE);
                verticalProgressBarView.setVisibility(View.GONE);
                horizontalProgressBarView.setProgressDrawable(false);
                horizontalProgressBarView.updateProgress(motionState.getMeaspoint());
                mTvKindTips.setText("数据上传中，请耐心等待...");
                btnAction.setVisibility(View.INVISIBLE);

            } else if (motionState.getMotorinfo().equals("8") || motionState.getMotorinfo().equals("9")) {//
                stopQueryMotorStateProgress();

                horizontalProgressBarView.setVisibility(View.VISIBLE);
                verticalProgressBarView.setVisibility(View.GONE);
                horizontalProgressBarView.setProgressDrawable(false);
                horizontalProgressBarView.setMaxProgress();
                mTvKindTips.setText("测量完成");
                btnAction.setVisibility(View.VISIBLE);
                btnAction.setText("下一步");
                btnAction.setBackgroundResource(R.drawable.bg_btn_pause_motor_motion);
            }

            mTvMotorInfo.setText(AdmeCTRMotionState.valueByCode(motionState.getMotorinfo()).getDescription());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String getMinTime() {
        DecimalFormat decimalFormat = new DecimalFormat("#");

        if (TextUtils.isEmpty(motionState.getWaittime()))
            return "--";
        try {
            int second = Integer.parseInt(motionState.getWaittime());
            float min = (float) second / 60 + 1;

            return decimalFormat.format(min);
        } catch (Exception exception) {
            exception.printStackTrace();
            return "--";
        }
    }

    private void showErrorProtectionTip(String abndiasis) {
        if (customDialog != null && customDialog.isShow())
            return;

        //列出异常原因
        StringBuilder stringBuilder = new StringBuilder();
        String[] codes = abndiasis.split("\\|");
        for (String code : codes) {
            AdmeModuleErrorType errorType = AdmeModuleErrorType.valueByCode(code);
            if (errorType != null) {
                stringBuilder.append(errorType.getDescription());
                stringBuilder.append("\n");
            }
        }
        customDialog = CustomDialog.build()
                .setCustomView(new OnBindView<CustomDialog>(R.layout.error_protection_tip) {
                    @Override
                    public void onBind(final CustomDialog dialog, View v) {
                        TextView tvContent = v.findViewById(R.id.tv_content);
                        Button btnOk = v.findViewById(R.id.btn_sure);
                        btnOk.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        tvContent.setText(stringBuilder.toString());
                    }
                })
                .setCancelable(false)
                .setMaskColor(com.blankj.utilcode.util.ColorUtils.getColor(R.color.dialog_mask));

        customDialog.show();
    }

    /**
     * 停止运动提醒
     */
    protected void showStopWarnDialog() {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(requireContext())
                .title("温馨提示：")
                .content("确定停止电机运动？")
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
                        stopQueryMotorStateProgress();
                        initStopActionCommands();
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    private void setResult() {
        Intent intent = new Intent();
        intent.putExtra(AppContants.Extras.MOTOR_INFO, motionState == null ? "8" : motionState.getMotorinfo());
        mActivity.setResult(Activity.RESULT_OK, intent);
    }

    @Override
    public boolean onBackPressed() {
        setResult();
        return false;
    }

}
