package com.shmedo.mcloudapp.ui.activity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import com.shmedo.mcloudapp.R;
import com.shmedo.mcloudapp.adapter.BluetoothDevicesAdapter;
import com.shmedo.mcloudapp.entity.ble.MDevice;
import com.shmedo.mcloudapp.util.ToastUtil;
import java.util.List;

/**
 * 项目名：  mCloudapp
 * 包名：    com.shmedo.mcloudapp.ui.activity
 * 文件名:   BlueToothListActivity
 * 创建者:   dpc
 * 创建时间:  2019/1/21 16:02
 * 描述：    TODO
 */
public class BlueToothListActivity extends Activity {
    private BluetoothAdapter mBluetoothAdapter;
    // Debugg
    private BluetoothAdapter mBtAdapter;


    private RecyclerView recyclerView;
    private BluetoothDevicesAdapter adapter;
    private List<MDevice> list;
    private boolean scaning;



    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.device_list);
        setResult(Activity.RESULT_CANCELED);
        list= (List<MDevice>) getIntent().getSerializableExtra("devlist");
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        initDevivce();
    }

    private void initDevivce() {
        Log.i("adu","-----------------初始化蓝牙222222222---------------");
        try{
            //获的recyclerView
            recyclerView = findViewById(R.id.recycleviewble);
            //给recyclerView   设置布局样式
            LinearLayoutManager llm = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(llm);
            //获取并设置设备适配器
            if (list.size()!=0){
                adapter = new BluetoothDevicesAdapter(list, this);
                recyclerView.setAdapter(adapter);
                //recyclerView  添加条目效果
                recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
                    @Override public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                        adapter.setDelayStartAnimation(false);
                        return false;
                    }
                    @Override public void onTouchEvent(RecyclerView rv, MotionEvent e) {

                    }
                    @Override public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

                    }
                });
                //adapter 点击事件
                adapter.setOnItemClickListener(new BluetoothDevicesAdapter.OnItemClickListener() {
                    @Override public void onItemClick(View itemView, int position) {
                        if (!scaning) {
                            //  mLoadingDialog.showNoCancelDialog("正在连接...");
                            //BluetoothDevice device = list.get(position).getDevice();
                            String deviceName = list.get(position).getDevice().getName();
                            //Intent intent = new Intent(BlueToothListActivity.this,DeviceActivity.class);
                            Intent intent = new Intent(BlueToothListActivity.this,AllDeviceActivity.class);
                            intent.putExtra("device",deviceName);
                            intent.putExtra("deviceTrue",true);
                            //setResult(Activity.RESULT_OK, intent);
                            //finish();
                            startActivity(intent);
                            finish();
                        }

                    }
                });
            }else {
                ToastUtil.showSToast("未发现设备，请尝试重新扫描");
                finish();
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }
    }
}
