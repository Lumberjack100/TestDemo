package com.shmedo.mcloudapp.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;

import com.hjq.toast.ToastUtils;
import com.shmedo.das.das.cmd.CommandManager;
import com.shmedo.das.das.cmd.CommandType;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.model.Extras;
import com.shmedo.mcloudapp.ui.activity.device.BaseDeviceConnectActivity;
import com.shmedo.mcloudapp.ui.fragment.AdvanceSetFragment;
import com.shmedo.mcloudapp.ui.fragment.DAGHomeFragment;
import com.shmedo.mcloudapp.ui.fragment.DeviceDetailsFragment;
import com.shmedo.mcloudapp.ui.fragment.QueryDataFragment;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

public class ConfigDAGActivity extends BaseDeviceConnectActivity {
    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    @BindView(R.id.tv_save)
    TextView mTvSave;

    @BindView(R.id.tv_parameter)
    TextView mTvParameter;

    @BindView(R.id.tv_query_data)
    TextView mTvQueryData;

    @BindView(R.id.tv_device_details)
    TextView mTvDeviceDetails;

    @BindView(R.id.tv_highsetting)
    TextView mTvHighsetting;

    private DAGHomeFragment dagHomeFragment;
    private QueryDataFragment queryDataFragment;        //查询数据
    private DeviceDetailsFragment deviceDetailsFragment;//设备详情
    private AdvanceSetFragment advanceSetFragment;      //高级设置
    private Fragment currentFragment;

    private boolean isFirstCall = true;


    public static void startActivity(Context context, String deviceInfo) {
        Intent intent = new Intent(context, ConfigDAGActivity.class);
        intent.putExtra(Extras.CUR_DEVICE_NAME, deviceInfo);
        context.startActivity(intent);
    }


    @Override
    protected int initContentView() {
        return R.layout.activity_config_dag;
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
            findAndConnectBleDevice();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        isFirstCall = false;
    }

    private void initView(Bundle savedInstanceState) {
        mToolbarTitle.setText("参数设置");
        mTvSave.setVisibility(View.VISIBLE);

        if (savedInstanceState != null) {  // “内存重启”时调用
            String curTag = savedInstanceState.getString("CurrentFragment");
            currentFragment = getSupportFragmentManager().findFragmentByTag(curTag);
            dagHomeFragment = (DAGHomeFragment) getSupportFragmentManager().findFragmentByTag(DAGHomeFragment.class.getName());
            queryDataFragment = (QueryDataFragment) getSupportFragmentManager().findFragmentByTag(QueryDataFragment.class.getName());
            deviceDetailsFragment = (DeviceDetailsFragment) getSupportFragmentManager().findFragmentByTag(DeviceDetailsFragment.class.getName());
            advanceSetFragment = (AdvanceSetFragment) getSupportFragmentManager().findFragmentByTag(AdvanceSetFragment.class.getName());

            // 解决重叠问题
            getSupportFragmentManager().beginTransaction()
                    .hide(dagHomeFragment)
                    .hide(queryDataFragment)
                    .hide(deviceDetailsFragment)
                    .hide(advanceSetFragment)
                    .show(currentFragment)
                    .commit();
        } else {
            dagHomeFragment = new DAGHomeFragment();
            queryDataFragment = new QueryDataFragment();
            deviceDetailsFragment = new DeviceDetailsFragment();
            advanceSetFragment = new AdvanceSetFragment();
            setDefaultFragment();
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


    @OnClick({R.id.back, R.id.tv_save, R.id.tv_parameter, R.id.tv_query_data, R.id.tv_device_details, R.id.tv_highsetting})
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
                sendSaveParamCommand();
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

            case R.id.tv_highsetting:
                resetTabState();//reset the tab state
                setTabState(mTvHighsetting, R.drawable.gjpz, getResources().getColor(R.color.colorPrimary));
                switchFrgment(3);
                break;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // 蓝牙已经开启
                if (resultCode != Activity.RESULT_OK) {
                    ToastUtils.show("蓝牙未启用");
                    return;
                }
                findAndConnectBleDevice();
                break;
        }
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
                showFragment(dagHomeFragment);
                break;

            case 1:
                showFragment(queryDataFragment);
                break;

            case 2:
                showFragment(deviceDetailsFragment);
                break;

            case 3:
                showFragment(advanceSetFragment);
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
    protected void obtainDeviceStateCmd() {
        //获取所有配置  ##333
        String allInfoCommand = CommandManager.getInstance().getCommand(CommandType.GET_ALL_SENSOR_CONFIG, null);
        sendCommonCommand(allInfoCommand);
        Timber.d("发送获取所有配置指令===" + allInfoCommand);

        //系统运行状态 ##014
//        String runstateCommand = CommandManager.getInstance().getCommand(CommandType.SYSTEM_RUN_STATE, null);
//        sendCommonCommand(runstateCommand);
//        Timber.d("发送系统运行状态指令===" + runstateCommand);

        //查询数字式渗压计参数 ##400
        String shenyajiCommand = CommandManager.getInstance().getCommand(CommandType.QUERY_OSMOMETER_PARAMETER, null);
        sendCommonCommand(shenyajiCommand);
        Timber.d("发送查询渗压计指令===" + shenyajiCommand);

        //版本信息 ##040
//        String versionCommand = CommandManager.getInstance().getCommand(CommandType.VERSION_MESSAGE, null);
//        sendCommonCommand(versionCommand);
//        Timber.d("发送版本信息指令===" + versionCommand);
    }


    @Override
    protected void parserResult(String cmdStr) {
        //查询数字式渗压计参数
        if (cmdStr.startsWith("$$400") && cmdStr.endsWith("\r\n")) {
            dismissLoadingDialog();
            hander.removeCallbacks(dismssDialogRunnable);
        }

        //恢复出厂设置应答
        if (cmdStr.startsWith("$$119") && cmdStr.endsWith("\r\n")) {

        }

        //重启
        if (cmdStr.startsWith("$$008") && cmdStr.endsWith("\r\n")) {

        }

        //此处是各个配置指令应答，表示已经更改配置了
        if ((cmdStr.startsWith("$$006")//调试模式
                || cmdStr.startsWith("$$005")//开关量功能
                || cmdStr.startsWith("$$121")//雨量计精度
                || cmdStr.startsWith("$$227")//断线报警器
                || (cmdStr.startsWith("$$40") && !cmdStr.equals("$$400\r\n"))//设置数字渗压计
                || cmdStr.startsWith("$$150")//采集器接入的传感器
                || cmdStr.startsWith("$$16")//采集器
                || cmdStr.startsWith("$$147")//采集器地址
                || cmdStr.startsWith("$$201")//平台服务器地址端口
                || (cmdStr.startsWith("$$202") && !cmdStr.equals("$$2020\r\n"))//网络链路通信协议
                || (cmdStr.startsWith("$$810") && !cmdStr.equals("$$8100\r\n"))//自动注册平台选择
                || cmdStr.startsWith("$$803")//自动注册平台参数
                || cmdStr.startsWith("$$807")//自动注册服务器地址端口
                || cmdStr.startsWith("$$805")//手动注册平台参数
                || (cmdStr.startsWith("$$809") && !cmdStr.equals("$$8090\r\n"))//MQTT KeepAlive 值
        ) && cmdStr.endsWith("\r\n")) {
            isConfigChange = true;
        }

        //设置保存参数应答
        if (cmdStr.startsWith("$$0191") && cmdStr.endsWith("\r\n")) {
            ToastUtils.show("已发送保存命令,设备即将断开连接重启");
            isConfigChange = false;
            hander.removeCallbacks(dismssDialogRunnable);
            dismissLoadingDialog();
            disconnectDevice();

            if (isExitMode) {
                hander.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ConfigDAGActivity.this.finish();
                    }
                }, 3000);
            }
        }

        EventBus.getDefault().post(cmdStr);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        MCloudApp.setCurDeviceToken(null);
        MCloudApp.setCurDeviceMacAddr(null);
    }
}
