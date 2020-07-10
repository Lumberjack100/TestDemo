package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hjq.toast.ToastUtils;
import com.dragon.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.interfaces.Extras;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.DASHomeFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.DeviceDetailsFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.QueryDeviceDataFragment;

import butterknife.BindView;
import butterknife.OnClick;

public class ConfigDASActivity extends BaseDeviceConnectActivity {
    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    @BindView(R.id.tv_save)
    TextView mTvSave;

    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigationView;

    private DASHomeFragment DASHomeFragment;
    private QueryDeviceDataFragment queryDataFragment;        //查询数据
    private DeviceDetailsFragment deviceDetailsFragment;//设备详情
    private Fragment currentFragment;

    private boolean isFirstCall = true;


    public static void startActivity(Context context, String deviceInfo) {
        Intent intent = new Intent(context, ConfigDASActivity.class);
        intent.putExtra(Extras.CUR_DEVICE_NAME, deviceInfo);
        context.startActivity(intent);
    }


    @Override
    protected int initContentView() {
        return R.layout.activity_config_das;
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("CurrentFragment", currentFragment.getClass().getName());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView(savedInstanceState);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (isFirstCall) {
            findAndConnectSpecificDevice();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isFirstCall = false;
    }

    private void initView(Bundle savedInstanceState) {
        mTvSave.setVisibility(View.VISIBLE);
        bottomNavigationView.getMenu().getItem(3).setVisible(false);//隐藏底部导航菜单 '高级配置' 按钮

        if (savedInstanceState != null) {  // “内存重启”时调用
            String curTag = savedInstanceState.getString("CurrentFragment");
            currentFragment = getSupportFragmentManager().findFragmentByTag(curTag);
            DASHomeFragment = (DASHomeFragment) getSupportFragmentManager().findFragmentByTag(DASHomeFragment.class.getName());
            queryDataFragment = (QueryDeviceDataFragment) getSupportFragmentManager().findFragmentByTag(QueryDeviceDataFragment.class.getName());
            deviceDetailsFragment = (DeviceDetailsFragment) getSupportFragmentManager().findFragmentByTag(DeviceDetailsFragment.class.getName());

            if(DASHomeFragment==null)
                DASHomeFragment = new DASHomeFragment();

            if(queryDataFragment==null)
                queryDataFragment=new QueryDeviceDataFragment();

            if(deviceDetailsFragment==null)
                deviceDetailsFragment = new DeviceDetailsFragment();

            // 解决重叠问题
            getSupportFragmentManager().beginTransaction()
                    .hide(DASHomeFragment)
                    .hide(queryDataFragment)
                    .hide(deviceDetailsFragment)
                    .show(currentFragment)
                    .commit();
        } else {
            DASHomeFragment = new DASHomeFragment();
            queryDataFragment = new QueryDeviceDataFragment();
            deviceDetailsFragment = new DeviceDetailsFragment();
            switchFrgment(0);
        }

        initBottomNavigationItemSelectedListener();
    }

    private void initBottomNavigationItemSelectedListener() {
        //为底部导航设置条目选中监听
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item_parameter:
                        mToolbarTitle.setText(getString(R.string.device_configuration));
                        switchFrgment(0);
                        break;
                    case R.id.item_query_data:
                        mToolbarTitle.setText(getString(R.string.query_data));
                        switchFrgment(1);
                        break;
                    case R.id.item_device_details:
                        mToolbarTitle.setText(getString(R.string.device_state_info));
                        switchFrgment(2);
                        break;
                    case R.id.item_highsetting:
                        mToolbarTitle.setText("高级配置");
                        switchFrgment(3);
                        break;
                }

                return true;    //这里返回true，表示事件已经被处理。如果返回false，为了达到条目选中效果，还需要下面的代码
                // item.setChecked(true);  不论点击了哪一个，都手动设置为选中状态true（该控件并没有默认实现)
                // 。如果不设置，只有第一个menu展示的时候是选中状态，其他的即便被点击选中了，图标和文字也不会做任何更改
            }
        });
    }


    @OnClick({R.id.back, R.id.tv_save})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;

            case R.id.tv_save:
                if (!MCloudApp.isIsBluetoothDeviceConnected()) {
                    ToastUtils.show(getString(R.string.param_config_bluetooth_disconnect_warn));
                    return;
                }
                saveConfigInfo();
                break;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // 蓝牙已经开启
                if (resultCode != Activity.RESULT_OK) {
                    ToastUtils.show("蓝牙未启用");
                    return;
                }
                findAndConnectSpecificDevice();
                break;
        }
    }


    /**
     * switch the fragment accordting to id
     */
    private void switchFrgment(int i) {
        switch (i) {
            case 0:
                showFragment(DASHomeFragment);
                break;
            case 1:
                showFragment(queryDataFragment);
                break;
            case 2:
                showFragment(deviceDetailsFragment);
                break;
        }
    }

    private void showFragment(Fragment fragment) {
        if (currentFragment != fragment) {//  判断传入的fragment是不是当前的currentFragmentgit
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if (!fragment.isAdded()) { //  判断传入的fragment是否已经被add()过
                transaction.add(R.id.content_frame, fragment, fragment.getClass().getName());
                if (currentFragment != null) {
                    transaction.hide(currentFragment);
                }
            } else {
                transaction.hide(currentFragment).show(fragment);
            }

            currentFragment = fragment;  //  然后将传入的fragment赋值给currentFragment
            transaction.commit();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        MCloudApp.setCurDeviceToken(null);
        MCloudApp.setCurDeviceMacAddr(null);
    }
}
