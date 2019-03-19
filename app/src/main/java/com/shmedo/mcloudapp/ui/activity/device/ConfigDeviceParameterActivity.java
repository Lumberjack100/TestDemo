package com.shmedo.mcloudapp.ui.activity.device;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.shmedo.das.common.BaseConfigInfo;
import com.shmedo.das.common.GetAllSensorConfigInfo;
import com.shmedo.das.das.cmd.CommandManager;
import com.shmedo.das.das.cmd.CommandResult;
import com.shmedo.das.das.cmd.CommandType;
import com.shmedo.das.das.cmd.parser.ParseManager;
import com.shmedo.das.utils.OnBytePackage;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.bluetooth.Message;
import com.shmedo.mcloudapp.entity.ble.BaseConfigInfoSub;
import com.shmedo.mcloudapp.entity.ble.CollectorInfoSub;
import com.shmedo.mcloudapp.entity.ble.QueryOsmometerParameterSubInfo;
import com.shmedo.mcloudapp.entity.ble.SystemRunStateSub;
import com.shmedo.mcloudapp.entity.ble.VersionMessageSub;
import com.shmedo.mcloudapp.ui.activity.device.config.DisplacementConfigActivity;
import com.shmedo.mcloudapp.ui.activity.device.config.GeneralSettingActivity;
import com.shmedo.mcloudapp.ui.activity.device.config.OsmometerConfigActivity;
import com.shmedo.mcloudapp.ui.activity.device.config.RainConfigActivity;
import com.shmedo.mcloudapp.ui.activity.device.config.SeniorSettingActivity;
import com.shmedo.mcloudapp.util.StringUtil;
import com.shmedo.mcloudapp.util.ToastUtil;
import com.shmedo.mcloudapp.util.bleutil.BlueResultParserUtil;
import com.shmedo.mcloudapp.util.bleutil.ByteManagerUtil;
import com.shmedo.mcloudapp.util.bleutil.LogTag;
import com.shmedo.mcloudapp.views.LoadingDialog;
import java.util.Objects;
import java.util.UUID;

import static com.shmedo.das.das.cmd.CommandType.BASE_CONFIG;
import static com.shmedo.das.das.cmd.CommandType.GET_ALL_SENSOR_CONFIG;
import static com.shmedo.das.das.cmd.CommandType.QUERY_OSMOMETER_PARAMETER;
import static com.shmedo.das.das.cmd.CommandType.SYSTEM_RUN_STATE;
import static com.shmedo.mcloudapp.ui.activity.device.DeviceActivity.isConneted;
import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_CONFIG_DEVICE;
import static com.shmedo.mcloudapp.util.bleutil.Constants.MESSAGE_BASE_CONFIG;
import static com.shmedo.mcloudapp.util.bleutil.Constants.MESSAGE_GET_ALL_SENSOR_CONFIG;
import static com.shmedo.mcloudapp.util.bleutil.Constants.MESSAGE_SYSTEM_RUN_STATE;
import static com.shmedo.mcloudapp.util.bleutil.Constants.MESSAGE_VERSION_MESSAGE;

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
    @BindView(R.id.sp_debug) Spinner mSpDebug;
    @BindView(R.id.sw_sim_A) Switch mSwSimA;
    @BindView(R.id.tv_sim_A) TextView mTvSimA;
    @BindView(R.id.tv_sim_1) TextView mTvSim1;

    @BindView(R.id.sw_sim_B) Switch mSwSimB;
    @BindView(R.id.tv_sim_B) TextView mTvSimB;
    @BindView(R.id.tv_sim_2) TextView mTvSim2;

    @BindView(R.id.sw_rain) Switch mSwRain;
    @BindView(R.id.tv_rain_config) TextView mTvRainConfig;
    @BindView(R.id.sw_osmometer) Switch mSwOsmometer;
    @BindView(R.id.tv_osmometer_config) TextView mTvOsmometerConfig;
    @BindView(R.id.sw_sensor) ImageView mSwSensor;
    @BindView(R.id.rl_sensor_setting) RelativeLayout mRlSensorSetting;
    @BindView(R.id.rl_general_setting) RelativeLayout mRlGeneralSetting;
    @BindView(R.id.rl_senior_setting) RelativeLayout mRlSeniorSetting;
    @BindView(R.id.sw_device_luck_state) Switch mSwDeviceLuckState;
    @BindView(R.id.tv_device_luck_state) TextView mTvDeviceLuckState;

    private BaseConfigInfoSub mInfoSub = new BaseConfigInfoSub();
    private SystemRunStateSub mStateInfoSub = new SystemRunStateSub();
    private QueryOsmometerParameterSubInfo mFuncSubInfo;
    private VersionMessageSub mVersionSub;
    private GetAllSensorConfigInfo mAllSensorConfigInfo;
    private CollectorInfoSub mCollectorInfoSub;


    private Menu mMenu;

    private String SN;
    private LoadingDialog mLoadingDialog;
    private MaterialDialog.Builder mBuilder;
    private MaterialDialog mMaterialDialog;
    private Handler handler;
    private Runnable dismssConDialogRunnable = new Runnable() {
        @Override
        public void run() {
            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }
            //setResultData();
        }
    };


    @Override protected int initContentView() {
        return R.layout.activity_config_device_parameter;
    }


    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
        sendBluetoothComd();

    }


    private void initView() {
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        mToolbarTitle.setText("设备详情");
        mToolbar.setOnMenuItemClickListener(onMenuItemClick);
        SN = getIntent().getStringExtra("deviceSN");
        mLoadingDialog = new LoadingDialog(this);
        handler = new Handler();
    }


    private void initData() {
        if (mTvLock.getText().equals("已锁定")) {
            mIvLock.setBackground(getResources().getDrawable(R.drawable.icon_close_lock));
            //mSpProjectName.setClickable(false);
            //mSpProjectName.setFocusable(false);
            //mSpProjectName.setFocusableInTouchMode(false);
        } else if (mTvLock.getText().equals("已解锁")) {
            //mSpProjectName.setFocusable(true);
            //mSpProjectName.setFocusableInTouchMode(true);
            mIvLock.setBackground(getResources().getDrawable(R.drawable.icon_open_lock));
        }
        //蓝牙连接状态
        String[] dataSize = getResources().getStringArray(R.array.project_name);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, R.layout.spinner_item,
            dataSize);
        mSpProjectName.setAdapter(spinnerAdapter);
        mSpProjectName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                ToastUtil.showSToast("" + position);
            }


            @Override public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        //调试模式
        String[] debugData = getResources().getStringArray(R.array.bluetooth_debug);
        ArrayAdapter<String> debugDataAdapter = new ArrayAdapter<>(this, R.layout.spinner_item,
            debugData);
        mSpDebug.setAdapter(debugDataAdapter);
        mSpDebug.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                ToastUtil.showSToast("" +adapterView.getSelectedItem().toString());
            }
            @Override public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //蓝牙连接状态
        mSwBluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    mTvBluetooth.setText("已连接");

                } else {
                    mTvBluetooth.setText("未连接");
                }
            }
        });

        //设备启用状态
        mSwDeviceState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                if (!b) {
                    mBuilder = new MaterialDialog.Builder(ConfigDeviceParameterActivity.this);
                    mBuilder.title("温馨提示：")
                        .content("关闭系统激活状态，将导致设备自动关机进入休眠状态。请确认是否关闭【激活状态】")
                        .contentColor(Color.parseColor("#000000"))
                        .canceledOnTouchOutside(false)
                        .positiveText("确定")
                        .negativeText("取消");
                    mMaterialDialog = mBuilder.build();
                    mMaterialDialog.show();
                    mBuilder.onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            //发送待机指令
                            Message msg = new Message("chat","##0181\r\n",true);
                            if (DeviceActivity.mdBluetoothManager!=null){
                                DeviceActivity.mdBluetoothManager.writeMessage(msg);
                            }
                            mTvDeviceState.setText("已待机");
                            mSwDeviceState.setChecked(false);

                        }
                    });
                    mBuilder.onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(
                            @NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            mTvDeviceState.setText("已激活");
                            mSwDeviceState.setChecked(true);
                        }
                    });
                } else {
                    //发送激活指令
                    Message msg = new Message("chat","##0182\r\n",true);
                    if (DeviceActivity.mdBluetoothManager!=null){
                        DeviceActivity.mdBluetoothManager.writeMessage(msg);
                    }
                    mTvDeviceState.setText("已激活");
                    mSwDeviceState.setChecked(true);
                }
            }
        });
        //设备锁定状态
        mSwDeviceLuckState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    mTvDeviceLuckState.setText("已激活");
                } else {
                    mTvDeviceLuckState.setText("未激活");
                }
            }
        });

        //SIM卡A功能
        mSwSimA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    mTvSimA.setText("已开启");
                } else {
                    mTvSimA.setText("已关闭");
                }
            }
        });
        //SIM卡B功能
        mSwSimB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    mTvSimB.setText("已开启");
                } else {
                    mTvSimB.setText("已关闭");
                }
            }
        });
        if (mSwRain.isChecked()) {
            mTvRainConfig.setText("配置");
            mTvRainConfig.setClickable(true);
            mTvRainConfig.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
        } else {
            mTvRainConfig.setBackgroundColor(getResources().getColor(R.color.secondary_text));
            mTvRainConfig.setText("已停用");
            mTvRainConfig.setClickable(false);
        }
        //雨量计功能
        mSwRain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    mTvRainConfig.setText("配置");
                    mTvRainConfig.setClickable(true);
                    mTvRainConfig.setBackgroundColor(
                        getResources().getColor(R.color.colorPrimaryDark));
                } else {
                    mTvRainConfig.setBackgroundColor(
                        getResources().getColor(R.color.secondary_text));
                    mTvRainConfig.setText("已停用");
                    mTvRainConfig.setClickable(false);
                }
            }
        });
        if (mSwOsmometer.isChecked()) {
            mTvOsmometerConfig.setText("配置");
            mTvOsmometerConfig.setClickable(true);
            mTvOsmometerConfig.setBackgroundColor(
                getResources().getColor(R.color.colorPrimaryDark));
        } else {
            mTvOsmometerConfig.setBackgroundColor(getResources().getColor(R.color.secondary_text));
            mTvOsmometerConfig.setText("已停用");
            mTvOsmometerConfig.setClickable(false);
        }
        mSwOsmometer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    mTvOsmometerConfig.setBackgroundColor(
                        getResources().getColor(R.color.colorPrimaryDark));
                    mTvOsmometerConfig.setText("配置");
                    mTvOsmometerConfig.setClickable(true);
                } else {
                    mTvOsmometerConfig.setBackgroundColor(
                        getResources().getColor(R.color.secondary_text));
                    mTvOsmometerConfig.setText("已停用");
                    mTvOsmometerConfig.setClickable(false);
                }
            }
        });
    }


    private void setResultData() {
        if (mInfoSub != null){
            if (mInfoSub.getEquipmentStatus().equals("待机")) {
                mSwDeviceState.setChecked(false);
                mTvDeviceState.setText("已待机");
            } else if (mInfoSub.getEquipmentStatus().equals("激活")) {
                mSwDeviceState.setChecked(true);
                mTvDeviceState.setText("已激活");
            }

            if (mInfoSub.getDebugModel().equals("DEBUG")){
                mSpDebug.setSelection(0);
            }else if (mInfoSub.getDebugModel().equals("INFO")){
                mSpDebug.setSelection(1);
            }

            if (mInfoSub.getSimChoose().equals("选择sim卡1")){
                mSwSimA.setChecked(true);
                mSwSimB.setChecked(false);
            }else if (mInfoSub.getSimChoose().equals("选择sim卡2")){
                mSwSimA.setChecked(false);
                mSwSimB.setChecked(true);
            }
        }
        Log.i(LogTag.INFO_TAG, "=======setResultData======" + mStateInfoSub.toString());
        if (mStateInfoSub != null) {
            mTvSim1.setText("=="+mStateInfoSub.getSimCCID());
        }
    }


    private void sendBluetoothComd() {
        if (isConneted) {
            mLoadingDialog.showNoCancelDialog("正在加载数据...");
            ByteManagerUtil.init(new MyOnBytePackage());
            mHandler.sendEmptyMessage(BT_CONFIG_DEVICE);
            mTvBluetooth.setText("已连接");
            mSwBluetooth.setChecked(true);
        }else {
            mSwBluetooth.setChecked(false);
            mTvBluetooth.setText("未连接");
        }
    }


    private class MyOnBytePackage implements OnBytePackage {

        @Override public void onPackageArrived(byte[] data) {
            //发送指令后反馈的结果
            try {
                handler.postDelayed(dismssConDialogRunnable, 20000);
                String str = new String(data, "utf-8");
                //Log.i(LogTag.INFO_TAG, "配置参数页面反馈结果===" + str);
                parserResult(str);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    Handler mHandler = new Handler(new Handler.Callback() {

        @Override public boolean handleMessage(android.os.Message message) {
            switch (message.what) {
                case BT_CONFIG_DEVICE:
                    sendDeviceStateComd();
                    break;
                case MESSAGE_BASE_CONFIG:
                    //更新基础配置信息界面
                    //Log.i(LogTag.INFO_TAG, "=======更新基础配置信息界面======" + mInfoSub.getEquipmentStatus());
                    String result  = (String) message.obj;
                    //String result  = "$$000,18A095-L,0,455872,2,1,1,5400,10,100,0,9600,9600,02,3,6,3,1,2,1\r\n";
                    mInfoSub = BlueResultParserUtil.getBaseConfig(result);

                    //CommandResult<BaseConfigInfo> baseBean = ParseManager.getInstance().parse(result);
                    Log.i(LogTag.INFO_TAG, "获取基础配置信息===" + mInfoSub.toString());
                    setResultData();
                    break;
                case MESSAGE_SYSTEM_RUN_STATE:
                    //系统运行状态
                    Log.i(LogTag.INFO_TAG, "=======系统运行状态======" + mStateInfoSub.getSimCCID());
                    if (mStateInfoSub != null) {
                        mTvSim1.setText("=="+mStateInfoSub.getSimCCID());
                    }

                    break;
                case MESSAGE_VERSION_MESSAGE:
                    //获取版本信息

                    break;
                case MESSAGE_GET_ALL_SENSOR_CONFIG:
                    //获取所有配置信息

                    break;

            }
            return false;
        }
    });


    /**
     * 发送蓝牙请求设备信息指令
     */
    private void sendDeviceStateComd() {
        if (isConneted) {
            //获取所有配置
            //final String allInfoCommand = CommandManager.getInstance().getCommand(GET_ALL_SENSOR_CONFIG, null);
            //获取基础信息配置
            String baseConfigInfoCommand = CommandManager.getInstance().getCommand(BASE_CONFIG, null);
            //运行状态
            String runstateCommand = CommandManager.getInstance().getCommand(SYSTEM_RUN_STATE, null);
            //渗压计开关
            //String shenyajiCommand = CommandManager.getInstance().getCommand(QUERY_OSMOMETER_PARAMETER, null);
            //版本信息
            //String versionCommand = CommandManager.getInstance().getCommand(CommandType.VERSION_MESSAGE, null);

            //String serverCommand = CommandManager.getInstance().getCommand(CommandType.SERVER_ADDRESS, null);
            if (DeviceActivity.mdBluetoothManager != null) {
                DeviceActivity.mdBluetoothManager.writeMessage(new Message(UUID.randomUUID().toString(), baseConfigInfoCommand, true));
                Log.i(LogTag.INFO_TAG, "baseConfigInfoCommand 发送指令===" + baseConfigInfoCommand);
                DeviceActivity.mdBluetoothManager.writeMessage(new Message(UUID.randomUUID().toString(), runstateCommand, true));
                Log.i(LogTag.INFO_TAG, "runstateCommand 发送指令===" + runstateCommand);
                //DeviceActivity.mdBluetoothManager.writeMessage(new Message(UUID.randomUUID().toString(), shenyajiCommand, true));
                //Log.i(LogTag.INFO_TAG, "shenyajiCommand 发送指令===" + shenyajiCommand);
                //DeviceActivity.mdBluetoothManager.writeMessage(new Message(UUID.randomUUID().toString(), versionCommand, true));
                //Log.i(LogTag.INFO_TAG, "versionCommand 发送指令===" + versionCommand);
            }

        } else {
            if (!isConneted) {
                ToastUtil.showSToast("蓝牙未连接");
            }
        }

    }


    /**
     * 得到数据，加载视图
     */
    private void parserResult(final String result) {
        Log.i(LogTag.INFO_TAG, "----------------------得到数据，加载视图------------------"+result);
        if (result.equals("Please verify the equipment.\n")) {
            android.os.Message message = new android.os.Message();
            message.what = BT_CONFIG_DEVICE;
            message.obj = "0";
            mHandler.sendMessage(message);
        } else {
            try {
                CommandType type = com.shmedo.das.utils.StringUtil.extractCommandType(result);
                switch (type) {
                    case BASE_CONFIG:{
                        // 获取基础配置信息
                        mInfoSub = BlueResultParserUtil.getBaseConfig(result);
                        Log.i(LogTag.INFO_TAG, "获取基础配置信息===" + mInfoSub.toString());
                        //mHandler.sendEmptyMessage(MESSAGE_BASE_CONFIG);
                        android.os.Message message = new android.os.Message();
                        message.what = MESSAGE_BASE_CONFIG;
                        message.obj = result;
                        mHandler.sendMessage(message);
                        break;
                    }

                    case SYSTEM_RUN_STATE:{
                        //系统运行状态
                        mStateInfoSub = BlueResultParserUtil.getSystemRunState(result);
                        Log.i(LogTag.INFO_TAG, "系统运行状态===" + mStateInfoSub.toString());
                        mHandler.sendEmptyMessage(MESSAGE_SYSTEM_RUN_STATE);
                        break;
                    }
                    //case VERSION_MESSAGE:{
                    //    //获取版本信息
                    //    mVersionSub = BlueResultParserUtil.getVersionMessage(result);
                    //    Log.i(LogTag.INFO_TAG, "获取版本信息===" + mVersionSub.toString());
                    //    //mHandler.sendEmptyMessage(MESSAGE_VERSION_MESSAGE);
                    //    break;
                    //}
                    //case QUERY_OSMOMETER_PARAMETER:{
                    //    //查询数字式渗压计参数
                    //    mFuncSubInfo = BlueResultParserUtil.getQueryOsmometerParameter(result);
                    //    Log.i(LogTag.INFO_TAG, "查询数字式渗压计参数===" + mFuncSubInfo.toString());
                    //    break;
                    //}
                    //case GET_ALL_SENSOR_CONFIG: {
                    //    获取所有采集器配置
                        //mAllSensorConfigInfo = BlueResultParserUtil.getAllBlueMessage(result);
                        //Log.i(LogTag.INFO_TAG, "获取所有采集器配置===" + mAllSensorConfigInfo.toString());
                        //if (mAllSensorConfigInfo != null) {
                        //    采集器信息
                            //mCollectorInfoSub = BlueResultParserUtil.getCollectorInfo(
                            //    mAllSensorConfigInfo);
                        //}
                        //基础信息
                        //mInfoSub = BlueResultParserUtil.getBasicFromAllBlueMessage(result);

                        //mHandler.sendEmptyMessage(MESSAGE_GET_ALL_SENSOR_CONFIG);
                        //rainPage=new SetRainPage.SetRainPageParameter();
                        //if (mAllSensorConfigInfo != null) {
                        //    switch (mAllSensorConfigInfo.getBaseConfig().getRainfallStation())
                        //    {
                        //        case RAIN_OPEN:
                        //            rainPage.setRainSelect(true);
                        //            break;
                        //        case RAIN_CLOSE:
                        //            rainPage.setRainSelect(false);
                        //            break;
                        //    }
                        //    rainPage.setRainAccury(String.valueOf(mAllSensorConfigInfo.getBaseConfig().getRainAccuracy()/100));
                        //}
                        //switch (mAllSensorConfigInfo.getBaseConfig().getCollectorModel().name()) {
                        //    case "DS08":
                        //        collectorType = "02";
                        //        break;
                        //}
                        //break;
                    //}
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @OnClick({ R.id.iv_lock, R.id.tv_query, R.id.rl_sensor_setting, R.id.rl_general_setting,
                 R.id.rl_senior_setting, R.id.tv_rain_config, R.id.tv_osmometer_config })
    public void onViewClicked(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.iv_lock:
                if (mTvLock.getText().equals("已锁定")) {
                    mTvLock.setText("已解锁");
                    //mSpProjectName.setClickable(true);
                    mIvLock.setBackground(getResources().getDrawable(R.drawable.icon_open_lock));
                    //mSpProjectName.setFocusable(true);
                    //mSpProjectName.setFocusableInTouchMode(true);
                } else if (mTvLock.getText().equals("已解锁")) {
                    //mSpProjectName.setClickable(false);
                    mTvLock.setText("已锁定");
                    mIvLock.setBackground(getResources().getDrawable(R.drawable.icon_close_lock));
                    //mSpProjectName.setFocusable(false);
                    //mSpProjectName.setFocusableInTouchMode(false);
                }
                break;
            case R.id.tv_query:
                if (StringUtil.isNullOrEmpty(mEtQuery.getText().toString().trim())) {
                    ToastUtil.showSToast("" + mEtQuery.getText().toString());
                } else {
                    ToastUtil.showSToast("搜索的内容不能为空");
                }
                break;
            case R.id.tv_rain_config:
                intent = new Intent(ConfigDeviceParameterActivity.this, RainConfigActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_osmometer_config:
                intent = new Intent(ConfigDeviceParameterActivity.this,
                    OsmometerConfigActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_sensor_setting:
                intent = new Intent(ConfigDeviceParameterActivity.this,
                    DisplacementConfigActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_general_setting:
                intent = new Intent(ConfigDeviceParameterActivity.this,
                    GeneralSettingActivity.class);
                startActivity(intent);
                break;
            case R.id.rl_senior_setting:
                intent = new Intent(ConfigDeviceParameterActivity.this,
                    SeniorSettingActivity.class);
                startActivity(intent);
                break;

        }
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_menu, menu);
        mMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }


    private Toolbar.OnMenuItemClickListener onMenuItemClick
        = new Toolbar.OnMenuItemClickListener() {

        @Override public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.current_blu:

                    if (isConneted){
                      ToastUtil.showSToast("当前蓝牙已连接");
                    }else {
                        //connectBluetooth();
                        ToastUtil.showSToast("当前蓝牙已断开");
                    }
                    break;
            }
            return true;
        }
    };
}
