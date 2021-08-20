package com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb.bluetooth_debug_box.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.at.ATCommand;
import com.shmedo.configlibrary.at.WHBLE102CommandType;
import com.shmedo.core.util.DeviceInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.usb_serial.ATCommandItem;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/18 <br/>
 * 描述：     TODO
 */
public class QueryBluetoothLinkStatusDialogFragment extends BaseDebugBoxDialogFragment {
    private int USB_SERIAL_LINK_QUERY = 0x10001;//

    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.tv_link_status)
    TextView mTvLinkStatus;

    @BindView(R.id.tv_mac_address)
    TextView mTvMacAddress;

    @BindView(R.id.tv_rssi)
    TextView mTvRssi;

    @BindView(R.id.btn_query_link)
    Button mBtnQuery;


    public static QueryBluetoothLinkStatusDialogFragment newInstance() {
        return new QueryBluetoothLinkStatusDialogFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.query_bluetooth_link_status_dialog_fragment;
    }

    @Override
    protected void setWindowStyle(int gravity) {
        super.setWindowStyle(Gravity.CENTER);
        Dialog mDialog = getDialog();
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.width = (int) (DeviceInfo.getScreenWidth() * 0.9f);
        wlp.height = (int) (DeviceInfo.getScreenHeight() * 0.6f);
        window.setAttributes(wlp);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTvTitle.setText("连接状态");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isConnected()) {
            enterCommand();
        }
    }

    /**
     * 进入命令模式
     */
    private void enterCommand() {
        atCommandItems.clear();

        ATCommandItem atCommandItem = new ATCommandItem(WHBLE102CommandType.ENTER_COMMAND, WHBLE102CommandType.ENTER_COMMAND.toString());
        atCommandItems.add(atCommandItem);//进入命令模式

        sendCommandFromCmdList(USB_SERIAL_LINK_QUERY, WRITE_TIME_OUT_500_MILLIS);
    }

    /**
     * 查询蓝牙测斜仪设备连接状态
     */
    private void QueryBluetoothLinkStatus() {
        atCommandItems.clear();

        String command = ATCommand.COMMAND_HEADER + WHBLE102CommandType.LINK.toString() + ATCommand.QUERY_FLAG + ATCommand.NEWLINE_CRLF;
        ATCommandItem atCommandItem = new ATCommandItem(WHBLE102CommandType.LINK, command);
        atCommandItems.add(atCommandItem);

        sendCommandFromCmdList(USB_SERIAL_LINK_QUERY, WRITE_TIME_OUT_1000_MILLIS);
    }


    @OnClick({R.id.iv_close, R.id.btn_query_link})
    public void onClick(View view) {
        if (isDoubleClick(view)) {
            return;
        }
        int id = view.getId();
        if (id == R.id.iv_close) {
            dismiss();

        } else if (id == R.id.btn_query_link) {
            if (!isConnected()) {
                ToastUtils.show(getString(R.string.usb_config_disconnect_warn));
                return;
            }
            mBtnQuery.setEnabled(false);
            QueryBluetoothLinkStatus();
        }
    }

    @Override
    protected void parseResponseMessage(byte[] data) {
        try {
            resultByteBuf.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void customHandleMessage(@NonNull @NotNull Message msg) {
        String cmdStr = resultByteBuf.toString();
        resultByteBuf.reset();
        Timber.e("接收串口数据: %s", cmdStr);
        if (atCommandItems.size() == 0)
            return;

        ATCommandItem commandItem = atCommandItems.getFirst();
        atCommandItems.removeFirst();//移除已经发送完的指令
        if (msg.what == USB_SERIAL_LINK_QUERY) {
            switch (commandItem.getCommandType()) {
                case ENTER_COMMAND: {
                    cmdStr = filterControlCharacter(cmdStr);
                    if (cmdStr.contains("a+ok") || TextUtils.isEmpty(cmdStr)) {
                        QueryBluetoothLinkStatus();
                    } else if (cmdStr.toUpperCase().contains("ERR")) {
                        cmdStr = filterControlCharacter(commandItem.getCommand());
                        Timber.e("%s  出错", cmdStr);
                        ToastUtils.show(cmdStr + "  出错");
                    }
                }
                break;

                case LINK: {
                    mBtnQuery.setEnabled(true);
                    if (cmdStr.toUpperCase().contains(WHBLE102CommandType.LINK.toString()) && cmdStr.contains(ATCommand.OK_FLAG)) {
                        if (cmdStr.toUpperCase().contains("ONLINE")) {
                            mTvLinkStatus.setText("Online");
                            cmdStr = cmdStr.replace(ATCommand.NEWLINE_CR, "").replace(ATCommand.NEWLINE_LF, "").toUpperCase().trim();
                            //{CR}{LF}PeerAddr:MAC{CR}{LF}Rssi:RssidBm{CR}{LF}+LINK:status{CR}{LF}OK{CR}{LF}
                            String mac = cmdStr.substring(cmdStr.indexOf("PEERADDR:"), cmdStr.indexOf("RSSI")).replace("PEERADDR:", "");
                            String rssi = cmdStr.substring(cmdStr.indexOf("RSSI:"), cmdStr.indexOf("DBM")).replace("RSSI:", "");
                            mTvMacAddress.setText(mac);
                            mTvRssi.setText(rssi + "dBm");
                        } else {
                            mTvLinkStatus.setText("Offline");
                        }
                    }else if (cmdStr.toUpperCase().contains("ERR")) {
                        cmdStr = filterControlCharacter(commandItem.getCommand());
                        Timber.e("%s  出错", cmdStr);
                        ToastUtils.show(cmdStr + "  出错");
                    }
                }
                break;
            }
        }
    }
}
