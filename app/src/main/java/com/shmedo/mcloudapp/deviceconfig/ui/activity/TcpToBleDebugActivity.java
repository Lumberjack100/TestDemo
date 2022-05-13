package com.shmedo.mcloudapp.deviceconfig.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.littlegreens.netty.client.listener.MessageStateListener;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.deviceconfig.adapter.CommonLogAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.CommonLogInfo;
import com.shmedo.mcloudapp.deviceconfig.model.TcpConnectionState;
import com.shmedo.mcloudapp.profile.TcpViewModel;
import com.shmedo.mcloudapp.profile.USRBleViewModel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import gdut.bsx.share2.Share2;
import gdut.bsx.share2.ShareContentType;
import no.nordicsemi.android.ble.livedata.state.ConnectionState;
import timber.log.Timber;

public class TcpToBleDebugActivity extends BaseActivity {
    public static final String TCP_HOST = "com.shmedo.mcloudapp.TCP_HOST";
    public static final String TCP_PORT = "com.shmedo.mcloudapp.TCP_PORT";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.tvConnect)
    AppCompatTextView mTvConnect;

    @BindView(R.id.recyclerView_log)
    RecyclerView mRecyclerView;

    private CommonLogAdapter commonLogAdapter;

    private List<CommonLogInfo> logInfoList = new ArrayList<>();

    private TcpViewModel tcpViewModel;
    private USRBleViewModel usrBleViewModel;

    private String host = "192.168.0.107";
    private int port = 1088;

    public static void startActivity(Context context, String host, int port) {
        Intent intent = new Intent(context, TcpToBleDebugActivity.class);
        intent.putExtra(TCP_HOST, host);
        intent.putExtra(TCP_PORT, port);
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
        mToolbar.setSubtitle(host + ":" + port);
        parseIntent();
        initLogAdapter();
        tcpViewModel = getApplicationScopeViewModel(TcpViewModel.class);
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
                if (TextUtils.isEmpty(msg) || msg.equals("\r\n"))
                    return;
                handleReceiveMsg(msg);
            }
        });
        usrBleViewModel = getApplicationScopeViewModel(USRBleViewModel.class);
        usrBleViewModel.getResponseMsg().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String msg) {
//                if (!result.startsWith("$$")) {
//                    if (usrBleViewModel.getLogOutputMode().getValue() == null || !usrBleViewModel.getLogOutputMode().getValue()) {
//                        return;
//                    }
//                }
                Timber.d("onResponseMsg: %s", msg);
                if (msg.startsWith("$$888")) {
                    return;
                }
                handleResponseMsg(msg);
            }
        });
        usrBleViewModel.getConnectionState().observe(this, new Observer<ConnectionState>() {
            @Override
            public void onChanged(ConnectionState connectionState) {
                switch (connectionState.getState()) {
                    case CONNECTING:
                        printLog("正在连接蓝牙...", R.color.title_text_color);
                        break;

                    case INITIALIZING:
                        break;

                    case READY:
                        printLog("蓝牙连接成功", R.color.title_text_color);
                        break;

                    case DISCONNECTED:
                        printLog("蓝牙连接断开", R.color.title_text_color);
//                        usrBleViewModel.reconnect();
                        break;

                    // fallthrough
                    case DISCONNECTING:
                        break;
                }
            }
        });
        setupTcpConnect();
    }

    protected void parseIntent() {
        Intent intent = getIntent();
        if (intent.getExtras() == null)
            return;
        host = intent.getStringExtra(TCP_HOST);
        port = intent.getIntExtra(TCP_PORT, 1088);
    }

    private void initLogAdapter() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commonLogAdapter = new CommonLogAdapter(logInfoList);
        mRecyclerView.setAdapter(commonLogAdapter);
    }

    /**
     * 建立 Tcp 通讯连接
     */
    private void setupTcpConnect() {
        printLog("正在连接远程TCP...", R.color.title_text_color);
        tcpViewModel.initTcpClient(host, port);
        tcpViewModel.connect();
    }

    private void handleReceiveMsg(String msg) {
        printLog(msg, R.color.receive_data_color);
        if (!usrBleViewModel.isConnected()) {
            ToastUtils.show(getString(R.string.ble_config_disconnect_warn));
            return;
        }
        if (!msg.endsWith("\r\n"))
            msg += "\r\n";
        usrBleViewModel.sendIOTProtocolCommand(msg);
    }

    private void handleResponseMsg(String msg) {
        printLog(msg, R.color.response_data_color);
        if (!tcpViewModel.getConnectStatus()) {
            ToastUtils.show(getString(R.string.tcp_config_disconnect_warn));
            return;
        }
        tcpViewModel.sendMsgToServer(msg, new MessageStateListener() {
            @Override
            public void isSendSuccss(boolean isSuccess) {
                if (isSuccess) {
//                    Timber.d("发送指令成功");
                } else {
                    Timber.e("发送指令失败");
                }
            }
        });
    }

    @OnClick({R.id.tvConnect, R.id.ivMore})
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.tvConnect) {
            if (!tcpViewModel.getConnectStatus()) {
                printLog("正在连接...", R.color.title_text_color);
                tcpViewModel.connect();
            } else {
                tcpViewModel.disconnect();
            }
        } else if (id == R.id.ivMore) {
            showMoreMenu();
        }
    }

    private void showMoreMenu() {
        BottomMenu.show(new String[]{"清空日志", "分享日志"})
                .setMessage("")
                .setOnMenuItemClickListener(new OnMenuItemClickListener<BottomMenu>() {
                    @Override
                    public boolean onClick(BottomMenu dialog, CharSequence text, int index) {
                        if (index == 0) {
                            clearLogs();
                        } else if (index == 1) {
                            shareLogs();
                        }
                        return false;
                    }
                });
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

    private void printLog(String msg, int color) {
        CommonLogInfo commonLogInfo = new CommonLogInfo(TimeUtils.getNowString(new SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())), msg, color);
        logInfoList.add(commonLogInfo);
        commonLogAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(commonLogAdapter.getItemCount() - 1);
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
        super.onDestroy();
    }

}