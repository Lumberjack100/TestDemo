package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;

import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.ConfigPageViewModel;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/28/20 <br/>
 * 描述：     TODO
 */
public abstract class BaseConfigFragmentContainerActivity extends BaseActivity {
    @BindView(R.id.maskLayerLayout)
    protected ViewGroup maskLayerLayout;

    @BindView(R.id.tv_title)
    protected TextView mToolbarTitle;

    @BindView(R.id.iv_action)
    protected ImageView mIvAction;

    @BindView(R.id.tv_action)
    protected TextView mTvAction;

    protected int connectWay = AppContants.CommunicationWay.NET_PLATFORM_CONNECT;

    protected Fragment fragment;

    protected Intent intent;

    protected ConfigPageViewModel configPageViewModel;


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
        configPageViewModel = getActivityScopeViewModel(ConfigPageViewModel.class);
        configPageViewModel.configPageEditableChanged.observeInActivity(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isEditable) {
                if (isEditable) {
                    mTvAction.setText("取消");
                    maskLayerLayout.setVisibility(View.GONE);
                } else {
                    mTvAction.setText("编辑");
                    maskLayerLayout.setVisibility(View.VISIBLE);
                }
            }
        });
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
        transaction.replace(R.id.fragment_container_view, fragment);
        transaction.commitAllowingStateLoss();
    }

    @OnClick({R.id.tv_action})
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.tv_action) {
            if (mTvAction.getText().toString().equals("编辑")) {
                configPageViewModel.configPageEditableChanged.setValue(true);
            } else if (mTvAction.getText().toString().equals("取消")) {
                configPageViewModel.configPageEditableChanged.setValue(false);
            }
        }
    }
}
