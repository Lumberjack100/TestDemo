package com.shmedo.mcloudapp.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.hjq.toast.ToastUtils;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.adapter.BluetoothDeviceAdapter;
import com.shmedo.mcloudapp.bluetooth.BluetoothDeviceFindEventData;
import com.shmedo.mcloudapp.bluetooth.BluetoothEvent;
import com.shmedo.mcloudapp.bluetooth.BluetoothEventHandler;
import com.shmedo.mcloudapp.bluetooth.MdBluetoothManager;
import com.shmedo.mcloudapp.entity.ble.MDevice;
import com.shmedo.mcloudapp.util.ActivityCollector;
import com.shmedo.mcloudapp.views.recycleviewitemdivider.DividerItemDecoration;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity
 * 文件名:   BlueToothListActivity
 * 创建者:   dpc
 * 创建时间:  2019/1/21 16:02
 */
public class BlueToothListActivity extends AppCompatActivity  {
    public static final int REQUEST_ENABLE_BT = 0x002;

    @BindView(R.id.recycleview_bluetooth_device)
    RecyclerView mRecyclerView;

    @BindView(R.id.pb_wifi_loading)
    ProgressBar progressBar;

    @BindView(R.id.tv_scan_state)
    TextView mTvScanState;

    @BindView(R.id.btn_scan)
    Button btnScan;

    private BluetoothDeviceAdapter deviceAdapter;

    private List<MDevice> deviceList = new ArrayList<>();

    private BluetoothAdapter mBluetoothAdapter;
    private MdBluetoothManager mdBluetoothManager;
    private MdBluetoothEventHandler mdBluetoothEventHandler = new MdBluetoothEventHandler();

    private Handler hander;


    private Runnable dismssDialogRunnable = new Runnable() {
        @Override
        public void run() {
            mdBluetoothManager.stopScan();
            updateViewState(false);
        }
    };

    /**
     * 说明：启动Activity
     * <p>
     * 注意：这里使用到了Intent的Flag属性singleTop。singleTop模式下，在同一个task中，如果存在该Activity的实例，
     * 并且该Activity实例位于栈顶(即，该Activity位于前端)，则调用startActivity()时，不再创建该Activity的示例；
     * 而仅仅只是调用Activity的onNewIntent()。否则的话，则新建该Activity的实例，并将其置于栈顶。
     * </p>
     */
    public static void startActivity(Context context) {
        Intent intent = new Intent(context, BlueToothListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.device_list);
        ButterKnife.bind(this);
        WeakReference<Activity> weakRefActivity = new WeakReference<>(this);
        ActivityCollector.add(weakRefActivity);

        hander = new Handler();
        initAdapter();
        initBluetooth();
        mdBluetoothManager.addBluetoothEventHandler(mdBluetoothEventHandler);
        startDiscoveryDevice();
    }

    private void initAdapter() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration());
        deviceAdapter = new BluetoothDeviceAdapter(R.layout.item_bluetoothdevice, deviceList);
        mRecyclerView.setAdapter(deviceAdapter);

        deviceAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                mdBluetoothManager.stopScan();

                BluetoothDevice bluetoothDevice = deviceList.get(position).getDevice();
                String macAddress = bluetoothDevice.getAddress();
                String deviceName = bluetoothDevice.getName();
                MCloudApp.setCurDeviceToken(deviceName.substring(3));
                MCloudApp.setCurDeviceMacAddr(macAddress);

                if (deviceName.endsWith("T")) {
                    String deviceInfo = "MEDO," + deviceName.substring(3) + ",ADME";
                    ConfigADMEActivity.startActivity(BlueToothListActivity.this, deviceInfo);

                } else if (deviceName.endsWith("L")) {
                    String deviceInfo = "MEDO," + deviceName.substring(3) + ",DAS";
                    ConfigDASActivity.startActivity(BlueToothListActivity.this, deviceInfo);
                }

                finish();
            }
        });
    }




    @OnClick({R.id.btn_scan, R.id.iv_close})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_scan:
                if (btnScan.getText().toString().contains("停止扫描")) {
                    mdBluetoothManager.stopScan();
                    updateViewState(false);
                } else {
                    startDiscoveryDevice();
                }
                break;

            case R.id.iv_close:
                finish();
                break;
        }
    }

    private void updateViewState(boolean isScanning) {
        progressBar.setVisibility(isScanning ? View.VISIBLE : View.GONE);
        mTvScanState.setVisibility(isScanning ? View.VISIBLE : View.GONE);
        btnScan.setText(isScanning ? "停止扫描" : "开始扫描");
    }


    /**
     * 初始化蓝牙
     */
    private void initBluetooth() {
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = Objects.requireNonNull(bluetoothManager).getAdapter();
        mdBluetoothManager = MdBluetoothManager.getInstance();
    }


    /**
     * 扫描蓝牙设备，主要用来判断要连接的设备是否能被搜索到
     */
    public void startDiscoveryDevice() {
        //未打开蓝牙
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        deviceList.clear();
        mdBluetoothManager.scanDevice(15, this);
        hander.postDelayed(dismssDialogRunnable, 15000);
        updateViewState(true);
    }


    private class MdBluetoothEventHandler implements BluetoothEventHandler {
        @Override
        public void handle(final BluetoothEvent event) {
            switch (event.getEventType()) {
                case DEVICE_FIND:
                    handleDeviceFind((BluetoothDeviceFindEventData) event.getEventData());
                    break;
            }
        }
    }


    private void handleDeviceFind(BluetoothDeviceFindEventData eventData) {
        if (eventData.getNewDevice().getDevice().getName() == null || !eventData.getNewDevice().getDevice().getName().startsWith("MD")) {
            return;
        }

        for (MDevice mDevice : deviceList) {
            if (eventData.getNewDevice()
                    .getDevice()
                    .getAddress()
                    .equals(mDevice.getDevice().getAddress())) {
                return;
            }
        }
//        deviceList.add(eventData.getNewDevice());
//        deviceAdapter.notifyDataSetChanged();

        deviceAdapter.addData(eventData.getNewDevice());
    }


    @SuppressLint("MissingSuperCall")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // 判断蓝牙是否启用
                if (resultCode != Activity.RESULT_OK) {
                    ToastUtils.show("蓝牙未启用");
                    return;
                }
                startDiscoveryDevice();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mdBluetoothManager.stopScan();
        mdBluetoothManager.removeBluetoothEventHandler(mdBluetoothEventHandler);
    }
}
