package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.littlegreens.netty.client.listener.MessageStateListener;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.TcpShareViewModel;

import java.util.Objects;

import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2020/11/11 <br/>
 * 描述：     TODO
 */
public abstract class BaseTcpConnectFragment extends BaseFragment {
    public static final int TCP_CONNECT_DELAY_MILLIS = 5000;//Tcp 连接超时时间

    public static final int QUERY_CMD_DELAY_MILLIS = 2000;//查询配置参数指令超时时间

    public static final int SEND_CMD_DELAY_MILLIS = 20000;//发送配置参数指令超时时间

    private static Handler uiHander = new Handler();

    protected String errMsg = "";

    protected boolean isExitMode = false;

    protected TcpShareViewModel tcpShareViewModel;

    private static ProgressRunnable progressRunnable;

    private class ProgressRunnable implements Runnable {
        @Override
        public void run() {
            dismissProgressDialog();
            progressRunnable = null;
            if (!TextUtils.isEmpty(errMsg)) {
                ToastUtils.show(errMsg);
            }
        }
    }

    protected void startProgressRunnable(String dialogContent, long delayMillis) {
        showProgressDialog(dialogContent, null, null);
        if (progressRunnable == null) {
            progressRunnable = new ProgressRunnable();
            uiHander.postDelayed(progressRunnable, delayMillis);
        }
    }

    protected void stopProgressRunnable() {
        dismissProgressDialog();
        uiHander.removeCallbacksAndMessages(null);
        progressRunnable = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        dismissProgressDialog();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tcpShareViewModel = getApplicationScopeViewModel(TcpShareViewModel.class);
        tcpShareViewModel.getReceivedMessage().observeInFragment(this, new Observer<String>() {
            @Override
            public void onChanged(String msg) {
                parseResponseMessage(msg);
            }
        });
    }

    /**
     * 解析设备的参数指令
     */
    protected void parseResponseMessage(String cmdStr) {
    }

    protected void sendCommand(String cmdStr) {
        Timber.d("发送指令：%s", cmdStr);
        tcpShareViewModel.sendMsgToServer(cmdStr, new MessageStateListener() {
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

    protected void showDisconnectDialog(String content) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(Objects.requireNonNull(getContext()))
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
                        tcpShareViewModel.disconnect();
                        if (isExitMode) {
                            mActivity.finish();
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }
}
