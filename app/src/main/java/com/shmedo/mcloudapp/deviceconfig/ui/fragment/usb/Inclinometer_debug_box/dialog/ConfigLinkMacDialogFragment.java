package com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb.Inclinometer_debug_box.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.toast.ToastUtils;
import com.shmedo.configlibrary.at.ATCommand;
import com.shmedo.configlibrary.at.WHBLE102CommandType;
import com.shmedo.core.AppContants;
import com.shmedo.core.util.DensityUtil;
import com.shmedo.core.util.DeviceInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.adapter.usb_serial.InclinometerAddrInfoAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.usb_serial.ATCommandItem;
import com.shmedo.mcloudapp.deviceconfig.model.usb_serial.InclinometerMacInfo;
import com.yanzhenjie.recyclerview.widget.DefaultItemDecoration;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 创建者:   gonghe <br/>
 * 创建时间:  2021/8/13 <br/>
 * 描述：    配置蓝牙测斜仪 MAC 连接地址
 */
@Deprecated
public class ConfigLinkMacDialogFragment extends BaseDebugBoxDialogFragment {
    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;

    private InclinometerAddrInfoAdapter adapter;

    private List<InclinometerMacInfo> macInfoList = new ArrayList<>();
    private InclinometerMacInfo macInfo = null;

    private int queryCont = 0;


    public static ConfigLinkMacDialogFragment newInstance() {
        return new ConfigLinkMacDialogFragment();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.config_link_mac_dialog_fragment;
    }

    @Override
    protected void setWindowStyle(int gravity) {
        super.setWindowStyle(Gravity.CENTER);
        Dialog mDialog = getDialog();
        Window window = mDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.width = (int) (DeviceInfo.getScreenWidth() * 0.9f);
        wlp.height = (int) (DeviceInfo.getScreenHeight() * 0.7f);
        window.setAttributes(wlp);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTvTitle.setText("配置连接");
        initAdapter();
    }

    private void initAdapter() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        DefaultItemDecoration mItemDecoration = new DefaultItemDecoration(ContextCompat.getColor(getActivity(), R.color.divider_line_bg_efefef), 0, DensityUtil.Dp2Px(getActivity(), 0.5f));
        mRecyclerView.addItemDecoration(mItemDecoration);
        adapter = new InclinometerAddrInfoAdapter(macInfoList);
//        adapter.setAnimationEnable(true);
//        adapter.setAnimationFirstOnly(false);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                macInfo = macInfoList.get(position);
//                if (macInfo.isChecked()) {
//                    return;
//                }
//                for (InclinometerMacInfo info : macInfoList) {
//                    if (!info.getAddr().equals(macInfo.getAddr()))
//                        info.setChecked(false);
//                }
//                macInfo.setChecked(true);
//                adapter.notifyDataSetChanged();

                String content = String.format("确定连接 %s 设备？", macInfo.getAddr());
                showConnectDialog(content);
            }
        });
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isConnected()) {
            sendScanCommand();
        }
    }

    private void sendScanCommand() {
        atCommandItems.clear();

        String command = ATCommand.COMMAND_HEADER + WHBLE102CommandType.SCAN.toString() + ATCommand.QUERY_FLAG + ATCommand.NEWLINE_CRLF;
        ATCommandItem atCommandItem = new ATCommandItem(WHBLE102CommandType.SCAN, command);
        atCommandItems.add(atCommandItem);

        sendCommandFromCmdList(AppContants.MsgWhat.USB_SERIAL_AT_SCAN, SCAN_TIME_OUT_MILLIS);
    }

    /**
     * 查询蓝牙测斜仪设备连接状态
     */
    private void queryBluetoothLinkStatus() {
        atCommandItems.clear();

        String command = ATCommand.COMMAND_HEADER + WHBLE102CommandType.LINK.toString() + ATCommand.QUERY_FLAG + ATCommand.NEWLINE_CRLF;
        ATCommandItem atCommandItem = new ATCommandItem(WHBLE102CommandType.LINK, command);
        atCommandItems.add(atCommandItem);
    }

    @OnClick({R.id.iv_close, R.id.btn_scan, R.id.btn_save, R.id.btn_link})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_close:
                dismiss();
                break;

            case R.id.btn_scan:
                if (!isConnected()) {
                    ToastUtils.show(getString(R.string.usb_config_disconnect_warn));
                    return;
                }
                sendScanCommand();
                break;
        }
    }

    public void showConnectDialog(String content) {
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
                        processLink();
                        mProgressBar.setVisibility(View.VISIBLE);
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    /**
     * 1.发送 AT+CONNADD=mac地址 设置默认连接的mac地址
     * 2.发送 AT+AUTOCONN=on 使能自动重连
     * 3.发送 AT+CONN=num 通过搜索到索引号快速建立连接
     */
    private void processLink() {
        if (macInfo == null) {
            return;
        }
        atCommandItems.clear();

        String command = ATCommand.COMMAND_HEADER + WHBLE102CommandType.CONNADD.toString() + "=" + macInfo.getAddr() + ATCommand.NEWLINE_CRLF;
        ATCommandItem atCommandItem = new ATCommandItem(WHBLE102CommandType.CONNADD, command);
        atCommandItems.add(atCommandItem);

        command = ATCommand.COMMAND_HEADER + WHBLE102CommandType.AUTOCONN.toString() + "=ON" + ATCommand.NEWLINE_CRLF;
        atCommandItem = new ATCommandItem(WHBLE102CommandType.AUTOCONN, command);
        atCommandItems.add(atCommandItem);

        command = ATCommand.COMMAND_HEADER + WHBLE102CommandType.CONN.toString() + "=" + macInfo.getNo() + ATCommand.NEWLINE_CRLF;
        atCommandItem = new ATCommandItem(WHBLE102CommandType.CONN, command);
        atCommandItems.add(atCommandItem);

        sendCommandFromCmdList(AppContants.MsgWhat.USB_SERIAL_AT_CONNECT, 500);
    }

//    private void setResultData(String cmdStr) {
//        resultBuilder.append(cmdStr);
//
//        //处理 AT+SCAN 指令
//        if (atCommandItems.size() > 0) {
//            ATCommandItem commandItem = atCommandItems.getFirst();
//            if (commandItem.getCommandType() == WHBLE102CommandType.SCAN) {
//                parseScanInfo(resultBuilder);
//            }
//        }
//    }

    /**
     * 解析处理扫描到的设备信息
     *
     * @param resultBuilder
     */
    public void parseScanInfo(StringBuilder resultBuilder) {
        String cmdStr = resultBuilder.toString();
        if (cmdStr.contains(WHBLE102CommandType.SCAN.toString()) && cmdStr.contains("OFF")) {
            //扫描失败处理
            ToastUtils.show("扫描失败");
            return;
        }
        /* +SCAN:ON
                OK

                No: 1 Addr:60B09416BAB3 RSSI:-80 dBm

                No: 2 Addr:6ADEE34C5522 RSSI:-72 dBm

                No: 3 Addr:9CA525996D13 RSSI:-89 dBm */
        if (cmdStr.contains("No") && cmdStr.contains("dBm\r\n")) {
            //No: 1 Addr:60B09416BAB3 RSSI:-80 dBm\r\n
            int startIndex = cmdStr.indexOf("No");
            int endIndex = cmdStr.lastIndexOf("dBm\r\n") + 5;

            resultBuilder.setLength(0);
            resultBuilder.append(cmdStr.substring(endIndex - 2));

            cmdStr = cmdStr.substring(startIndex, endIndex);
            String[] addrRssis = cmdStr.split(ATCommand.NEWLINE_CRLF);
            for (String addrRssi : addrRssis) {
                Timber.e("拆分数据: %s", cmdStr);
                if (!TextUtils.isEmpty(addrRssi) && addrRssi.startsWith("No") && addrRssi.endsWith("dBm")) {
                    boolean isAdd = false;
                    for (InclinometerMacInfo macInfo : macInfoList) {
                        if (addrRssi.contains(macInfo.getAddr())) {
                            isAdd = true;
                            break;
                        }
                    }
                    //macInfoList
                    if (!isAdd) {
                        addrRssi = addrRssi.trim();
                        String no = addrRssi.substring(0, addrRssi.indexOf("Addr")).replace("No:", "");
                        String addr = addrRssi.substring(addrRssi.indexOf("Addr:"), addrRssi.indexOf("RSSI")).replace("Addr:", "");
                        String rssi = addrRssi.substring(addrRssi.indexOf("RSSI:")).replace("RSSI:", "");
                        InclinometerMacInfo macInfo = new InclinometerMacInfo(no, addr, rssi);
                        macInfoList.add(macInfo);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }

    public void updateSuccessStatus() {
        mProgressBar.setVisibility(View.GONE);
        String content = String.format("蓝牙测斜仪连接成功！", macInfo.getAddr());
        ToastUtils.show(content);
    }

    public void updateFailureStatus(String content) {
        mProgressBar.setVisibility(View.GONE);
        ToastUtils.show(content);
    }

    @Override
    protected void customHandleMessage(@NonNull @NotNull Message msg) {
        if (msg.what == AppContants.MsgWhat.USB_SERIAL_AT_SCAN) {


        } else if (msg.what == AppContants.MsgWhat.USB_SERIAL_AT_CONNECT) {
            String cmdStr = resultByteBuf.toString();
            resultByteBuf.reset();
            Timber.e("接收串口数据: %s", cmdStr);
            if (atCommandItems.size() == 0)
                return;

            ATCommandItem commandItem = atCommandItems.getFirst();
            atCommandItems.removeFirst();//移除已经发送完的指令
            switch (commandItem.getCommandType()) {
                case CONNADD: {
                    if (cmdStr.contains(WHBLE102CommandType.CONNADD.toString()) && cmdStr.contains(ATCommand.OK_FLAG)) {
                        sendCommandFromCmdList(AppContants.MsgWhat.USB_SERIAL_AT_CONNECT, WRITE_TIME_OUT_500_MILLIS);
                    } else {
                        updateFailureStatus("蓝牙测斜仪连接失败！");
                    }
                }
                break;

                case AUTOCONN: {
                    if (cmdStr.contains(WHBLE102CommandType.AUTOCONN.toString()) && cmdStr.contains(ATCommand.OK_FLAG)) {
                        sendCommandFromCmdList(AppContants.MsgWhat.USB_SERIAL_AT_CONNECT, WRITE_TIME_OUT_500_MILLIS);
                    } else {
                        updateFailureStatus("蓝牙测斜仪连接失败！");
                    }
                }
                break;

                case CONN: {
                    if (cmdStr.contains(WHBLE102CommandType.CONN.toString()) && cmdStr.contains(ATCommand.OK_FLAG)) {
                        if (atCommandItems.size() == 0) {
                            ATCommandItem atCommandItem = new ATCommandItem(WHBLE102CommandType.ENTER_COMMAND, WHBLE102CommandType.ENTER_COMMAND.toString());
                            atCommandItems.add(atCommandItem);//进入命令模式
                        }
                        sendCommandFromCmdList(AppContants.MsgWhat.USB_SERIAL_AT_CONNECT, WRITE_TIME_OUT_500_MILLIS);
                    } else {
                        updateFailureStatus("蓝牙测斜仪连接失败！");
                    }
                }
                break;

                case ENTER_COMMAND: {
                    cmdStr = cmdStr.replace(ATCommand.NEWLINE_CR, "").replace(ATCommand.NEWLINE_LF, "").trim();
                    if (cmdStr.contains("a+ok") || TextUtils.isEmpty(cmdStr)) {
                        if (atCommandItems.size() == 0) {
                            queryCont = 1;
                            queryBluetoothLinkStatus();
                        }
                        sendCommandFromCmdList(AppContants.MsgWhat.USB_SERIAL_AT_CONNECT, WRITE_TIME_OUT_500_MILLIS);
                    }
                }
                break;

                case LINK: {
                    if (cmdStr.contains(WHBLE102CommandType.LINK.toString()) && cmdStr.contains(ATCommand.OK_FLAG)) {
                        if (cmdStr.toUpperCase().contains("ONLINE")) {
                            queryCont = 0;
                            //连接成功
                            updateSuccessStatus();
                        } else {
                            //查询连接状态超过10次，判定超时
                            if (queryCont >= 3 || !isConnected()) {
                                stopProgress(AppContants.MsgWhat.USB_SERIAL_AT_CONNECT);
                                updateFailureStatus("蓝牙测斜仪连接超时！");
                                return;
                            }
                            queryCont++;
                            queryBluetoothLinkStatus();
                            sendCommandFromCmdList(AppContants.MsgWhat.USB_SERIAL_AT_CONNECT, 1000);
                        }
                    } else {
                        updateFailureStatus("蓝牙测斜仪连接失败！");
                    }
                }
                break;
            }
        }
    }
}