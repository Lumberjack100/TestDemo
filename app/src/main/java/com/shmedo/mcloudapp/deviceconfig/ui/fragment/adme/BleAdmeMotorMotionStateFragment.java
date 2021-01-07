package com.shmedo.mcloudapp.deviceconfig.ui.fragment.adme;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.cmd.IOTCommandResult;
import com.shmedo.configlibrary.iot.cmd.parser.IOTParseManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.model.CommonSettingCmdResult;
import com.shmedo.configlibrary.iot.model.adme.AdmeMeasuringHoleDepthInfo;
import com.shmedo.configlibrary.iot.model.adme.AdmeMotorMotionStateInfo;
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
 * 描述：     ADME 电机运行状态实时展示底部弹窗
 */
public class BleAdmeMotorMotionStateFragment extends BaseDialogFragment {
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
    Button btnStop;

    @BindView(R.id.btn_pause)
    Button btnPause;

    @BindView(R.id.btn_exit)
    Button btnExit;

    private USRBleViewModel usrBleViewModel;

    private AdmeMeasuringHoleDepthInfo measuringHoleDepthInfo;

    private AdmeMotorMotionStateInfo motionStateInfo;


    public static BleAdmeMotorMotionStateFragment newInstance(AdmeMeasuringHoleDepthInfo measuringHoleDepthInfo) {
        BleAdmeMotorMotionStateFragment fragment = new BleAdmeMotorMotionStateFragment();
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
        return R.layout.ble_adme_motor_motion_state_fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        usrBleViewModel = getApplicationScopeViewModel(USRBleViewModel.class);
        usrBleViewModel.getResponseMsg().observeInFragment(this, new Observer<String>() {
            @Override
            public void onChanged(String result) {
                if (!result.startsWith("$cmd="))
                    return;

                try {
                    parseResponseMessage(result);

                } catch (Exception ex) {
                    Timber.e(ex);
                }
            }
        });
        getMotorMotionState();
    }

    private void initView() {
        mIvClose.setVisibility(View.VISIBLE);
        mTvClearData.setVisibility(View.GONE);
        btnStop.setVisibility(View.VISIBLE);
        btnPause.setVisibility(View.VISIBLE);
        btnExit.setVisibility(View.GONE);
    }

    /**
     * 查询ADME测孔深运动的脉冲数、运动距离指令
     */
    private void getMotorMotionState() {
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
        if (!usrBleViewModel.isConnected()) {
            ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
            return;
        }

        if (id == R.id.iv_close) {
            dismiss();

        } else if (id == R.id.tv_clear_data) {
            clearMotorMotionData();

        } else if (id == R.id.btn_stop) {
            stopMotorMotion();

        } else if (id == R.id.btn_pause) {
            stopMotorMotion();

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
                IOTCommandResult<AdmeMotorMotionStateInfo> commandResult = IOTParseManager.getInstance().parse(cmdStr);
                if (!commandResult.isSuccess()) {
                    String errMsg = String.format("%s %s", "获取电机的实时运行状态出错!", commandResult.getMessage());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
                motionStateInfo = commandResult.getResult();
                initParamConfigInfo();
            }
            break;

            case ADME_MD_STOP_MEASURING_HOLEDEPTH: {
                CommonSettingCmdResult cmdResult = IOTParseManager.getInstance().parseSettingCmd(cmdStr);
                if (!cmdResult.isSucceed()) {
                    String errMsg = String.format("%s %s", "删除终端出错!", cmdResult.getReason());
                    Timber.e(errMsg);
                    ToastUtils.show(errMsg);
                    return;
                }
//                doAfterSetting();
            }
            break;
        }
    }

    private void initParamConfigInfo() {
        if (motionStateInfo == null) {
            Timber.e("AdmeMotorMotionStateInfo is Null!");
            return;
        }
        mTvMotionPulse.setText(motionStateInfo.getPulsenumber());
        mTvMotionDistance.setText(motionStateInfo.getRealmovedistance());

    }

}