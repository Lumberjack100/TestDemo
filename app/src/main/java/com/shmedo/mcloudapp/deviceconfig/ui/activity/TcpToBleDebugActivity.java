package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.PathUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.UriUtils;
import com.hjq.toast.ToastUtils;
import com.kongzue.dialogx.dialogs.BottomMenu;
import com.kongzue.dialogx.dialogs.WaitDialog;
import com.kongzue.dialogx.interfaces.OnBackPressedListener;
import com.kongzue.dialogx.interfaces.OnMenuItemClickListener;
import com.shmedo.configlibrary.ble.cmd.CommandResult;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.deviceconfig.adapter.CommonLogAdapter;
import com.shmedo.mcloudapp.deviceconfig.callback.WeakHandler;
import com.shmedo.mcloudapp.deviceconfig.model.CommonLogInfo;
import com.shmedo.mcloudapp.deviceconfig.model.TcpConnectionState;
import com.shmedo.mcloudapp.profile.BleViewModel;
import com.shmedo.mcloudapp.profile.TcpViewModel;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import butterknife.BindView;
import butterknife.OnClick;
import gdut.bsx.share2.Share2;
import gdut.bsx.share2.ShareContentType;
import no.nordicsemi.android.ble.livedata.state.ConnectionState;
import timber.log.Timber;

public class TcpToBleDebugActivity extends BaseActivity {
    public static final String TCP_HOST = "com.shmedo.mcloudapp.TCP_HOST";
    public static final String TCP_PORT = "com.shmedo.mcloudapp.TCP_PORT";
    public static final String IOT_CMD = "com.shmedo.mcloudapp.IOT_CMD";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.tvConnect)
    AppCompatTextView mTvConnect;

    @BindView(R.id.recyclerView_log)
    RecyclerView mRecyclerView;

    private CommonLogAdapter commonLogAdapter;

    private List<CommonLogInfo> logInfoList = new ArrayList<>();

    private TcpViewModel tcpViewModel;
    private BleViewModel usrBleViewModel;

    private String host;
    private int port;
    private volatile boolean isIOTCmd = true;

    private final DefaultHandler mDefaultHandler = new DefaultHandler(this);
    private volatile boolean isNeedReconnect = true;
    private volatile boolean isConnecting = false;


    public static void startActivity(Context context, String host, int port, boolean isIOTCmd) {
        Intent intent = new Intent(context, TcpToBleDebugActivity.class);
        intent.putExtra(TCP_HOST, host);
        intent.putExtra(TCP_PORT, port);
        intent.putExtra(IOT_CMD, isIOTCmd);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_tcp_to_ble_debug;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setToolBar(R.id.toolbar);
        mToolbar.setTitle("TCP远程调试");
        parseIntent();
        initLogAdapter();
        tcpViewModel = getActivityScopeViewModel(TcpViewModel.class);
        tcpViewModel.getTcpConnectionState().observe(this, new Observer<TcpConnectionState>() {
            @Override
            public void onChanged(TcpConnectionState tcpConnectionState) {
                if (tcpConnectionState == TcpConnectionState.CONNECT_SUCCESS) {
                    mTvConnect.setText("断开");
                    printLog("TCP连接成功", R.color.title_text_color);

                } else if (tcpConnectionState == TcpConnectionState.CONNECT_CLOSED) {
                    mTvConnect.setText("连接");
                    printLog("TCP连接断开", R.color.title_text_color);
                }
            }
        });
        tcpViewModel.getReceivedMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String msg) {
                Timber.d("getReceivedMessage: %s", msg);
                if (TextUtils.isEmpty(msg) || (!msg.startsWith("##") && !msg.startsWith("$cmd")))
                    return;
                handleReceiveMsgFromTCPServer(msg);
            }
        });
        usrBleViewModel = getApplicationScopeViewModel(BleViewModel.class);
        usrBleViewModel.getResponseMsg().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String msg) {
                Timber.d("onResponseMsg: %s", msg);
                handleResponseMsgFromDevice(msg);
            }
        });
        usrBleViewModel.getConnectionState().observe(this, new Observer<ConnectionState>() {
            @Override
            public void onChanged(ConnectionState connectionState) {
                switch (connectionState.getState()) {
                    case CONNECTING:
                        isConnecting = true;
                        printLog("正在连接蓝牙...", R.color.title_text_color);
                        break;

                    case INITIALIZING:
                        break;

                    case READY:
                        if (isConnecting) {
                            isConnecting = false;
                            isNeedReconnect = true;
                            printLog("蓝牙连接成功", R.color.title_text_color);
                            if (!isIOTCmd)
                                usrBleViewModel.setAuthenticateWay();
                        }
                        break;

                    case DISCONNECTED:
                        printLog("蓝牙连接断开", R.color.title_text_color);
                        if (isNeedReconnect) {
                            isNeedReconnect = false;
                            mDefaultHandler.sendEmptyMessageDelayed(AppContants.MsgWhat.CONNECT_DEVICE, 5000);
                        }
                        break;

                    // fallthrough
                    case DISCONNECTING:
                        printLog("蓝牙连接正在断开...", R.color.title_text_color);
                        break;
                }
            }
        });
        tcpViewModel.initTcpClient(host, port, false, "\r\n");
        setupTcpConnect();
    }

    protected void parseIntent() {
        Intent intent = getIntent();
        if (intent.getExtras() == null)
            return;
        isIOTCmd = intent.getBooleanExtra(IOT_CMD, true);
        host = intent.getStringExtra(TCP_HOST);
        port = intent.getIntExtra(TCP_PORT, 1088);
        mToolbar.setSubtitle(host + ":" + port);
    }

    private void initLogAdapter() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commonLogAdapter = new CommonLogAdapter(logInfoList);
        mRecyclerView.setAdapter(commonLogAdapter);
        mRecyclerView.setHasFixedSize(true);
    }

    /**
     * 建立 Tcp 通讯连接
     */
    private void setupTcpConnect() {
        tcpViewModel.connect();
    }

    /**
     * 处理接收的 TCP 消息，通过蓝牙转发给设备
     *
     * @param msg
     */
    private void handleReceiveMsgFromTCPServer(String msg) {
        if (TextUtils.isEmpty(msg))
            return;

        printLog(msg.replace("\r\n", ""), R.color.receive_data_color);
        if (!usrBleViewModel.isConnected()) {
            ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
            return;
        }
        if (isIOTCmd) {
            if (!msg.contains("&apikey")) {
                String apiKey = "b12aac6b-0bd2-4a01-80fd-97fe4f5d4ff9";
                if (usrBleViewModel.deviceRequest.getDeviceApiKeyLiveData().getValue() != null && !TextUtils.isEmpty(usrBleViewModel.deviceRequest.getDeviceApiKeyLiveData().getValue().getApikey())) {
                    apiKey = usrBleViewModel.deviceRequest.getDeviceApiKeyLiveData().getValue().getApikey();
                }
                msg += "&apikey=" + apiKey
                        + "&msgid=" + UUID.randomUUID().toString().substring(30);
            }
        } else if (!msg.endsWith("\r\n")) {
            msg += "\r\n";
        }

        usrBleViewModel.sendIOTProtocolCommand(msg);
    }

    /**
     * 处理设备相应的消息，通过 TCP 转发给远程调试客户端
     *
     * @param msg
     */
    private void handleResponseMsgFromDevice(String msg) {
        printLog(msg, R.color.response_data_color);
        try {
            if (isIOTCmd) {
                if (!tcpViewModel.getConnectStatus()) {
                    ToastUtils.show(getString(R.string.tcp_config_disconnect_warn));
                    return;
                }
                tcpViewModel.sendMsgToServer(msg);
                return;
            }
            String cmdArray[] = msg.replace("\r\n", "").split(",");
            if (msg.startsWith("$$224")) {//认证方式
                if (msg.replace("\r\n", "").endsWith(CommandResult.ERROR_END)) {
                    usrBleViewModel.setAuthenticateWay();//重新认证
                    return;
                }
                usrBleViewModel.sendAuthenticateCodeCmd(cmdArray[3]);

            } else if (msg.startsWith("$$223")) {//设备登录验证结果指令
                if (!cmdArray[1].contains("1")) {
                    printLog("设备认证失败!", R.color.response_data_color);
                }
            } else if (msg.contains("Please verify the equipment.\r\n")) {
                printLog("设备认证失败!", R.color.response_data_color);

            } else {
                if (!tcpViewModel.getConnectStatus()) {
                    ToastUtils.show(getString(R.string.tcp_config_disconnect_warn));
                    return;
                }
                tcpViewModel.sendMsgToServer(msg);
            }
        } catch (Exception ex) {
            Timber.e(ex);
        }
    }

    @OnClick({R.id.tvConnect, R.id.ivMore})
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.tvConnect) {
            if (!tcpViewModel.getConnectStatus()) {
                setupTcpConnect();
            } else {
                tcpViewModel.disconnect();
            }
        } else if (id == R.id.ivMore) {
            showMoreMenu();
        }
    }

    private void showMoreMenu() {
        String[] menuItems = usrBleViewModel.isConnected() ? new String[]{"清空日志", "分享日志", "打开debug模式", "打开info模式", "测试"} : new String[]{"蓝牙重连", "清空日志", "分享日志"};
        BottomMenu.show(menuItems)
                .setMessage("")
                .setOnMenuItemClickListener(new OnMenuItemClickListener<BottomMenu>() {
                    @Override
                    public boolean onClick(BottomMenu dialog, CharSequence text, int index) {
                        if (text.equals("蓝牙重连")) {
                            usrBleViewModel.reconnect();
                        } else if (text.equals("清空日志")) {
                            clearLogs();
                        } else if (text.equals("分享日志")) {
                            shareLogs();
                        } else if (text.equals("打开debug模式")) {
                            handleReceiveMsgFromTCPServer(!isIOTCmd ? "##2261\r\n" : "");
                            handleReceiveMsgFromTCPServer(!isIOTCmd ? "##0062\r\n" : "$cmd=md_setlogoutput&level=debug&type=bt");
                        } else if (text.equals("打开info模式")) {
                            handleReceiveMsgFromTCPServer(!isIOTCmd ? "##2261\r\n" : "");
                            handleReceiveMsgFromTCPServer(!isIOTCmd ? "##0063\r\n" : "$cmd=md_setlogoutput&level=info&type=bt");
                        } else if (text.equals("测试")) {
                            handleReceiveMsgFromTCPServer(!isIOTCmd ? "##000\r\n" : "$cmd=sample");
                        }
                        return false;
                    }
                });
    }

    /**
     * 断开 Tcp 连接警告
     *
     * @param content
     */
    private void showDisconnectDialog(String content) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(this)
                .title("温馨提示：")
                .content(content)
                .contentColorRes(R.color.title_text_color)
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .positiveColorRes(R.color.blue_52B4F8)
                .negativeColorRes(R.color.sub_title_text_color)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        tcpViewModel.disconnect();
                        finish();
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    private void printLog(String msg, int color) {
        CommonLogInfo commonLogInfo = new CommonLogInfo(TimeUtils.getNowString(new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())), msg, color);
        logInfoList.add(commonLogInfo);
        commonLogAdapter.notifyItemInserted(logInfoList.size() - 1);
        mRecyclerView.scrollToPosition(commonLogAdapter.getItemCount() - 1);
    }

    private void clearLogs() {
        logInfoList.clear();
        commonLogAdapter.notifyDataSetChanged();
    }

    private void shareLogs() {
        if (logInfoList.size() == 0) {
            ToastUtils.show("没有日志");
            return;
        }
        WaitDialog.show("处理中...")
                .setOnBackPressedListener(new OnBackPressedListener() {//返回按键监听
                    @Override
                    public boolean onBackPressed() {
                        WaitDialog.dismiss();
                        return false;
                    }
                });
        String timeStr = TimeUtils.getNowString(new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()));
        String str2 = AppUtils.getAppName() + "_tcp_client_realtime_log_" + timeStr + ".txt";
        File file = new File(PathUtils.getInternalAppCachePath(), str2);
        if (file.exists())
            file.delete();

        if (!FileUtils.createOrExistsFile(file)) {
            WaitDialog.dismiss();
            Timber.e("create file <" + file + "> failed.");
            return;
        }
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file, true));
            for (CommonLogInfo commonLogInfo : logInfoList) {
                StringBuilder stringBuilder1 = new StringBuilder();
                stringBuilder1.append(commonLogInfo.getLogTime());
                stringBuilder1.append(' ');
                stringBuilder1.append(commonLogInfo.getLogContent());
                stringBuilder1.append('\n');
                str2 = stringBuilder1.toString();
                bw.write(str2);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                WaitDialog.dismiss();
                if (bw != null) {
                    bw.close();
                    shareFile(file);
                }
            } catch (IOException e) {
                e.printStackTrace();
                WaitDialog.dismiss();
            }
        }
    }

    private void shareFile(File file) {
        if (!file.exists()) {
            ToastUtils.show("日志文件不存在");
            return;
        }
        Uri contentUri = UriUtils.file2Uri(file);
        new Share2.Builder(this)
                .setContentType(ShareContentType.FILE)
                .setShareFileUri(contentUri)
                .setTitle("分享文件")
                .setOnActivityResult(300)
                .build()
                .shareBySystem();
    }

    protected void customHandleMessage(@NonNull @NotNull Message msg) {
        switch (msg.what) {
            case AppContants.MsgWhat.CONNECT_DEVICE: {
                if (!usrBleViewModel.isConnected()) {
                    usrBleViewModel.reconnect();
                }
            }
            break;
        }
    }

    private static final class DefaultHandler extends WeakHandler<TcpToBleDebugActivity> {
        private DefaultHandler(TcpToBleDebugActivity context) {
            super(context);
        }

        @Override
        protected void handleMessage(Message msg, TcpToBleDebugActivity context) {
            context.customHandleMessage(msg);
        }
    }

    protected void stopDefaultProgress(int what) {
        mDefaultHandler.removeMessages(what);
    }

    protected void stopAllProgress() {
        mDefaultHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAllProgress();
    }
//    @Override
//    public void onStop() {
//        super.onStop();
//        stopAllProgress();
//    }

    @Override
    public void onBackPressed() {
        if (tcpViewModel.getConnectStatus()) {
            showDisconnectDialog(StringUtils.getString(R.string.finish_activity_disconnect_tcp_device));
        } else {
            finish();
        }
    }

    @Override
    public void onDestroy() {
        handleReceiveMsgFromTCPServer(!isIOTCmd ? "##2260\r\n" : "");
        handleReceiveMsgFromTCPServer(!isIOTCmd ? "##0061\r\n" : "$cmd=md_setlogoutput&level=off&type=bt");
        super.onDestroy();
    }
}