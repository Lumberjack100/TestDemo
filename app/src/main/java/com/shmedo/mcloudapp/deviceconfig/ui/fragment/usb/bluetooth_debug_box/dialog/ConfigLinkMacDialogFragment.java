package com.shmedo.mcloudapp.deviceconfig.ui.fragment.usb.bluetooth_debug_box.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.shmedo.configlibrary.at.ATCommand;
import com.shmedo.configlibrary.at.WHBLE102CommandType;
import com.shmedo.core.util.DeviceInfo;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.deviceconfig.adapter.usb_serial.InclinometerAddrInfoAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.usb_serial.InclinometerMacInfo;
import com.shmedo.mcloudapp.deviceconfig.ui.fragment.dialog.BaseDialogFragment;
import com.shmedo.mcloudapp.profile.USBSerialViewModel;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

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

    private InclinometerAddrInfoAdapter adapter;

    private List<InclinometerMacInfo> macInfoList = new ArrayList<>();
    private InclinometerMacInfo macInfo = null;

    private USBSerialViewModel usbSerialViewModel;


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
        wlp.width =(int) (DeviceInfo.getScreenWidth() * 0.9f);
        wlp.height = (int) (DeviceInfo.getScreenHeight() * 0.7f);
        window.setAttributes(wlp);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        usbSerialViewModel = getApplicationScopeViewModel(USBSerialViewModel.class);
        mTvTitle.setText("配置连接");
        initAdapter();
    }

    private void initAdapter() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
//        DefaultItemDecoration mItemDecoration = new DefaultItemDecoration(ContextCompat.getColor(getActivity(), R.color.divider_line_bg_efefef), 0, DensityUtil.Dp2Px(getActivity(), 0.5f));
//        mRecyclerView.addItemDecoration(mItemDecoration);
        adapter = new InclinometerAddrInfoAdapter(macInfoList);
        adapter.setAnimationEnable(true);
        adapter.setAnimationFirstOnly(false);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                macInfo = macInfoList.get(position);
//                if (firmWareInfo.isChecked()) {
//                    return;
//                }
//
//                for (FirmWareInfo info : firmWareInfoList) {
//                    info.setChecked(false);
//                }
//                firmWareInfo.setChecked(true);
//                adapter.notifyDataSetChanged();
            }
        });
        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (usbSerialViewModel.isConnected()) {
            sendScanCommand();
        }
    }

    private void sendScanCommand() {
        String command = ATCommand.COMMAND_HEADER + WHBLE102CommandType.SCAN.toString() + ATCommand.QUERY_FLAG + ATCommand.NEWLINE_CRLF;
        usbSerialViewModel.sendData(command);
    }

    @OnClick({R.id.iv_close, R.id.btn_scan, R.id.btn_save, R.id.btn_link})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_close:
                dismiss();
                break;

            case R.id.btn_scan:
                break;

            case R.id.btn_save:
                break;

            case R.id.btn_link:
                break;
        }
    }

    /**
     * 刷新扫描到的地址信息
     */
    public void addMacInfo(InclinometerMacInfo macInfo) {
        if (macInfo != null) {
            macInfoList.add(macInfo);
            adapter.notifyDataSetChanged();
        }
    }

}