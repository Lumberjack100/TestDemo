package com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb.bluetooth_debug_box.dialog;

import android.app.Dialog;
import android.os.Bundle;
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
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.BaseDialogFragment;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb.bluetooth_debug_box.BluetoothDebugBoxHomeFragment;
import com.yanzhenjie.recyclerview.widget.DefaultItemDecoration;

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
public class ConfigLinkMacDialogFragment extends BaseDialogFragment {
    @BindView(R.id.tv_title)
    TextView mTvTitle;

    @BindView(R.id.recyclerview)
    RecyclerView mRecyclerView;

    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;

    private InclinometerAddrInfoAdapter adapter;

    private List<InclinometerMacInfo> macInfoList = new ArrayList<>();
    private InclinometerMacInfo macInfo = null;

    private BluetoothDebugBoxHomeFragment debugBoxHomeFragment;

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
        debugBoxHomeFragment = (BluetoothDebugBoxHomeFragment) getParentFragment();
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
        if (debugBoxHomeFragment.isConnected()) {
            sendScanCommand();
        }
    }

    private void sendScanCommand() {
        debugBoxHomeFragment.atCommandItems.clear();

        String command = ATCommand.COMMAND_HEADER + WHBLE102CommandType.SCAN.toString() + ATCommand.QUERY_FLAG + ATCommand.NEWLINE_CRLF;
        ATCommandItem atCommandItem = new ATCommandItem(WHBLE102CommandType.SCAN, command);
        debugBoxHomeFragment.atCommandItems.add(atCommandItem);

        debugBoxHomeFragment.sendCommandFromCmdList(AppContants.MsgWhat.USB_SERIAL_AT_SCAN, 5000);
    }

    @OnClick({R.id.iv_close, R.id.btn_scan, R.id.btn_save, R.id.btn_link})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_close:
                dismiss();
                break;

            case R.id.btn_scan:
                if (!debugBoxHomeFragment.isConnected()) {
                    ToastUtils.show(getString(R.string.usb_config_disconnect_warn));
                    return;
                }
                sendScanCommand();
                break;

            case R.id.btn_save:
                if (!debugBoxHomeFragment.isConnected()) {
                    ToastUtils.show(getString(R.string.usb_config_disconnect_warn));
                    return;
                }
                if (macInfo == null || !macInfo.isChecked()) {
                    ToastUtils.show("请先选中要操作的设备");
                }
                break;

            case R.id.btn_link:
                break;
        }
    }

    public void showConnectDialog(String content) {
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(requireContext())
                .title("温馨提示：")
                .content("确定连接此设备？")
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
        debugBoxHomeFragment.atCommandItems.clear();

        String command = ATCommand.COMMAND_HEADER + WHBLE102CommandType.CONNADD.toString() + "=" + macInfo.getAddr() + ATCommand.NEWLINE_CRLF;
        ATCommandItem atCommandItem = new ATCommandItem(WHBLE102CommandType.CONNADD, command);
        debugBoxHomeFragment.atCommandItems.add(atCommandItem);

        command = ATCommand.COMMAND_HEADER + WHBLE102CommandType.AUTOCONN.toString() + "=ON" + ATCommand.NEWLINE_CRLF;
        atCommandItem = new ATCommandItem(WHBLE102CommandType.AUTOCONN, command);
        debugBoxHomeFragment.atCommandItems.add(atCommandItem);

        command = ATCommand.COMMAND_HEADER + WHBLE102CommandType.CONN.toString() + "=" + macInfo.getNo() + ATCommand.NEWLINE_CRLF;
        atCommandItem = new ATCommandItem(WHBLE102CommandType.CONN, command);
        debugBoxHomeFragment.atCommandItems.add(atCommandItem);

        debugBoxHomeFragment.sendCommandFromCmdList(AppContants.MsgWhat.USB_SERIAL_AT_CONN, 500);
    }

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

    public void updateFailureStatus() {
        mProgressBar.setVisibility(View.GONE);
        String content = String.format("蓝牙测斜仪连接超时！", macInfo.getAddr());

        ToastUtils.show(content);
    }
}