package com.shmedo.mcloudapp.deviceconfig.ui.fragment.tcpcommon;

import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.blankj.utilcode.util.SPStaticUtils;
import com.hjq.toast.ToastUtils;
import com.littlegreens.netty.client.listener.MessageStateListener;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.deviceconfig.callback.WeakHandler;
import com.shmedo.mcloudapp.deviceconfig.model.TcpConnectionState;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.DeviceApiKeyViewModel;
import com.shmedo.mcloudapp.profile.TcpViewModel;
import com.umeng.analytics.MobclickAgent;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  3/3/21 <br/>
 * 描述：    与支持通过Tcp进行物联网指令通讯的设备的页面基类
 */
public abstract class BaseTcpIotCommunicateFragment extends BaseFragment {
    public static final int TCP_CONNECT_DELAY_MILLIS = 5000;//Tcp 连接超时时间
    protected static final int DELAY_5000_MILLIS = 5000;
    protected static final int DELAY_10000_MILLIS = 10000;//发送指令超时时间
    protected static final int DELAY_15000_MILLIS = 15000;

    protected boolean isExitMode = false;

    protected TcpViewModel tcpViewModel;

    protected DeviceApiKeyViewModel deviceApiKeyViewModel;

    private final DefaultHandler mDefaultHandler = new DefaultHandler(this);


    protected void customHandleMessage(@NonNull @NotNull Message msg) {

    }

    private static final class DefaultHandler extends WeakHandler<BaseTcpIotCommunicateFragment> {
        private DefaultHandler(BaseTcpIotCommunicateFragment fragment) {
            super(fragment);
        }

        @Override
        protected void handleMessage(Message msg, BaseTcpIotCommunicateFragment fragment) {
            fragment.dismissProgressDialog();
            fragment.customHandleMessage(msg);
        }
    }

    protected void startDefaultProgress(String dialogContent, int what, long delayMillis) {
        if (!TextUtils.isEmpty(dialogContent)) {
            showProgressDialog(dialogContent, null, null);
        }
        mDefaultHandler.sendEmptyMessageDelayed(what, delayMillis);
    }

    protected void stopDefaultProgress(int what) {
        dismissProgressDialog();
        mDefaultHandler.removeMessages(what);
    }

    protected void stopAllProgress() {
        dismissProgressDialog();
        mDefaultHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onStop() {
        super.onStop();
        stopAllProgress();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tcpViewModel = getApplicationScopeViewModel(TcpViewModel.class);
        tcpViewModel.getTcpConnectionState().observe(getViewLifecycleOwner(), new Observer<TcpConnectionState>() {
            @Override
            public void onChanged(TcpConnectionState tcpConnectionState) {
                //只供当前处于Active(即处于onResume状态)的页面观察者消费此事件
                // TODO #gh# 返到上一级页面时，LiveData事件会早于上一级页面的onResume()方法分发，即上级页面处于isActive前事件就来了
                if (!isActive) {
                    return;
                }
                onConnectionChange(tcpConnectionState);
            }
        });
        tcpViewModel.getReceivedMessage().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String msg) {
                //只供当前处于Active(即处于onResume状态)的页面观察者消费此事件
                // TODO #gh# 返到上一级页面时，LiveData事件会早于上一级页面的onResume()方法分发，即上级页面处于isActive前事件就来了
                if (!isActive) {
                    return;
                }
                parseResponseMessage(msg);

                try {
                    Map<String, Object> valueMap = new HashMap<String, Object>();
                    valueMap.put("login_user", SPStaticUtils.getString(AppContants.User.UID, ""));
                    valueMap.put("device_sn", MCloudApp.getCurDeviceToken());
                    valueMap.put("command_content", msg);
                    MobclickAgent.onEventObject(MCloudApp.getContext(), "Response_Command", valueMap);
                } catch (Exception ex) {
                    Timber.e(ex);
                }
            }
        });
        deviceApiKeyViewModel = getApplicationScopeViewModel(DeviceApiKeyViewModel.class);
    }

    /**
     * 连接状态改变事件
     *
     * @param tcpConnectionState
     */
    protected void onConnectionChange(TcpConnectionState tcpConnectionState) {
        if (tcpConnectionState == TcpConnectionState.CONNECT_CLOSED) {
            ToastUtils.show("通讯连接断开");
        }
    }

    /**
     * 解析设备的参数指令
     */
    protected void parseResponseMessage(String cmdStr) {
    }

    protected void sendCommand(String cmdStr) {
        String apiKey = "b12aac6b-0bd2-4a01-80fd-97fe4f5d4ff9";
        if (deviceApiKeyViewModel.deviceRequest.getDeviceApiKeyLiveData().getValue() != null &&!TextUtils.isEmpty(deviceApiKeyViewModel.deviceRequest.getDeviceApiKeyLiveData().getValue().getApikey())) {
            apiKey = deviceApiKeyViewModel.deviceRequest.getDeviceApiKeyLiveData().getValue().getApikey();
        }
        cmdStr += "&apikey=" + apiKey
                + "&msgid=" + UUID.randomUUID().toString();

        try {
            Map<String, Object> valueMap = new HashMap<String, Object>();
            valueMap.put("login_user", SPStaticUtils.getString(AppContants.User.UID, ""));
            valueMap.put("device_sn", MCloudApp.getCurDeviceToken());
            valueMap.put("command_content", cmdStr);
            MobclickAgent.onEventObject(MCloudApp.getContext(), "Dispatch_Command", valueMap);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        tcpViewModel.sendMsgToServer(cmdStr, new MessageStateListener() {
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

    /**
     * 断开 Tcp 连接警告
     *
     * @param content
     */
    protected void showDisconnectDialog(String content) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(requireContext())
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
                        if (isExitMode) {
                            mActivity.finish();
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    /**
     * 页面数据修改未保存警告
     */
    protected void warnNotYetSettingBeforeLeavePage() {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(requireContext())
                .title("温馨提示：")
                .content("您已经修改了参数，还未配置到设备，确定离开页面吗？")
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
                        mActivity.finish();
                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }
}
