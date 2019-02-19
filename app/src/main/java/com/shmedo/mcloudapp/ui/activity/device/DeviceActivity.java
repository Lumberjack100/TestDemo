package com.shmedo.mcloudapp.ui.activity.device;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.BindView;
import butterknife.OnClick;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import java.util.Objects;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity
 * 文件名:   DeviceActivity
 * 创建者:   dpc
 * 创建时间:  2019/1/17 17:16
 * 描述：    设备页面
 */
public class DeviceActivity extends BaseActivity {

    @BindView(R.id.tv_device_name) TextView mTvDeviceName;
    @BindView(R.id.tv_device_sn) TextView mTvDeviceSn;
    @BindView(R.id.tv_device_model) TextView mTvDeviceModel;
    @BindView(R.id.tv_sensor_type) TextView mTvSensorType;
    @BindView(R.id.Rl_device_config) RelativeLayout mRlDeviceConfig;
    @BindView(R.id.Rl_device_query) RelativeLayout mRlDeviceQuery;
    @BindView(R.id.Rl_device_details) RelativeLayout mRlDeviceDetails;
    @BindView(R.id.toolbar_title) TextView mToolbarTitle;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    private String deviceInfo = null;


    @Override protected int initContentView() {
        return R.layout.activity_device;
    }


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }


    private void initView() {
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        mToolbarTitle.setText("设备");
        mToolbar.setOnMenuItemClickListener(onMenuItemClick);
    }


    private void initData() {

        if (Objects.requireNonNull(getIntent().getExtras()).containsKey("device")) {
            deviceInfo = getIntent().getStringExtra("device");
            Log.i("adu", "扫一扫-===" + deviceInfo);
        } else if (getIntent().getExtras().containsKey("inputDevice")) {
            deviceInfo = getIntent().getStringExtra("inputDevice");
            Log.i("adu", "手动输入-===" + deviceInfo);
        } else if (getIntent().getExtras().containsKey("ScanDevice")){
            deviceInfo = getIntent().getStringExtra("ScanDevice");
            String[] scanData = deviceInfo.split(",");
            scanResult(scanData);
        }

    }


    private void scanResult(String[] scanData) {
        mTvDeviceName.setText("");
        mTvDeviceSn.setText(scanData[1]);
        mTvDeviceModel.setText(scanData[2]);
    }


    @OnClick({ R.id.Rl_device_config, R.id.Rl_device_query, R.id.Rl_device_details })
    public void onViewClicked(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.Rl_device_config:
                intent = new Intent(DeviceActivity.this,ConfigDeviceParameterActivity.class);
                startActivity(intent);
                break;
            case R.id.Rl_device_query:
                intent = new Intent(DeviceActivity.this,QueryDataRecordActivity.class);
                startActivity(intent);
                break;
            case R.id.Rl_device_details:
                intent = new Intent(DeviceActivity.this,AccessDeviceDetailsActivity.class);
                startActivity(intent);
                break;
        }
    }


    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    private Toolbar.OnMenuItemClickListener onMenuItemClick
        = new Toolbar.OnMenuItemClickListener() {

        @Override public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.current_blu:

                    break;
            }
            return true;
        }
    };
}
