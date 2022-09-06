package com.shmedo.mcloudapp.deviceconfig.ui.fragment.blecommon;

import static autodispose2.AutoDispose.autoDisposable;

import android.bluetooth.BluetoothDevice;
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
import com.shmedo.configlibrary.iot.cmd.IOTCommandManager;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.core.AppContants;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.deviceconfig.callback.WeakHandler;
import com.shmedo.mcloudapp.deviceconfig.model.DeviceDebugAddress;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.TcpToBleDebugActivity;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.AdmeViewModel;
import com.shmedo.mcloudapp.deviceconfig.viewmodels.ConfigPageViewModel;
import com.shmedo.mcloudapp.network.BaseObserver;
import com.shmedo.mcloudapp.network.ErrorInfo;
import com.shmedo.mcloudapp.network.MDRetrofit;
import com.shmedo.mcloudapp.network.RequestHeader;
import com.shmedo.mcloudapp.network.ServiceAddressType;
import com.shmedo.mcloudapp.profile.BleViewModel;
import com.shmedo.mcloudapp.util.ResponseHandler;
import com.umeng.analytics.MobclickAgent;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import autodispose2.androidx.lifecycle.AndroidLifecycleScopeProvider;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  12/7/20 <br/>
 * 描述：    与有人物联网蓝牙模块通讯的页面基类
 */
public abstract class BaseUSRBleIotCommunicateFragment extends BaseFragment {
    protected static final int DELAY_5000_MILLIS = 5000;
    protected static final int DELAY_10000_MILLIS = 10000;//发送指令超时时间
    protected static final int DELAY_15000_MILLIS = 15000;//蓝牙连接超时时间
    public static final int DELAY_20000_MILLIS = 20000;

    protected BleViewModel bleViewModel;

    protected ConfigPageViewModel configPageViewModel;

    protected AdmeViewModel admeViewModel;

    public boolean isExitMode = false;//是否退出页面标志

    private final DefaultHandler mDefaultHandler = new DefaultHandler(this);

    protected LinkedList<String> commandItems = new LinkedList<>();

    protected void customHandleMessage(@NonNull @NotNull Message msg) {

    }

    private static final class DefaultHandler extends WeakHandler<BaseUSRBleIotCommunicateFragment> {
        private DefaultHandler(BaseUSRBleIotCommunicateFragment fragment) {
            super(fragment);
        }

        @Override
        protected void handleMessage(Message msg, BaseUSRBleIotCommunicateFragment fragment) {
            fragment.dismissProgressDialog();
            fragment.customHandleMessage(msg);
        }
    }

    protected void startDefaultProgress(String dialogContent, int what, long delayMillis) {
        if (!TextUtils.isEmpty(dialogContent)) {
            showProgressDialog(dialogContent);
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
    public void onPause() {
        super.onPause();
        stopAllProgress();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bleViewModel = getApplicationScopeViewModel(BleViewModel.class);
        bleViewModel.getResponseMsg().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String result) {
//                if (!result.startsWith("$cmd=")) {
//                    if (bleViewModel.getLogOutputMode().getValue() == null || !bleViewModel.getLogOutputMode().getValue()) {
//                        return;
//                    }
//                }

                try {
                    parseResponseMessage(result);

                    Map<String, Object> valueMap = new HashMap<String, Object>();
                    valueMap.put("login_user", SPStaticUtils.getString(AppContants.User.UID, ""));
                    valueMap.put("device_sn", MCloudApp.getCurDeviceToken());
                    valueMap.put("command_content", result);
                    MobclickAgent.onEventObject(MCloudApp.getContext(), "Response_Command", valueMap);
                } catch (Exception ex) {
                    Timber.e(ex);
                }
            }
        });
        configPageViewModel = getActivityScopeViewModel(ConfigPageViewModel.class);
        configPageViewModel.configPageEditableChanged.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isEditable) {
                onEditableChanged(isEditable);
            }
        });
        admeViewModel = getApplicationScopeViewModel(AdmeViewModel.class);
    }

    protected void onEditableChanged(boolean isEditable) {
    }

    /**
     * ble 建立连接
     */
    protected void connectDevice(BluetoothDevice device) {
        bleViewModel.connect(device);
    }

    /**
     * ble 取消连接
     */
    protected void disconnectDevice() {
        Timber.d("disconnectDevice()调用");
        bleViewModel.disconnect();
    }

    /**
     * This method returns true if the device is connected. Services could have not been
     * discovered yet.
     */
    public final boolean isConnected() {
        return bleViewModel.isConnected();
    }

    protected void clearDevice() {
        bleViewModel.clearDevice();
    }

    /**
     * 解析设备的参数指令
     */
    protected void parseResponseMessage(String cmdStr) {
        stopAllProgress();
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        if (type == IOTCommandType.UNKNOWN_TYPE) {
            Timber.e("未知的命令:%s", cmdStr);
            ToastUtils.show("未知的命令:" + cmdStr);
        }
    }

    /**
     * 发送心跳数据(未定义的指令)
     */
    protected void sendHeartData() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.HEART_BEAT);
        Timber.d("发送心跳数据：%s", command);
        sendCommand(command);
    }

    /**
     * 保存配置信息
     */
    protected void saveConfigInfo() {
        String command = IOTCommandManager.getInstance().getCommand(IOTCommandType.MD_SAVE_CONFIG_PARAM);
        sendCommand(command);
    }

    public void sendCommand(String cmdStr) {
        if (!isConnected()) {
            return;
        }
        String apiKey = "b12aac6b-0bd2-4a01-80fd-97fe4f5d4ff9";
        if (bleViewModel.deviceRequest.getDeviceApiKeyLiveData().getValue() != null && !TextUtils.isEmpty(bleViewModel.deviceRequest.getDeviceApiKeyLiveData().getValue().getApikey())) {
            apiKey = bleViewModel.deviceRequest.getDeviceApiKeyLiveData().getValue().getApikey();
        }
        if (!cmdStr.contains("&apikey")) {
            cmdStr += "&apikey=" + apiKey
                    + "&msgid=" + UUID.randomUUID().toString().substring(30);
        }

        bleViewModel.sendIOTProtocolCommand(cmdStr);
    }

    /**
     * 发送指令队列中的第一条指令
     */
    protected void sendCommandFromCmdList() {
        if (commandItems.size() > 0) {
            String command = commandItems.getFirst();
            sendCommand(command);
            commandItems.removeFirst();
        } else {
            stopDefaultProgress(AppContants.MsgWhat.MSG_DEFAULT);
        }
    }

    /**
     * 发送指令队列中的第一条指令
     */
    protected void sendCommandFromCmdList(String dialogContent, int what, long delayMillis) {
        if (commandItems.size() > 0) {
            String command = commandItems.getFirst();
            sendCommand(command);
            commandItems.removeFirst();
            if (TextUtils.isEmpty(dialogContent) && delayMillis != 0)
                startDefaultProgress(dialogContent, what, delayMillis);
        } else {
            stopDefaultProgress(what);
        }
    }

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
                        disconnectDevice();
                        if (isExitMode) {
                            mActivity.finish();
                        }
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

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

    /**
     * 查询设备远程调试连接地址信息
     */
    protected void getDeviceLogin() {
        JSONObject jsonObjectRequest = new JSONObject();
        try {
            jsonObjectRequest.put("appKey", "b80dd379-5256-48c8-947a-2208872c8a8f");
            jsonObjectRequest.put("appSecret", "3dc8e0ec1f673325c6694b4da534dabe");
            jsonObjectRequest.put("deviceSn", MCloudApp.getCurDeviceToken());
            jsonObjectRequest.put("deviceKey", bleViewModel.deviceRequest.getDeviceApiKeyLiveData().getValue() != null ? bleViewModel.deviceRequest.getDeviceApiKeyLiveData().getValue().getApikey() : "b12aac6b-0bd2-4a01-80fd-97fe4f5d4ff9");
            jsonObjectRequest.put("reCreate", false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonObjectRequest.toString(), RequestHeader.JSON_TYPE);

        MDRetrofit.getInstance()
                .createService(ServiceAddressType.getCustomAddress("http://ams4.shmedo.com:22000/api/v1/"))
                .DeviceLogin(body)
                .doOnDispose(() -> Timber.i("Disposing subscription"))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .to(autoDisposable(AndroidLifecycleScopeProvider.from(getViewLifecycleOwner())))
                .subscribe(new BaseObserver<DeviceDebugAddress>() {
                    @Override
                    protected void onResponse(DeviceDebugAddress deviceDebugAddress, ErrorInfo errorInfo) {
                        if (!ResponseHandler.getInstance().handleResponse(errorInfo)) {
                            if (errorInfo.getCode() == 0) {
                                if (deviceDebugAddress == null || deviceDebugAddress.getDeviceServerInfo() == null || TextUtils.isEmpty(deviceDebugAddress.getDeviceServerInfo().getServerAddr())) {
                                    ToastUtils.show("未获取到远程服务器地址和端口信息");
                                    return;
                                }
                                TcpToBleDebugActivity.startActivity(mActivity, deviceDebugAddress.getDeviceServerInfo().getServerAddr(), deviceDebugAddress.getDeviceServerInfo().getServerPort(),true);
                            } else {
                                if (!TextUtils.isEmpty(errorInfo.getMsg())) {
                                    ToastUtils.show(errorInfo.getMsg());
                                }
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        ResponseHandler.getInstance().handleFailure((Exception) e);
                    }
                });
    }
}
