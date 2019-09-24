package com.shmedo.mcloudapp.util.bleutil;

import android.bluetooth.BluetoothAdapter;

import com.shmedo.mcloudapp.bluetooth.MdBluetoothManager;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.util.bleutil
 * 文件名:   BluetoothUtil
 * 创建者:   dpc
 * 创建时间:  2019/2/28 14:00
 * 描述：    TODO
 */
public class BluetoothUtil {
    private MdBluetoothManager mdBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;


    //public void initBluetooth(Context context){
    //    BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
    //    mBluetoothAdapter = bluetoothManager.getAdapter();
    //    MdBluetoothManager.init(mBluetoothAdapter, bluetoothManager);
    //    mdBluetoothManager = MdBluetoothManager.getInstance();
    //    //mdBluetoothManager.setEventHandler(new MdBluetoothEventHandler());
    //    if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
    //        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    //        startActivityForResult(enableBtIntent, 1);
    //    }
    //    mdBluetoothManager.scanDevice(20, context);
    //}

    public void connectBluetooth(MdBluetoothManager mdBluetoothManager){
        //mdBluetoothManager.connectDevice();
    }
}
