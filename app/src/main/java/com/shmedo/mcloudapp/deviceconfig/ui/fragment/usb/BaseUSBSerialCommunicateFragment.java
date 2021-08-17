package com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.iot.enums.IOTCommandType;
import com.shmedo.configlibrary.iot.utils.IOTStringUtil;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.profile.USBSerialViewModel;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;

import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/11 <br/>
 * 描述：     TODO
 */
public abstract class BaseUSBSerialCommunicateFragment extends BaseFragment {
    protected static final int WRITE_TIME_OUT_MILLIS = 500;//发送指令超时时间
    protected static final int SCAN_TIME_OUT_MILLIS = 5000;//扫描指令超时时间

    protected USBSerialViewModel usbSerialViewModel;

    protected boolean isExitMode = false;//是否退出页面标志
    protected StringBuilder resultBuilder = new StringBuilder();

    private final InnerHandler mInnerHandler = new InnerHandler(this);


    private static class InnerHandler extends Handler {
        private final WeakReference<BaseUSBSerialCommunicateFragment> fragmentWeakReference;

        public InnerHandler(BaseUSBSerialCommunicateFragment fragment) {
            fragmentWeakReference = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            BaseUSBSerialCommunicateFragment fragment = fragmentWeakReference.get();
            if (fragment != null) {
//                fragment.dismissProgressDialog();
                fragment.customHandleMessage(msg);
            }
        }
    }

    protected void customHandleMessage(@NonNull @NotNull Message msg) {

    }

    protected void startProgress(int what, long delayMillis) {
        mInnerHandler.sendEmptyMessageDelayed(what, delayMillis);
    }

    protected void stopProgress(int what) {
        mInnerHandler.removeMessages(what);
    }

    protected void stopProgressAll() {
        mInnerHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        usbSerialViewModel = getApplicationScopeViewModel(USBSerialViewModel.class);
        usbSerialViewModel.getResponseMsg().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String result) {
                try {
                    parseResponseMessage(result);
                } catch (Exception ex) {
                    Timber.e(ex);
                }
            }
        });
    }


    /**
     * USB建立连接
     */
    protected void connectDevice() {
        usbSerialViewModel.connect();
    }

    /**
     * USB 取消连接
     */
    protected void disconnectDevice() {
        Timber.d("disconnectDevice()调用");
        usbSerialViewModel.disconnect();
    }

    /**
     * This method returns true if the device is connected. Services could have not been
     * discovered yet.
     */
    public final boolean isConnected() {
        return usbSerialViewModel.isConnected();
    }

    /**
     * 解析设备的参数指令
     */
    protected void parseResponseMessage(String cmdStr) {
//        stopProgressAll();
        IOTCommandType type = IOTStringUtil.extractCommandType(cmdStr);
        if (type == IOTCommandType.UNKNOWN_TYPE) {
            Timber.e("未知的命令:%s", cmdStr);
            ToastUtils.show("未知的命令:" + cmdStr);
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

    @Override
    public void onStop() {
        super.onStop();
        stopProgressAll();
    }
}
