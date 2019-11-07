package com.shmedo.mcloudapp.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.bluetooth.MdBluetoothManager;
import com.shmedo.mcloudapp.bluetooth.Message;
import com.shmedo.mcloudapp.model.Extras;
import com.shmedo.mcloudapp.ui.fragment.ADMEHomeFragment;
import com.shmedo.mcloudapp.ui.fragment.DeviceDetailsFragment;
import com.shmedo.mcloudapp.ui.fragment.QueryDataFragment;
import com.shmedo.mcloudapp.util.bleutil.BlueDeviceCommunicateUtil;
import com.shmedo.mcloudapp.util.common.HandleBackUtil;

import java.util.UUID;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp
 * 创建者:   gonghe
 * 创建时间:  2019-10-21
 * 描述：   ADME 设备配置页面
 */
public class ConfigADMEActivity extends BaseActivity implements BlueDeviceCommunicateUtil.ObtainDeviceStateCmdCallback {

    @BindView(R.id.tv_parameter)
    TextView mTvParameter;

    @BindView(R.id.tv_query_data)
    TextView mTvQueryData;

    @BindView(R.id.tv_device_details)
    TextView mTvDeviceDetails;

    @BindView(R.id.tv_highsetting)
    TextView mTvHighsetting;

    private ADMEHomeFragment admeHomeFragment;
    private QueryDataFragment queryDataFragment;        //查询数据
    private DeviceDetailsFragment deviceDetailsFragment;//设备详情
    private Fragment currentFragment;

    private BlueDeviceCommunicateUtil blueDeviceCommunicateUtil;

    private boolean isFirstEnter = true;

    private String SN = "";
    private String deviceInfo;
    private String macAddress;


    public static void startActivity(Context context, String deviceInfo) {
        Intent intent = new Intent(context, ConfigADMEActivity.class);
        intent.putExtra(Extras.CUR_DEVICE_NAME, deviceInfo);
        context.startActivity(intent);
    }

    public static void startActivity(Context context, String deviceInfo, String macAddress) {
        Intent intent = new Intent(context, ConfigADMEActivity.class);
        intent.putExtra(Extras.CUR_DEVICE_NAME, deviceInfo);
        intent.putExtra(Extras.DEVICE_MAC_ADDRESS, macAddress);
        context.startActivity(intent);
    }


    @Override
    protected int initContentView() {
        return R.layout.activity_config_adme;
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
        getIntentData();
        blueDeviceCommunicateUtil = BlueDeviceCommunicateUtil.getInstance();
        blueDeviceCommunicateUtil.init(this, SN, macAddress, this);
        blueDeviceCommunicateUtil.findAndConnectBleDevice();
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (!isFirstEnter) {
            blueDeviceCommunicateUtil.init(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isFirstEnter = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isFirstEnter = false;
    }

    private void initView(Bundle savedInstanceState) {
        mTvHighsetting.setVisibility(View.GONE);

        if (savedInstanceState != null) {  // “内存重启”时调用
            String curTag = savedInstanceState.getString("CurrentFragment");
            currentFragment = getSupportFragmentManager().findFragmentByTag(curTag);
            admeHomeFragment = (ADMEHomeFragment) getSupportFragmentManager().findFragmentByTag(ADMEHomeFragment.class.getName());
            queryDataFragment = (QueryDataFragment) getSupportFragmentManager().findFragmentByTag(QueryDataFragment.class.getName());
            deviceDetailsFragment = (DeviceDetailsFragment) getSupportFragmentManager().findFragmentByTag(DeviceDetailsFragment.class.getName());

            // 解决重叠问题
            getSupportFragmentManager().beginTransaction()
                    .hide(admeHomeFragment)
                    .hide(queryDataFragment)
                    .hide(deviceDetailsFragment)
                    .show(currentFragment)
                    .commit();
        } else {
            admeHomeFragment = new ADMEHomeFragment();
            queryDataFragment = new QueryDataFragment();
            deviceDetailsFragment = new DeviceDetailsFragment();
            setDefaultFragment();
        }
    }


    private void getIntentData() {
        Intent intent = getIntent();
        if (intent.getExtras().containsKey(Extras.DEVICE_MAC_ADDRESS)) {
            macAddress = intent.getStringExtra(Extras.DEVICE_MAC_ADDRESS);
        }

        if (intent.getExtras().containsKey(Extras.CUR_DEVICE_NAME)) {
            deviceInfo = intent.getStringExtra(Extras.CUR_DEVICE_NAME);
            String[] scanData = deviceInfo.split(",");
            SN = scanData[1];
        }
    }


    /**
     * set the default Fragment
     */
    private void setDefaultFragment() {
        switchFrgment(0);
        //set the defalut tab state
        setTabState(mTvParameter, R.drawable.szxd, getResources().getColor(R.color.colorPrimary));
    }


    @OnClick({R.id.back, R.id.tv_parameter, R.id.tv_query_data, R.id.tv_device_details})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;

            case R.id.tv_parameter:
                resetTabState();//reset the tab state
                setTabState(mTvParameter, R.drawable.szxd, getResources().getColor(R.color.colorPrimary));
                switchFrgment(0);
                break;

            case R.id.tv_query_data:
                resetTabState();//reset the tab state
                setTabState(mTvQueryData, R.drawable.yxzt, getResources().getColor(R.color.colorPrimary));
                switchFrgment(1);
                break;

            case R.id.tv_device_details:
                resetTabState();//reset the tab state
                setTabState(mTvDeviceDetails, R.drawable.xtgj, getResources().getColor(R.color.colorPrimary));
                switchFrgment(2);
                break;
        }
    }


    @Override
    public void obtainDeviceStateCmd() {
        if (MdBluetoothManager.getInstance() == null)
            return;

        //##7010，查询工作模式
        MdBluetoothManager.getInstance().writeMessage(new Message(UUID.randomUUID().toString(), "##7010\r\n", true));
        Timber.d("发送查询工作模式指令===" + "##7010");

        //##2001，查询服务器地址1
        MdBluetoothManager.getInstance().writeMessage(new Message(UUID.randomUUID().toString(), "##2001\r\n", true));
        Timber.d("发送查询服务器地址1指令===" + "##2001");

        //##2002，查询服务器地址2
        MdBluetoothManager.getInstance().writeMessage(new Message(UUID.randomUUID().toString(), "##2002\r\n", true));
        Timber.d("发送查询服务器地址2指令===" + "##2002");

        //##7000，查询采集器参数
        MdBluetoothManager.getInstance().writeMessage(new Message(UUID.randomUUID().toString(), "##7000\r\n", true));
        Timber.d("发送查询采集器参数指令===" + "##7000");

        //##7002，查询执行机构参数
        MdBluetoothManager.getInstance().writeMessage(new Message(UUID.randomUUID().toString(), "##7002\r\n", true));
        Timber.d("发送查询执行机构参数指令===" + "##7002");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BlueDeviceCommunicateUtil.REQUEST_ENABLE_BT:
                // 蓝牙已经开启
                if (resultCode != Activity.RESULT_OK) {
                    ToastUtils.show("蓝牙未启用");
                    return;
                }
                blueDeviceCommunicateUtil.findAndConnectBleDevice();
                break;
        }
    }


    /**
     * 是否切换连接模式
     */
    public void showChangeModle(String content) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(this)
                .title("温馨提示：")
                .content(content)
                .contentColor(Color.parseColor("#000000"))
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        blueDeviceCommunicateUtil.disconnectDevice();
                        ConfigADMEActivity.this.finish();
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }


    /**
     * revert the image color and text color to black
     */
    private void resetTabState() {
        setTabState(mTvParameter, R.drawable.szxd_wxz, getResources().getColor(R.color.font_main));
        setTabState(mTvQueryData, R.drawable.yxzt_wxz, getResources().getColor(R.color.font_main));
        setTabState(mTvDeviceDetails, R.drawable.xtgj_wxz, getResources().getColor(R.color.font_main));
        setTabState(mTvHighsetting, R.drawable.gjpz_wxz, getResources().getColor(R.color.font_main));
    }

    /**
     * set the tab state of bottom navigation bar
     *
     * @param textView the text to be shown
     * @param image    the image
     * @param color    the text color
     */
    private void setTabState(TextView textView, int image, int color) {
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, image, 0, 0);//Call requires API level 17
        textView.setTextColor(color);
    }

    /**
     * switch the fragment accordting to id
     */
    private void switchFrgment(int i) {
        switch (i) {
            case 0:
                showFragment(admeHomeFragment);
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
    public void onBackPressed() {
        if (!HandleBackUtil.handleBackPress(this)) {
            if (MCloudApp.isIsBluetoothDeviceConnected()) {
                showChangeModle(getResources().getString(R.string.finish_activity_disconnect_bluetooth_device));

            } else {
//                dismissLoadingDialog();
//                blueDeviceCommunicateUtil.disconnectDevice();
//                blueDeviceCommunicateUtil.isAutoConnectBlue = false;
//                MCloudApp.setIsBluetoothDeviceConnected(false);
                this.finish();
            }
        }
    }
}
