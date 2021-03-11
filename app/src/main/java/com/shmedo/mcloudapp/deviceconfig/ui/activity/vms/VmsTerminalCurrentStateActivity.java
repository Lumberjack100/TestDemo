package com.shmedo.mcloudapp.deviceconfig.ui.activity.vms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.Fragment;

import com.shmedo.configlibrary.iot.model.vms.VmsTerminalInfo;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.BaseConfigFragmentContainerActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms.TcpVmsTerminalCurrentStateFragment;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/20 <br/>
 * 描述：     Vms 网关终端运行状态详情页面
 */
public class VmsTerminalCurrentStateActivity extends BaseConfigFragmentContainerActivity {
    private static final String TERMINAL_INFO = "terminal_info";

    private VmsTerminalInfo vmsTerminalInfo;


    public static void startActivity(Context context, VmsTerminalInfo vmsTerminalInfo) {
        Intent intent = new Intent(context, VmsTerminalCurrentStateActivity.class);
        intent.putExtra(TERMINAL_INFO, vmsTerminalInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("运行状态");
        mIvAction.setVisibility(View.GONE);
    }

    @Override
    protected void parseIntent() {
        super.parseIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(TERMINAL_INFO)) {
            vmsTerminalInfo = intent.getParcelableExtra(TERMINAL_INFO);
        }
    }

    @Override
    protected Fragment initFragment() {
        return TcpVmsTerminalCurrentStateFragment.newInstance(vmsTerminalInfo);
    }
}