package com.shmedo.mcloudapp.ui.activity.device;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.ui.activity.device.config.GeneralSettingActivity;
import com.shmedo.mcloudapp.ui.activity.device.config.SeniorSettingActivity;
import com.shmedo.mcloudapp.util.StringUtil;
import com.shmedo.mcloudapp.util.ToastUtil;
import java.util.Objects;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity.device
 * 文件名:   ConfigDeviceParameterActivity
 * 创建者:   dpc
 * 创建时间:  2019/1/28 16:59
 * 描述：    配置设备参数
 */
public class ConfigDeviceParameterActivity extends BaseActivity {

    @BindView(R.id.toolbar_title) TextView mToolbarTitle;
    @BindView(R.id.toolbar) Toolbar mToolbar;

    @BindView(R.id.iv_lock) ImageView mIvLock;
    @BindView(R.id.tv_lock) TextView mTvLock;
    @BindView(R.id.et_query) EditText mEtQuery;
    @BindView(R.id.tv_query) TextView mTvQuery;
    @BindView(R.id.sp_project_name) Spinner mSpProjectName;
    @BindView(R.id.sw_bluetooth) Switch mSwBluetooth;
    @BindView(R.id.tv_bluetooth) TextView mTvBluetooth;
    @BindView(R.id.sw_device_state) Switch mSwDeviceState;
    @BindView(R.id.tv_device_state) TextView mTvDeviceState;
    @BindView(R.id.sw_debug) Switch mSwDebug;
    @BindView(R.id.tv_debug) TextView mTvDebug;
    @BindView(R.id.sw_sim_A) Switch mSwSimA;
    @BindView(R.id.tv_sim_A) TextView mTvSimA;
    @BindView(R.id.sw_sim_B) Switch mSwSimB;
    @BindView(R.id.tv_sim_B) TextView mTvSimB;
    @BindView(R.id.sw_rain) Switch mSwRain;
    @BindView(R.id.tv_rain_config) TextView mTvRainConfig;
    @BindView(R.id.sw_osmometer) Switch mSwOsmometer;
    @BindView(R.id.tv_osmometer_config) TextView mTvOsmometerConfig;
    @BindView(R.id.sw_sensor) ImageView mSwSensor;
    @BindView(R.id.rl_sensor_setting) RelativeLayout mRlSensorSetting;
    @BindView(R.id.rl_general_setting) RelativeLayout mRlGeneralSetting;
    @BindView(R.id.rl_senior_setting) RelativeLayout mRlSeniorSetting;

    @Override protected int initContentView() {
        return R.layout.activity_config_device_parameter;
    }


    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }


    private void initView() {
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        mToolbarTitle.setText("设备详情");

    }


    private void initData() {
        if (mTvLock.getText().equals("已锁定")){
            mIvLock.setBackground(getResources().getDrawable(R.drawable.icon_close_lock));
            //mSpProjectName.setClickable(false);
            //mSpProjectName.setFocusable(false);
            //mSpProjectName.setFocusableInTouchMode(false);
        }else if(mTvLock.getText().equals("已解锁")){
            //mSpProjectName.setFocusable(true);
            //mSpProjectName.setFocusableInTouchMode(true);
            mIvLock.setBackground(getResources().getDrawable(R.drawable.icon_open_lock));
        }
        String[] dataSize = getResources().getStringArray(R.array.project_name);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, dataSize);
        mSpProjectName.setAdapter(spinnerAdapter);
        mSpProjectName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                ToastUtil.showSToast("" + position);
            }
            @Override public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        if (mSwBluetooth.isChecked()){
            mTvBluetooth.setText("已连接");
        }else {
            mTvBluetooth.setText("未连接");
        }
        mSwBluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    mTvBluetooth.setText("已连接");
                }else {
                    mTvBluetooth.setText("未连接");
                }
            }
        });
        if (mSwDeviceState.isChecked()){
            mTvDeviceState.setText("已连接");
        }else {
            mTvDeviceState.setText("未连接");
        }
        mSwDeviceState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    mTvDeviceState.setText("已激活");
                }else {
                    mTvDeviceState.setText("未激活");
                }
            }
        });
        if (mSwDebug.isChecked()){
            mTvDebug.setText("已开启");
        }else {
            mTvDebug.setText("已关闭");
        }
        mSwDebug.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    mTvDebug.setText("已开启");
                }else {
                    mTvDebug.setText("已关闭");
                }
            }
        });
        if (mSwSimA.isChecked()){
            mTvSimA.setText("已开启");
        }else {
            mTvSimA.setText("已关闭");
        }
        mSwSimA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    mTvSimA.setText("已开启");
                }else {
                    mTvSimA.setText("已关闭");
                }
            }
        });
        if (mSwSimB.isChecked()){
            mTvSimB.setText("已开启");
        }else {
            mTvSimB.setText("已关闭");
        }
        mSwSimB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    mTvSimB.setText("已开启");
                }else {
                    mTvSimB.setText("已关闭");
                }
            }
        });
        if (mSwSimB.isChecked()){
            mTvRainConfig.setText("配置");
            mTvRainConfig.setClickable(true);
        }else {
            mTvRainConfig.setText("已停用");
            mTvRainConfig.setClickable(false);
        }
        mSwRain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    mTvRainConfig.setText("配置");
                    mTvRainConfig.setClickable(true);
                }else {
                    mTvRainConfig.setText("已停用");
                    mTvRainConfig.setClickable(false);
                }
            }
        });
        if (mSwSimB.isChecked()){
            mTvOsmometerConfig.setText("配置");
            mTvOsmometerConfig.setClickable(true);
        }else {
            mTvOsmometerConfig.setText("已停用");
            mTvOsmometerConfig.setClickable(false);
        }
        mSwOsmometer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    mTvOsmometerConfig.setText("配置");
                    mTvOsmometerConfig.setClickable(true);
                }else {
                    mTvOsmometerConfig.setText("已停用");
                    mTvOsmometerConfig.setClickable(false);
                }
            }
        });
    }


    @OnClick({ R.id.iv_lock, R.id.tv_query,R.id.rl_sensor_setting, R.id.rl_general_setting,
                 R.id.rl_senior_setting,R.id.tv_rain_config,R.id.tv_osmometer_config})
    public void onViewClicked(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.iv_lock:
                if (mTvLock.getText().equals("已锁定")){
                    mTvLock.setText("已解锁");
                    //mSpProjectName.setClickable(true);
                    mIvLock.setBackground(getResources().getDrawable(R.drawable.icon_open_lock));
                    //mSpProjectName.setFocusable(true);
                    //mSpProjectName.setFocusableInTouchMode(true);
                }else if (mTvLock.getText().equals("已解锁")){
                    //mSpProjectName.setClickable(false);
                    mTvLock.setText("已锁定");
                    mIvLock.setBackground(getResources().getDrawable(R.drawable.icon_close_lock));
                    //mSpProjectName.setFocusable(false);
                    //mSpProjectName.setFocusableInTouchMode(false);
                }
                break;
            case R.id.tv_query:
                if (StringUtil.isNullOrEmpty(mEtQuery.getText().toString().trim())){
                    ToastUtil.showSToast(""+mEtQuery.getText().toString());
                }else {
                    ToastUtil.showSToast("搜索的内容不能为空");
                }
                break;
            case R.id.tv_rain_config:
                ToastUtil.showSToast("雨量计配置");
                break;
            case R.id.tv_osmometer_config:
                ToastUtil.showSToast("渗压计配置");
                break;
            case R.id.rl_sensor_setting:
                ToastUtil.showSToast("传感器参数配置");
                break;
            case R.id.rl_general_setting:
                intent = new Intent(ConfigDeviceParameterActivity.this,GeneralSettingActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_senior_setting:
                intent = new Intent(ConfigDeviceParameterActivity.this,SeniorSettingActivity.class);
                startActivity(intent);
                break;

        }
    }
}
