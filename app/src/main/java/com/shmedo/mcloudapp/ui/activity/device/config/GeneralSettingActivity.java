package com.shmedo.mcloudapp.ui.activity.device.config;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import java.util.Objects;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity.device.config
 * 文件名:   GeneralSettingActivity
 * 创建者:   dpc
 * 创建时间:  2019/2/15 15:23
 * 描述：    通用配置页面
 */
public class GeneralSettingActivity extends BaseActivity {
    @BindView(R.id.toolbar_title) TextView mToolbarTitle;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.iv_collector_address) ImageView mIvCollectorAddress;
    @BindView(R.id.et_collector_address) EditText mEtCollectorAddress;
    @BindView(R.id.iv_calculating_time) ImageView mIvCalculatingTime;
    @BindView(R.id.et_calculating_time) EditText mEtCalculatingTime;
    @BindView(R.id.iv_standby_time) ImageView mIvStandbyTime;
    @BindView(R.id.et_standby_time) EditText mEtStandbyTime;
    @BindView(R.id.iv_collect_time) ImageView mIvCollectTime;
    @BindView(R.id.et_collect_time) EditText mEtCollectTime;
    @BindView(R.id.btn_confirm_complete) Button mBtnConfirmComplete;


    @Override protected int initContentView() {
        return R.layout.activity_general_setting;
    }


    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }


    private void initView() {
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        mToolbarTitle.setText("通用设置");

    }


    @OnClick({ R.id.iv_collector_address, R.id.iv_calculating_time, R.id.iv_standby_time,
                 R.id.iv_collect_time, R.id.btn_confirm_complete })
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_collector_address:
                break;
            case R.id.iv_calculating_time:
                break;
            case R.id.iv_standby_time:
                break;
            case R.id.iv_collect_time:
                break;
            case R.id.btn_confirm_complete:
                finish();
                break;
        }
    }
}
