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

import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.vms.TcpVmsHomeFragment;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/20 <br/>
 * 描述：     Vms 网关主页面
 */
public class VmsHomeActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    @BindView(R.id.iv_action)
    ImageView mIvRightIcon;

    private int connectWay = AppContants.CommunicationWay.NET_PLATFORM_CONNECT;

    private Fragment fragment;

    public static void startActivity(Context context, int connectWay) {
        Intent intent = new Intent(context, VmsHomeActivity.class);
        intent.putExtra(AppContants.Extras.COMMUNICATION_WAY, connectWay);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.vms_home_activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbarTitle.setText("网关连接");
        mIvRightIcon.setVisibility(View.VISIBLE);
        mIvRightIcon.setImageResource(R.drawable.ic_vms_advanced_settings);
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
    }

    private void initFragment() {
        if (connectWay == AppContants.CommunicationWay.NET_PLATFORM_CONNECT) {

        } else if (connectWay == AppContants.CommunicationWay.TCP_CONNECT) {
            fragment = TcpVmsHomeFragment.newInstance();
        }
        replaceFragment(fragment);
    }

    @OnClick({R.id.iv_action})
    public void onClick(View v) {
        if (v.getId() == R.id.iv_action) {
            VmsAdvancedSettingsActivity.startActivity(this, AppContants.CommunicationWay.TCP_CONNECT);
        }
    }

    private void replaceFragment(Fragment fragment) {
        if (fragment == null)
            return;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commitAllowingStateLoss();
    }
}