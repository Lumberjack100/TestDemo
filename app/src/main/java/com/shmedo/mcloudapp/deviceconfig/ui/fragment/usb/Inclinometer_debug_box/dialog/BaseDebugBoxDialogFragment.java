package com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb.Inclinometer_debug_box.dialog;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.shmedo.configlibrary.at.ATCommand;
import com.shmedo.configlibrary.ble.utils.CRC16;
import com.shmedo.configlibrary.ble.utils.StringUtil;
import com.shmedo.mcloudapp.deviceconfig.model.usb_serial.ATCommandItem;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.BaseDialogFragment;
import com.shmedo.mcloudapp.profile.USBSerialViewModel;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.util.LinkedList;

import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/17 <br/>
 * 描述：     TODO
 */
public abstract class BaseDebugBoxDialogFragment extends BaseDialogFragment {
    protected static final int WRITE_TIME_OUT_500_MILLIS = 500;//发送指令超时时间
    protected static final int WRITE_TIME_OUT_1000_MILLIS = 1000;//发送指令超时时间
    protected static final int WRITE_TIME_OUT_2000_MILLIS = 2000;//发送指令超时时间
    protected static final int SCAN_TIME_OUT_MILLIS = 10000;//扫描指令超时时间

    protected USBSerialViewModel usbSerialViewModel;

    protected LinkedList<ATCommandItem> atCommandItems = new LinkedList<>();

    protected ByteArrayOutputStream resultByteBuf = new ByteArrayOutputStream();

    private final InnerHandler mInnerHandler = new InnerHandler(this);


    private static class InnerHandler extends Handler {
        private final WeakReference<BaseDebugBoxDialogFragment> fragmentWeakReference;

        public InnerHandler(BaseDebugBoxDialogFragment fragment) {
            fragmentWeakReference = new WeakReference<>(fragment);
        }

        @Override
        public void handleMessage(Message msg) {
            BaseDebugBoxDialogFragment fragment = fragmentWeakReference.get();
            if (fragment != null) {
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
     * 解析设备的参数指令
     */
    protected void parseResponseMessage(byte[] data) {

    }

    /**
     * This method returns true if the device is connected. Services could have not been
     * discovered yet.
     */
    public final boolean isConnected() {
        return usbSerialViewModel.isConnected();
    }

    protected void sendCommandFromCmdList(int what, long delayMillis) {
        if (atCommandItems.size() > 0) {
            String command = atCommandItems.getFirst().getCommand();
            usbSerialViewModel.sendData(command);
            startProgress(what, delayMillis);
        }
    }

    protected void sendHexCommandFromCmdList(int what, long delayMillis) {
        if (atCommandItems.size() > 0) {
            String command = atCommandItems.getFirst().getCommand();
            byte[] data = StringUtil.hexStringToBytes2(command);
            usbSerialViewModel.sendData(data);
            startProgress(what, delayMillis);
        }
    }

    protected String filterControlCharacter(String str) {
        str = str.replace(ATCommand.OK_FLAG, "")
                .replace(ATCommand.NEWLINE_CR, "")
                .replace(ATCommand.NEWLINE_LF, "");
        return str;
    }

    protected boolean checkCRCData(String hexData) {
        hexData = hexData.trim();
        String crcStr = hexData.substring(hexData.length() - 4);
        String rawData = hexData.replace(crcStr, "");

        return CRC16.getCRC(rawData).equals(crcStr);
    }

    @Override
    public void onStop() {
        super.onStop();
        stopProgressAll();
    }
}
