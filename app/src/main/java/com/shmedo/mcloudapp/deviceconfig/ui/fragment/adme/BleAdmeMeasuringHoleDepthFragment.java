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

import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.shmedo.configlibrary.ble.utils.ValidateUtil;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.entity.adme.AdmeMeasuringHoleDepthEntity;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeMeasuringHoleDepthInfo;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.view.ClearEditText;
import com.shmedo.mcloudapp.util.KeyBordUtils;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  1/6/21 <br/>
 * 描述：     ADME 测量孔深
 */
public class BleAdmeMeasuringHoleDepthFragment extends BaseBleIotCommunicateFragment {
    @BindView(R.id.tv_motion_way)
    TextView mTvMotionWay;

    @BindView(R.id.et_movement_speed)
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
    private String motionDistance;//  运动距离

    private AdmeMeasuringHoleDepthInfo measuringHoleDepthInfo;
    private BleAdmeMotorMotionStateFragment motorMotionStateFragment;

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
        setFilter();
        setRadioButtonListener();
        queryParamInfo();

        rbAutoControl.setChecked(true);
        rbManualControl.setTextColor(GlobalUtil.getColor(R.color.text_color_cccccc));
    }

    private void setFilter() {
        mEtMovementSpeed.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2)});
        mEtMotionDistance.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});

        mEtMovementSpeed.setHint("1-99");
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
     * 获取运动状态参数
     */
    private void queryParamInfo() {
        errMsg = "查询数据超时,请稍后尝试";
        startProgressRunnable("加载中...", DELAY_MILLIS);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_GET_MEASURING_HOLEDEPTH_PARAMETERS);
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

        } else if (id == R.id.ll_clear_motion_data) {
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
        }
    }

    private boolean checkValueIsValid() {
        movementSpeed = mEtMovementSpeed.getText().toString().trim();
        motionDistance = mEtMotionDistance.getText().toString().trim();
        if (TextUtils.isEmpty(movementSpeed)) {
            ToastUtils.show("电机运动速度不能为空!");
            mEtMovementSpeed.requestFocus();
            return false;
        }
        try {
            int port = Integer.parseInt(movementSpeed);
            if (port < 0 || port > 100) {
                ToastUtils.show("请输入有效的电机运动速度!");
                mEtMovementSpeed.requestFocus();
                return false;
            }
        } catch (Exception ex) {
            ToastUtils.show("请输入有效的电机运动速度!");
            mEtMovementSpeed.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(motionDistance)) {
            ToastUtils.show("运动距离不能为空!");
            mEtMotionDistance.requestFocus();
            return false;
        }

        if (!ValidateUtil.isInteger(motionDistance)) {
            ToastUtils.show("请输入正确的运动距离!");
            mEtMotionDistance.requestFocus();
            return false;
        }
        return true;
    }

    private void processSave() {
        AdmeMeasuringHoleDepthEntity entity = new AdmeMeasuringHoleDepthEntity();
        entity.setMovementway(motionWay);
        entity.setMotorspeed(movementSpeed);
        entity.setMovedistance(motionDistance);

        mBtnRun.setEnabled(false);

        errMsg = "发送指令超时,请稍后尝试";
        startProgressRunnable("正在发送配置指令...", DELAY_MILLIS);
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.ADME_MD_SET_MEASURING_HOLEDEPTH_PARAMETERS, entity);
        sendCommand(command);
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

    @Override
    protected void parseResponseMessage(@NotNull String cmdStr) {
        setResultData(cmdStr);
    }

    private void setResultData(final String cmdStr) {
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        switch (type) {
            case ADME_MD_GET_MEASURING_HOLEDEPTH_PARAMETERS: {//获取ADME的测量孔深配置参数
                stopProgressRunnable();
                IOTCommandResult<AdmeMeasuringHoleDepthInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取设备的测量孔深配置参数出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                measuringHoleDepthInfo = commandResult.getResult();
                initParamConfigInfo();
            }
            break;

            case ADME_MD_SET_MEASURING_HOLEDEPTH_PARAMETERS: {//设置ADME的测量孔深配置参数
                stopProgressRunnable();
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "保存基础配置参数出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    mBtnRun.setEnabled(true);
                    return;
                }
                doAfterSetting();
            }
            break;

            default:
                stopProgressRunnable();
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
        motionDistance = measuringHoleDepthInfo.getMovedistance().trim();
        if (motionWay.equals("0")) {
            motionWayPos = 0;
            mTvMotionWay.setText("上拉");
        } else {
            motionWayPos = 1;
            mTvMotionWay.setText("下放");
        }
        mEtMovementSpeed.setText(movementSpeed);
        mEtMotionDistance.setText(motionDistance);

        if (measuringHoleDepthInfo.getMorunstate().trim().equals('1'))
            showMotorMotionDialog();
    }

    private void doAfterSetting() {
        if (measuringHoleDepthInfo != null) {
            measuringHoleDepthInfo.setMovementway(motionWay);
            measuringHoleDepthInfo.setMotorspeed(movementSpeed);
            measuringHoleDepthInfo.setMovedistance(motionDistance);
        }
        mBtnRun.setEnabled(true);
        showMotorMotionDialog();
    }

    private void showMotorMotionDialog() {
        if (measuringHoleDepthInfo != null) {
            motorMotionStateFragment = BleAdmeMotorMotionStateFragment.newInstance(measuringHoleDepthInfo);
            motorMotionStateFragment.show(getChildFragmentManager(), "dialog");
        }
    }
}