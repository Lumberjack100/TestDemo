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
import com.shmedo.configlibrary.at.ATCommand;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.deviceconfig.model.usb_serial.ATCommandItem;
import com.shmedo.mcloudapp.profile.USBSerialViewModel;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.util.LinkedList;

import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/11 <br/>
 * 描述：     TODO
 */
public abstract class BaseUSBSerialCommunicateFragment extends BaseFragment {
    protected static final int WRITE_TIME_OUT_500_MILLIS = 500;//发送指令超时时间
    protected static final int WRITE_TIME_OUT_1000_MILLIS = 1000;//发送指令超时时间

    protected USBSerialViewModel usbSerialViewModel;

    protected boolean isExitMode = false;//是否退出页面标志

    protected int cmdRepeatCount = 0;

    protected LinkedList<ATCommandItem> atCommandItems = new LinkedList<>();

    protected ByteArrayOutputStream resultByteBuf = new ByteArrayOutputStream();

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
        usbSerialViewModel.getResponseMsg().observe(getViewLifecycleOwner(), new Observer<byte[]>() {
            @Override
            public void onChanged(byte[] data) {
                try {
                    parseResponseMessage(data);
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
    protected void parseResponseMessage(byte[] data) {

    }

    protected void sendCommandFromCmdList(int what, long delayMillis) {
        if (atCommandItems.size() > 0) {
            String command = atCommandItems.getFirst().getCommand();
            usbSerialViewModel.sendData(command);
            startProgress(what, delayMillis);
        }
    }

    protected String filterControlCharacter(String str) {
        str = str.replace(ATCommand.OK_FLAG, "")
                .replace(ATCommand.NEWLINE_CR, "")
                .replace(ATCommand.NEWLINE_LF, "");
        return str;
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
