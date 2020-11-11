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

public class TestBActivity extends BaseActivity {
    @BindView(R.id.et_msg)
    EditText mMsgET;

    private TcpShareViewModel tcpShareViewModel;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, TestBActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_test_b;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tcpShareViewModel = getApplicationScopeViewModel(TcpShareViewModel.class);

        tcpShareViewModel.getTcpConnectionState().observeInActivity(this, new Observer<TcpConnectionState>() {
            @Override
            public void onChanged(TcpConnectionState tcpConnectionState) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Timber.e("onChanged=%s", tcpConnectionState);
                        ToastUtils.show("TestBActivity: " + tcpConnectionState);
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
                        ToastUtils.show("TestBActivity: " + s);
                    }
                });
            }
        });
    }

    @OnClick({R.id.btn_confirm, R.id.btn_connect, R.id.btn_disconnect})
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
            case R.id.btn_connect:
                tcpShareViewModel.connect();
                break;

            case R.id.btn_disconnect:
                tcpShareViewModel.disconnect();
                break;
        }
    }
}