package com.shmedo.mcloudapp.ui.activity.device.config;

import android.graphics.Color;
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
import com.afollestad.materialdialogs.MaterialDialog;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.views.LoadingDialog;
import java.util.Objects;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity.device.config
 * 文件名:   OsmometerConfigActivity
 * 创建者:   dpc
 * 创建时间:  2019/2/22 14:44
 * 描述：   渗压计功能配置
 */
public class OsmometerConfigActivity extends BaseActivity {
    @BindView(R.id.toolbar_title) TextView mToolbarTitle;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.iv_osmometer_address) ImageView mIvCollectorAddress;
    @BindView(R.id.et_osmometer_address) EditText mEtOsmometerAddress;
    @BindView(R.id.iv_water_alarm_value) ImageView mIvWaterAlarmValue;
    @BindView(R.id.et_water_alarm_value) EditText mEtWaterAlarmValue;
    @BindView(R.id.iv_water_revised) ImageView mIvWaterRevised;
    @BindView(R.id.et_water_revised) EditText mEtWaterRevised;
    @BindView(R.id.iv_osmometer_cord) ImageView mIvOsmometerCord;
    @BindView(R.id.et_osmometer_cord) EditText mEtOsmometerCord;
    @BindView(R.id.iv_nozzel_height) ImageView mIvNozzelHeight;
    @BindView(R.id.et_nozzel_height) EditText mEtNozzelHeight;
    @BindView(R.id.et_note) EditText mEtNote;
    @BindView(R.id.btn_confirm_complete) Button mBtnConfirmComplete;



    @Override protected int initContentView() {
        return R.layout.activity_osmometer_config;
    }


    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        mToolbarTitle.setText("配置渗压计");

    }

    @OnClick({ R.id.iv_osmometer_address, R.id.iv_water_alarm_value, R.id.iv_water_revised,
                 R.id.iv_osmometer_cord, R.id.iv_nozzel_height, R.id.btn_confirm_complete })
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_osmometer_address:
                LoadingDialog.showScanResultDialog(this,getResources().getString(R.string.osmometer_address));
                break;
            case R.id.iv_water_alarm_value:
                LoadingDialog.showScanResultDialog(this,getResources().getString(R.string.water_alarm_value));
                break;
            case R.id.iv_water_revised:
                LoadingDialog.showScanResultDialog(this,getResources().getString(R.string.water_revised));
                break;
            case R.id.iv_osmometer_cord:
                LoadingDialog.showScanResultDialog(this,getResources().getString(R.string.osmometer_cord));
                break;
            case R.id.iv_nozzel_height:
                LoadingDialog.showScanResultDialog(this,getResources().getString(R.string.nozzel_height));
                break;
            case R.id.btn_confirm_complete:
                finish();
                break;
        }
    }


}
