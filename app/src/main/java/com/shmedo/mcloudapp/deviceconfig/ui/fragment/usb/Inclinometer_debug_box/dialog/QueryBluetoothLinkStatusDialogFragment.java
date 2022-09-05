package com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb.Inclinometer_debug_box.dialog;

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

import com.blankj.utilcode.util.ScreenUtils;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.at.ATCommand;
import com.shmedo.configlibrary.at.WHBLE102CommandType;
import com.shmedo.core.AppContants;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.model.usb_serial.ATCommandItem;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/18 <br/>
 * 描述：     TODO
 */
public class QueryBluetoothLinkStatusDialogFragment extends BaseDebugBoxDialogFragment {

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
        wlp.width = (int) (ScreenUtils.getScreenWidth() * 0.9f);
        wlp.height = (int) (ScreenUtils.getScreenHeight() * 0.6f);
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
            mBtnQuery.setEnabled(false);
            enterCommand();
        }
    }

    @OnClick({R.id.iv_close, R.id.btn_query_link})
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.iv_close) {
            dismiss();

        } else if (id == R.id.btn_query_link) {
            if (!isConnected()) {
                ToastUtils.show(StringUtils.getString(R.string.usb_config_disconnect_warn));
                return;
            }
            stopProgressAll();
            mBtnQuery.setEnabled(false);
            QueryBluetoothLinkStatus();
        }
    }


    @Override
    protected void customHandleMessage(@NonNull @NotNull Message msg) {
        if (atCommandItems.size() == 0)
            return;
        String cmdStr = resultByteBuf.toString();
        resultByteBuf.reset();
        Timber.e("接收串口数据: %s", cmdStr);

        ATCommandItem commandItem = atCommandItems.getFirst();
        atCommandItems.removeFirst();//移除已经发送完的指令
        if (msg.what == AppContants.UsbSerialMsgWhat.USB_SERIAL_LINK_QUERY.getCode()) {
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
                    } else if (cmdStr.toUpperCase().contains("ERR")) {
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
