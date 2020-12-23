package com.shmedo.mcloudapp.deviceconfig.ui.activity.vms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.shmedo.configlibrary.iot.enums.VmsAisleNumber;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms.TcpVmsAisleSettingFragment;

import butterknife.BindView;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/20 <br/>
 * 描述：     Vms 网关通道控制参数配置
 */
public class VmsAisleSettingActivity extends BaseActivity {
    private static final String VMS_AISLE_NUMBER = "vms_aisle_number";

    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    private int connectWay = AppContants.CommunicationWay.NET_PLATFORM_CONNECT;

    private Fragment fragment;

    private VmsAisleNumber vmsAisleNumber;


    public static void startActivity(Context context, int connectWay, VmsAisleNumber vmsAisleNumber) {
        Intent intent = new Intent(context, VmsAisleSettingActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.putExtra(VMS_AISLE_NUMBER, vmsAisleNumber);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.vms_aisle_setting_activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
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

        if (intent.getExtras().containsKey(VMS_AISLE_NUMBER)) {
            vmsAisleNumber = (VmsAisleNumber) intent.getSerializableExtra(VMS_AISLE_NUMBER);
            if (vmsAisleNumber == VmsAisleNumber.NUMBER_ONE) {
                mToolbarTitle.setText("注册通道");
            } else if (vmsAisleNumber == VmsAisleNumber.NUMBER_TWO) {
                mToolbarTitle.setText("数据通道1");
            } else if (vmsAisleNumber == VmsAisleNumber.NUMBER_THREE) {
                mToolbarTitle.setText("数据通道2");
            }
        }
    }

    private void initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {

        } else if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
            fragment = TcpVmsAisleSettingFragment.newInstance(vmsAisleNumber);
        }

        replaceFragment(fragment);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commitAllowingStateLoss();
    }
}