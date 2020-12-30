package com.shmedo.mcloudapp.deviceconfig.ui.activity.vms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.shmedo.configlibrary.iot.model.vms.VmsTerminalInfo;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.QueryDeviceDataActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms.TcpVmsTerminalHomeFragment;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/19 <br/>
 * 描述：     Vms 网关挂载的终端设备主页面
 */
public class VmsTerminalHomeActivity extends BaseActivity {
    private static final String DEVICE_INFO = "device_info";

    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    @BindView(R.id.iv_action)
    ImageView mIvRightIcon;

    private int connectWay = AppContants.CommunicationWay.NET_PLATFORM_CONNECT;

    private Fragment fragment;

    private VmsTerminalInfo vmsTerminalInfo;


    public static void startActivity(Context context, int connectWay, VmsTerminalInfo vmsTerminalInfo) {
        Intent intent = new Intent(context, VmsTerminalHomeActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(DEVICE_INFO, vmsTerminalInfo);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_vms_terminal_home;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("设备配置");
        mIvRightIcon.setVisibility(View.INVISIBLE);
        mIvRightIcon.setImageResource(R.drawable.ic_query_device_data);
        parseIntent();
        initFragment();
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(AppContants.Extras.COMMUNICATION_WAY)) {
            connectWay = intent.getIntExtra(AppContants.Extras.COMMUNICATION_WAY, AppContants.CommunicationWay.NET_PLATFORM_CONNECT);
        }

        if (intent.getExtras().containsKey(DEVICE_INFO)) {
            vmsTerminalInfo = intent.getParcelableExtra(DEVICE_INFO);
        }
    }

    private void initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {

        } else if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
            fragment = TcpVmsTerminalHomeFragment.newInstance(vmsTerminalInfo);
        }
        replaceFragment(fragment);
    }

    @OnClick({R.id.iv_action})
    public void onClick(View v) {
        if (v.getId() == R.id.iv_action) {
            if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
                QueryDeviceDataActivity.startActivity(VmsTerminalHomeActivity.this, vmsTerminalInfo.getSn());
            }
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commitAllowingStateLoss();
    }

}