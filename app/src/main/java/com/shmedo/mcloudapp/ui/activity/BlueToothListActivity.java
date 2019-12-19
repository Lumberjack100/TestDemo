package com.shmedo.mcloudapp.ui.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hjq.toast.ToastUtils;
import com.shmedo.mcloudapp.MCloudApp;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.adapter.BluetoothDevicesAdapter;
import com.shmedo.mcloudapp.entity.ble.MDevice;
import com.shmedo.mcloudapp.model.Extras;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity
 * 文件名:   BlueToothListActivity
 * 创建者:   dpc
 * 创建时间:  2019/1/21 16:02
 *
 */
public class BlueToothListActivity extends Activity {
    private RecyclerView mRecyclerView;

    private BluetoothDevicesAdapter adapter;

    private List<MDevice> deviceList = new ArrayList<>();

    /**
     * 说明：启动Activity
     * <p>
     * 注意：这里使用到了Intent的Flag属性singleTop。singleTop模式下，在同一个task中，如果存在该Activity的实例，
     * 并且该Activity实例位于栈顶(即，该Activity位于前端)，则调用startActivity()时，不再创建该Activity的示例；
     * 而仅仅只是调用Activity的onNewIntent()。否则的话，则新建该Activity的实例，并将其置于栈顶。
     * </p>
     */
    public static void startActivity(Context context, List<MDevice> tempList) {
        Intent intent = new Intent(context, BlueToothListActivity.class);
        intent.putExtra(Extras.SCAN_DEVICE_LIST, (Serializable) tempList);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.device_list);
        setResult(Activity.RESULT_CANCELED);

        getIntentData();
        initDevivce();
    }

    private void getIntentData() {
        List<MDevice> tempList = (List<MDevice>) getIntent().getSerializableExtra(Extras.SCAN_DEVICE_LIST);
        if (tempList != null)
            deviceList.addAll(tempList);

        if (deviceList.size() == 0) {
            ToastUtils.show("未发现设备，请尝试重新扫描");
            finish();
        }
    }


    private void initDevivce() {
        mRecyclerView = findViewById(R.id.recycleviewble);
        //RecyclerView 设置布局样式
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        adapter = new BluetoothDevicesAdapter(deviceList, this);
        mRecyclerView.setAdapter(adapter);
        //mRecyclerView  添加条目效果
        mRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                adapter.setDelayStartAnimation(false);
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });
        //adapter 点击事件
        adapter.setOnItemClickListener(new BluetoothDevicesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View itemView, int position) {
                String macAddress = deviceList.get(position).getDevice().getAddress();
                String deviceName = deviceList.get(position).getDevice().getName();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
