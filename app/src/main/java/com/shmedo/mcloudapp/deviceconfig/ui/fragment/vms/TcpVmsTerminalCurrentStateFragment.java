package com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.shmedo.configlibrary.iot.model.TerminalBean;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;

/**
 * Vms 终端运行状态
 */
public class TcpVmsTerminalCurrentStateFragment extends BaseFragment {
    private static final String DEVICE_INFO = "device_info";

    private TerminalBean terminalBean;

    public static TcpVmsTerminalCurrentStateFragment newInstance(TerminalBean terminalBean) {
        TcpVmsTerminalCurrentStateFragment fragment = new TcpVmsTerminalCurrentStateFragment();
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
        return R.layout.tcp_vms_terminal_current_state_fragment;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

}