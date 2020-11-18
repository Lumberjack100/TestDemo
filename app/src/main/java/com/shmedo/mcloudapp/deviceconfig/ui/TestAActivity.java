package com.shmedo.mcloudapp.deviceconfig.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.lifecycle.Observer;

import com.hjq.toast.ToastUtils;
import com.littlegreens.netty.client.listener.MessageStateListener;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.activity.BaseActivity;
import com.shmedo.mcloudapp.deviceconfig.model.TcpConnectionState;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.TcpShareViewModel;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

public class TestAActivity extends BaseActivity {
    private static final String ARG_PARAM1 = "param1";

    @BindView(R.id.et_msg)
    EditText mMsgET;

    private TcpShareViewModel tcpShareViewModel;

    private String ipAddress;

    public static void DataCenterInfo(Context context, String ip) {
        Intent intent = new Intent(context, TestAActivity.class);
        intent.putExtra(ARG_PARAM1, ip);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }


    @Override
    protected int getLayoutId() {
        return R.layout.activity_test_a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parseIntent();
        tcpShareViewModel = getApplicationScopeViewModel(TcpShareViewModel.class);

        tcpShareViewModel.initTcpClient(ipAddress, 10002);
        tcpShareViewModel.getTcpConnectionState().observeInActivity(this, new Observer<TcpConnectionState>() {
            @Override
            public void onChanged(TcpConnectionState tcpConnectionState) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Timber.e("onChanged=%s", tcpConnectionState);
                        ToastUtils.show("TestAActivity: " + tcpConnectionState);
                    }
                });
            }
        });

        tcpShareViewModel.getReceivedMessage().observeInActivity(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.show("TestAActivity: " + s);
                    }
                });
            }
        });
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.getExtras() != null && intent.getExtras().containsKey(ARG_PARAM1)) {
            ipAddress = intent.getStringExtra(ARG_PARAM1);
        }
    }

    @OnClick({R.id.btn_confirm, R.id.btn_jump, R.id.btn_connect, R.id.btn_disconnect})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_confirm:
                if (!tcpShareViewModel.getConnectStatus()) {
                    ToastUtils.show("未连接,请先连接");
                    return;
                }
                tcpShareViewModel.sendMsgToServer(mMsgET.getText().toString(), new MessageStateListener() {
                    @Override
                    public void isSendSuccss(boolean isSuccess) {
                        if (isSuccess) {
                            Timber.d("Write auth successful");
//                    logSend(msg);
                        } else {
                            Timber.d("Write auth error");
                        }
                    }
                });
                break;

            case R.id.btn_jump:
                break;

            case R.id.btn_connect:
                tcpShareViewModel.connect();
                break;

            case R.id.btn_disconnect:
                tcpShareViewModel.disconnect();
                break;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        clear();
    }

    private void clear() {
//        scannerViewModel.getDevices().clear();
//        scannerViewModel.getScannerState().clearRecords();
    }
}