package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.toast.ToastUtils;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.shmedo.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.common.ui.fragment.BaseFragment;
import com.shmedo.mcloudapp.deviceconfig.adapter.USBDeviceAdapter;
import com.shmedo.mcloudapp.deviceconfig.model.USBDeviceItem;
import com.shmedo.mcloudapp.deviceconfig.ui.activity.usb.BluetoothDebugBoxHomeActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class USBDeviceListFragment extends BaseFragment {
    private String USB_PERMISSION = "com.shmedo.mcloudapp.usb.permission";

    @BindView(R.id.tv_usb_count)
    TextView mTvUSBCount;

    @BindView(R.id.iv_refresh_scan)
    ImageView mIvRefreshScan;

    @BindView(R.id.tv_scan_state)
    TextView mTvScanState;

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    private USBDeviceAdapter usbDeviceAdapter;

    private List<USBDeviceItem> usbDeviceItemList = new ArrayList<>();

    private USBDeviceItem usbDeviceItem;

    private Animator animator;

    private int baudRate = 57600;


    public static USBDeviceListFragment newInstance() {
        return new USBDeviceListFragment();
    }

    private BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                refresh();
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                usbDeviceItemList.clear();
                usbDeviceAdapter.setList(usbDeviceItemList);
                usbDeviceAdapter.setEmptyView(R.layout.empty_usb_device);
            }
        }
    };

    @Override
    protected int getLayoutId() {
        return R.layout.usb_device_list_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initAdapter();
        initRefreshAnimation();
    }

    @Override
    public void onResume() {
        super.onResume();
        register();
        refresh();
    }

    private void initAdapter() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        usbDeviceAdapter = new USBDeviceAdapter();
//        usbDeviceAdapter.setAnimationEnable(true);
//        usbDeviceAdapter.setAnimationFirstOnly(false);
        mRecyclerView.setAdapter(usbDeviceAdapter);
        usbDeviceAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                usbDeviceItem = usbDeviceAdapter.getItem(position);
                if (usbDeviceItem.getDriver() == null) {
                    ToastUtils.show("no driver");
                    return;
                }
                showBaudDialog();
            }
        });
    }

    private void initRefreshAnimation() {
        animator = AnimatorInflater.loadAnimator(mActivity, R.animator.rotation);
        animator.setTarget(mIvRefreshScan);
    }

    /**
     * 动态注册usb广播，拔插动作，注册动作
     */
    private void register() {
        //注册在此service下的receiver的监听的action
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        mActivity.registerReceiver(usbReceiver, intentFilter);//注册receiver
    }

    private void refresh() {
        UsbManager usbManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);
        UsbSerialProber usbDefaultProber = UsbSerialProber.getDefaultProber();
//        UsbSerialProber usbCustomProber = CustomProber.getCustomProber();
        usbDeviceItemList.clear();
        for (UsbDevice device : usbManager.getDeviceList().values()) {
            UsbSerialDriver driver = usbDefaultProber.probeDevice(device);
//            if(driver == null) {
//                driver = usbCustomProber.probeDevice(device);
//            }
            if (driver != null) {
                for (int port = 0; port < driver.getPorts().size(); port++)
                    usbDeviceItemList.add(new USBDeviceItem(device, port, driver));
            } else {
                //不添加 driver 为 null 的设备
                //usbDeviceItemList.add(new USBDeviceItem(device, 0, null));
            }
        }
        usbDeviceAdapter.setList(usbDeviceItemList);
        if (usbDeviceItemList.size() == 0) {
            usbDeviceAdapter.setEmptyView(R.layout.empty_usb_device);
        }
    }

    /**
     * 选择波特率
     */
    private void showBaudDialog() {
        final String[] baudRates = getResources().getStringArray(R.array.baud_rates);
        int pos = Arrays.asList(baudRates).indexOf(String.valueOf(baudRate));
        MaterialDialog.Builder mBuilder = new MaterialDialog.Builder(mActivity)
                .title("选择波特率")
                .contentColorRes(R.color.title_text_color)
                .canceledOnTouchOutside(false)
                .positiveText("确定")
                .negativeText("取消")
                .positiveColorRes(R.color.blue_52B4F8)
                .negativeColorRes(R.color.sub_title_text_color)
                .items(baudRates)
                .itemsCallbackSingleChoice(pos, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        baudRate = Integer.parseInt(baudRates[which]);
                        BluetoothDebugBoxHomeActivity.startActivity(mActivity,usbDeviceItem.getDevice().getDeviceId(),usbDeviceItem.getPort(),baudRate);
                        return true;
                    }
                });
        MaterialDialog mMaterialDialog = mBuilder.build();
        mMaterialDialog.show();
    }

    @OnClick({R.id.ll_scan_refresh})
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ll_scan_refresh) {
            updateRefreshView();
        }
    }

    /**
     * 刷新动画处理
     */
    private void updateRefreshView() {
        if (animator == null) {
            return;
        }

        if (animator.isStarted() || animator.isRunning()) {
            animator.end();
            mTvScanState.setText("刷新");
        } else {
            animator.start();
            mTvScanState.setText("刷新中...");
            refresh();
            MCloudApp.getMainHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    animator.end();
                    mTvScanState.setText("刷新");
                }
            }, 300);
        }
    }

    @Override
    public void onStop() {
        mActivity.unregisterReceiver(usbReceiver);
        super.onStop();
    }

    @Override
    public void onDestroy() {
        MCloudApp.getMainHandler().removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}