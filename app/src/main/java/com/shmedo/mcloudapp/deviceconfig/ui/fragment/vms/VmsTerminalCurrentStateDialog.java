package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.shmedo.configlibrary.iot.model.SensorErrnoInfo;
import com.shmedo.configlibrary.iot.model.vms.VmsTerminalInfo;
import com.shmedo.core.util.DeviceInfo;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.vms.VmsTerminalCurrentStateActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.BaseDialogFragment;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/20 <br/>
 * 描述：     Vms 网关终端运行状态弹框
 */
public class VmsTerminalCurrentStateDialog extends BaseDialogFragment {
    private static final String TERMINAL_INFO = "terminal_info";
    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.tv_uplink_signal_strength)
    TextView mTvUplinkSignalStrength;

    @BindView(R.id.tv_downlink_signal_strength)
    TextView mTvDownlinkSignalStrength;

    @BindView(R.id.tv_send_data)
    TextView mTvSendData;

    @BindView(R.id.tv_receive_data)
    TextView mTvReceiveData;

    @BindView(R.id.tv_sensor_status)
    TextView mTvSensorStatus;

    @BindView(R.id.tv_power_volt)
    TextView mTvPowerVolt;

    @BindView(R.id.tv_confirm)
    TextView mTvConfirm;

    private VmsTerminalInfo vmsTerminalInfo;


    public static VmsTerminalCurrentStateDialog newInstance(VmsTerminalInfo vmsTerminalInfo) {
        VmsTerminalCurrentStateDialog fragment = new VmsTerminalCurrentStateDialog();
        Bundle args = new Bundle();
        args.putParcelable(TERMINAL_INFO, vmsTerminalInfo);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            vmsTerminalInfo = getArguments().getParcelable(TERMINAL_INFO);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.vms_terminal_current_state_dialog;
    }

    @Override
    protected void setWindowStyle(int gravity) {
        super.setWindowStyle(Gravity.CENTER);
        Dialog mDialog = getDialog();
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.width = (int) (DeviceInfo.getScreenWidth() * 0.8f);
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(wlp);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }

    private void initView() {
        mTvTitle.setText("设备状态");
        mTvConfirm.setText("查看详情");
        mTvConfirm.setTextColor(GlobalUtil.getColor(R.color.blue_52B4F8));
        mTvUplinkSignalStrength.setText(String.valueOf(vmsTerminalInfo.getUprssi()));
        mTvDownlinkSignalStrength.setText(String.valueOf(vmsTerminalInfo.getDownrssi()));
        mTvSendData.setText(String.valueOf(vmsTerminalInfo.getTx()));
        mTvReceiveData.setText(String.valueOf(vmsTerminalInfo.getRx()));

        DecimalFormat df = new DecimalFormat("#");//格式化小数
        String rate = df.format(vmsTerminalInfo.getVolt()) + "%";
        mTvPowerVolt.setText(rate);

        boolean sensorAbnormal = false;
        if (vmsTerminalInfo.getSensor() != null) {
            for (SensorErrnoInfo errnoInfo : vmsTerminalInfo.getSensor()) {
                if (errnoInfo.getErrno() != 0) {
                    sensorAbnormal = true;
                }
            }
            mTvSensorStatus.setText(sensorAbnormal ? "未接入" : "正常");
            mTvSensorStatus.setTextColor(sensorAbnormal ? GlobalUtil.getColor(R.color.red) : GlobalUtil.getColor(R.color.text_color_3AD094));
        }
    }

    @OnClick({R.id.iv_close, R.id.tv_confirm})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
                dismiss();
                break;

            case R.id.tv_confirm:
                dismiss();
                VmsTerminalCurrentStateActivity.startActivity(mActivity, vmsTerminalInfo);
                break;
        }
    }
}