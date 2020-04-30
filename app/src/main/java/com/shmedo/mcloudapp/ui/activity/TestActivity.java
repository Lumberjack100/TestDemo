package com.shmedo.mcloudapp.ui.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.hjq.toast.ToastUtils;
import com.shmedo.core.cmd.CommandManager;
import com.shmedo.core.cmd.CommandResult;
import com.shmedo.core.cmd.entity.AuthenticationConfigEntity;
import com.shmedo.core.enums.CommandType;
import com.shmedo.core.interfaces.OnBytePackage;
import com.shmedo.core.utils.DesUtil;
import com.shmedo.core.utils.StringUtil;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseActivity;
import com.shmedo.mcloudapp.bluetooth.BluetoothEvent;
import com.shmedo.mcloudapp.bluetooth.BluetoothEventHandler;
import com.shmedo.mcloudapp.bluetooth.MdBluetoothManager;
import com.shmedo.mcloudapp.bluetooth.Message;
import com.shmedo.mcloudapp.entity.event.BluetoothStateEvent;
import com.shmedo.mcloudapp.util.bleutil.ByteManagerUtil;
import com.shmedo.mcloudapp.util.bleutil.Constants;

import org.greenrobot.eventbus.EventBus;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity
 * 创建者:   gonghe
 * 创建时间:  2020-02-28
 * 描述：    TODO
 */
public class TestActivity extends BaseActivity {
    /**
     * 说明：启动Activity
     * <p>
     * 注意：这里使用到了Intent的Flag属性singleTop。singleTop模式下，在同一个task中，如果存在该Activity的实例，
     * 并且该Activity实例位于栈顶(即，该Activity位于前端)，则调用startActivity()时，不再创建该Activity的示例；
     * 而仅仅只是调用Activity的onNewIntent()。否则的话，则新建该Activity的实例，并将其置于栈顶。
     * </p>
     */
    public static void startActivity(Context context) {
        Intent intent = new Intent(context, TestActivity.class);
        context.startActivity(intent);
    }


    @BindView(R.id.tv_title)
    TextView mToolbarTitle;

    @BindView(R.id.tv_log)
    EditText mTvLog;

    public static final int REQUEST_ENABLE_BT = 0x001;

    public static final int CONNECT_DELAY_MILLIS = 20000;

    private MdBluetoothManager mdBluetoothManager;

    private BluetoothAdapter mBluetoothAdapter;

    private MdBluetoothEventHandler mdBluetoothEventHandler = new MdBluetoothEventHandler();

    protected Handler hander = new Handler();

    protected String errMsg = "";

    protected String SN = MCloudApp.getCurDeviceToken();

    private String macAddress = MCloudApp.getCurDeviceMacAddr();

    private ProgressRunnable progressRunnable;

    private boolean isFirstCall = true;

    private boolean isAutoConnectBlue = true;//是否自动连接蓝牙

    private boolean isConnSucceed = false;

    private StringBuilder stringBuilder = new StringBuilder();

    private int totalConnNum = 0;

    private int connSuccessNum = 0;

    private int varifySuccessNum = 0;

    private int varifyFailedNum = 0;

    private Runnable runnableConn = new Runnable() {
        @Override
        public void run() {
            connectBleDevice();
        }
    };

    private class ProgressRunnable implements Runnable {
        @Override
        public void run() {
            dismissLoadingDialog();
            progressRunnable = null;

            if (!TextUtils.isEmpty(errMsg)) {
                ToastUtils.show(errMsg);

                if (errMsg.contains("连接超时")) {
                    updateLog("连接超时");
                    disconnectDevice();
                    MCloudApp.setIsBluetoothDeviceConnected(false);
                    EventBus.getDefault().post(new BluetoothStateEvent(false));
                }
            }
        }
    }

    protected void startProgressRunnable(String dialogContent, long delayMillis) {
        showLoadingDialog(dialogContent);
        if (progressRunnable == null) {
            progressRunnable = new ProgressRunnable();
            hander.postDelayed(progressRunnable, delayMillis);
        }
    }

    protected void stopProgressRunnable() {
        dismissLoadingDialog();
        hander.removeCallbacks(progressRunnable);
        progressRunnable = null;
    }

    @Override
    protected int initContentView() {
        return R.layout.activity_test;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mToolbarTitle.setText("测试页面");
        initBluetooth();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mdBluetoothManager.addBluetoothEventHandler(mdBluetoothEventHandler);
        ByteManagerUtil.init(new TestActivity.MyOnBytePackage());

        if (isFirstCall) {
            connectBleDevice();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        mdBluetoothManager.removeBluetoothEventHandler(mdBluetoothEventHandler);
        isFirstCall = false;
    }

    /**
     * 初始化蓝牙
     */
    private void initBluetooth() {
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = Objects.requireNonNull(bluetoothManager).getAdapter();
        mdBluetoothManager = MdBluetoothManager.getInstance();
    }


    /**
     * 搜索并连接指定的蓝牙设备
     */
    private void connectBleDevice() {
        //蓝牙未打开
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        //通过蓝牙设备列表页面跳转过来时，直接连接设备
        if (!TextUtils.isEmpty(macAddress)) {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(macAddress);
            if (device != null) {
                doConnect(device);
            } else {
                Timber.e("Device not found.  Unable to connect.");
            }
        }
    }


    /**
     * ble 建立连接
     */
    private void doConnect(BluetoothDevice device) {
        isConnSucceed = false;
        setLogBeforeConn();

        mdBluetoothManager.stopScan();
        mdBluetoothManager.connectDevice(device, this);
        errMsg = "连接超时,请稍后尝试";
        startProgressRunnable("正在连接设备：" + SN, CONNECT_DELAY_MILLIS);
    }

    /**
     * ble 取消连接
     */
    private void disconnectDevice() {
        if (null != mdBluetoothManager) {
            mdBluetoothManager.disconnect();
        }
    }

    /**
     * 连接失败时，中断一会儿再连接
     */
    private void autoConnectBlueAfterAWhile(long delayMillis) {
        hander.postDelayed(runnableConn, delayMillis);
    }

    private class MdBluetoothEventHandler implements BluetoothEventHandler {
        @Override
        public void handle(final BluetoothEvent event) {
            switch (event.getEventType()) {
                case CONNECTED:
                    isConnSucceed = true;
                    connSuccessNum++;
                    mHandler.sendEmptyMessage(Constants.BT_CONNECT);
                    break;

                case DISCONNECTED:
                    Timber.e("蓝牙连接断开");
                    if (!isConnSucceed)
                        updateLog("蓝牙连接断开！\r\n");
                    mHandler.sendEmptyMessage(Constants.BT_DISCONNECTED);
                    break;

                case REQUEST_MTU_FAIL:
                    Timber.e("MTU请求设置失败");
                    updateLog("MTU请求设置失败！\r\n");
                    break;

                case SERVICE_FIND_FAIL:
                    Timber.e("蓝牙服务发现失败");
                    updateLog("蓝牙服务发现失败！\r\n");
                    break;

                case CHARACTERISTICS_FIND_FAIL:
                    Timber.e("特征读取失败");
                    updateLog("特征读取失败！\r\n");
                    break;

                case ENABLE_READ_SUCCESS:
                    Timber.i("设置读取Descriptor成功");
                    break;

                case ENABLE_READ_FAIL:
                    Timber.e("设置读取Descriptor失败");
                    updateLog("设置读取Descriptor失败！\r\n");
                    break;

                case WRITE_TIME_OUT:
                    Timber.e("写入等待超时");
                    updateLog("写入等待超时！\r\n");
                    break;

                case MESSAGE_WRITE_SUCCESS:
//                    Timber.i("消息写入成功");
                    mHandler.sendEmptyMessage(Constants.BT_MESSAGE_WRITE_SUCCESS);
                    try {
                        String msg = ((Message) event.getEventData()).getResponseMessage();
                        byte[] data = (byte[]) msg.getBytes();
                        if (data.length > 0) {
                            ByteManagerUtil.getInstance().writeByte(data);
                        }
                    } catch (Exception ex) {
                        Timber.e(ex);
                    }
                    break;

                case MESSAGE_RESPONSE_TIME_OUT:
                    Timber.e("消息等待响应超时");
                    updateLog("消息等待响应超时！\r\n\r\n");
                    break;

                case MESSAGE_WRITE_FAIL:
                    Timber.e("消息写入失败");
                    updateLog("消息写入失败！\r\n\r\n");
                    break;

                case RESPONSE_WITH_NO_MESSAGE:
                    Timber.i("RESPONSE_WITH_NO_MESSAGE");
                    try {
                        byte[] data = ((String) event.getEventData()).getBytes();
                        if (data.length > 0) {
                            ByteManagerUtil.getInstance().writeByte(data);
                        }
                    } catch (Exception ex) {
                        Timber.e(ex);
                    }
                    break;

                default:
                    break;
            }
        }
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case Constants.BT_CONNECT:
                    stopProgressRunnable();
                    startBluAuthenticate();//蓝牙连接成功开始进行验证
                    startProgressRunnable("开始认证...", CONNECT_DELAY_MILLIS);
                    break;

                case Constants.BT_DISCONNECTED:
                    stopProgressRunnable();
                    if (isAutoConnectBlue) {
                        ToastUtils.show("30s后重连");
                        autoConnectBlueAfterAWhile(30000);
                    }
                    break;

                case Constants.VERIFY_RESULT:
                    stopProgressRunnable();
                    if (msg.obj.equals("1")) {
                        varifySuccessNum++;
                        updateLog("蓝牙连接认证通过！\r\n");
                    } else {
                        varifyFailedNum++;
                        updateLog("蓝牙连接认证失败！\r\n");
                    }
                    disconnectDevice();
                    break;
            }

            return false;
        }
    });


    private class MyOnBytePackage implements OnBytePackage {
        @Override
        public void onPackageArrived(final byte[] data) {
            try {
                final String cmdStr = new String(data, StandardCharsets.UTF_8);
                Timber.d("应答指令===%s", cmdStr);
                String cmdArray[] = cmdStr.replace("\r\n", "").split(",");

                updateLog(cmdStr);

                if (cmdStr.startsWith("$$224")) {//认证方式
                    if (cmdStr.replace("\r\n", "").endsWith(CommandResult.ERROR_END)) {
                        startBluAuthenticate();//重新认证
                        return;
                    }

                    byte[] resultData = StringUtil.hexStringToBytes(cmdArray[3]);
                    try {
                        String deskey = "12345678";
                        String strdes = new String(DesUtil.decrypt(resultData, deskey), StandardCharsets.UTF_8);
                        if (!TextUtils.isEmpty(strdes)) {
                            String desStr = StringUtil.bytesToHexString(DesUtil.encrypt((StringUtil.reverseString(strdes.substring(0, 6)) + deskey).getBytes(), deskey));
                            String cmd = "##222," + SN + ",0," + desStr.toUpperCase() + "\r\n";
                            sendCommonCommandImmediately(cmd);
                            Timber.d("设备登录验证指令===%s", cmd);
                            return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }

                //设备登录验证结果指令
                if (cmdStr.startsWith("$$223")) {
                    sendHandleMessage(Constants.VERIFY_RESULT, cmdArray[1]);
                    Timber.d("设备登录验证状态===%s", cmdArray[1]);
                    return;
                }

                //需要验证设备
                if (cmdStr.equals("Please verify the equipment.\r\n")) {
                    sendHandleMessage(Constants.VERIFY_RESULT, "0");
                    return;
                }

                if (cmdStr.equals("Equipment Verify OK.\r\n")) {
                    sendHandleMessage(Constants.VERIFY_RESULT, "1");
                    return;
                }

            } catch (Exception ex) {
                Timber.e(ex);
            }
        }
    }

    private void setLogBeforeConn() {
        if (totalConnNum % 20 == 0) {
            stringBuilder = new StringBuilder();
        }

        String msg = "已连接 " + totalConnNum + " 次\r\n";
        msg += "连接成功 " + connSuccessNum + " 次,失败" + (totalConnNum - connSuccessNum) + "次\r\n";
        msg += "认证成功 " + varifySuccessNum + " 次,失败" + varifyFailedNum + "次\r\n";

        msg += "开始第 " + (++totalConnNum) + " 次连接";
        Timber.i(msg);
        updateLog("--------------------------------------");
        updateLog(msg + "\r\n");
    }


    private void updateLog(String cmdStr) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stringBuilder.append(cmdStr + "\r\n");
                mTvLog.setText(stringBuilder.toString());
                mTvLog.setSelection(mTvLog.getText().length());
            }
        });
    }


    private void sendHandleMessage(int what, Object obj) {
        android.os.Message message = new android.os.Message();
        message.what = what;
        if (obj != null) {
            message.obj = obj;
        }
        mHandler.sendMessage(message);
    }


    /**
     * 马上发送蓝牙指令
     *
     * @param cmdStr
     */
    public void sendCommonCommandImmediately(final String cmdStr) {
        final Message msg = new Message(UUID.randomUUID().toString(), cmdStr, true);
        if (mdBluetoothManager != null) {
            mdBluetoothManager.writeMessage(msg);
        }
    }

    /**
     * 蓝牙连接成功开始进行验证  lock
     */
    private void startBluAuthenticate() {
        AuthenticationConfigEntity configEntity = new AuthenticationConfigEntity(SN, 0);
        String command = CommandManager.getInstance().getCommand(CommandType.AUTHENTICATION_CONFIG, configEntity);
        sendCommonCommandImmediately("\r\n" + command);
        Timber.d("设置认证类型指令===%s", command);
    }

    @OnClick({R.id.back, R.id.btn_1, R.id.btn_2})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                onBackPressed();
                break;

            case R.id.btn_1:
                isAutoConnectBlue = true;
                autoConnectBlueAfterAWhile(0);
                break;

            case R.id.btn_2:
                isAutoConnectBlue = false;
                hander.removeCallbacks(runnableConn);
                disconnectDevice();
                break;
        }
    }


    @Override
    protected void onDestroy() {
        dismissLoadingDialog();
//        totalConnNum = 0;
//        connSuccessNum = 0;
//        varifySuccessNum = 0;
//        varifyFailedNum = 0;
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}
