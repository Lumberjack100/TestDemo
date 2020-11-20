package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.shmedo.configlibrary.iot.model.TerminalBean;
import com.shmedo.core.AppContants;
import com.shmedo.core.util.DeviceInfo;
import com.shmedo.core.util.GlobalUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.vms.VmsTerminalCurrentStateActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.BaseDialogFragment;

import butterknife.BindView;
import butterknife.OnClick;


/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/20 <br/>
 * 描述：     Vms 网关终端运行状态弹框
 */
public class TcpVmsTerminalCurrentStateDialog extends BaseDialogFragment {
    private static final String DEVICE_INFO = "device_info";
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

    @BindView(R.id.tv_sensor_state)
    TextView mTvSensorState;

    @BindView(R.id.tv_power_volt)
    TextView mTvPowerVolt;

    @BindView(R.id.tv_confirm)
    TextView mTvConfirm;

    private TerminalBean terminalBean;


    public static TcpVmsTerminalCurrentStateDialog newInstance(TerminalBean terminalBean) {
        TcpVmsTerminalCurrentStateDialog fragment = new TcpVmsTerminalCurrentStateDialog();
        Bundle args = new Bundle();
        args.putParcelable(DEVICE_INFO, terminalBean);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            terminalBean = getArguments().getParcelable(DEVICE_INFO);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.tcp_vms_terminal_current_state_dialog;
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

        mTvUplinkSignalStrength.setText(String.valueOf(terminalBean.getUprssi()));
        mTvDownlinkSignalStrength.setText(String.valueOf(terminalBean.getDownrssi()));
        mTvSendData.setText(String.valueOf(terminalBean.getTx()));
        mTvReceiveData.setText(String.valueOf(terminalBean.getRx()));
        mTvSensorState.setText(String.valueOf(terminalBean.getUprssi()));
        mTvPowerVolt.setText(terminalBean.getVolt() + "V");
    }

    @OnClick({R.id.iv_close, R.id.tv_confirm})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_close:
                dismiss();
                break;

            case R.id.tv_confirm:
                dismiss();
                VmsTerminalCurrentStateActivity.startActivity(mActivity, AppContants.CommunicationWay.TCP_CONNECT, terminalBean);
                break;
        }
    }
}