package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;

import butterknife.BindView;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/28/20 <br/>
 * 描述：     TODO
 */
public abstract class BaseConfigFragmentContainerActivity extends BaseActivity {

    @BindView(R.id.tv_title)
    protected TextView mToolbarTitle;

    protected int connectWay = AppContants.CommunicationWay.NET_PLATFORM_CONNECT;

    protected Fragment fragment;

    protected Intent intent;

    @Override
    protected int getLayoutId() {
        return R.layout.base_config_fragment_container_activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        parseIntent();
        replaceFragment(initFragment());
    }

    protected void parseIntent() {
        intent = getIntent();
        if (intent.getExtras() == null)
            return;

        if (intent.getExtras().containsKey(AppContants.Extras.COMMUNICATION_WAY)) {
            connectWay = intent.getIntExtra(AppContants.Extras.COMMUNICATION_WAY, AppContants.CommunicationWay.NET_PLATFORM_CONNECT);
        }
    }

    protected abstract Fragment initFragment();

    private void replaceFragment(Fragment fragment) {
        if (fragment == null)
            return;

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commitAllowingStateLoss();
    }
}
