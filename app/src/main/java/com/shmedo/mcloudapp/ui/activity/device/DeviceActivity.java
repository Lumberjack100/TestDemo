package com.shmedo.mcloudapp.ui.activity.device;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.OnClick;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.shmedo.das.das.cmd.CommandManager;
import com.shmedo.das.das.cmd.CommandType;
import com.shmedo.das.utils.DesUtil;
import com.shmedo.das.utils.OnBytePackage;
import com.shmedo.das.utils.StringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.bluetooth.BluetoothDeviceFindEventData;
import com.shmedo.mcloudapp.bluetooth.BluetoothEvent;
import com.shmedo.mcloudapp.bluetooth.BluetoothEventHandler;
import com.shmedo.mcloudapp.bluetooth.MdBluetoothManager;
import com.shmedo.mcloudapp.bluetooth.Message;
import com.shmedo.mcloudapp.entity.ble.MDevice;
import com.shmedo.mcloudapp.util.ToastUtil;
import com.shmedo.mcloudapp.util.bleutil.ByteManagerUtil;
import com.shmedo.mcloudapp.util.bleutil.LogTag;
import com.shmedo.mcloudapp.views.LoadingDialog;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;



import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_CHARACTERISTICS_FIND_FAIL;
import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_CONNECT;
import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_DISCONNECTED;
import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_ENABLE_READ_FAIL;
import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_ENABLE_READ_SUCCESS;
import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_MESSAGE_WRITE_FAIL;
import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_MESSAGE_WRITE_SUCCESS;
import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_RECOVERY_SUCCESS;
import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_REQUEST_MTU_FAIL;
import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_SERVICE_FIND_FAIL;
import static com.shmedo.mcloudapp.util.bleutil.Constants.BT_WRITE_TIME_OUT;
import static com.shmedo.mcloudapp.util.bleutil.Constants.MESSAGE_LOCK_REBOOT_DEVICE;
import static com.shmedo.mcloudapp.util.bleutil.Constants.MESSAGE_RESPONSE_REBOOT_DEVICE;
import static com.shmedo.mcloudapp.util.bleutil.Constants.MESSAGE_RESPONSE_SAVE_SETTINGS_SUCCESS;
import static com.shmedo.mcloudapp.util.bleutil.Constants.MESSAGE_RESPONSE_TIME_OUT;
import static com.shmedo.mcloudapp.util.bleutil.Constants.REFRESH_RUN_STATE;
import static com.shmedo.mcloudapp.util.bleutil.Constants.VERIFY_RESULT;

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

    private MaterialDialog mMaterialDialog;
    private MaterialDialog.Builder mBuilder;
    private String deviceInfo = null;
    private LoadingDialog mLoadingDialog;
    private Handler hander;
    public static MdBluetoothManager mdBluetoothManager;
    public static BluetoothAdapter mBluetoothAdapter;
    private List<MDevice> list = new ArrayList<>();
    private String SN = "";
    //当前模式是否是蓝牙模式
    private boolean isBluModle = true;
    public static boolean isConneted = false;
    private Menu mMenu;
    //private static final int REQUEST_CONNECT_DEVICE = 1;
    //private static final int REQUEST_ENABLE_BT = 2;
    private String currentMessageId = "";

    @Override protected int initContentView() {
        return R.layout.activity_device;
    }


    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
        //initBluetooth();
    }


    private void initView() {
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");
        mToolbarTitle.setText("设备");
        mToolbar.setOnMenuItemClickListener(onMenuItemClick);
        mLoadingDialog = new LoadingDialog(this);
        hander = new Handler();
    }


    private void initData() {

        if (Objects.requireNonNull(getIntent().getExtras()).containsKey("device")) {
            String name = getIntent().getStringExtra("device");
            String deviceName = name.substring(3,name.length());
            deviceInfo = "MEDO,"+deviceName+",DAS";
            Log.i("adu", "从蓝牙列表跳转===" + deviceInfo);
        } else if (getIntent().getExtras().containsKey("inputDevice")) {
            deviceInfo = getIntent().getStringExtra("inputDevice");
            Log.i("adu", "手动输入-===" + deviceInfo);
        } else if (getIntent().getExtras().containsKey("ScanDevice")){
            deviceInfo = getIntent().getStringExtra("ScanDevice");
            Log.i("adu", "扫一扫-===" + deviceInfo);
        }
        String[] scanData = deviceInfo.split(",");
        scanResult(scanData);

    }


    private void scanResult(String[] scanData) {
        mTvDeviceName.setText("");
        mTvDeviceSn.setText(scanData[1]);
        SN = mTvDeviceSn.getText().toString();
        mTvDeviceModel.setText(scanData[2]);

        connectBluetooth();

    }


    public  void connectBluetooth() {
        if (isBluModle && !isConneted){
            disconnectDevice();
            initBluetooth();
            if (null != mBluetoothAdapter && mBluetoothAdapter.isEnabled()) {

                if (null != list && list.size() > 0) {
                    list.clear();
                }
                if (null == mLoadingDialog.getDialog() ||
                    !mLoadingDialog.getDialog().isShowing()) {
                    mLoadingDialog.showCancelDialog("正在获取附近的蓝牙设备...");
                    hander.postDelayed(dismssDialogRunnable, 10000);
                }
            }
        }else if (isBluModle && isConneted) {
            showChangeModle( getResources().getString(R.string.blue_model));
        } else {
            if (null != list && list.size() > 0) {
                list.clear();
            }
            initBluetooth();
        }
    }


    private Runnable dismssDialogRunnable = new Runnable() {
        @Override
        public void run() {
            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
                initBluetoothAdapter();
            }
            // disconnectDevice();
        }
    };
    private Runnable dismssConDialogRunnable = new Runnable() {
        @Override
        public void run() {
            if (mLoadingDialog != null) {
                mLoadingDialog.dismiss();
            }
        }
    };

    private void initBluetooth(){
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        MdBluetoothManager.init( mBluetoothAdapter, bluetoothManager);
        mdBluetoothManager = MdBluetoothManager.getInstance();
        mdBluetoothManager.setEventHandler(new MdBluetoothEventHandler());
        //if (mBluetoothAdapter == null || ! mBluetoothAdapter.isEnabled()) {
        //    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        //    startActivityForResult(enableBtIntent, 1);
        //}
         mdBluetoothManager.scanDevice(20, this);
    }

    //自动连接蓝牙
    private void initBluetoothAdapter() {

            //自动连接
            //int temp = 0;
            for (int i = 0; i < list.size(); i++) {
                if ((list.get(i).getDevice().getName()).contains(SN)) {
                     mdBluetoothManager.connectDevice(list.get(i).getDevice(), this);
                    if (null != mLoadingDialog) {
                        mLoadingDialog.showNoCancelDialog("正在连接蓝牙：" + SN);
                    }
                    hander.postDelayed(dismssConDialogRunnable, 20000);
                    break;

                }
                //else {
                //    temp++;
                //}
            }
            //if (temp == list.size()) {
            //    Intent serverIntent = new Intent(this, BlueToothListActivity.class);
            //    serverIntent.putExtra("devlist", (Serializable) list);
            //    startActivityForResult(serverIntent, REQUEST_ENABLE_BT);
            //}
         mdBluetoothManager.stopScan();
    }

    //扫码或者手动输入SN号进入设备页面
    //进行蓝牙认证，设备SN号与蓝牙SN匹配才会认证成功
    /**
     * 蓝牙连接成功开始进行验证  lock
     */
    private void startBluAuthenticate() {
        String com = "##224," + SN + ",0\r\n";
        Message msg = new Message(UUID.randomUUID().toString(), com, true);
        DeviceActivity.mdBluetoothManager.writeMessage(msg);
        Log.i(LogTag.INFO_TAG, "发送指令===" + com);

    }


    /**
     * 判断设备锁的状态
     */
    private void isLockStatus(){
        String com = "##224," + SN + ",2\r\n";
        Message msg = new Message(UUID.randomUUID().toString(), com, true);
        DeviceActivity.mdBluetoothManager.writeMessage(msg);
    }

    private class MyOnBytePackage implements OnBytePackage {
        @Override public void onPackageArrived(final byte[] data) {
            try {
                String str = new String(data, "utf-8");
                String deskey = "12345678";
                Log.i(LogTag.INFO_TAG, "反馈结果===" + str);

                String temp = str.replace("\r\n", "");
                String result[] = temp.split(",");
                Log.i(LogTag.INFO_TAG, "设备反馈结果的锁状态===" + result[result.length-1]);
                if (result[result.length-1].equals("lock")){
                    startBluAuthenticate();
                    return;
                }else if (result[result.length-1].equals("unlock")){
                    android.os.Message message = new android.os.Message();
                    message.what = MESSAGE_LOCK_REBOOT_DEVICE;
                    mHandler.sendMessage(message);
                    return;
                }
                if (str.startsWith("$$224") && str.endsWith("\r\n")) {
                    if (str.substring(0, str.length() - 2).equals("$$224ce")) {
                        startBluAuthenticate();//重新 认证
                    } else {
                        String[] strs = str.substring(0, str.length() - 2).split(",");
                        byte[] resultData = StringUtil.hexStringToBytes(strs[3]);
                        try {
                            String strdes = new String(DesUtil.decrypt(resultData, deskey),
                                "utf-8");
                            if (strdes.length() != 0) {
                                String desStr = StringUtil.bytesToHexString(DesUtil.encrypt(
                                    (StringUtil.reverseString(strdes.substring(0, 6)) +
                                        deskey).getBytes(), deskey));
                                String com = "##222," + SN + ",0," + desStr.toUpperCase() + "\r\n";
                                Message msg = new Message(UUID.randomUUID().toString(), com, true);
                                 mdBluetoothManager.writeMessage(msg);
                                Log.i(LogTag.INFO_TAG, "===-发送指令===" + com);
                                return;
                            }
                        } catch (Exception e) {
                            //CommonUtil.handlerException(MainActivity.this, e);
                        }
                    }
                } else if (str.startsWith("$$223")) {
                    String[] verifyReult = str.substring(0, str.length() - 2).split(",");
                    android.os.Message message = new android.os.Message();
                    message.what = VERIFY_RESULT;
                    message.obj = verifyReult[1];
                    mHandler.sendMessage(message);
                    Log.i(LogTag.INFO_TAG, "认证结果===" + verifyReult[1]);
                }
                //else if (str.startsWith("$$119")) {
                //
                //    mHandler.sendEmptyMessage(BT_RECOVERY_SUCCESS);
                //} else if (str.startsWith("$$0191")) {
                //    mHandler.sendEmptyMessage(MESSAGE_RESPONSE_SAVE_SETTINGS_SUCCESS);
                //}
                //else {
                //    parserResult(str);
                //}

            } catch (Exception ex) {
                Log.e(LogTag.ERROR_TAG, ex.getMessage(), ex);
            }

        }
    }


    private class MdBluetoothEventHandler implements BluetoothEventHandler {
        @Override
        public void handle(final BluetoothEvent event) {
            switch (event.getEventType()) {
                case DEVICE_FIND:
                    handleDeviceFind((BluetoothDeviceFindEventData) event.getEventData());
                    break;
                case CONNECTED: {
                    ByteManagerUtil.init(new MyOnBytePackage());
                    mHandler.sendEmptyMessage(BT_CONNECT);
                    break;
                }
                case DISCONNECTED:
                    mHandler.sendEmptyMessage(BT_DISCONNECTED);

                    break;
                case REQUEST_MTU_FAIL: {
                    Log.e(LogTag.ERROR_TAG, "MTU请求设置失败");
                    mHandler.sendEmptyMessage(BT_REQUEST_MTU_FAIL);
                    break;
                }
                case SERVICE_FIND_FAIL: {
                    Log.e(LogTag.ERROR_TAG, "蓝牙服务发现失败");
                    mHandler.sendEmptyMessage(BT_SERVICE_FIND_FAIL);
                    break;
                }
                case CHARACTERISTICS_FIND_FAIL: {
                    Log.e(LogTag.ERROR_TAG, "特征读取失败");
                    mHandler.sendEmptyMessage(BT_CHARACTERISTICS_FIND_FAIL);
                    break;
                }
                case ENABLE_READ_SUCCESS: {
                    Log.i(LogTag.INFO_TAG, "设置读取Descriptor成功");
                    mHandler.sendEmptyMessage(BT_ENABLE_READ_SUCCESS);
                    break;
                }
                case ENABLE_READ_FAIL: {
                    Log.e(LogTag.ERROR_TAG, "设置读取Descriptor失败");
                    mHandler.sendEmptyMessage(BT_ENABLE_READ_FAIL);
                    break;
                }
                case WRITE_TIME_OUT: {
                    Log.e(LogTag.ERROR_TAG, "写入等待超时");
                    disconnectDevice();
                    mHandler.sendEmptyMessage(BT_WRITE_TIME_OUT);
                    break;
                }
                case MESSAGE_WRITE_SUCCESS: {
                    Log.i(LogTag.INFO_TAG, "消息写入成功");
                    mHandler.sendEmptyMessage(BT_MESSAGE_WRITE_SUCCESS);
                    try {
                        currentMessageId = ((Message) event.getEventData()).getMessageID();
                        Log.i(LogTag.INFO_TAG, "消息id===" + ((Message) event.getEventData()).getMessageID());
                        String msg = ((Message) event.getEventData()).getResponseMessage();
                        byte[] data = (byte[]) msg.getBytes();

                        if (data != null && data.length > 0) {
                            ByteManagerUtil.getInstance().writeByte(data);
                        }

                    } catch (Exception ex) {
                        Log.e(LogTag.ERROR_TAG, ex.getMessage(), ex);
                    }

                    break;
                }
                case MESSAGE_RESPONSE_TIME_OUT:
                    Log.e(LogTag.ERROR_TAG, "消息等待响应超时");
                    mHandler.sendEmptyMessage(MESSAGE_RESPONSE_TIME_OUT);
                    disconnectDevice();
                    break;
                case MESSAGE_WRITE_FAIL: {
                    Log.e(LogTag.ERROR_TAG, "消息写入失败");
                    mHandler.sendEmptyMessage(BT_MESSAGE_WRITE_FAIL);
                    break;
                }
                case RESPONSE_WITH_NO_MESSAGE: {
                    try {
                        byte[] data = (byte[]) event.getEventData();
                        if (data != null && data.length > 0) {
                            ByteManagerUtil.getInstance().writeByte(data);
                        }

                    } catch (Exception ex) {
                        Log.e(LogTag.ERROR_TAG, ex.getMessage(), ex);
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }

    public Handler mHandler = new Handler(new Handler.Callback() {
        @Override public boolean handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case BT_CONNECT:
                    mMenu.findItem(R.id.current_blu)
                        .setIcon(R.drawable.bar_item_blu_connect_yellow);
                    Log.i(LogTag.INFO_TAG, "蓝牙连接成功");
                    if (mLoadingDialog != null) {
                        mLoadingDialog.dismiss();
                    }
                    isConneted = true;
                    ToastUtil.showSToast("蓝牙连接成功");
                    isLockStatus();
                    //startBluAuthenticate();//蓝牙连接成功开始进行验证
                    hander.removeCallbacks(dismssDialogRunnable);
                    hander.removeCallbacks(dismssConDialogRunnable);

                    break;
                case BT_DISCONNECTED:
                    if (isConneted) {
                        isConneted = false;
                        if (!isBluModle) {
                            mMenu.findItem(R.id.current_blu)
                                .setIcon(R.drawable.bar_item_offline);
                        } else {
                            mMenu.findItem(R.id.current_blu)
                                .setIcon(R.drawable.bar_item_bt);
                        }
                        Log.i(LogTag.INFO_TAG, "蓝牙连接已断开");
                        disconnectDevice();//非手动断开，清除蓝牙数据
                        ToastUtil.showSToast( "蓝牙连接已断开!");
                    }
                    break;
                case BT_MESSAGE_WRITE_SUCCESS:
                    ToastUtil.showSToast( "蓝牙发送指令成功");
                    break;
                case BT_MESSAGE_WRITE_FAIL:
                    ToastUtil.showSToast("蓝牙发送指令失败");
                    break;
                case BT_WRITE_TIME_OUT:
                    ToastUtil.showSToast("蓝牙发送指令超时");
                    break;
                case VERIFY_RESULT:
                    if (msg.obj.equals("1")) {
                        ToastUtil.showSToast( "蓝牙认证通过!");
                        //sendDeviceStateComd();
                    } else {
                        ToastUtil.showSToast( "蓝牙认证失败!");
                        try {
                            Thread.sleep(1000);
                            disconnectDevice();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                    break;
                case MESSAGE_RESPONSE_TIME_OUT:
                    ToastUtil.showSToast("消息等待响应超时！");
                    break;
                case REFRESH_RUN_STATE:
                    //loadWebView(runState);
                    break;
                case MESSAGE_RESPONSE_SAVE_SETTINGS_SUCCESS:
                    ToastUtil.showSToast("保存设置信息成功！");
                    //mWebView.loadUrl("javascript:restart()");
                    break;
                case BT_REQUEST_MTU_FAIL:
                    ToastUtil.showSToast("MTU请求设置失败！");
                    break;
                case BT_SERVICE_FIND_FAIL:
                    ToastUtil.showSToast("蓝牙服务发现失败！");
                    break;
                case BT_CHARACTERISTICS_FIND_FAIL:
                    ToastUtil.showSToast("蓝牙特征读取失败！");
                    break;
                case BT_ENABLE_READ_FAIL:
                    ToastUtil.showSToast("设置读取Descriptor失败！");
                    break;
                case BT_RECOVERY_SUCCESS:
                    ToastUtil.showSToast("恢复出厂设置成功！" );
                    break;
                case MESSAGE_RESPONSE_REBOOT_DEVICE:
                    ToastUtil.showSToast("重启系统成功！");
                    break;
                case MESSAGE_LOCK_REBOOT_DEVICE:
                    ToastUtil.showSToast("蓝牙通讯已就绪！");
                    break;
                default:
                    break;
            }

            return false;
        }
    });


    private void handleDeviceFind(BluetoothDeviceFindEventData eventData) {
        if (list.contains(eventData.getNewDevice()) ||
            eventData.getNewDevice().getDevice().getName() == null) {
            return;
        }
        if (null != list && list.size() > 0) {
            for (MDevice mDevice : list) {
                if (eventData.getNewDevice()
                    .getDevice()
                    .getName()
                    .equals(mDevice.getDevice().getName())) {
                    return;
                }
            }
        }
        list.add(eventData.getNewDevice());
    }



    /**
     * ble 取消连接
     */
    private void disconnectDevice() {
        //isShowingDialog = false;
        if (null != MdBluetoothManager.getInstance()) {
            MdBluetoothManager.getInstance().disconnect();
        }
    }
    @OnClick({ R.id.Rl_device_config, R.id.Rl_device_query, R.id.Rl_device_details })
    public void onViewClicked(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.Rl_device_config:
                if (isConneted){
                    intent = new Intent(DeviceActivity.this,ConfigDeviceParameterActivity.class);
                    intent.putExtra("deviceSN",SN);
                    startActivity(intent);
                }else {
                    connectBluetooth();
                }
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
        mMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }


    private Toolbar.OnMenuItemClickListener onMenuItemClick
        = new Toolbar.OnMenuItemClickListener() {

        @Override public boolean onMenuItemClick(MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.current_blu:

                    if (isConneted){
                        showChangeModle( getResources().getString(R.string.blue_model));
                    }else {
                        connectBluetooth();
                    }
                    break;
            }
            return true;
        }
    };

    /**
     * 是否切换连接模式
     */
    public  void showChangeModle( String content) {
        mBuilder = new MaterialDialog.Builder(this);
        mBuilder.title("温馨提示：")
            .content(content)
            .contentColor(Color.parseColor("#000000"))
            .canceledOnTouchOutside(false)
            .positiveText("确定")
            .negativeText("取消");
        mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
        mBuilder.onAny(new MaterialDialog.SingleButtonCallback() {
            @Override
            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                if (which == DialogAction.POSITIVE) {
                    isConneted = false;
                    disconnectDevice();
                    mMenu.findItem(R.id.current_blu).setIcon(R.drawable.bar_item_bt);
                    mMaterialDialog.dismiss();
                } else if (which == DialogAction.NEGATIVE) {
                    if (mBluetoothAdapter != null) {
                        mBluetoothAdapter.isEnabled();
                    }
                    mMaterialDialog.dismiss();
                }
            }
        });
    }
}
