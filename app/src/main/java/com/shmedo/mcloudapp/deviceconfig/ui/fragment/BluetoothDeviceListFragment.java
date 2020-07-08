package com.shmedo.mcloudapp.deviceconfig.ui.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.hjq.toast.ToastUtils;
import com.dragon.core.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.base.BaseFragment;
import com.shmedo.mcloudapp.bluetooth.BluetoothDeviceFindEventData;
import com.shmedo.mcloudapp.bluetooth.BluetoothEvent;
import com.shmedo.mcloudapp.bluetooth.BluetoothEventHandler;
import com.shmedo.mcloudapp.bluetooth.BluetoothEventType;
import com.shmedo.mcloudapp.bluetooth.MdBluetoothManager;
import com.shmedo.mcloudapp.deviceconfig.ui.view.DiffuseView;
import com.shmedo.mcloudapp.entity.DeviceBasicInfoResult;
import com.shmedo.mcloudapp.entity.DeviceTypeEnum;
import com.shmedo.mcloudapp.entity.ble.MDevice;
import com.shmedo.mcloudapp.ui.activity.ConfigADMEActivity;
import com.shmedo.mcloudapp.ui.activity.ConfigDASActivity;
import com.shmedo.mcloudapp.ui.activity.ConfigE60Activity;
import com.shmedo.mcloudapp.ui.activity.ScanActivity;
import com.shmedo.mcloudapp.user.ui.activity.NewUserInfoActivity;
import com.shmedo.mcloudapp.util.XPermissionUtils;
import com.shmedo.mcloudapp.views.recycleviewitemdivider.DividerItemDecoration;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * 蓝牙设备列表页面
 */
public class BluetoothDeviceListFragment extends BaseFragment {
    private static final int REQUEST_ENABLE_BT = 0x002;

    @BindView(R.id.ivDiscoveryDevice)
    DiffuseView mIvDiscoveryDevice;

    @BindView(R.id.tvDiscoveryState)
    TextView mTvDiscoveryState;

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    private DeviceAdapter deviceAdapter;

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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hander = new Handler();
        initBluetooth();
        mdBluetoothManager.addBluetoothEventHandler(mdBluetoothEventHandler);
    }

    @Override
    protected int initContentView() {
        return R.layout.fragment_bluetooth_device_list;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        initView();
        initAdapter();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        startDiscoveryDevice();
    }

    private void initView() {

    }

    private void initAdapter() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration());
        deviceAdapter = new DeviceAdapter();
        mRecyclerView.setAdapter(deviceAdapter);
        deviceAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter adapter, @NonNull View view, int position) {
                mdBluetoothManager.stopScan();

                BluetoothDevice bluetoothDevice = deviceAdapter.getItem(position).getDevice();
                String macAddress = bluetoothDevice.getAddress();
                String deviceName = bluetoothDevice.getName();
                MCloudApp.setCurDeviceToken(deviceName.substring(3));
                MCloudApp.setCurDeviceMacAddr(macAddress);

                if (deviceName.endsWith("T")) {
                    String deviceInfo = "MEDO," + deviceName.substring(3) + ",ADME";
                    ConfigADMEActivity.startActivity(getActivity(), deviceInfo);

                } else if (deviceName.endsWith("L")) {
                    String deviceInfo = "MEDO," + deviceName.substring(3) + ",DAS";
                    ConfigDASActivity.startActivity(getActivity(), deviceInfo);
                }
            }
        });
    }


    @OnClick({R.id.img_user, R.id.ivDiscoveryDevice, R.id.iv_open_camera_scan})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_user://用户信息
                NewUserInfoActivity.startActivity(getActivity());
                break;

            case R.id.ivDiscoveryDevice:
                if (mTvDiscoveryState.getText().toString().contains("正在使劲搜索...")) {
                    mdBluetoothManager.stopScan();
                    updateViewState(false);

                } else {
                    startDiscoveryDevice();
                }
                break;

            case R.id.iv_open_camera_scan:
                doScanButtonClick();
                break;
        }
    }

    private void updateViewState(boolean isScanning) {
        mTvDiscoveryState.setText(isScanning ? "正在使劲搜索..." : "点击搜索");
        if (isScanning)
            mIvDiscoveryDevice.start();
        else
            mIvDiscoveryDevice.stop();
    }

    /**
     * 初始化蓝牙
     */
    private void initBluetooth() {
        final BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = Objects.requireNonNull(bluetoothManager).getAdapter();
        mdBluetoothManager = MdBluetoothManager.getInstance();
    }

    /**
     * 扫描蓝牙设备，主要用来判断要连接的设备是否能被搜索到
     */
    private void startDiscoveryDevice() {
        //未打开蓝牙
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return;
        }

        deviceAdapter.setNewInstance(new ArrayList<>());
        mdBluetoothManager.scanDevice(30, getActivity());
        hander.postDelayed(dismssDialogRunnable, 30000);
        updateViewState(true);
    }

    private class MdBluetoothEventHandler implements BluetoothEventHandler {
        @Override
        public void handle(final BluetoothEvent event) {
            if (event.getEventType() == BluetoothEventType.DEVICE_FIND) {
                handleDeviceFind((BluetoothDeviceFindEventData) event.getEventData());
            }
        }
    }

    private void handleDeviceFind(BluetoothDeviceFindEventData eventData) {
        if (eventData.getNewDevice().getDevice().getName() == null || !eventData.getNewDevice().getDevice().getName().startsWith("MD")) {
            return;
        }

        for (MDevice mDevice : deviceAdapter.getData()) {
            if (eventData.getNewDevice()
                    .getDevice()
                    .getAddress()
                    .equals(mDevice.getDevice().getAddress())) {
                return;
            }
        }

        deviceAdapter.addData(eventData.getNewDevice());
    }

    private void doScanButtonClick() {
        XPermissionUtils.requestPermissionsResult(getActivity(), 200, new String[]{
                        Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                new XPermissionUtils.OnPermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        ScanActivity.startActivityForResultByFragment(BluetoothDeviceListFragment.this, XPermissionUtils.REQUEST_CODE_SCAN);
                    }

                    @Override
                    public void onPermissionDenied() {
                        XPermissionUtils.showRefusePermissionDialog(getActivity(),
                                getActivity().getResources().getString(R.string.permission_request_camera_external_storage));
                    }
                });
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

            case XPermissionUtils.REQUEST_CODE_SCAN:
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {
                        String content = data.getStringExtra(ScanActivity.CODED_CONTENT);
                        Timber.d("扫描结果为：" + content);
                        scanResult(content);
                    }
                }
                break;
        }
    }

    private void scanResult(String result) {
        if (TextUtils.isEmpty(result)) {
            showTipDialog("请扫码正确的设备二维码");
            return;
        }

        if (result.contains("=")) {
            String results = result.substring(result.indexOf("=") + 1);
            scan(results);
        } else {
            scan(result);
        }
    }

    /**
     * 处理扫描结果，例如：MEDO,189150L,DAS
     */
    private void scan(String deviceInfo) {
        if (!deviceInfo.startsWith("MEDO")) {
            showTipDialog("请扫码正确的设备二维码");
            return;
        }

        String[] localData = deviceInfo.split(",");
        if (localData.length != 3) {
            showTipDialog("请扫码正确的设备二维码");
            return;
        }

        if (TextUtils.isEmpty(localData[0]) || TextUtils.isEmpty(localData[1]) || TextUtils.isEmpty(localData[2])) {
            showTipDialog("二维码信息不能为空");
            return;
        }

        if (localData[1].length() != 7) {
            showTipDialog("设备标识有误,请扫码正确的设备二维码");
            return;
        }

        if (!DeviceTypeEnum.value(localData[2])) {
            showTipDialog("此设备类型暂时不支持");
            return;
        }

        MCloudApp.setCurDeviceToken(localData[1]);
        MCloudApp.setCurDeviceMacAddr(null);

        switch (localData[2]) {
            case "DAS":
                ConfigDASActivity.startActivity(getActivity(), deviceInfo);
                break;

            case "ADME":
                ConfigADMEActivity.startActivity(getActivity(), deviceInfo);
                break;

            case "E60":
                DeviceBasicInfoResult deviceBasicInfoResult = new DeviceBasicInfoResult();
                deviceBasicInfoResult.setDeviceToken(localData[2]);
                deviceBasicInfoResult.setDeviceTypeName(localData[1]);
                ConfigE60Activity.startActivity(getActivity(), deviceBasicInfoResult);
                break;
        }
    }

    public class DeviceAdapter extends BaseQuickAdapter<MDevice, BaseViewHolder> {
        public DeviceAdapter() {
            super(R.layout.item_bluetoothdevice);
        }


        @Override
        protected void convert(@NotNull BaseViewHolder holder, MDevice mDevice) {
//            Timber.d("This is an Item, pos: " + (holder.getAdapterPosition() - getHeaderLayoutCount()));
            BluetoothDevice bluetoothDevice = mDevice.getDevice();
            holder.setText(R.id.tv_dev_name, TextUtils.isEmpty(bluetoothDevice.getName()) ? "N/A" : bluetoothDevice.getName());
            holder.setText(R.id.tv_dev_mac, bluetoothDevice.getAddress());
            holder.setText(R.id.tv_dev_signal, mDevice.getRssi() + "dBm");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mdBluetoothManager.stopScan();
        mdBluetoothManager.removeBluetoothEventHandler(mdBluetoothEventHandler);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

}
